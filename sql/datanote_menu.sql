-- 数据标注菜单SQL

-- 菜单 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2000, '数据标注', 0, 6, '#', 'menuItem', 'M', '0', '1', '', 'fa fa-tag', 'admin', sysdate(), '', null, '数据标注菜单');

-- 菜单对应按钮SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2001, '数据标注', 2000, 1, '/datanote/datanote', 'menuItem', 'C', '0', '1', 'datanote:datanote:view', '#', 'admin', sysdate(), '', null, '数据标注菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2002, '数据标注查询', @parentId, 1, '#', '', 'F', '0', '1', 'datanote:datanote:list', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2003, '数据标注新增', @parentId, 2, '#', '', 'F', '0', '1', 'datanote:datanote:add', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2004, '数据标注修改', @parentId, 3, '#', '', 'F', '0', '1', 'datanote:datanote:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2005, '数据标注删除', @parentId, 4, '#', '', 'F', '0', '1', 'datanote:datanote:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2006, '数据标注导出', @parentId, 5, '#', '', 'F', '0', '1', 'datanote:datanote:export', '#', 'admin', sysdate(), '', null, ''); 