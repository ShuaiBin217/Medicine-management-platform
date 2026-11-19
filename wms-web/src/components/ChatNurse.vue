<template>
    <div>
        <div class="chat-nurse-float" @click="openChat" :class="{ 'has-new': hasNew }">
            <div class="float-icon">
                <el-icon><ChatDotRound/></el-icon>
            </div>
            <span class="float-label" v-if="!chatVisible">智能小药</span>
        </div>

        <el-dialog
                v-model="chatVisible"
                width="540px"
                :show-close="false"
                class="chat-nurse-dialog"
                append-to-body
                :modal="false"
                top="6vh"
        >
            <div class="chat-container">
                <div class="chat-header">
                    <div class="chat-header-left">
                        <div class="nurse-avatar-small">
                            <el-icon><FirstAidKit/></el-icon>
                        </div>
                        <div class="chat-title-group">
                            <span class="chat-title">智能小药</span>
                            <span class="chat-subtitle">药品问答助手</span>
                        </div>
                    </div>
                    <div class="chat-header-right">
                        <el-button link size="small" @click="clearHistory" title="清空对话">
                            <el-icon class="header-icon"><Delete/></el-icon>
                        </el-button>
                        <el-button link size="small" @click="chatVisible = false" title="关闭">
                            <el-icon class="header-icon"><Close/></el-icon>
                        </el-button>
                    </div>
                </div>

                <div class="chat-messages" ref="chatMessages">
                    <div v-if="messages.length === 0" class="chat-welcome">
                        <div class="welcome-avatar">
                            <el-icon><FirstAidKit/></el-icon>
                        </div>
                        <p class="welcome-name">智能小药</p>
                        <p class="welcome-desc">可以问库存、药房分布和药品用法。回答来自库存台账与知识库。</p>
                        <div class="quick-questions">
                            <div class="quick-q" @click="sendQuick('当前有哪些药品库存不足？')">
                                <el-icon><Box/></el-icon><span>库存不足的药品</span>
                            </div>
                            <div class="quick-q" @click="sendQuick('请帮我查看所有药品的库存情况')">
                                <el-icon><DataLine/></el-icon><span>查看库存情况</span>
                            </div>
                            <div class="quick-q" @click="sendQuick('感冒药有哪些？用法用量是什么？')">
                                <el-icon><FirstAidKit/></el-icon><span>感冒药查询</span>
                            </div>
                            <div class="quick-q" @click="sendQuick('各药房的药品分布情况如何？')">
                                <el-icon><OfficeBuilding/></el-icon><span>药房分布查询</span>
                            </div>
                        </div>
                    </div>

                    <div v-for="(msg, index) in displayMessages" :key="index"
                         :class="['chat-message', msg.role === 'user' ? 'message-user' : 'message-assistant']">
                        <div class="message-avatar" v-if="msg.role === 'assistant'">
                            <el-icon><FirstAidKit/></el-icon>
                        </div>
                        <div class="message-bubble" :class="msg.role">
                            <span v-html="formatMessage(msg.content)"></span>
                        </div>
                    </div>

                    <div v-if="loading && !streaming" class="chat-message message-assistant">
                        <div class="message-avatar">
                            <el-icon><FirstAidKit/></el-icon>
                        </div>
                        <div class="message-bubble assistant typing">
                            <div v-if="toolStatus" class="tool-status">{{ toolStatus }}</div>
                            <span class="typing-dots">
                                <span class="dot"></span>
                                <span class="dot"></span>
                                <span class="dot"></span>
                            </span>
                        </div>
                    </div>
                </div>

                <div class="chat-input-area">
                    <el-input
                            v-model="inputMessage"
                            placeholder="输入问题，例如：哪些药品库存不足"
                            @keyup.enter="sendMessage"
                            :disabled="loading"
                            size="default"
                            class="chat-input"
                    >
                    </el-input>
                    <el-button
                            type="primary"
                            icon="Promotion"
                            circle
                            size="small"
                            @click="sendMessage"
                            :loading="loading"
                            class="send-btn"
                    ></el-button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        name: "ChatNurse",
        data() {
            return {
                chatVisible: false,
                inputMessage: '',
                messages: [],
                loading: false,
                hasNew: false,
                streaming: false,
                toolStatus: ''
            }
        },
        computed: {
            // 思考等待期隐藏末尾的空 assistant 占位，避免双头像
            displayMessages() {
                if (this.loading && !this.streaming && this.messages.length) {
                    const last = this.messages[this.messages.length - 1];
                    if (last.role === 'assistant' && !last.content) {
                        return this.messages.slice(0, -1);
                    }
                }
                return this.messages;
            }
        },
        methods: {
            openChat() {
                this.chatVisible = true;
                this.hasNew = false;
            },
            sendMessage() {
                let msg = this.inputMessage.trim();
                if (!msg || this.loading) return;

                this.messages.push({ role: 'user', content: msg });
                this.inputMessage = '';
                this.scrollToBottom();

                this.loading = true;
                this.streaming = false;
                this.toolStatus = '';
                let history = this.messages.slice(-10).map(m => ({
                    role: m.role,
                    content: m.content
                }));

                this.messages.push({ role: 'assistant', content: '' });
                let idx = this.messages.length - 1;
                let self = this;

                // SSE 流式：POST 无法用 EventSource，改用 fetch + ReadableStream 手动解析
                fetch(this.$aiUrl + '/ai/chat/stream', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: msg, history: history.slice(0, -1) })
                }).then(res => {
                    if (!res.ok || !res.body) throw new Error('HTTP ' + res.status);
                    let reader = res.body.getReader();
                    let decoder = new TextDecoder('utf-8');
                    let buf = '';

                    let handleLine = (line) => {
                        if (!line.startsWith('data:')) return;
                        let payload = line.slice(5).trim();
                        if (!payload) return;
                        let evt;
                        try { evt = JSON.parse(payload); } catch (e) { return; }
                        if (evt.type === 'token') {
                            self.streaming = true;
                            self.toolStatus = '';
                            self.messages[idx].content += evt.content;
                            self.scrollToBottom();
                        } else if (evt.type === 'tool_start') {
                            self.toolStatus = '正在查询：' + (evt.label || evt.name) + '…';
                        } else if (evt.type === 'tool_end') {
                            self.toolStatus = '';
                        } else if (evt.type === 'error') {
                            self.messages[idx].content = evt.content || '抱歉，暂时无法回答，请稍后再试。';
                            self.scrollToBottom();
                        }
                    };

                    let pump = () => reader.read().then(({ done, value }) => {
                        if (done) return;
                        buf += decoder.decode(value, { stream: true });
                        let lines = buf.split('\n');
                        buf = lines.pop();
                        lines.forEach(handleLine);
                        return pump();
                    });

                    return pump();
                }).catch(() => {
                    if (!this.messages[idx].content) {
                        this.messages[idx].content = '网络异常，请检查网络连接后重试。';
                    }
                }).finally(() => {
                    this.loading = false;
                    this.streaming = false;
                    this.toolStatus = '';
                    this.scrollToBottom();
                });
            },
            sendQuick(question) {
                this.inputMessage = question;
                this.sendMessage();
            },
            clearHistory() {
                this.$confirm('确定清空所有对话记录吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.messages = [];
                    this.$message.success('对话已清空');
                }).catch(() => {});
            },
            scrollToBottom() {
                this.$nextTick(() => {
                    let container = this.$refs.chatMessages;
                    if (container) {
                        container.scrollTop = container.scrollHeight;
                    }
                });
            },
            formatMessage(content) {
                if (!content) return '';
                return content
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/\n/g, '<br/>')
                    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
            }
        }
    }
