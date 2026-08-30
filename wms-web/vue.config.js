module.exports = {
  publicPath: process.env.NODE_ENV === 'production'
    ? '/Medicine-management-platform/'
    : '/',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false
}