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
           <el-icon><HomeFilled/></el-icon>
           <template #title>首页</template>
       </el-menu-item>

        <el-menu-item :index="'/'+item.menuclick" v-for="(item,i) in menu" :key="i">
            <FIcon :name="item.menuicon"/>
            <template #title>{{item.menuname}}</template>
        </el-menu-item>


    </el-menu>
</template>

<script>
    import { useMenuStore } from '../stores/menu'
    import FIcon from './FIcon'

    export default {
        name: "Aside",
        components: {FIcon},
        data(){
            return {
                //isCollapse:false
            }
        },
        computed:{
            "menu":{
                get(){
                    return useMenuStore().menu
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
    .aside-menu .el-menu-item :deep(.el-icon) {
        color: inherit;
        margin-right: 8px;
        font-size: 18px;
        vertical-align: middle;
    }
</style>