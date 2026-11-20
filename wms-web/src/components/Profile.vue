<template>
    <div class="profile">
        <header class="page-intro">
            <div class="eyebrow mono">ACCOUNT</div>
            <h1 class="intro-title">个人中心</h1>
        </header>

        <section class="identity-card">
            <div class="identity-avatar">{{ userInitial }}</div>
            <div class="identity-main">
                <div class="identity-name">
                    <span>{{ displayName }}</span>
                    <span class="role-badge" :class="'role-' + user.roleId">{{ roleText }}</span>
                </div>
                <div class="identity-meta mono">
                    <span>账号 {{ user.no || '—' }}</span>
                    <span class="meta-sep">·</span>
                    <span>ID {{ userIdText }}</span>
                </div>
            </div>
            <div class="identity-state" :class="{ 'is-off': !isValid }">
                <span class="status-dot" :class="isValid ? 'is-ok' : 'is-danger'"></span>
                {{ isValid ? '账号正常' : '账号已停用' }}
            </div>
        </section>

        <div class="profile-grid">
            <section class="card info-card">
                <div class="card-head">
                    <h2 class="card-title">账号资料</h2>
                    <span class="card-tag mono">PROFILE</span>
                </div>
                <dl class="info-list">
                    <div class="info-row">
                        <dt>账号</dt>
                        <dd class="mono">{{ user.no || '—' }}</dd>
                    </div>
                    <div class="info-row">
                        <dt>姓名</dt>
                        <dd>{{ user.name || '—' }}</dd>
                    </div>
                    <div class="info-row">
                        <dt>性别</dt>
                        <dd>{{ sexText }}</dd>
                    </div>
                    <div class="info-row">
                        <dt>年龄</dt>
                        <dd>{{ ageText }}</dd>
                    </div>
                    <div class="info-row">
                        <dt>联系电话</dt>
                        <dd class="mono">{{ user.phone || '未填写' }}</dd>
                    </div>
                    <div class="info-row">
                        <dt>账号状态</dt>
                        <dd>
                            <span class="status-dot" :class="isValid ? 'is-ok' : 'is-danger'"></span>
                            {{ isValid ? '正常' : '已停用' }}
                        </dd>
                    </div>
                </dl>
                <div class="scope-block">
                    <div class="scope-label mono">权限范围</div>
                    <p class="scope-text">{{ roleScope }}</p>
                </div>
            </section>

            <section class="card security-card">
                <div class="card-head">
                    <h2 class="card-title">修改密码</h2>
                    <span class="card-tag mono">SECURITY</span>
                </div>
                <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-position="top" class="pwd-form">
                    <el-form-item label="原密码" prop="oldPassword">
                        <el-input v-model="pwdForm.oldPassword" type="password" show-password
                                  autocomplete="current-password" placeholder="请输入当前密码"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                        <el-input v-model="pwdForm.newPassword" type="password" show-password
                                  autocomplete="new-password" placeholder="请输入新密码"></el-input>
                    </el-form-item>
                    <el-form-item label="确认密码" prop="confirmPassword">
                        <el-input v-model="pwdForm.confirmPassword" type="password" show-password
                                  autocomplete="new-password" placeholder="请再次输入新密码"
                                  @keyup.enter="submitPwd"></el-input>
                    </el-form-item>
                    <el-button type="primary" class="pwd-submit" :loading="pwdSubmitting" @click="submitPwd">
                        更新密码
                    </el-button>
                </el-form>
                <p class="security-tip">
                    <el-icon><InfoFilled/></el-icon>
                    <span>修改成功后需使用新密码重新登录。密码长度 3–20 位。</span>
                </p>
            </section>
        </div>
    </div>
</template>

<script>
    export default {
        name: "Profile",
        data(){
            let validateNew = (rule, value, callback) => {
                const v = (value || '').trim()
                if (!v) {
                    callback(new Error('请输入新密码'))
                } else if (v.length < 3 || v.length > 20) {
                    callback(new Error('长度在 3 到 20 个字符'))
                } else if (v === (this.pwdForm.oldPassword || '').trim()) {
                    callback(new Error('新密码不能与原密码相同'))
                } else {
                    callback()
                }
            };
            let validateConfirm = (rule, value, callback) => {
                if ((value || '').trim() !== (this.pwdForm.newPassword || '').trim()) {
                    callback(new Error('两次输入的新密码不一致'))
                } else {
                    callback()
                }
            };
            return {
                user: JSON.parse(sessionStorage.getItem('CurUser')) || {},
                pwdSubmitting: false,
                pwdForm: {
                    oldPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                },
                pwdRules: {
                    oldPassword: [
                        {required: true, message: '请输入原密码', trigger: 'blur'}
                    ],
                    newPassword: [
                        {required: true, message: '请输入新密码', trigger: 'blur'},
                        {validator: validateNew, trigger: 'blur'}
                    ],
                    confirmPassword: [
                        {required: true, message: '请再次输入新密码', trigger: 'blur'},
                        {validator: validateConfirm, trigger: 'blur'}
                    ]
                }
            }
        },
        computed: {
            displayName() {
                return this.user.name || this.user.no || '未命名账号'
            },
            userInitial() {
                const source = this.user.name || this.user.no || 'U'
                return source.slice(0, 1)
            },
            userIdText() {
                return this.user.id === null || this.user.id === undefined ? '—' : this.user.id
            },
            roleText() {
                const role = String(this.user.roleId)
                if (role === '0') return '超级管理员'
                if (role === '1') return '管理员'
                return '普通用户'
            },
            roleScope() {
                const role = String(this.user.roleId)
                if (role === '0') return '全部功能：管理员、用户、药房、分类、药品与流转记录管理。'
                if (role === '1') return '业务维护：用户、药房、分类、药品与流转记录。'
                return '只读查询：药品、库存与流转记录。'
            },
            isValid() {
                return this.user.isvalid !== 'N'
            },
            sexText() {
                if (this.user.sex === 1) return '男'
                if (this.user.sex === 0) return '女'
                return '未填写'
            },
            ageText() {
                return this.user.age === null || this.user.age === undefined || this.user.age === ''
                    ? '未填写'
                    : this.user.age + ' 岁'
            }
        },
        methods: {
            submitPwd() {
                if (this.pwdSubmitting) return
                this.$refs.pwdForm.validate((valid) => {
                    if (!valid) return
                    this.pwdSubmitting = true
                    this.$axios.post(this.$httpUrl + '/user/changePassword', {
                        oldPassword: this.pwdForm.oldPassword,
                        newPassword: this.pwdForm.newPassword.trim()
                    }).then(res => res.data).then(res => {
                        if (res.code == 200) {
                            this.$message({message: '密码修改成功，请重新登录', type: 'success'})
                            this.$axios.post(this.$httpUrl + '/user/logout').finally(() => {
                                sessionStorage.clear()
                                this.$router.push('/')
                            })
                        } else {
                            this.$message({message: res.msg || '修改失败', type: 'error'})
                        }
                    }).catch(() => {
                        this.$message({message: '修改失败，请稍后重试', type: 'error'})
                    }).finally(() => {
                        this.pwdSubmitting = false
                    })
                })
            }
        }
    }
