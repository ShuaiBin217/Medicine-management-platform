<template>
    <el-container style="height: 100%;">
        <el-aside :width="aside_witdh" class="index-aside">
            <Aside :isCollapse="isCollapse"></Aside>
        </el-aside>

        <el-container style="height: 100%;">
            <el-header class="index-header">
                <Header @doCollapse="doCollapse" :icon="icon"></Header>
            </el-header>

            <el-main class="index-main">
                <router-view/>
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
                aside_witdh:'200px',
                icon:'el-icon-s-fold'
            }
        },
        methods:{
            doCollapse(){
                console.log(1111)

                this.isCollapse = !this.isCollapse
                if(!this.isCollapse){// 展开
                    this.aside_witdh='200px'
                    this.icon='el-icon-s-fold'
                }else{//关起、关闭、收起
                    this.aside_witdh='64px'
                    this.icon='el-icon-s-unfold'
                }
            }
        }
    }
</script>

<style scoped>
    .index-aside {
        height: 100vh;
        background-color: #1E2A3A;
        margin-left: -1px;
        transition: width 0.3s ease;
        overflow-x: hidden;
        box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
    }
    .index-header {
        text-align: right;
        font-size: 12px;
        height: 100%;
        border-bottom: 1px solid #E2E8F0;
        background: #FFFFFF;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
    }
    .index-main {
        height: 100%;
        padding: 16px;
        background: #F0F2F5;
    }
    .chat-nurse-slot {
        position: absolute;
        width: 0;
        height: 0;
        overflow: visible;
        pointer-events: none;
    }
</style>