-- 添加实体类别字段
ALTER TABLE sys_datanote_annotations 
ADD COLUMN entity_category VARCHAR(50) DEFAULT NULL COMMENT '实体类别' AFTER importance;

-- 更新已有数据，将entity_type设置为重要程度描述
UPDATE sys_datanote_annotations 
SET entity_category = entity_type,
    entity_type = CASE 
        WHEN importance = 1 THEN '轻度重要'
        WHEN importance = 2 THEN '中度重要'
        WHEN importance = 3 THEN '高度重要'
        ELSE '未知重要程度'
    END
WHERE entity_category IS NULL; 