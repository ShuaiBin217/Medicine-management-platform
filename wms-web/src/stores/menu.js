import { defineStore } from 'pinia'
import { addDynamicRoutes, resetRouter } from '../router'

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

export const useMenuStore = defineStore('menu', {
    state: () => ({
        menu: []
    }),
    getters: {
        getMenu(state) {
            return state.menu
        }
    },
    actions: {
        // 应用启动时调用：恢复菜单并重建动态路由（等价原 Vuex 模块顶层副作用）
        restoreFromStorage() {
            this.menu = loadMenuFromStorage()
            if (this.menu.length > 0) {
                addDynamicRoutes(this.menu)
            }
        },
        setMenu(menuList) {
            this.menu = menuList
            sessionStorage.setItem('MenuList', JSON.stringify(menuList))
            resetRouter()
            addDynamicRoutes(menuList)
        }
    }
})
