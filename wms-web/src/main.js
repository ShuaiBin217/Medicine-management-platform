import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/global.css';
import axios from "axios";
import VueRouter from 'vue-router';
import router from './router';
import store from './store';
Vue.prototype.$axios=axios;
Vue.prototype.$httpUrl = process.env.VUE_APP_API_URL || 'http://localhost:8090'
// AI 服务地址：开发环境默认指向 Python FastAPI(8091)；生产环境设 VUE_APP_AI_URL="" 走 Nginx 同源代理
Vue.prototype.$aiUrl = process.env.VUE_APP_AI_URL != null ? process.env.VUE_APP_AI_URL : 'http://localhost:8091'
Vue.config.productionTip = false
//Vue.use(ElementUI);
Vue.use(VueRouter);
Vue.use(ElementUI,{size:'small'});
new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app')