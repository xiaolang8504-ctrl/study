const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 9527,
    proxy: {
      '/api': {
        target: process.env.VUE_APP_BASE_API || 'http://127.0.0.1:8091',
        changeOrigin: true
      }
    }
  }
})
