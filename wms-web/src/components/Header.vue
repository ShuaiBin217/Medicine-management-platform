<template>
    <div class="header-container">
        <div class="header-left">
            <div class="collapse-icon" @click="collapse">
                <el-icon><component :is="icon"/></el-icon>
            </div>
        </div>
        <div class="header-center">
            <span class="header-title">药品管理系统</span>
        </div>
        <div class="header-right">
            <el-dropdown>
                <span class="user-info">
                    <el-icon style="margin-right: 6px; font-size: 16px;"><UserFilled/></el-icon>
                    {{user.name}}
                    <el-icon style="margin-left: 4px; font-size: 12px;"><ArrowDown/></el-icon>
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

        <el-dialog v-model="pwdVisible" title="修改密码" width="380px" center>
            <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="90px">
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
                    <el-button @click="pwdVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="pwdSubmitting" @click="submitPwd">确 定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script>
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
                user : JSON.parse(sessionStorage.getItem('CurUser')),
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
        methods:{
            handleCommand(cmd){
                if (cmd === 'toUser') this.toUser()
                else if (cmd === 'logout') this.logout()
            },
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
                console.log('to_user')

                this.$router.push("/Home")
            },
            logout(){
                this.$confirm('您确定要退出登录吗?', '提示', {
                    confirmButtonText: '确定',
                    type: 'warning',
                    center: true,
                })
                    .then(() => {
                        // 调用后端登出接口，清除 Redis 中的 Session
                        this.$axios.post(this.$httpUrl + '/user/logout').finally(() => {
                            this.$message({ type: 'success', message: '退出登录成功' })
                            sessionStorage.clear()
                            this.$router.push('/')
                        })
                    })
                    .catch(() => {
                        this.$message({ type: 'info', message: '已取消退出登录' })
                    })
            },
            collapse(){
                this.$emit('doCollapse')
            }

        },
        created(){
        }

    }
</script>

<style scoped>
    .header-container {
        display: flex;
        align-items: center;
        line-height: 60px;
        height: 60px;
        padding: 0 20px;
    }
    .header-left {
        display: flex;
        align-items: center;
    }
    .collapse-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 22px;
        cursor: pointer;
        color: #4A5568;
        transition: all 0.25s ease;
        padding: 10px;
        border-radius: 8px;
    }
    .collapse-icon:hover {
        background: #F0F2F5;
        color: #4A6CF7;
    }
    .header-center {
        flex: 1;
        text-align: center;
    }
    .header-title {
        font-size: 18px;
        font-weight: 600;
        color: #1A202C;
        letter-spacing: 2px;
    }
    .header-right {
        display: flex;
        align-items: center;
    }
    .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        color: #4A5568;
        font-size: 14px;
        font-weight: 500;
        padding: 6px 12px;
        border-radius: 8px;
        transition: all 0.25s ease;
    }
    .user-info:hover {
        background: #F0F2F5;
        color: #4A6CF7;
    }
</style>