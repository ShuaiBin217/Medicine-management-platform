import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  base: '/',
  resolve: {
    // 兼容原 Vue CLI(webpack) 的无扩展名导入写法：import('../components/Login')
    extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  server: {
    port: 8080,
    // 开发环境代理：与生产 Nginx 的 /ai/、/knowledge/ 转发行为保持一致
    proxy: {
      '/ai': {
        target: 'http://localhost:8091',
        changeOrigin: true
      },
      '/knowledge': {
        target: 'http://localhost:8091',
        changeOrigin: true
      },
      '/health': {
        target: 'http://localhost:8091',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'static',
    sourcemap: false,
    chunkSizeWarningLimit: 3000
  }
})
