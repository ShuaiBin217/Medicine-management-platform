import vue from 'vue'
import Vuex from 'vuex'
import router,{resetRouter} from "../router";
vue.use(Vuex)

function addNewRoute(menuList) {
    let routes = router.options.routes
    routes.forEach(routeItem=>{
        if(routeItem.path=="/Index"){
            let existingPaths = new Set(routeItem.children.map(c => c.path))
            menuList.forEach(menu=>{
                let path = '/'+menu.menuclick
                if(!existingPaths.has(path)){
                    let childRoute =  {
                        path:path,
                        name:menu.menuname,
                        meta:{
                            title:menu.menuname
                        },
                        component:()=>import('../components/'+menu.menucomponent)
                    }
                    routeItem.children.push(childRoute)
                    existingPaths.add(path)
                }
            })
        }
    })

    resetRouter()
    router.addRoutes(routes)
}

function loadMenuFromStorage() {
    try {
        let stored = sessionStorage.getItem('MenuList')
        if (stored) {
            return JSON.parse(stored)
        }
    } catch (e) {
        sessionStorage.removeItem('MenuList')
    }
    return []
}

let savedMenu = loadMenuFromStorage()

export default new Vuex.Store({
    state: {
        menu: savedMenu
    },
    mutations: {
        setMenu(state,menuList) {
            state.menu = menuList
            sessionStorage.setItem('MenuList', JSON.stringify(menuList))
            addNewRoute(menuList)
        }
    },
    getters: {
        getMenu(state) {
            return state.menu
        }
    }
})

if (savedMenu.length > 0) {
    addNewRoute(savedMenu)
}