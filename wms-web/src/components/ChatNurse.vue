<template>
    <div>
        <div class="chat-nurse-float" @click="openChat" :class="{ 'has-new': hasNew }" style="pointer-events: auto;">
            <div class="float-icon">
                <svg viewBox="0 0 1024 1024" width="36" height="36">
                    <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z" fill="#4A6CF7"/>
                    <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z" fill="#6B8AFF"/>
                    <path d="M463.3 322.6c8.7-3.3 18.1-5 27.7-5 9.6 0 19 1.7 27.7 5 8.7 3.3 16.5 8.2 23 14.6 6.5 6.4 11.6 14.2 14.9 22.9 3.3 8.7 5 18.1 5 27.7 0 9.6-1.7 19-5 27.7-3.3 8.7-8.3 16.5-14.9 22.9-6.5 6.4-14.3 11.3-23 14.6-8.7 3.3-18.1 5-27.7 5-9.6 0-19-1.7-27.7-5-8.7-3.3-16.5-8.2-23-14.6-6.5-6.4-11.6-14.2-14.9-22.9-3.3-8.7-5-18.1-5-27.7 0-9.6 1.7-19 5-27.7 3.3-8.7 8.3-16.5 14.9-22.9 6.5-6.4 14.3-11.3 23-14.6z" fill="white"/>
                    <path d="M649.5 548.3c-12.4-5.3-26-8.3-40.2-8.3-14.2 0-27.8 3-40.2 8.3-12.4 5.3-23.5 12.9-32.8 22.4-9.3 9.5-16.7 20.8-21.8 33.3-5.1 12.5-7.8 26-7.8 39.7 0 13.7 2.7 27.2 7.8 39.7 5.1 12.5 12.5 23.8 21.8 33.3 9.3 9.5 20.4 17.1 32.8 22.4 12.4 5.3 26 8.3 40.2 8.3 14.2 0 27.8-3 40.2-8.3 12.4-5.3 23.5-12.9 32.8-22.4 9.3-9.5 16.7-20.8 21.8-33.3 5.1-12.5 7.8-26 7.8-39.7 0-13.7-2.7-27.2-7.8-39.7-5.1-12.5-12.5-23.8-21.8-33.3-9.3-9.5-20.4-17.1-32.8-22.4z" fill="white"/>
                    <path d="M384.5 548.3c-12.4-5.3-26-8.3-40.2-8.3-14.2 0-27.8 3-40.2 8.3-12.4 5.3-23.5 12.9-32.8 22.4-9.3 9.5-16.7 20.8-21.8 33.3-5.1 12.5-7.8 26-7.8 39.7 0 13.7 2.7 27.2 7.8 39.7 5.1 12.5 12.5 23.8 21.8 33.3 9.3 9.5 20.4 17.1 32.8 22.4 12.4 5.3 26 8.3 40.2 8.3 14.2 0 27.8-3 40.2-8.3 12.4-5.3 23.5-12.9 32.8-22.4 9.3-9.5 16.7-20.8 21.8-33.3 5.1-12.5 7.8-26 7.8-39.7 0-13.7-2.7-27.2-7.8-39.7-5.1-12.5-12.5-23.8-21.8-33.3-9.3-9.5-20.4-17.1-32.8-22.4z" fill="white"/>
                </svg>
            </div>
            <span class="float-label" v-if="!chatVisible">智能小药</span>
        </div>

        <el-dialog
                :visible.sync="chatVisible"
                width="560px"
                :show-close="false"
                custom-class="chat-nurse-dialog"
                append-to-body
                :modal="false"
                top="5vh"
        >
            <div class="chat-container">
                <div class="chat-header">
                    <div class="chat-header-left">
                        <div class="nurse-avatar-small">
                            <svg viewBox="0 0 1024 1024" width="28" height="28">
                                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z" fill="#4A6CF7"/>
                                <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z" fill="#6B8AFF"/>
                            </svg>
                        </div>
                        <span class="chat-title">智能小药 · 药品问答助手</span>
                    </div>
                    <div class="chat-header-right">
                        <el-button type="text" size="mini" @click="clearHistory" title="清空对话">
                            <i class="el-icon-delete" style="font-size:16px;color:#909399;"></i>
                        </el-button>
                        <el-button type="text" size="mini" @click="chatVisible = false" title="关闭">
                            <i class="el-icon-close" style="font-size:16px;color:#909399;"></i>
                        </el-button>
                    </div>
                </div>

                <div class="chat-messages" ref="chatMessages">
                    <div v-if="messages.length === 0" class="chat-welcome">
                        <div class="welcome-avatar">
                            <svg viewBox="0 0 1024 1024" width="56" height="56">
                                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z" fill="#4A6CF7"/>
                                <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z" fill="#6B8AFF"/>
                            </svg>
                        </div>
                        <p class="welcome-name">你好，我是小药 💊</p>
                        <p class="welcome-desc">药品管理系统的智能问答助手</p>
                        <div class="quick-questions">
                            <div class="quick-q" @click="sendQuick('当前有哪些药品库存不足？')">📦 库存不足的药品</div>
                            <div class="quick-q" @click="sendQuick('请帮我查看所有药品的库存情况')">📊 查看库存情况</div>
                            <div class="quick-q" @click="sendQuick('感冒药有哪些？用法用量是什么？')">💊 感冒药查询</div>
                            <div class="quick-q" @click="sendQuick('各药房的药品分布情况如何？')">🏥 药房分布查询</div>
                        </div>
                    </div>

                    <div v-for="(msg, index) in messages" :key="index"
                         :class="['chat-message', msg.role === 'user' ? 'message-user' : 'message-assistant']">
                        <div class="message-avatar" v-if="msg.role === 'assistant'">
                            <svg viewBox="0 0 1024 1024" width="28" height="28">
                                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z" fill="#4A6CF7"/>
                                <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z" fill="#6B8AFF"/>
                            </svg>
                        </div>
                        <div class="message-bubble" :class="msg.role">
                            <span v-html="formatMessage(msg.content)"></span>
                        </div>
                    </div>

                    <div v-if="loading" class="chat-message message-assistant">
                        <div class="message-avatar">
                            <svg viewBox="0 0 1024 1024" width="28" height="28">
                                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z" fill="#4A6CF7"/>
                                <path d="M512 140c-205.4 0-372 166.6-372 372s166.6 372 372 372 372-166.6 372-372-166.6-372-372-372z" fill="#6B8AFF"/>
                            </svg>
                        </div>
                        <div class="message-bubble assistant typing">
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
                            placeholder="请输入您的问题..."
                            @keyup.enter.native="sendMessage"
                            :disabled="loading"
                            size="small"
                            class="chat-input"
                    >
                    </el-input>
                    <el-button
                            type="primary"
                            icon="el-icon-s-promotion"
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
                hasNew: false
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
                let history = this.messages.slice(-10).map(m => ({
                    role: m.role,
                    content: m.content
                }));

                this.$axios.post(this.$httpUrl + '/ai/chat', {
                    message: msg,
                    history: history.slice(0, -1)
                }).then(res => res.data).then(res => {
                    if (res.code == 200 && res.data && res.data.reply) {
                        this.messages.push({ role: 'assistant', content: res.data.reply });
                    } else {
                        this.messages.push({ role: 'assistant', content: '抱歉，我暂时无法回答您的问题，请稍后再试。' });
                    }
                }).catch(() => {
                    this.messages.push({ role: 'assistant', content: '网络异常，请检查网络连接后重试。' });
                }).finally(() => {
                    this.loading = false;
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
        right: 28px;
        bottom: 28px;
        z-index: 2000;
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        transition: all 0.3s ease;
    }
    .chat-nurse-float:hover {
        transform: scale(1.08);
    }
    .chat-nurse-float:hover .float-icon {
        box-shadow: 0 6px 24px rgba(74, 108, 247, 0.45);
    }
    .float-icon {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 16px rgba(74, 108, 247, 0.35);
        transition: box-shadow 0.3s ease;
        background: #FFFFFF;
    }
    .float-label {
        background: #4A6CF7;
        color: #FFFFFF;
        padding: 6px 14px;
        border-radius: 20px;
        font-size: 13px;
        font-weight: 500;
        white-space: nowrap;
        box-shadow: 0 2px 8px rgba(74, 108, 247, 0.3);
    }
    .chat-nurse-float.has-new .float-icon {
        animation: pulse-float 1.5s infinite;
    }
    @keyframes pulse-float {
        0% { box-shadow: 0 4px 16px rgba(74, 108, 247, 0.35); }
        50% { box-shadow: 0 4px 24px rgba(74, 108, 247, 0.6); }
        100% { box-shadow: 0 4px 16px rgba(74, 108, 247, 0.35); }
    }

    .chat-container {
        display: flex;
        flex-direction: column;
        height: 680px;
        overflow: hidden;
    }
    .chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 0 12px 0;
        border-bottom: 1px solid #EBEEF5;
    }
    .chat-header-left {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .nurse-avatar-small {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #EEF0FF;
    }
    .chat-title {
        font-size: 15px;
        font-weight: 600;
        color: #1A202C;
    }
    .chat-header-right {
        display: flex;
        align-items: center;
        gap: 4px;
    }

    .chat-messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px 4px;
        scroll-behavior: smooth;
    }
    .chat-messages::-webkit-scrollbar {
        width: 4px;
    }
    .chat-messages::-webkit-scrollbar-thumb {
        background: #D0D5DD;
        border-radius: 2px;
    }

    .chat-welcome {
        text-align: center;
        padding: 36px 0;
    }
    .welcome-avatar {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        margin: 0 auto 14px;
        background: #EEF0FF;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .welcome-name {
        font-size: 20px;
        font-weight: 600;
        color: #1A202C;
        margin: 0 0 6px;
    }
    .welcome-desc {
        font-size: 14px;
        color: #909399;
        margin: 0 0 28px;
    }
    .quick-questions {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 10px;
        padding: 0 16px;
    }
    .quick-q {
        padding: 12px 14px;
        background: #F7F8FC;
        border-radius: 12px;
        font-size: 13px;
        color: #4A5568;
        cursor: pointer;
        transition: all 0.2s ease;
        text-align: left;
        border: 1px solid transparent;
    }
    .quick-q:hover {
        background: #EEF0FF;
        color: #4A6CF7;
        border-color: #C3CBFF;
    }

    .chat-message {
        display: flex;
        align-items: flex-start;
        margin-bottom: 16px;
        gap: 10px;
    }
    .message-user {
        flex-direction: row-reverse;
    }
    .message-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #EEF0FF;
        overflow: hidden;
    }
    .message-bubble {
        max-width: 80%;
        padding: 12px 16px;
        border-radius: 14px;
        font-size: 14px;
        line-height: 1.7;
        word-break: break-word;
    }
    .message-bubble.user {
        background: #4A6CF7;
        color: #FFFFFF;
        border-top-right-radius: 4px;
    }
    .message-bubble.assistant {
        background: #F7F8FC;
        color: #1A202C;
        border-top-left-radius: 4px;
    }
    .message-bubble.typing {
        padding: 14px 18px;
    }
    .typing-dots {
        display: flex;
        gap: 4px;
        align-items: center;
    }
    .typing-dots .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #A0AEC0;
        animation: typing-bounce 1.2s infinite ease-in-out;
    }
    .typing-dots .dot:nth-child(2) {
        animation-delay: 0.2s;
    }
    .typing-dots .dot:nth-child(3) {
        animation-delay: 0.4s;
    }
    @keyframes typing-bounce {
        0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
        40% { transform: scale(1); opacity: 1; }
    }

    .chat-input-area {
        display: flex;
        align-items: center;
        gap: 10px;
        padding-top: 14px;
        border-top: 1px solid #EBEEF5;
    }
    .chat-input {
        flex: 1;
    }
    .chat-input >>> .el-input__inner {
        border-radius: 20px;
        border-color: #DCDFE6;
    }
    .chat-input >>> .el-input__inner:focus {
        border-color: #4A6CF7;
    }
    .send-btn {
        border-radius: 50%;
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;
        flex-shrink: 0;
    }
</style>

<style>
    .chat-nurse-dialog {
        border-radius: 16px !important;
        overflow: hidden;
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15) !important;
    }
    .chat-nurse-dialog .el-dialog__header {
        display: none;
    }
    .chat-nurse-dialog .el-dialog__body {
        padding: 20px 24px;
    }
</style>