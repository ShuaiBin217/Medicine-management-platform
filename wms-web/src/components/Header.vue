<template>
    <div class="header-container">
        <div class="header-left">
            <div class="collapse-icon" @click="collapse">
                <el-icon><component :is="icon"/></el-icon>
            </div>
            <div class="page-heading">
                <span class="page-eyebrow">{{ pageSection }} / DRUGWMS</span>
                <span class="page-title">{{ pageTitle }}</span>
            </div>
        </div>
        <div class="header-right">
            <span class="header-date mono">{{ todayText }}</span>
            <el-dropdown>
                <span class="user-info">
                    <span class="user-avatar">{{ userInitial }}</span>
                    <span class="user-meta">
                        <span class="user-name">{{ user.name }}</span>
                        <span class="user-role">{{ roleText }}</span>
                    </span>
                    <el-icon class="user-caret"><ArrowDown/></el-icon>
                </span>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item @click="toUser">
                            <el-icon style="margin-right: 8px;"><User/></el-icon>个人中心
                        </el-dropdown-item>
                        <el-dropdown-item @click="openPwd">
                            <el-icon style="margin-right: 8px;"><Lock/></el-icon>修改密码
                        </el-dropdown-item>
                        <el-dropdown-item @click="logout" divided>
                            <el-icon style="margin-right: 8px;"><SwitchButton/></el-icon>退出登录
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>

        <el-dialog v-model="pwdVisible" title="修改密码" width="400px">
            <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="80px">
                <el-form-item label="原密码" prop="oldPassword">
                    <el-input v-model="pwdForm.oldPassword" type="password" show-password autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="pwdForm.newPassword" type="password" show-password autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="pwdForm.confirmPassword" type="password" show-password autocomplete="off"
                              @keyup.enter="submitPwd"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="pwdVisible = false">取消</el-button>
                    <el-button type="primary" :loading="pwdSubmitting" @click="submitPwd">确定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script>
    import { useMenuStore } from '../stores/menu'

    export default {
        name: "Header",
        data(){
            let validateConfirm = (rule, value, callback) => {
                if (value !== this.pwdForm.newPassword) {
                    callback(new Error('两次输入的新密码不一致'));
                } else {
                    callback();
                }
            };
            return {
                user : JSON.parse(sessionStorage.getItem('CurUser')) || {},
                pwdVisible: false,
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
                        {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'}
                    ],
                    confirmPassword: [
                        {required: true, message: '请再次输入新密码', trigger: 'blur'},
                        {validator: validateConfirm, trigger: 'blur'}
                    ]
                }
            }
        },
        props:{
            icon:String
        },
        computed: {
            menu() {
                return useMenuStore().menu
            },
            pageTitle() {
                const path = this.$route.path
                if (path === '/Home') return '库存仪表盘'
                if (path === '/GoodsDetail') return '药品流转详细'
                const hit = this.menu.find(item => '/' + item.menuclick === path)
                return hit ? hit.menuname : '药房工作台'
            },
            pageSection() {
                const path = this.$route.path
                if (path === '/Home') return '工作台'
                if (path === '/GoodsDetail' || path.indexOf('/Goods') === 0) return '药品管理'
                if (path.indexOf('/Record') === 0) return '流转记录'
                if (path.indexOf('/Storage') === 0 || path.indexOf('/Goodstype') === 0) return '基础资料'
                if (path.indexOf('/User') === 0 || path.indexOf('/Admin') === 0) return '账号与权限'
                return '药房工作台'
            },
            roleText() {
                const role = String(this.user.roleId)
                if (role === '0') return '超级管理员'
                if (role === '1') return '管理员'
                return '普通用户'
            },
            userInitial() {
                return this.user.name ? this.user.name.slice(0, 1) : 'U'
            },
            todayText() {
                const d = new Date()
                const pad = n => String(n).padStart(2, '0')
                return d.getFullYear() + '.' + pad(d.getMonth() + 1) + '.' + pad(d.getDate())
            }
        },
        methods:{
            openPwd(){
                this.pwdVisible = true
                this.$nextTick(() => {
                    this.pwdForm.oldPassword = ''
                    this.pwdForm.newPassword = ''
                    this.pwdForm.confirmPassword = ''
                    if (this.$refs.pwdForm) this.$refs.pwdForm.clearValidate()
                })
            },
            submitPwd(){
                if (this.pwdSubmitting) return
                this.$refs.pwdForm.validate((valid) => {
                    if (!valid) return
                    this.pwdSubmitting = true
                    this.$axios.post(this.$httpUrl + '/user/changePassword', {
                        oldPassword: this.pwdForm.oldPassword,
                        newPassword: this.pwdForm.newPassword
                    }).then(res => res.data).then(res => {
                        if (res.code == 200) {
                            this.pwdVisible = false
                            this.$message({message: '密码修改成功，请重新登录', type: 'success'})
                            this.forceLogout()
                        } else {
                            this.$message({message: res.msg || '修改失败', type: 'error'})
                        }
                    }).finally(() => { this.pwdSubmitting = false })
                })
            },
            forceLogout(){
                this.$axios.post(this.$httpUrl + '/user/logout').finally(() => {
                    sessionStorage.clear()
                    this.$router.push('/')
                })
            },
            toUser(){
                this.$router.push("/Home")
            },
            logout(){
                this.$confirm('确定要退出登录吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                    .then(() => {
                        this.$axios.post(this.$httpUrl + '/user/logout').finally(() => {
                            this.$message({ type: 'success', message: '已退出登录' })
                            sessionStorage.clear()
                            this.$router.push('/')
                        })
                    })
                    .catch(() => {})
            },
            collapse(){
                this.$emit('doCollapse')
            }
        }
    }
</script>

<style scoped>
    .header-container {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 56px;
        padding: 0 24px;
    }
    .header-left {
        display: flex;
        align-items: center;
        gap: 14px;
        min-width: 0;
    }
    .collapse-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 30px;
        height: 30px;
        font-size: 18px;
        cursor: pointer;
        color: var(--ink-2);
        border-radius: 6px;
        transition: var(--transition);
        flex-shrink: 0;
    }
    .collapse-icon:hover {
        background: var(--raised);
        color: var(--ink);
    }
    .page-heading {
        display: flex;
        flex-direction: column;
        line-height: 1.25;
        min-width: 0;
    }
    .page-eyebrow {
        font-size: 10px;
        color: var(--muted);
        letter-spacing: 0.12em;
        font-weight: 500;
    }
    .page-title {
        font-size: 15px;
        font-weight: 500;
        color: var(--ink);
        white-space: nowrap;
    }
    .header-right {
        display: flex;
        align-items: center;
        gap: 18px;
    }
    .header-date {
        font-size: 11px;
        color: var(--muted);
        letter-spacing: 0.02em;
    }
    .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px 4px 4px;
        border-radius: 8px;
        transition: var(--transition);
    }
    .user-info:hover {
        background: var(--raised);
    }
    .user-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--raised);
        border: 1px solid var(--line);
        color: var(--ink-2);
        font-size: 12px;
        font-weight: 500;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }
    .user-meta {
        display: flex;
        flex-direction: column;
        line-height: 1.2;
    }
    .user-name {
        font-size: 13px;
        font-weight: 500;
        color: var(--ink);
    }
    .user-role {
        font-size: 10px;
        color: var(--muted);
        margin-top: 2px;
    }
    .user-caret {
        font-size: 12px;
        color: var(--muted);
    }
    @media (max-width: 900px) {
        .header-date,
        .user-role {
            display: none;
        }
        .header-container {
            padding: 0 14px;
        }
    }
</style>
