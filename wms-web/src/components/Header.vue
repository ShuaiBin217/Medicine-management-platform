<template>
    <div class="header-container">
        <div class="header-left">
            <i :class="icon" class="collapse-icon" @click="collapse"></i>
        </div>
        <div class="header-center">
            <span class="header-title">药品管理系统</span>
        </div>
        <div class="header-right">
            <el-dropdown>
                <span class="user-info">
                    <i class="el-icon-user-solid" style="margin-right: 6px; font-size: 16px;"></i>
                    {{user.name}}
                    <i class="el-icon-arrow-down" style="margin-left: 4px; font-size: 12px;"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="toUser">
                        <i class="el-icon-s-custom" style="margin-right: 8px;"></i>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item @click.native="logout" divided>
                        <i class="el-icon-switch-button" style="margin-right: 8px;"></i>退出登录
                    </el-dropdown-item>
                </el-dropdown-menu>
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
            toUser(){
                console.log('to_user')

                this.$router.push("/Home")
            },
            logout(){
                console.log('logout')

                this.$confirm('您确定要退出登录吗?', '提示', {
                    confirmButtonText: '确定',  //确认按钮的文字显示
                    type: 'warning',
                    center: true, //文字居中显示

                })
                    .then(() => {
                        this.$message({
                            type:'success',
                            message:'退出登录成功'
                        })

                        this.$router.push("/")
                        sessionStorage.clear()
                    })
                    .catch(() => {
                        this.$message({
                            type:'info',
                            message:'已取消退出登录'
                        })
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
        font-size: 20px;
        cursor: pointer;
        color: #4A5568;
        transition: all 0.25s ease;
        padding: 6px;
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