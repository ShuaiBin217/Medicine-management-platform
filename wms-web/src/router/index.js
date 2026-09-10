import { createRouter, createWebHistory } from 'vue-router';

// Vite 下的变量动态导入：key 为含扩展名的相对路径（'../components/goods/GoodsManage.vue'）
export const modules = import.meta.glob('../components/**/*.vue');

const FALLBACK = () => import('../components/Index.vue');

export function resolveComponent(menucomponent) {
    const key = '../components/' + menucomponent + '.vue';
    return modules[key] || modules['../components/' + menucomponent] || FALLBACK;
}

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
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 动态路由记录：name 用于 reset 时 removeRoute，path 保留原按路径去重的语义
const addedNames = new Set()
const addedPaths = new Set()

export function addDynamicRoutes(menuList) {
    menuList.forEach(menu=>{
        let path = '/'+menu.menuclick
        if(!addedPaths.has(path)){
            router.addRoute('index', {
                path: path,
                name: menu.menuname,
                meta: {
                    title: menu.menuname
                },
                component: resolveComponent(menu.menucomponent)
            })
            addedNames.add(menu.menuname)
            addedPaths.add(path)
        }
    })
}

export function resetRouter() {
    addedNames.forEach(n => {
        if (router.hasRoute(n)) router.removeRoute(n)
    })
    addedNames.clear()
    addedPaths.clear()
}

router.beforeEach((to) => {
    let user = sessionStorage.getItem('CurUser')
    if (to.path === '/') {
        return true
    }
    return user ? true : { path: '/' }
})

export default router;