</script>

<style scoped>
    .chat-nurse-float {
        position: fixed;
        right: 24px;
        bottom: 24px;
        z-index: 2000;
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        pointer-events: auto;
    }
    .float-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--card);
        border: 1px solid var(--line);
        color: var(--accent);
        font-size: 22px;
        box-shadow: 0 4px 16px rgba(31, 27, 22, 0.10);
        transition: var(--transition);
    }
    .chat-nurse-float:hover .float-icon {
        border-color: var(--accent);
        background: #F2F6F1;
    }
    .float-label {
        background: var(--card);
        color: var(--ink-2);
        padding: 5px 10px;
        border-radius: 6px;
        border: 1px solid var(--line);
        font-size: 11px;
        font-weight: 500;
        white-space: nowrap;
        box-shadow: 0 2px 8px rgba(31, 27, 22, 0.06);
    }

    .chat-container {
        display: flex;
        flex-direction: column;
        height: 620px;
        overflow: hidden;
    }
    .chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 0 12px 0;
        border-bottom: 1px solid var(--line);
    }
    .chat-header-left {
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .nurse-avatar-small {
        width: 30px;
        height: 30px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--accent-soft);
        color: var(--accent);
        font-size: 16px;
    }
    .chat-title-group {
        display: flex;
        flex-direction: column;
        line-height: 1.25;
    }
    .chat-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--ink);
    }
    .chat-subtitle {
        font-size: 10px;
        color: var(--muted);
        margin-top: 1px;
    }
    .chat-header-right {
        display: flex;
        align-items: center;
        gap: 2px;
    }
    .header-icon {
        font-size: 15px;
        color: var(--muted);
    }
    .header-icon:hover {
        color: var(--ink);
    }

    .chat-messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px 2px;
    }
    .chat-messages::-webkit-scrollbar {
        width: 4px;
    }
    .chat-messages::-webkit-scrollbar-thumb {
        background: #D8D0C2;
        border-radius: 2px;
    }

    .chat-welcome {
        text-align: center;
        padding: 30px 0 20px;
    }
    .welcome-avatar {
        width: 54px;
        height: 54px;
        border-radius: 12px;
        margin: 0 auto 12px;
        background: var(--accent-soft);
        color: var(--accent);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 26px;
    }
    .welcome-name {
        font-size: 19px;
        font-weight: 500;
        color: var(--ink);
        margin: 0 0 6px;
    }
    .welcome-desc {
        font-size: 12px;
        color: var(--muted);
        line-height: 1.7;
        margin: 0 auto 24px;
        max-width: 340px;
    }
    .quick-questions {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 8px;
        padding: 0 4px;
    }
    .quick-q {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        background: #F5F0E7;
        border-radius: 8px;
        border: 1px solid var(--line);
        font-size: 12px;
        color: var(--ink-2);
        cursor: pointer;
        text-align: left;
        transition: var(--transition);
    }
    .quick-q .el-icon {
        font-size: 14px;
        color: var(--muted);
        flex-shrink: 0;
    }
    .quick-q:hover {
        background: var(--raised);
        border-color: var(--line-strong);
        color: var(--ink);
    }

    .chat-message {
        display: flex;
        align-items: flex-start;
        margin-bottom: 14px;
        gap: 8px;
    }
    .message-user {
        flex-direction: row-reverse;
    }
    .message-avatar {
        width: 28px;
        height: 28px;
        border-radius: 8px;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--accent-soft);
        color: var(--accent);
        font-size: 15px;
    }
    .message-bubble {
        max-width: 82%;
        padding: 10px 13px;
        border-radius: 10px;
        font-size: 13px;
        line-height: 1.7;
        word-break: break-word;
    }
    .message-bubble.user {
        background: var(--ink);
        color: #FDFBF7;
        border-top-right-radius: 2px;
    }
    .message-bubble.assistant {
        background: #F3EDE2;
        color: var(--ink);
        border-top-left-radius: 2px;
    }
    .message-bubble.typing {
        padding: 12px 14px;
    }
    .tool-status {
        font-size: 11px;
        color: var(--muted);
        margin-bottom: 6px;
    }
    .typing-dots {
        display: flex;
        gap: 4px;
        align-items: center;
    }
    .typing-dots .dot {
        width: 5px;
        height: 5px;
        border-radius: 50%;
        background: var(--muted);
        animation: typing-bounce 1.2s infinite ease-in-out;
    }
    .typing-dots .dot:nth-child(2) {
        animation-delay: 0.2s;
    }
    .typing-dots .dot:nth-child(3) {
        animation-delay: 0.4s;
    }
    @keyframes typing-bounce {
        0%, 80%, 100% { opacity: 0.35; }
        40% { opacity: 1; }
    }

    .chat-input-area {
        display: flex;
        align-items: center;
        gap: 8px;
        padding-top: 12px;
        border-top: 1px solid var(--line);
    }
    .chat-input {
        flex: 1;
    }
    .chat-input :deep(.el-input__wrapper) {
        border-radius: 8px !important;
    }
    .send-btn {
        width: 32px;
        height: 32px;
        padding: 0;
        flex-shrink: 0;
        border-radius: 8px !important;
    }
</style>

<style>
    .chat-nurse-dialog {
        border-radius: 14px !important;
        overflow: hidden;
        border: 1px solid rgba(31, 27, 22, 0.12);
        box-shadow: 0 24px 64px rgba(31, 27, 22, 0.18) !important;
        background: #FDFBF7 !important;
    }
    .chat-nurse-dialog .el-dialog__header {
        display: none;
    }
    .chat-nurse-dialog .el-dialog__body {
        padding: 18px 20px 16px;
    }
</style>
