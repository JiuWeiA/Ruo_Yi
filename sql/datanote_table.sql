-- 创建数据标注表
CREATE TABLE IF NOT EXISTS sys_datanote_annotations (
    id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    text_id VARCHAR(100) NOT NULL COMMENT '文本编号',
    original_text TEXT COMMENT '原始文本内容',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型',
    entity_text VARCHAR(500) NOT NULL COMMENT '实体文本',
    start_position INT(11) NOT NULL COMMENT '开始位置',
    end_position INT(11) NOT NULL COMMENT '结束位置',
    importance INT(1) DEFAULT 2 COMMENT '重要程度（1-轻度，2-中度，3-高度）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_text_id (text_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据标注表';

-- 增加相关权限（使用更高的ID值避免冲突）
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2100, '保存标注', 2001, 1, '#', '', 'F', '0', '1', 'datanote:datanote:save', '#', 'admin', sysdate(), '', null, '');

-- 新增导入权限（使用更高的ID值避免冲突）
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, update_by, update_time, remark) 
VALUES (2101, '导入数据', 2001, 2, '#', '', 'F', '0', '1', 'datanote:datanote:import', '#', 'admin', sysdate(), '', null, '');