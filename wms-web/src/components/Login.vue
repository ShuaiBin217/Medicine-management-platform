<template>
    <div class="login-page" @mousemove="onMouseMove">
        <div class="login-left">
            <div class="login-left__brand">
                <div class="brand-icon">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                        <path d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3Z" fill="white" opacity="0.3"/>
                        <path d="M12 7V17M7 12H17" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
                    </svg>
                </div>
                <span class="brand-text">DrugWMS</span>
            </div>
            <div class="login-left__content">
                <h2 class="login-left__title">药品智能管理平台</h2>
                <p class="login-left__desc">高效管理药品库存，精准追踪流转记录，<br/>让药品管理更安全、更智能</p>
                <div class="login-left__features">
                    <div class="feature-item">
                        <div class="feature-dot"></div>
                        <span>实时库存监控</span>
                    </div>
                    <div class="feature-item">
                        <div class="feature-dot"></div>
                        <span>智能预警提醒</span>
                    </div>
                    <div class="feature-item">
                        <div class="feature-dot"></div>
                        <span>全程流转追踪</span>
                    </div>
                </div>
            </div>
            <div class="login-left__footer">
                <span>DrugWMS 药品管理系统</span>
            </div>
        </div>

        <div class="login-right">
            <div class="doctor-scene">
                <svg class="doctor-svg" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <linearGradient id="skinGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#FFE0C2"/>
                            <stop offset="100%" style="stop-color:#FFCBA4"/>
                        </linearGradient>
                        <linearGradient id="coatGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#FFFFFF"/>
                            <stop offset="100%" style="stop-color:#F0F4FF"/>
                        </linearGradient>
                        <linearGradient id="coatShade" x1="0%" y1="0%" x2="100%" y2="0%">
                            <stop offset="0%" style="stop-color:#E8EDF5"/>
                            <stop offset="100%" style="stop-color:#F0F4FF"/>
                        </linearGradient>
                        <linearGradient id="syringeBody" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#F0F0FF"/>
                            <stop offset="50%" style="stop-color:#E0E7FF"/>
                            <stop offset="100%" style="stop-color:#F0F0FF"/>
                        </linearGradient>
                        <linearGradient id="liquidGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                            <stop offset="0%" style="stop-color:#93C5FD"/>
                            <stop offset="100%" style="stop-color:#BFDBFE"/>
                        </linearGradient>
                        <linearGradient id="hatGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#FFFFFF"/>
                            <stop offset="100%" style="stop-color:#F1F5F9"/>
                        </linearGradient>
                        <linearGradient id="pantsGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#3B5998"/>
                            <stop offset="100%" style="stop-color:#2D4373"/>
                        </linearGradient>
                        <linearGradient id="shoeGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#FFFFFF"/>
                            <stop offset="100%" style="stop-color:#E2E8F0"/>
                        </linearGradient>
                        <linearGradient id="hairGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                            <stop offset="0%" style="stop-color:#4A5568"/>
                            <stop offset="100%" style="stop-color:#2D3748"/>
                        </linearGradient>
                        <filter id="shadow">
                            <feDropShadow dx="0" dy="6" stdDeviation="8" flood-color="#1E293B" flood-opacity="0.12"/>
                        </filter>
                        <filter id="softShadow">
                            <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="#1E293B" flood-opacity="0.08"/>
                        </filter>
                        <filter id="glow">
                            <feGaussianBlur stdDeviation="3" result="blur"/>
                            <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
                        </filter>
                    </defs>

                    <ellipse cx="250" cy="470" rx="130" ry="18" fill="#CBD5E1" opacity="0.25"/>

                    <g class="doctor-body" filter="url(#shadow)">
                        <path d="M190 290 Q185 260 210 248 L215 245 Q250 232 285 245 L290 248 Q315 260 310 290 L318 385 Q320 410 295 412 L205 412 Q180 410 182 385 Z" fill="url(#coatGrad)" stroke="#D4DAE8" stroke-width="0.8"/>

                        <path d="M215 248 Q215 270 215 290 L215 410" stroke="#D4DAE8" stroke-width="0.8" fill="none"/>
                        <path d="M285 248 Q285 270 285 290 L285 410" stroke="#D4DAE8" stroke-width="0.8" fill="none"/>

                        <path d="M215 248 L235 268 L250 252 L265 268 L285 248" stroke="none" fill="#E8EDF5"/>
                        <path d="M215 248 L235 268 L250 252 L265 268 L285 248" stroke="#D4DAE8" stroke-width="0.8" fill="none"/>
                        <line x1="250" y1="252" x2="250" y2="410" stroke="#D4DAE8" stroke-width="0.8"/>

                        <circle cx="250" cy="285" r="4" fill="#D4DAE8"/>
                        <circle cx="250" cy="315" r="4" fill="#D4DAE8"/>
                        <circle cx="250" cy="345" r="4" fill="#D4DAE8"/>

                        <rect x="195" y="310" width="30" height="22" rx="3" fill="url(#coatShade)" stroke="#D4DAE8" stroke-width="0.6"/>
                        <line x1="195" y1="321" x2="225" y2="321" stroke="#D4DAE8" stroke-width="0.5"/>
                        <rect x="275" y="310" width="30" height="22" rx="3" fill="url(#coatShade)" stroke="#D4DAE8" stroke-width="0.6"/>
                        <line x1="275" y1="321" x2="305" y2="321" stroke="#D4DAE8" stroke-width="0.5"/>

                        <rect x="270" y="270" width="28" height="12" rx="2" fill="#FEF3C7" stroke="#FDE68A" stroke-width="0.6"/>
                        <line x1="276" y1="273" x2="292" y2="273" stroke="#D97706" stroke-width="0.5"/>
                        <line x1="276" y1="276" x2="288" y2="276" stroke="#D97706" stroke-width="0.5"/>
                        <line x1="276" y1="279" x2="290" y2="279" stroke="#D97706" stroke-width="0.5"/>

                        <path d="M205 412 L200 455 Q198 468 212 468 L232 468 Q240 468 240 460 L238 412" fill="url(#pantsGrad)"/>
                        <path d="M262 412 L260 460 Q260 468 268 468 L288 468 Q302 468 300 455 L295 412" fill="url(#pantsGrad)"/>

                        <path d="M195 465 Q192 468 192 472 Q192 480 204 480 L232 480 Q240 480 240 472 L238 465" fill="url(#shoeGrad)" stroke="#CBD5E1" stroke-width="0.8"/>
                        <path d="M262 465 L260 472 Q260 480 268 480 L296 480 Q308 480 308 472 Q308 468 305 465" fill="url(#shoeGrad)" stroke="#CBD5E1" stroke-width="0.8"/>

                        <path d="M190 275 Q170 290 155 315 Q150 325 158 328 L170 322 Q178 318 185 300 L192 290" fill="url(#coatGrad)" stroke="#D4DAE8" stroke-width="0.8"/>
                        <ellipse cx="160" cy="326" rx="14" ry="12" fill="url(#skinGrad)" stroke="#F5C89A" stroke-width="0.8"/>
                        <path d="M148 322 Q154 318 160 320 Q166 318 172 322" stroke="#F0B887" stroke-width="0.8" fill="none" stroke-linecap="round"/>
                        <path d="M150 328 Q160 332 170 328" stroke="#F0B887" stroke-width="0.6" fill="none" stroke-linecap="round"/>
                    </g>

                    <g class="doctor-arm" :style="{ transform: 'rotate(' + armRotation + 'deg)', transformOrigin: '310px 270px' }">
                        <path d="M310 270 Q338 258 358 248 Q372 240 378 250 L374 258 Q366 268 352 276 L318 292" fill="url(#coatGrad)" stroke="#D4DAE8" stroke-width="0.8"/>
                        <path d="M312 275 Q335 265 355 255" stroke="#E8EDF5" stroke-width="0.6" fill="none"/>

                        <ellipse cx="360" cy="255" rx="13" ry="12" fill="url(#skinGrad)" stroke="#F5C89A" stroke-width="0.8"/>
                        <path d="M348 252 Q354 248 360 250 Q366 248 372 252" stroke="#F0B887" stroke-width="0.8" fill="none" stroke-linecap="round"/>
                        <path d="M350 258 Q360 262 370 258" stroke="#F0B887" stroke-width="0.6" fill="none" stroke-linecap="round"/>

                        <g class="syringe" filter="url(#softShadow)">
                            <rect x="362" y="237" width="90" height="12" rx="4" fill="url(#syringeBody)" stroke="#C7D2FE" stroke-width="0.8"/>
                            <rect x="387" y="239.5" width="60" height="7" rx="2.5" fill="url(#liquidGrad)" opacity="0.7"/>
                            <line x1="397" y1="239.5" x2="397" y2="246.5" stroke="#A5B4FC" stroke-width="0.4" opacity="0.6"/>
                            <line x1="407" y1="239.5" x2="407" y2="246.5" stroke="#A5B4FC" stroke-width="0.4" opacity="0.6"/>
                            <line x1="417" y1="239.5" x2="417" y2="246.5" stroke="#A5B4FC" stroke-width="0.4" opacity="0.6"/>
                            <line x1="427" y1="239.5" x2="427" y2="246.5" stroke="#A5B4FC" stroke-width="0.4" opacity="0.6"/>
                            <line x1="437" y1="239.5" x2="437" y2="246.5" stroke="#A5B4FC" stroke-width="0.4" opacity="0.6"/>

                            <rect x="340" y="234" width="22" height="18" rx="4" fill="#818CF8" stroke="#6366F1" stroke-width="0.8"/>
                            <rect x="344" y="237" width="14" height="12" rx="2" fill="#6366F1" opacity="0.3"/>
                            <line x1="351" y1="234" x2="351" y2="228" stroke="#6366F1" stroke-width="2" stroke-linecap="round"/>
                            <circle cx="351" cy="226" r="3" fill="#818CF8" stroke="#6366F1" stroke-width="0.8"/>

                            <polygon points="452,243 464,240.5 464,245.5" fill="#94A3B8" stroke="#64748B" stroke-width="0.5"/>
                            <line x1="464" y1="243" x2="478" y2="243" stroke="#94A3B8" stroke-width="2" stroke-linecap="round"/>
                            <circle cx="478" cy="243" r="1" fill="#64748B"/>
                        </g>
                    </g>

                    <g class="doctor-head" filter="url(#shadow)">
                        <circle cx="250" cy="170" r="72" fill="url(#skinGrad)" stroke="#F5C89A" stroke-width="0.8"/>

                        <path d="M178 155 Q175 95 250 88 Q325 95 322 155 L318 148 Q318 102 250 96 Q182 102 182 148 Z" fill="url(#hairGrad)"/>
                        <path d="M178 155 Q175 148 180 140 Q182 100 250 93 Q318 100 320 140 Q325 148 322 155 Q318 148 315 142 Q312 105 250 98 Q188 105 185 142 Q182 148 178 155" fill="#4A5568"/>

                        <ellipse cx="250" cy="90" rx="48" ry="12" fill="url(#hatGrad)" stroke="#D4DAE8" stroke-width="0.8"/>
                        <ellipse cx="250" cy="86" rx="44" ry="9" fill="url(#hatGrad)" stroke="#D4DAE8" stroke-width="0.8"/>

                        <rect x="243" y="76" width="14" height="20" rx="3" fill="#EF4444"/>
                        <rect x="246" y="79" width="8" height="14" rx="2" fill="#FCA5A5" opacity="0.5"/>
                        <line x1="250" y1="79" x2="250" y2="93" stroke="#DC2626" stroke-width="0.5" opacity="0.5"/>
                        <line x1="246" y1="86" x2="254" y2="86" stroke="#DC2626" stroke-width="0.5" opacity="0.5"/>

                        <ellipse cx="186" cy="148" rx="12" ry="14" fill="url(#skinGrad)" stroke="#F5C89A" stroke-width="0.8"/>
                        <path d="M183 140 Q186 138 189 140" stroke="#F0B887" stroke-width="0.6" fill="none"/>

                        <g class="eye-left">
                            <ellipse cx="225" cy="168" rx="14" ry="15" fill="#FFFFFF" stroke="#1E293B" stroke-width="1.8"/>
                            <ellipse cx="228" cy="169" rx="8" ry="9" fill="#1E293B"/>
                            <ellipse cx="230" cy="166" rx="3.5" ry="4" fill="#FFFFFF"/>
                            <circle cx="225" cy="172" r="2" fill="#FFFFFF" opacity="0.6"/>
                        </g>

                        <g class="eye-right">
                            <ellipse cx="275" cy="168" rx="14" ry="15" fill="#FFFFFF" stroke="#1E293B" stroke-width="1.8"/>
                            <ellipse cx="278" cy="169" rx="8" ry="9" fill="#1E293B"/>
                            <ellipse cx="280" cy="166" rx="3.5" ry="4" fill="#FFFFFF"/>
                            <circle cx="275" cy="172" r="2" fill="#FFFFFF" opacity="0.6"/>
                        </g>

                        <path d="M218 155 Q225 150 232 155" stroke="#1E293B" stroke-width="2.5" fill="none" stroke-linecap="round"/>
                        <path d="M268 155 Q275 150 282 155" stroke="#1E293B" stroke-width="2.5" fill="none" stroke-linecap="round"/>

                        <ellipse cx="210" cy="182" rx="12" ry="7" fill="#FDBA74" opacity="0.35"/>
                        <ellipse cx="290" cy="182" rx="12" ry="7" fill="#FDBA74" opacity="0.35"/>

                        <ellipse cx="225" cy="193" rx="5" ry="2.5" fill="#F97316" opacity="0.15"/>
                        <ellipse cx="275" cy="193" rx="5" ry="2.5" fill="#F97316" opacity="0.15"/>

                        <path d="M235 200 Q242 210 250 210 Q258 210 265 200" stroke="#E11D48" stroke-width="2.8" fill="none" stroke-linecap="round"/>
                        <path d="M238 200 Q244 206 250 206 Q256 206 262 200" stroke="#FB7185" stroke-width="1.2" fill="none" stroke-linecap="round" opacity="0.5"/>

                        <ellipse cx="215" cy="190" rx="9" ry="6" fill="#FDA4AF" opacity="0.5"/>
                        <ellipse cx="285" cy="190" rx="9" ry="6" fill="#FDA4AF" opacity="0.5"/>
                    </g>

                    <g class="stethoscope" filter="url(#softShadow)">
                        <path d="M222 240 Q205 255 208 280 Q210 300 225 305" stroke="#475569" stroke-width="3.5" fill="none" stroke-linecap="round"/>
                        <path d="M278 240 Q295 255 292 280 Q290 300 275 305" stroke="#475569" stroke-width="3.5" fill="none" stroke-linecap="round"/>
                        <circle cx="250" cy="310" r="10" fill="#475569" stroke="#334155" stroke-width="1"/>
                        <circle cx="250" cy="310" r="5" fill="#94A3B8"/>
                        <circle cx="250" cy="310" r="2.5" fill="#CBD5E1"/>
                    </g>

                    <g class="sparkles">
                        <g filter="url(#glow)">
                            <path d="M110 140 L113 148 L121 150 L113 152 L110 160 L107 152 L99 150 L107 148 Z" fill="#FBBF24" opacity="0.8">
                                <animate attributeName="opacity" values="0.8;0.2;0.8" dur="2s" repeatCount="indefinite"/>
                            </path>
                        </g>
                        <g filter="url(#glow)">
                            <path d="M410 110 L412 116 L418 118 L412 120 L410 126 L408 120 L402 118 L408 116 Z" fill="#A78BFA" opacity="0.7">
                                <animate attributeName="opacity" values="0.7;0.2;0.7" dur="2.5s" repeatCount="indefinite"/>
                            </path>
                        </g>
                        <g filter="url(#glow)">
                            <path d="M390 390 L392 395 L397 397 L392 399 L390 404 L388 399 L383 397 L388 395 Z" fill="#34D399" opacity="0.6">
                                <animate attributeName="opacity" values="0.6;0.1;0.6" dur="3s" repeatCount="indefinite"/>
                            </path>
                        </g>
                        <g filter="url(#glow)">
                            <path d="M95 340 L97 345 L102 347 L97 349 L95 354 L93 349 L88 347 L93 345 Z" fill="#60A5FA" opacity="0.7">
                                <animate attributeName="opacity" values="0.7;0.2;0.7" dur="1.8s" repeatCount="indefinite"/>
                            </path>
                        </g>
                        <g filter="url(#glow)">
                            <path d="M430 220 L432 225 L437 227 L432 229 L430 234 L428 229 L423 227 L428 225 Z" fill="#F472B6" opacity="0.6">
                                <animate attributeName="opacity" values="0.6;0.1;0.6" dur="2.2s" repeatCount="indefinite"/>
                            </path>
                        </g>
                        <g filter="url(#glow)">
                            <path d="M80 240 L82 244 L86 246 L82 248 L80 252 L78 248 L74 246 L78 244 Z" fill="#FCD34D" opacity="0.5">
                                <animate attributeName="opacity" values="0.5;0.1;0.5" dur="2.8s" repeatCount="indefinite"/>
                            </path>
                        </g>
                        <circle cx="140" cy="420" r="2" fill="#93C5FD" opacity="0.5">
                            <animate attributeName="opacity" values="0.5;0.1;0.5" dur="2.3s" repeatCount="indefinite"/>
                        </circle>
                        <circle cx="370" cy="80" r="2" fill="#C084FC" opacity="0.6">
                            <animate attributeName="opacity" values="0.6;0.1;0.6" dur="1.9s" repeatCount="indefinite"/>
                        </circle>
                    </g>
                </svg>
                <div class="doctor-bubble">
                    <span>{{ bubbleText }}</span>
                </div>
            </div>

            <div class="login-card">
                <div class="login-card__logo">
                    <i class="el-icon-first-aid-kit"></i>
                </div>
                <h1 class="login-card__title">药品管理系统</h1>
                <p class="login-card__subtitle">欢迎回来，请登录您的账号</p>
                <el-form :model="loginForm" label-width="0px"
                         :rules="rules" ref="loginForm" class="login-form">
                    <el-form-item prop="no">
                        <el-input type="text" v-model="loginForm.no"
                                  autocomplete="off" size="medium" prefix-icon="el-icon-user"
                                  placeholder="请输入账号"
                                  @focus="bubbleText='输入账号就能进来啦~'"
                                  class="login-input"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input type="password" v-model="loginForm.password"
                                  show-password autocomplete="off" size="medium" prefix-icon="el-icon-lock"
                                  placeholder="请输入密码" @keyup.enter.native="confirm"
                                  @focus="bubbleText='放心，密码我不会看的~'"
                                  class="login-input"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="confirm" :disabled="confirm_disabled"
                                   :loading="confirm_disabled" class="login-btn">
                            {{ confirm_disabled ? '登录中...' : '登 录' }}
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "Login",
        data() {
            return {
                confirm_disabled: false,
                armRotation: -15,
                bubbleText: '别怕，打针不疼的~',
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
            onMouseMove(e) {
                let svgEl = document.querySelector('.doctor-svg');
                if (!svgEl) return;
                let rect = svgEl.getBoundingClientRect();
                let svgScale = rect.width / 500;
                let pivotX = rect.left + 310 * svgScale;
                let pivotY = rect.top + 270 * svgScale;
                let dx = e.clientX - pivotX;
                let dy = e.clientY - pivotY;
                let angle = Math.atan2(dy, dx) * (180 / Math.PI);
                let clamped = Math.max(-60, Math.min(60, angle));
                this.armRotation = clamped;
            },
            confirm() {
                this.confirm_disabled = true;
                this.bubbleText = '正在验证，稍等哦~';
                this.$refs.loginForm.validate((valid) => {
                    if (valid) {
                        this.$axios.post(this.$httpUrl + '/user/login', this.loginForm).then(res => res.data).then(res => {
                            if (res.code == 200) {
                                sessionStorage.setItem("CurUser", JSON.stringify(res.data.user));
                                this.$store.commit("setMenu", res.data.menu);
                                this.$router.replace('/Index');
                            } else {
                                this.confirm_disabled = false;
                                this.bubbleText = '哎呀，账号或密码错了~';
                                this.$message({
                                    message: '用户名或密码错误！',
                                    type: 'error'
                                });
                                return false;
                            }
                        }).catch(() => {
                            this.confirm_disabled = false;
                            this.bubbleText = '网络出了点问题~';
                        });
                    } else {
                        this.confirm_disabled = false;
                        this.bubbleText = '填完才能登录哦~';
                        return false;
                    }
                });
            }
        }
    }
