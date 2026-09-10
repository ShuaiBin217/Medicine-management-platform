// 旧 element-ui 字体图标类名 → Element Plus 图标组件名映射
// DB menu 表的 menuIcon 字段存的是旧 el-icon-* 类名，运行时经此表转换为 EP 图标组件
export const FALLBACK_ICON = 'Menu'

export const legacyIconMap = {
  // 首页/侧边栏/个人信息
  'el-icon-s-home': 'HomeFilled',
  'el-icon-s-custom': 'UserFilled',
  'el-icon-user-solid': 'UserFilled',
  'el-icon-user': 'User',
  'el-icon-mobile-phone': 'Iphone',
  'el-icon-location-outline': 'Location',
  'el-icon-male': 'Male',
  'el-icon-female': 'Female',
  'el-icon-tickets': 'Tickets',
  'el-icon-data-analysis': 'DataAnalysis',
  'el-icon-goods': 'Goods',
  'el-icon-office-building': 'OfficeBuilding',
  'el-icon-collection-tag': 'CollectionTag',
  'el-icon-warning-outline': 'Warning',
  'el-icon-first-aid-kit': 'FirstAidKit',
  // 折叠/操作类
  'el-icon-s-fold': 'Fold',
  'el-icon-s-unfold': 'Expand',
  'el-icon-arrow-down': 'ArrowDown',
  'el-icon-switch-button': 'SwitchButton',
  'el-icon-delete': 'Delete',
  'el-icon-close': 'Close',
  'el-icon-search': 'Search',
  'el-icon-lock': 'Lock',
  'el-icon-s-promotion': 'Promotion',
  // DB menu 库表常见值（持续补充）
  'el-icon-menu': 'Grid',
  'el-icon-setting': 'Setting',
  'el-icon-document': 'Document',
  'el-icon-s-grid': 'Grid',
  'el-icon-s-data': 'TrendCharts',
  'el-icon-s-order': 'Document',
  'el-icon-s-shop': 'Shop',
  'el-icon-s-check': 'CircleCheck',
  'el-icon-s-marketing': 'Histogram',
  'el-icon-s-management': 'Files',
  'el-icon-s-cooperation': 'Handshake',
  'el-icon-s-platform': 'Platform',
  'el-icon-s-tools': 'SetUp',
  'el-icon-s-operation': 'Operation',
  'el-icon-box': 'Box',
  'el-icon-monitor': 'Monitor'
}