</script>

<style scoped>
    .profile {
        padding-bottom: 8px;
    }
    .page-intro {
        margin-bottom: 24px;
    }
    .eyebrow {
        font-size: 10px;
        letter-spacing: 0.16em;
        color: var(--muted);
        margin-bottom: 8px;
    }
    .intro-title {
        font-size: 26px;
        font-weight: 500;
        color: var(--ink);
        margin: 0;
        letter-spacing: 0.01em;
    }

    .identity-card {
        display: flex;
        align-items: center;
        gap: 18px;
        padding: 22px 24px;
        margin-bottom: 16px;
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 12px;
    }
    .identity-avatar {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        background: var(--accent-soft);
        border: 1px solid #D3E4DA;
        color: var(--accent);
        font-size: 24px;
        font-weight: 500;
    }
    .identity-main {
        flex: 1;
        min-width: 0;
    }
    .identity-name {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;
        font-size: 20px;
        line-height: 1.3;
        color: var(--ink);
    }
    .identity-meta {
        margin-top: 6px;
        font-size: 11px;
        color: var(--muted);
    }
    .meta-sep {
        margin: 0 6px;
    }
    .identity-state {
        display: flex;
        align-items: center;
        flex-shrink: 0;
        padding: 4px 12px;
        border-radius: 999px;
        border: 1px solid #D3E4DA;
        background: var(--accent-soft);
        color: var(--accent);
        font-size: 12px;
        white-space: nowrap;
    }
    .identity-state.is-off {
        background: var(--red-soft);
        border-color: #E9D1CF;
        color: var(--red);
    }

    .profile-grid {
        display: grid;
        grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr);
        gap: 16px;
    }
    .card {
        padding: 20px 22px;
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 12px;
    }
    .card-head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
    }
    .card-title {
        margin: 0;
        font-size: 15px;
        font-weight: 500;
        color: var(--ink);
    }
    .card-tag {
        padding-top: 2px;
        font-size: 10px;
        letter-spacing: 0.08em;
        color: var(--muted);
    }

    .info-list {
        margin-top: 10px;
    }
    .info-row {
        display: flex;
        align-items: baseline;
        justify-content: space-between;
        gap: 24px;
        padding: 13px 0;
        border-top: 1px solid var(--line);
    }
    .info-row:first-child {
        border-top: none;
    }
    .info-row dt {
        flex-shrink: 0;
        font-size: 12px;
        color: var(--muted);
    }
    .info-row dd {
        margin: 0;
        font-size: 13px;
        color: var(--ink);
        text-align: right;
        word-break: break-all;
    }
    .scope-block {
        margin-top: 18px;
        padding-top: 14px;
        border-top: 1px solid var(--line);
    }
    .scope-label {
        margin-bottom: 6px;
        font-size: 10px;
        letter-spacing: 0.12em;
        color: var(--muted);
    }
    .scope-text {
        margin: 0;
        font-size: 12px;
        line-height: 1.7;
        color: var(--ink-2);
    }

    .pwd-form {
        margin-top: 14px;
    }
    .pwd-form :deep(.el-form-item) {
        margin-bottom: 16px;
    }
    .pwd-form :deep(.el-form-item__label) {
        padding-bottom: 6px;
        font-size: 12px;
        line-height: 1.4;
        color: var(--muted);
    }
    .pwd-submit {
        width: 100%;
        margin-top: 2px;
    }
    .security-tip {
        display: flex;
        align-items: flex-start;
        gap: 6px;
        margin: 14px 0 0;
        font-size: 11px;
        line-height: 1.65;
        color: var(--muted);
    }
    .security-tip .el-icon {
        margin-top: 2px;
        flex-shrink: 0;
        color: var(--accent);
    }

    @media (max-width: 1100px) {
        .profile-grid {
            grid-template-columns: 1fr;
        }
    }
    @media (max-width: 720px) {
        .page-intro {
            margin-bottom: 18px;
        }
        .intro-title {
            font-size: 24px;
        }
        .identity-card {
            gap: 14px;
            padding: 18px;
        }
        .identity-avatar {
            width: 52px;
            height: 52px;
            font-size: 20px;
        }
        .identity-name {
            font-size: 18px;
        }
        .identity-state {
            display: none;
        }
        .card {
            padding: 18px;
        }
    }
</style>
