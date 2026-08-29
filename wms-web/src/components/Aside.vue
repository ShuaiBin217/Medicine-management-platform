<template>
    <el-menu
            background-color="#1E2A3A"
            text-color="#A0AEC0"
            active-text-color="#FFFFFF"
            class="aside-menu"
            :default-active="$route.path"
            :collapse="isCollapse"
            :collapse-transition="false"
            router
    >
       <el-menu-item index="/Home">
           <i class="el-icon-s-home"></i>
           <span slot="title">首页</span>
       </el-menu-item>

        <el-menu-item :index="'/'+item.menuclick" v-for="(item,i) in menu" :key="i">
            <i :class="item.menuicon"></i>
            <span slot="title">{{item.menuname}}</span>
        </el-menu-item>


    </el-menu>
</template>

<script>
    export default {
        name: "Aside",
        data(){
            return {
                //isCollapse:false

               /* menu:[
                    {
                        menuClick:'Admin',
                        menuName:'管路员管理',
                        menuIcon:'el-icon-s-custom'
                    },{
                        menuClick:'User',
                        menuName:'用户管理',
                        menuIcon:'el-icon-user-solid'
                    }
                ]*/
            }
        },
        computed:{
            "menu":{
                get(){
                    return this.$store.state.menu
                }
            }
        },
        props:{
            isCollapse:Boolean
        }
    }
</script>

<style scoped>
    .aside-menu {
        height: 100%;
        border-right: none;
    }
    .aside-menu .el-menu-item {
        height: 50px;
        line-height: 50px;
        margin: 4px 8px;
        border-radius: 8px;
        transition: all 0.25s ease;
    }
    .aside-menu .el-menu-item:hover {
        background-color: rgba(74, 108, 247, 0.15) !important;
        color: #FFFFFF !important;
    }
    .aside-menu .el-menu-item.is-active {
        background: linear-gradient(135deg, #4A6CF7, #6B8AFF) !important;
        color: #FFFFFF !important;
        box-shadow: 0 4px 12px rgba(74, 108, 247, 0.35);
    }
    .aside-menu .el-menu-item i {
        color: inherit;
        margin-right: 8px;
    }
</style>