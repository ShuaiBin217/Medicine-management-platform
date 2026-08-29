
import VueRouter from 'vue-router';

const routes = [
    {
        path:'/',
        name:'login',
        component:()=>import('../components/Login')
    },
    {
        path:'/Index',
        name:'index',
        redirect:'/Home',
        component:()=>import('../components/Index'),
        children:[
            {
                path:'/Home',
                name:'home',
                meta:{
                    title:'首页'
                },
                component:()=>import('../components/Home')
            },
            /*{
                path:'/Admin',
                name:'admin',
                meta:{
                    title:'管理员管理'
                },
                component:()=>import('../components/admin/AdminManage.vue')
            },
            {
                path:'/User',
                name:'user',
                meta:{
                    title:'用户管理'
                },
                component:()=>import('../components/user/UserManage.vue')
            },*/
        ]
    }
]

const router = new VueRouter({
    mode:'history',
    routes
})

export function resetRouter() {
    router.matcher = new VueRouter({
        mode:'history',
        routes: []
    }).matcher
}
router.beforeEach((to, from, next) => {
    let user = sessionStorage.getItem('CurUser')
    if (to.path === '/') {
        next()
    } else {
        if (user) {
            next()
        } else {
            next({ path: '/' })
        }
    }
})

const VueRouterPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (to) {
    return VueRouterPush.call(this, to).catch(err => err)
}
export  default router;