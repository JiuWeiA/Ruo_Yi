-- 数据标注统计菜单项
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2008, '标注统计', 2000, 2, '/datanote/datanote/toStatistics', 'menuItem', 'C', '0', '1', 'datanote:datanote:view', 'fa fa-bar-chart', 'admin', sysdate(), '', null, '数据标注统计页面'); 