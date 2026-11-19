-- ============================================================
-- 2025-11-19 增量脚本：药品流转页
-- 适用于已经在用的 wms02 库，执行一次即可（可重复执行）
-- 新建库不需要执行本脚本，table.sql 里已经带上这条菜单
-- ============================================================

-- 新增菜单：进入 "药品流转详细" 页（/GoodsDetail）
-- 从「物品管理」点击药品名进入时带 ?id=xxx，直接展示该药品；
-- 从左侧菜单进入时页面内提供药品选择器。
DELETE FROM `menu` WHERE `id` = 7;
INSERT INTO `menu` (`id`, `menuCode`, `menuName`, `menuLevel`, `menuParentCode`, `menuClick`, `menuRight`, `menuComponent`, `menuIcon`)
VALUES (7, '007', '药品流转详细', '1', NULL, 'GoodsDetail', '0,1,2', 'goods/GoodsDetail', 'el-icon-share');
