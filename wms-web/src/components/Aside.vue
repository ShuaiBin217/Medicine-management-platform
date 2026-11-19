<template>
    <div class="aside-inner">
        <div class="aside-brand" :class="{ 'is-collapse': isCollapse }">
            <div class="brand-mark">
                <FIcon name="FirstAidKit"/>
            </div>
            <div class="brand-copy" v-if="!isCollapse">
                <span class="brand-name">DrugWMS</span>
                <span class="brand-sub">药品智能管理平台</span>
            </div>
        </div>

        <el-menu
                background-color="transparent"
                text-color="#5E554A"
                active-text-color="#1F1B16"
                class="aside-menu"
                :default-active="$route.path"
                :collapse="isCollapse"
                :collapse-transition="false"
                router
        >
            <div class="menu-label" v-if="!isCollapse">工作台</div>
            <el-menu-item index="/Home">
                <el-icon><HomeFilled/></el-icon>
                <template #title>库存仪表盘</template>
            </el-menu-item>

            <div class="menu-label" v-if="!isCollapse">业务管理</div>
            <el-menu-item :index="'/'+item.menuclick" v-for="(item,i) in menu" :key="i">
                <FIcon :name="item.menuicon"/>
                <template #title>{{item.menuname}}</template>
            </el-menu-item>
        </el-menu>

        <div class="aside-foot" v-if="!isCollapse">
            <span class="status-dot is-ok"></span>
            药房内部系统
        </div>
    </div>
</template>

<script>
    import { useMenuStore } from '../stores/menu'
    import FIcon from './FIcon'

    export default {
        name: "Aside",
        components: {FIcon},
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
    .aside-inner {
        height: 100%;
        display: flex;
        flex-direction: column;
        background: var(--sidebar-bg);
        border-right: 1px solid var(--line);
    }
    .aside-brand {
        height: 56px;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 0 16px;
        border-bottom: 1px solid var(--line);
        flex-shrink: 0;
    }
    .aside-brand.is-collapse {
        justify-content: center;
        padding: 0;
    }
    .brand-mark {
        width: 30px;
        height: 30px;
        border-radius: 8px;
        background: var(--accent);
        color: #FDFBF7;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 17px;
        flex-shrink: 0;
    }
    .brand-copy {
        display: flex;
        flex-direction: column;
        line-height: 1.2;
        min-width: 0;
    }
    .brand-name {
        font-size: 15px;
        font-weight: 600;
        color: var(--ink);
        letter-spacing: 0.02em;
    }
    .brand-sub {
        font-size: 11px;
        color: var(--muted);
        margin-top: 2px;
        white-space: nowrap;
    }
    .aside-menu {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        border-right: none;
        background: transparent;
        padding: 6px 0 12px;
    }
    .menu-label {
        font-size: 10px;
        color: var(--muted);
        letter-spacing: 0.12em;
        padding: 16px 18px 6px;
        font-weight: 500;
    }
    .aside-menu .el-menu-item {
        height: 36px;
        line-height: 36px;
        margin: 2px 10px;
        border-radius: 8px;
        font-size: 13px;
        color: var(--ink-2);
        position: relative;
        padding-left: 12px !important;
    }
    .aside-menu .el-menu-item:hover {
        background-color: rgba(31, 27, 22, 0.05) !important;
        color: var(--ink) !important;
    }
    .aside-menu .el-menu-item.is-active {
        background: var(--raised) !important;
        color: var(--ink) !important;
        font-weight: 500;
        box-shadow: none;
    }
    .aside-menu .el-menu-item.is-active::before {
        content: '';
        position: absolute;
        left: 0;
        top: 9px;
        bottom: 9px;
        width: 2px;
        border-radius: 0 2px 2px 0;
        background: var(--accent);
    }
    .aside-menu .el-menu-item :deep(.el-icon) {
        color: inherit;
        margin-right: 8px;
        font-size: 17px;
        vertical-align: middle;
    }
    .aside-menu.el-menu--collapse .el-menu-item {
        justify-content: center;
        padding: 0 !important;
        margin: 2px 8px;
    }
    .aside-menu.el-menu--collapse .el-menu-item :deep(.el-icon) {
        margin-right: 0;
    }
    .aside-foot {
        padding: 14px 18px 16px;
        font-size: 11px;
        color: var(--muted);
        border-top: 1px solid var(--line);
        flex-shrink: 0;
    }
    .aside-foot .status-dot {
        background: var(--accent);
    }
</style>
