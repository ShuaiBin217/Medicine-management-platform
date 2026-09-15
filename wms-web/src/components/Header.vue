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
                        <el-dropdown-item @click="logout" divided>
                            <el-icon style="margin-right: 8px;"><SwitchButton/></el-icon>退出登录
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </div>
</template>

<script>
    export default {
        name: "Header",
        data(){
            return {
                user : JSON.parse(sessionStorage.getItem('CurUser'))
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