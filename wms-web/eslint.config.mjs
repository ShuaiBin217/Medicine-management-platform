import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import globals from 'globals'

export default [
  {
    ignores: ['dist/**', 'node_modules/**', 'generate_thesis.js', 'read_pdf.js']
  },
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  {
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      // 保留原 Vue 2 项目中的 Aside/Header/Main 等组件命名
      'vue/no-reserved-component-names': 'off',
      // catch (e) {} 中的错误参数允许省略使用
      'no-unused-vars': ['error', { caughtErrors: 'none' }]
    }
  }
]
