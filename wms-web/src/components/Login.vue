<template>
    <div class="login-page">
        <div class="ambient-layer" aria-hidden="true">
            <div class="ambient-wash wash-a"></div>
            <div class="ambient-wash wash-b"></div>
            <div class="ambient-sheen"></div>
            <span v-for="n in 12" :key="n" class="dust-mote"></span>
        </div>

        <section class="login-brand">
            <div class="brand-visual" aria-hidden="true">
                <svg viewBox="0 0 240 240" focusable="false">
                    <path pathLength="1" d="M96 24H144V96H216V144H144V216H96V144H24V96H96Z"/>
                    <circle cx="120" cy="120" r="3"/>
                </svg>
            </div>

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
                <h1 class="brand-title"
                    @mouseenter="handleSpotlightEnter"
                    @mousemove="handleSpotlightMove"
                    @mouseleave="handleSpotlightLeave">药房库存与<br/>药品流转管理</h1>
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
            <div class="login-card" :class="{ 'is-error': errorShake }">
                <div class="card-head">
                    <span class="card-kicker mono">SIGN IN</span>
                    <h2 class="card-title">登录</h2>
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
                            <span>{{ confirm_disabled ? '正在登录…' : '登录' }}</span>
                            <el-icon v-if="!confirm_disabled" class="login-btn-arrow" aria-hidden="true">
                                <Right/>
                            </el-icon>
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

        <div class="title-spotlight title-spotlight--invert"
             :class="{ 'is-active': spotlight.visible }"
             :style="{ left: spotlight.x + 'px', top: spotlight.y + 'px' }"
             aria-hidden="true"></div>
        <div class="title-spotlight title-spotlight--green"
             :class="{ 'is-active': spotlight.visible }"
             :style="{ left: spotlight.x + 'px', top: spotlight.y + 'px' }"
             aria-hidden="true"></div>
    </div>
</template>