</script>

<style scoped>
    @import url('https://fonts.googleapis.com/css2?family=Figtree:wght@400;500;600;700&family=Noto+Sans+SC:wght@400;500;700&display=swap');

    .login-page {
        position: absolute;
        width: 100%;
        height: 100%;
        display: flex;
        background: #F8FAFC;
        font-family: 'Figtree', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
    }

    .login-left {
        width: 25%;
        min-width: 260px;
        background: linear-gradient(160deg, #0F766E 0%, #15803D 50%, #166534 100%);
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 40px 48px;
        position: relative;
        overflow: hidden;
    }

    .login-left::before {
        content: '';
        position: absolute;
        top: -80px;
        right: -80px;
        width: 300px;
        height: 300px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.05);
    }

    .login-left::after {
        content: '';
        position: absolute;
        bottom: -120px;
        left: -60px;
        width: 400px;
        height: 400px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.03);
    }

    .login-left__brand {
        display: flex;
        align-items: center;
        gap: 12px;
        position: relative;
        z-index: 1;
    }

    .brand-icon {
        width: 44px;
        height: 44px;
        background: rgba(255, 255, 255, 0.15);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(8px);
        border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .brand-text {
        font-size: 22px;
        font-weight: 700;
        color: #FFFFFF;
        letter-spacing: 0.5px;
    }

    .login-left__content {
        position: relative;
        z-index: 1;
    }

    .login-left__title {
        font-size: 32px;
        font-weight: 700;
        color: #FFFFFF;
        margin: 0 0 16px;
        line-height: 1.3;
        letter-spacing: 0.5px;
    }

    .login-left__desc {
        font-size: 15px;
        color: rgba(255, 255, 255, 0.75);
        line-height: 1.7;
        margin: 0 0 36px;
    }

    .login-left__features {
        display: flex;
        flex-direction: column;
        gap: 14px;
    }

    .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        color: rgba(255, 255, 255, 0.9);
        font-size: 14px;
        font-weight: 500;
    }

    .feature-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #86EFAC;
        flex-shrink: 0;
        box-shadow: 0 0 8px rgba(134, 239, 172, 0.5);
    }

    .login-left__footer {
        position: relative;
        z-index: 1;
        font-size: 12px;
        color: rgba(255, 255, 255, 0.4);
    }

    .login-right {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 40px;
        position: relative;
        background: linear-gradient(180deg, #F0FDF4 0%, #ECFEFF 50%, #F0F9FF 100%);
        gap: 48px;
    }

    .doctor-scene {
        position: relative;
        width: 320px;
        height: 380px;
        flex-shrink: 0;
        order: 2;
    }

    .doctor-svg {
        width: 100%;
        height: 100%;
    }

    .doctor-arm {
        transition: transform 0.15s ease-out;
    }

    .doctor-bubble {
        position: absolute;
        top: 16px;
        right: -20px;
        background: #FFFFFF;
        border-radius: 16px 16px 16px 4px;
        padding: 10px 18px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        font-size: 13px;
        color: #334155;
        font-weight: 500;
        white-space: nowrap;
        animation: bubbleFloat 3s ease-in-out infinite;
        border: 1px solid #E2E8F0;
    }

    @keyframes bubbleFloat {
        0%, 100% { transform: translateY(0); }
        50% { transform: translateY(-6px); }
    }

    .login-card {
        width: 400px;
        background: #FFFFFF;
        border-radius: 20px;
        padding: 40px 36px 36px;
        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);
        border: 1px solid rgba(226, 232, 240, 0.6);
        order: 1;
    }

    .login-card__logo {
        width: 56px;
        height: 56px;
        background: linear-gradient(135deg, #15803D, #22C55E);
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
        box-shadow: 0 6px 20px rgba(21, 128, 61, 0.25);
    }

    .login-card__logo i {
        color: #FFFFFF !important;
        font-size: 26px !important;
    }

    .login-card__title {
        text-align: center;
        font-size: 22px;
        font-weight: 700;
        color: #0F172A;
        margin: 0 0 6px;
        letter-spacing: 1px;
    }

    .login-card__subtitle {
        text-align: center;
        font-size: 14px;
        color: #64748B;
        margin: 0 0 28px;
    }

    .login-form .el-form-item {
        margin-bottom: 20px;
    }

    .login-input >>> .el-input__inner {
        height: 44px;
        border-radius: 10px !important;
        border-color: #E2E8F0 !important;
        font-size: 14px;
        transition: all 0.2s ease;
    }

    .login-input >>> .el-input__inner:focus {
        border-color: #15803D !important;
        box-shadow: 0 0 0 3px rgba(21, 128, 61, 0.1) !important;
    }

    .login-input >>> .el-input__prefix {
        color: #94A3B8;
    }

    .login-btn {
        width: 100%;
        height: 46px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 12px !important;
        letter-spacing: 4px;
        background: linear-gradient(135deg, #15803D, #22C55E) !important;
        border: none !important;
        box-shadow: 0 4px 16px rgba(21, 128, 61, 0.3);
        transition: all 0.25s ease;
        cursor: pointer;
    }

    .login-btn:hover {
        transform: translateY(-1px);
        box-shadow: 0 6px 20px rgba(21, 128, 61, 0.4);
    }

    .login-btn:active {
        transform: translateY(0);
    }

    @media (max-width: 900px) {
        .login-page {
            flex-direction: column;
        }
        .login-left {
            width: 100%;
            min-width: unset;
            padding: 32px 24px;
            min-height: auto;
        }
        .login-left__title {
            font-size: 24px;
        }
        .login-left__features {
            display: none;
        }
        .login-right {
            flex-direction: column;
            padding: 24px;
            gap: 24px;
        }
        .doctor-scene {
            width: 240px;
            height: 280px;
        }
        .login-card {
            width: 100%;
            max-width: 400px;
        }
    }

    @media (max-width: 480px) {
        .doctor-scene {
            width: 200px;
            height: 230px;
        }
        .login-card {
            padding: 28px 24px 24px;
        }
        .doctor-bubble {
            font-size: 11px;
            padding: 6px 12px;
        }
    }
</style>