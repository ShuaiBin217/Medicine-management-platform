<template>
    <div class="login-page">
        <section class="login-brand">
            <div class="brand-top">
                <div class="brand-mark">
                    <el-icon><FirstAidKit/></el-icon>
                </div>
                <div class="brand-copy">
                    <span class="brand-name">DrugWMS</span>
                    <span class="brand-sub">药品智能管理平台</span>
                </div>
            </div>

            <div class="brand-main">
                <div class="brand-kicker mono">PHARMACY / INVENTORY</div>
                <h1 class="brand-title">药房库存与<br/>药品流转管理</h1>
                <p class="brand-desc">面向药房内部的库存、出入库与流转记录系统。数据仅对授权人员开放。</p>

                <ul class="brand-points">
                    <li>
                        <span class="point-no mono">01</span>
                        <div class="point-body">
                            <b>库存可见</b>
                            <span>按药房、分类查看当前在库数量</span>
                        </div>
                    </li>
                    <li>
                        <span class="point-no mono">02</span>
                        <div class="point-body">
                            <b>流转留痕</b>
                            <span>采购入库、发药出库，逐笔记录</span>
                        </div>
                    </li>
                    <li>
                        <span class="point-no mono">03</span>
                        <div class="point-body">
                            <b>账号分权</b>
                            <span>管理员维护数据，普通用户只读查询</span>
                        </div>
                    </li>
                </ul>
            </div>

            <div class="brand-foot">
                <span class="status-dot"></span>
                药房内部系统 · 仅限授权人员
            </div>
        </section>

        <main class="login-main">
            <div class="login-card">
                <div class="card-head">
                    <span class="card-kicker mono">SIGN IN</span>
                    <h2 class="card-title">登录</h2>
                    <p class="card-desc">使用药房分配的账号进入系统</p>
                </div>

                <el-form :model="loginForm" :rules="rules" ref="loginForm"
                         label-position="top" class="login-form">
                    <el-form-item label="账号" prop="no">
                        <el-input type="text" v-model="loginForm.no" size="large"
                                  autocomplete="off" prefix-icon="User"
                                  placeholder="请输入账号"
                                  @keyup.enter="confirm"></el-input>
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input type="password" v-model="loginForm.password" size="large"
                                  show-password autocomplete="off" prefix-icon="Lock"
                                  placeholder="请输入密码" @keyup.enter="confirm"></el-input>
                    </el-form-item>
                    <el-form-item class="submit-item">
                        <el-button type="primary" @click="confirm" :disabled="confirm_disabled"
                                   :loading="confirm_disabled" class="login-btn">
                            {{ confirm_disabled ? '正在登录…' : '登录' }}
                        </el-button>
                    </el-form-item>
                </el-form>

                <div class="card-foot">
                    <span>忘记密码请联系管理员重置</span>
                    <span class="mono">v1.0</span>
                </div>
            </div>
            <p class="login-copy">DrugWMS · 药房内部使用</p>
        </main>
    </div>
</template>

<script>
    import { useMenuStore } from '../stores/menu'

    export default {
        name: "Login",
        data() {
            return {
                confirm_disabled: false,
                loginForm: {
                    no: '',
                    password: ''
                },
                rules: {
                    no: [
                        {required: true, message: '请输入账号', trigger: 'blur'}
                    ],
                    password: [
                        {required: true, message: '请输入密码', trigger: 'blur'}
                    ],
                }
            }
        },
        methods: {
            confirm() {
                this.confirm_disabled = true;
                this.$refs.loginForm.validate((valid) => {
                    if (valid) {
                        this.$axios.post(this.$httpUrl + '/user/login', this.loginForm).then(res => res.data).then(res => {
                            if (res.code == 200) {
                                sessionStorage.setItem("CurUser", JSON.stringify(res.data.user));
                                useMenuStore().setMenu(res.data.menu);
                                this.$router.replace('/Index');
                            } else {
                                this.confirm_disabled = false;
                                this.$message({
                                    message: res.msg || '账号或密码错误',
                                    type: 'error'
                                });
                                return false;
                            }
                        }).catch(() => {
                            this.confirm_disabled = false;
                            this.$message.error('网络异常，请稍后重试');
                        });
                    } else {
                        this.confirm_disabled = false;
                        return false;
                    }
                });
            }
        }
    }
</script>