<script>
    import { useMenuStore } from '../stores/menu'

    export default {
        name: "Login",
        data() {
            return {
                confirm_disabled: false,
                spotlight: {
                    visible: false,
                    x: 0,
                    y: 0
                },
                errorShake: false,
                errorTimer: null,
                errorFrame: null,
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
        beforeUnmount() {
            this.resetError();
        },
        methods: {
            handleSpotlightEnter(event) {
                this.spotlight.x = event.clientX;
                this.spotlight.y = event.clientY;
                this.spotlight.visible = true;
            },
            handleSpotlightMove(event) {
                this.spotlight.x = event.clientX;
                this.spotlight.y = event.clientY;
            },
            handleSpotlightLeave() {
                this.spotlight.visible = false;
            },
            resetError() {
                if (this.errorTimer) {
                    window.clearTimeout(this.errorTimer);
                    this.errorTimer = null;
                }
                if (this.errorFrame) {
                    window.cancelAnimationFrame(this.errorFrame);
                    this.errorFrame = null;
                }
                this.errorShake = false;
            },
            flagError() {
                this.resetError();
                this.errorFrame = window.requestAnimationFrame(() => {
                    this.errorFrame = null;
                    this.errorShake = true;
                    this.errorTimer = window.setTimeout(() => {
                        this.errorShake = false;
                        this.errorTimer = null;
                    }, 420);
                });
            },
            confirm() {
                this.resetError();
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
                                this.flagError();
                                return false;
                            }
                        }).catch(() => {
                            this.confirm_disabled = false;
                            this.$message.error('网络异常，请稍后重试');
                            this.flagError();
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
        --login-ease: cubic-bezier(0.22, 0.61, 0.36, 1);
        position: relative;
        overflow: hidden;
        isolation: isolate;
        min-height: 100vh;
        display: flex;
        align-items: stretch;
        justify-content: center;
        gap: clamp(40px, 7vw, 112px);
        padding: 0 clamp(24px, 6vw, 96px);
        background: var(--paper);
        color: var(--ink);
    }

    .ambient-layer {
        position: absolute;
        inset: 0;
        z-index: 0;
        overflow: hidden;
        pointer-events: none;
    }
    .ambient-wash {
        position: absolute;
        border-radius: 50%;
        mix-blend-mode: multiply;
        will-change: transform;
    }
    .wash-a {
        width: 72vw;
        height: 72vw;
        min-width: 780px;
        min-height: 780px;
        left: -22vw;
        top: -34vw;
        background: radial-gradient(circle at 50% 50%, rgba(229, 196, 147, 0.52) 0%, rgba(229, 196, 147, 0.24) 40%, rgba(229, 196, 147, 0) 72%);
        animation: ambient-drift-a 26s ease-in-out infinite alternate;
    }
    .wash-b {
        width: 64vw;
        height: 64vw;
        min-width: 700px;
        min-height: 700px;
        right: -18vw;
        bottom: -30vw;
        background: radial-gradient(circle at 50% 50%, rgba(187, 202, 178, 0.42) 0%, rgba(187, 202, 178, 0.18) 42%, rgba(187, 202, 178, 0) 74%);
        animation: ambient-drift-b 32s ease-in-out infinite alternate;
    }
    .ambient-sheen {
        position: absolute;
        top: -40%;
        left: -85%;
        width: 72%;
        height: 180%;
        background: linear-gradient(90deg, rgba(255, 251, 240, 0) 0%, rgba(255, 251, 240, 0.80) 45%, rgba(255, 251, 240, 0.34) 62%, rgba(255, 251, 240, 0) 100%);
        mix-blend-mode: soft-light;
        opacity: 0;
        transform: translate3d(0, 0, 0) rotate(16deg);
        animation: ambient-sheen 20s ease-in-out infinite;
        will-change: transform, opacity;
    }
    .dust-mote {
        position: absolute;
        width: 3px;
        height: 3px;
        border-radius: 50%;
        background: rgba(166, 128, 81, 0.50);
        opacity: 0;
        animation-name: ambient-dust;
        animation-timing-function: linear;
        animation-iteration-count: infinite;
        will-change: transform, opacity;
    }
    .dust-mote:nth-of-type(1) {
        left: 7%;
        top: 80%;
        width: 4px;
        height: 4px;
        --drift: 32px;
        animation-duration: 22s;
        animation-delay: -3s;
    }
    .dust-mote:nth-of-type(2) {
        left: 14%;
        top: 68%;
        width: 3px;
        height: 3px;
        --drift: -24px;
        animation-duration: 26s;
        animation-delay: -9s;
    }
    .dust-mote:nth-of-type(3) {
        left: 24%;
        top: 86%;
        width: 5px;
        height: 5px;
        --drift: 40px;
        animation-duration: 20s;
        animation-delay: -5s;
    }
    .dust-mote:nth-of-type(4) {
        left: 34%;
        top: 72%;
        width: 3px;
        height: 3px;
        --drift: -30px;
        animation-duration: 24s;
        animation-delay: -14s;
    }
    .dust-mote:nth-of-type(5) {
        left: 45%;
        top: 88%;
        width: 4px;
        height: 4px;
        --drift: 22px;
        animation-duration: 27s;
        animation-delay: -2s;
    }
    .dust-mote:nth-of-type(6) {
        left: 56%;
        top: 76%;
        width: 3px;
        height: 3px;
        --drift: -34px;
        animation-duration: 22s;
        animation-delay: -11s;
    }
    .dust-mote:nth-of-type(7) {
        left: 66%;
        top: 84%;
        width: 4px;
        height: 4px;
        --drift: 30px;
        animation-duration: 25s;
        animation-delay: -17s;
    }
    .dust-mote:nth-of-type(8) {
        left: 76%;
        top: 70%;
        width: 3px;
        height: 3px;
        --drift: -22px;
        animation-duration: 29s;
        animation-delay: -7s;
    }
    .dust-mote:nth-of-type(9) {
        left: 85%;
        top: 88%;
        width: 5px;
        height: 5px;
        --drift: 36px;
        animation-duration: 21s;
        animation-delay: -13s;
    }
    .dust-mote:nth-of-type(10) {
        left: 92%;
        top: 76%;
        width: 3px;
        height: 3px;
        --drift: -28px;
        animation-duration: 24s;
        animation-delay: -19s;
    }
    .dust-mote:nth-of-type(11) {
        left: 50%;
        top: 62%;
        width: 2px;
        height: 2px;
        --drift: 18px;
        animation-duration: 30s;
        animation-delay: -23s;
    }
    .dust-mote:nth-of-type(12) {
        left: 30%;
        top: 58%;
        width: 2px;
        height: 2px;
        --drift: -20px;
        animation-duration: 28s;
        animation-delay: -6s;
    }

    .login-brand {
        flex: 0 1 520px;
        width: 520px;
        max-width: 520px;
        min-width: 380px;
        padding: 40px 0 32px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        position: relative;
        z-index: 1;
    }
    .brand-visual {
        position: absolute;
        right: 0;
        bottom: 0;
        width: 320px;
        height: 320px;
        color: var(--accent);
        pointer-events: none;
        z-index: 0;
    }
    .brand-visual svg {
        display: block;
        width: 100%;
        height: 100%;
        overflow: visible;
    }
    .brand-visual path {
        fill: none;
        stroke: currentColor;
        stroke-width: 1.5;
        stroke-dasharray: 1;
        stroke-dashoffset: 1;
        animation: login-draw 1.5s var(--login-ease) 0.2s forwards;
    }
    .brand-visual circle {
        fill: #B91C1C;
        opacity: 0;
        animation: login-fade 0.5s var(--login-ease) 1.05s forwards;
    }
    .brand-top {
        display: flex;
        align-items: center;
        gap: 12px;
        position: relative;
        top: 94px;
        left: -40px;
        align-self: center;
        z-index: 1;
        animation: login-rise 0.55s var(--login-ease) both;
    }
    .brand-mark {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: var(--accent);
        color: #FDFBF7;
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
        color: var(--ink);
    }
    .brand-sub {
        font-size: 11px;
        color: var(--muted);
        margin-top: 3px;
    }
    .brand-main {
        position: relative;
        z-index: 1;
        max-width: 440px;
        animation: login-rise 0.6s var(--login-ease) 0.08s both;
    }
    .brand-kicker {
        font-size: 10px;
        letter-spacing: 0.18em;
        color: var(--accent);
        margin-bottom: 18px;
    }
    .brand-title {
        font-size: 34px;
        line-height: 1.32;
        font-weight: 500;
        letter-spacing: 0.01em;
        color: var(--ink);
        width: fit-content;
        margin: 0 0 18px;
    }
    .brand-desc {
        font-size: 13px;
        line-height: 1.8;
        color: var(--ink-2);
        margin: 0 0 34px;
    }
    .brand-points {
        list-style: none;
        border-top: 1px solid var(--line);
    }
    .brand-points li {
        display: grid;
        grid-template-columns: 34px 1fr;
        gap: 12px;
        padding: 14px 0;
        border-bottom: 1px solid var(--line);
        animation: login-rise 0.5s var(--login-ease) both;
    }
    .brand-points li:nth-child(1) {
        animation-delay: 0.16s;
    }
    .brand-points li:nth-child(2) {
        animation-delay: 0.22s;
    }
    .brand-points li:nth-child(3) {
        animation-delay: 0.28s;
    }
    .point-no {
        font-size: 11px;
        color: var(--muted);
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
        color: var(--ink);
    }
    .point-body span {
        font-size: 12px;
        color: var(--muted);
    }
    .brand-foot {
        position: relative;
        z-index: 1;
        font-size: 11px;
        color: var(--muted);
        animation: login-fade 0.6s var(--login-ease) 0.32s both;
    }
    .brand-foot .status-dot {
        background: var(--accent);
        margin-right: 8px;
    }

    .title-spotlight {
        position: fixed;
        width: 64px;
        height: 64px;
        border-radius: 50%;
        pointer-events: none;
        opacity: 0;
        transform: translate(-50%, -50%) scale(0.72);
        transition: opacity 0.16s ease, transform 0.16s var(--login-ease);
        will-change: left, top, opacity, transform;
    }
    .title-spotlight--invert {
        background: #FFFFFF;
        mix-blend-mode: difference;
        z-index: 59;
    }
    .title-spotlight--green {
        background: var(--accent);
        mix-blend-mode: lighten;
        z-index: 60;
    }
    .title-spotlight.is-active {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
    }
    @media (hover: none), (pointer: coarse) {
        .title-spotlight {
            display: none;
        }
    }

    .login-main {
        flex: 0 1 520px;
        width: 520px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px 0;
        position: relative;
        z-index: 1;
        animation: login-card-in 0.6s var(--login-ease) 0.12s both;
        min-width: 0;
    }
    .login-card {
        width: 100%;
        max-width: 380px;
        background: var(--card);
        border: 1px solid var(--line);
        border-radius: 16px;
        padding: 34px 32px 22px;
        transition: border-color 0.18s var(--login-ease);
    }
    .login-card:focus-within {
        border-color: rgba(29, 107, 79, 0.30);
    }
    .login-card.is-error {
        border-color: rgba(185, 28, 28, 0.38);
        animation: login-shake 0.38s var(--login-ease);
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
    .login-form :deep(.el-form-item__label) {
        padding-bottom: 6px;
        line-height: 1.4;
        font-size: 12px;
        color: var(--ink-2);
        transition: color 0.18s var(--login-ease);
    }
    .login-form :deep(.el-form-item:focus-within .el-form-item__label) {
        color: var(--accent) !important;
    }
    .login-form :deep(.el-input__wrapper) {
        transition: border-color 0.18s var(--login-ease), box-shadow 0.18s var(--login-ease);
    }
    .login-form :deep(.el-input__wrapper:focus-within),
    .login-form :deep(.el-input__wrapper.is-focus) {
        border-color: var(--accent) !important;
        box-shadow: 0 0 0 1px var(--accent) inset, 0 0 0 3px rgba(29, 107, 79, 0.10) !important;
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
        transition: background-color 0.18s var(--login-ease), transform 0.12s var(--login-ease);
    }
    .login-btn :deep(> span) {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
    }
    .login-btn-arrow {
        font-size: 14px;
        transition: transform 0.18s var(--login-ease);
    }
    .login-btn:hover:not(:disabled) {
        transform: translateY(-1px);
    }
    .login-btn:hover:not(:disabled) .login-btn-arrow {
        transform: translateX(3px);
    }
    .login-btn:active:not(:disabled) {
        transform: translateY(0);
    }
    .login-btn:focus-visible {
        outline: 2px solid var(--accent);
        outline-offset: 2px;
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
        bottom: 32px;
        font-size: 10px;
        color: var(--muted);
        letter-spacing: 0.06em;
    }

    @keyframes login-rise {
        from {
            opacity: 0;
            transform: translate3d(0, 8px, 0);
        }
        to {
            opacity: 1;
            transform: translate3d(0, 0, 0);
        }
    }
    @keyframes login-card-in {
        from {
            opacity: 0;
            transform: translate3d(0, 12px, 0);
        }
        to {
            opacity: 1;
            transform: translate3d(0, 0, 0);
        }
    }
    @keyframes login-fade {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }
    @keyframes login-draw {
        to {
            stroke-dashoffset: 0;
        }
    }
    @keyframes ambient-drift-a {
        0% {
            transform: translate3d(0, 0, 0) scale(1);
        }
        100% {
            transform: translate3d(9vw, 5vh, 0) scale(1.14);
        }
    }
    @keyframes ambient-drift-b {
        0% {
            transform: translate3d(0, 0, 0) scale(1.04);
        }
        100% {
            transform: translate3d(-8vw, -5vh, 0) scale(0.94);
        }
    }
    @keyframes ambient-sheen {
        0%, 54% {
            transform: translate3d(0, 0, 0) rotate(16deg);
            opacity: 0;
        }
        62% {
            opacity: 0.78;
        }
        82% {
            opacity: 0.46;
        }
        100% {
            transform: translate3d(150vw, 0, 0) rotate(16deg);
            opacity: 0;
        }
    }
    @keyframes ambient-dust {
        0% {
            opacity: 0;
            transform: translate3d(0, 16px, 0) scale(0.7);
        }
        12% {
            opacity: 0.72;
        }
        70% {
            opacity: 0.38;
        }
        100% {
            opacity: 0;
            transform: translate3d(var(--drift, 20px), -170px, 0) scale(1.08);
        }
    }
    @keyframes login-shake {
        0%, 100% {
            transform: translateX(0);
        }
        20% {
            transform: translateX(-4px);
        }
        40% {
            transform: translateX(4px);
        }
        60% {
            transform: translateX(-3px);
        }
        80% {
            transform: translateX(2px);
        }
    }

    @media (max-width: 960px) {
        .login-page {
            padding: 0;
        }
        .login-brand {
            display: none;
        }
        .login-main {
            flex: 1;
            width: 100%;
            padding: 48px 24px;
        }
    }
    @media (max-width: 480px) {
        .login-main {
            padding: 24px 16px;
        }
        .login-card {
            padding: 26px 22px 18px;
        }
        .login-copy {
            bottom: 18px;
        }
    }
    @media (prefers-reduced-motion: reduce) {
        .login-main,
        .brand-top,
        .brand-main,
        .brand-points li,
        .brand-foot {
            animation: none;
            opacity: 1;
            transform: none;
        }
        .brand-visual path {
            animation: none;
            stroke-dashoffset: 0;
        }
        .brand-visual circle {
            animation: none;
            opacity: 1;
        }
        .login-card.is-error {
            animation: none;
        }
        .login-btn,
        .login-btn-arrow,
        .login-card,
        .login-form :deep(.el-input__wrapper),
        .login-form :deep(.el-form-item__label) {
            transition: none;
        }
        .login-btn:hover:not(:disabled),
        .login-btn:hover:not(:disabled) .login-btn-arrow {
            transform: none;
        }
        .ambient-wash,
        .ambient-sheen,
        .dust-mote {
            animation: none;
        }
        .ambient-sheen,
        .dust-mote {
            opacity: 0;
        }
    }
</style>
