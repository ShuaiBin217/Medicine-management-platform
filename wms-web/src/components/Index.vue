<template>
    <el-container class="index-shell">
        <el-aside :width="aside_witdh" class="index-aside">
            <Aside :isCollapse="isCollapse"></Aside>
        </el-aside>

        <el-container class="index-body">
            <el-header class="index-header">
                <Header @doCollapse="doCollapse" :icon="icon"></Header>
            </el-header>

            <el-main class="index-main">
                <div class="page-shell">
                    <router-view/>
                </div>
            </el-main>
        </el-container>

        <ChatNurse class="chat-nurse-slot"></ChatNurse>
    </el-container>
</template>

<script>
    import Aside from "./Aside";
    import Header from "./Header";
    import ChatNurse from "./ChatNurse";

    export default {
        name: "Index",
        components:{Aside,Header,ChatNurse},
        data(){
            return {
                isCollapse:false,
                aside_witdh:'240px',
                icon:'Fold'
            }
        },
        methods:{
            doCollapse(){
                this.isCollapse = !this.isCollapse
                if(!this.isCollapse){// 展开
                    this.aside_witdh='240px'
                    this.icon='Fold'
                }else{// 收起
                    this.aside_witdh='64px'
                    this.icon='Expand'
                }
            }
        }
    }
</script>

<style scoped>
    .index-shell {
        height: 100vh;
        background: var(--paper);
    }
    .index-aside {
        height: 100vh;
        background-color: var(--sidebar-bg);
        transition: width 0.2s ease;
        overflow-x: hidden;
        flex-shrink: 0;
    }
    .index-body {
        height: 100vh;
        min-width: 0;
    }
    .index-header {
        height: 56px;
        padding: 0;
        border-bottom: 1px solid var(--line);
        background: var(--card);
        flex-shrink: 0;
    }
    .index-main {
        flex: 1;
        overflow: auto;
        padding: 24px 28px 32px;
        background: var(--paper);
    }
    .page-shell {
        max-width: 1440px;
        margin: 0 auto;
    }
    .chat-nurse-slot {
        position: absolute;
        width: 0;
        height: 0;
        overflow: visible;
        pointer-events: none;
    }
    @media (max-width: 900px) {
        .index-main {
            padding: 16px;
        }
    }
</style>