<style scoped>
    .login-page {
        min-height: 100vh;
        display: flex;
        background: var(--paper);
        color: var(--ink);
    }
    .login-brand {
        width: 44%;
        min-width: 380px;
        max-width: 620px;
        background: #1B3B2F;
        color: #FDFBF7;
        padding: 36px 48px 32px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        position: relative;
        overflow: hidden;
    }
    .login-brand::after {
        content: '';
        position: absolute;
        right: -160px;
        bottom: -220px;
        width: 460px;
        height: 460px;
        border: 1px solid rgba(253, 251, 247, 0.10);
        border-radius: 50%;
        pointer-events: none;
    }
    .brand-top {
        display: flex;
        align-items: center;
        gap: 12px;
        position: relative;
        z-index: 1;
    }
    .brand-mark {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: #FDFBF7;
        color: var(--accent);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
        flex-shrink: 0;
    }
    .brand-copy {
        display: flex;
        flex-direction: column;
        line-height: 1.2;
    }
    .brand-name {
        font-size: 17px;
        font-weight: 500;
        letter-spacing: 0.02em;
    }
    .brand-sub {
        font-size: 11px;
        color: rgba(253, 251, 247, 0.62);
        margin-top: 3px;
    }
    .brand-main {
        position: relative;
        z-index: 1;
        max-width: 440px;
    }
    .brand-kicker {
        font-size: 10px;
        letter-spacing: 0.18em;
        color: rgba(253, 251, 247, 0.55);
        margin-bottom: 18px;
    }
    .brand-title {
        font-size: 34px;
        line-height: 1.32;
        font-weight: 500;
        letter-spacing: 0.01em;
        margin: 0 0 18px;
    }
    .brand-desc {
        font-size: 13px;
        line-height: 1.8;
        color: rgba(253, 251, 247, 0.72);
        margin: 0 0 34px;
    }
    .brand-points {
        list-style: none;
        border-top: 1px solid rgba(253, 251, 247, 0.14);
    }
    .brand-points li {
        display: grid;
        grid-template-columns: 34px 1fr;
        gap: 12px;
        padding: 14px 0;
        border-bottom: 1px solid rgba(253, 251, 247, 0.14);
    }
    .point-no {
        font-size: 11px;
        color: rgba(253, 251, 247, 0.45);
        padding-top: 2px;
    }
    .point-body {
        display: flex;
        flex-direction: column;
        gap: 3px;
    }
    .point-body b {
        font-size: 13px;
        font-weight: 500;
        color: #FDFBF7;
    }
    .point-body span {
        font-size: 12px;
        color: rgba(253, 251, 247, 0.58);
    }
    .brand-foot {
        position: relative;
        z-index: 1;
        font-size: 11px;
        color: rgba(253, 251, 247, 0.45);
    }
    .brand-foot .status-dot {
        background: #8FBFA8;
        margin-right: 8px;
    }

    .login-main {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 48px 24px;
        position: relative;
    }
    .login-card {
        width: 380px;
        max-width: 100%;
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 16px;
        padding: 34px 32px 22px;
    }
    .card-head {
        margin-bottom: 26px;
    }
    .card-kicker {
        font-size: 10px;
        letter-spacing: 0.18em;
        color: var(--muted);
    }
    .card-title {
        font-size: 24px;
        font-weight: 500;
        color: var(--ink);
        margin: 8px 0 6px;
    }
    .card-desc {
        font-size: 13px;
        color: var(--muted);
        margin: 0;
    }
    .login-form :deep(.el-form-item__label) {
        padding-bottom: 6px;
        line-height: 1.4;
        font-size: 12px;
        color: var(--ink-2);
    }
    .login-form :deep(.el-form-item) {
        margin-bottom: 18px;
    }
    .submit-item {
        margin-top: 26px;
        margin-bottom: 6px !important;
    }
    .login-btn {
        width: 100%;
        height: 42px;
        font-size: 14px;
        letter-spacing: 0.04em;
        border-radius: 8px !important;
    }
    .card-foot {
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-top: 1px solid var(--line);
        margin-top: 20px;
        padding-top: 14px;
        font-size: 11px;
        color: var(--muted);
    }
    .login-copy {
        position: absolute;
        bottom: 18px;
        font-size: 10px;
        color: var(--muted);
        letter-spacing: 0.06em;
    }

    @media (max-width: 960px) {
        .login-brand {
            display: none;
        }
    }
    @media (max-width: 480px) {
        .login-main {
            padding: 24px 16px;
        }
        .login-card {
            padding: 26px 22px 18px;
        }
    }
</style>
