package com.ruoyi.system.domain;

import java.util.Date;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据标注对象 sys_datanote_annotations
 * 
 * @author ruoyi
 */
public class SysDatanoteAnnotation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 文本编号 */
    @Excel(name = "文本编号")
    private String textId;

    /** 原始文本内容 */
    @Excel(name = "原始文本内容")
    private String originalText;

    /** 实体类型 */
    @Excel(name = "实体类型")
    private String entityType;

    /** 实体文本 */
    @Excel(name = "实体文本")
    private String entityText;

    /** 开始位置 */
    @Excel(name = "开始位置")
    private Integer startPosition;

    /** 结束位置 */
    @Excel(name = "结束位置")
    private Integer endPosition;

    /** 重要程度（1-轻度，2-中度，3-高度） */
    @Excel(name = "重要程度", readConverterExp = "1=轻度,2=中度,3=高度")
    private Integer importance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityText() {
        return entityText;
    }

    public void setEntityText(String entityText) {
        this.entityText = entityText;
    }

    public Integer getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Integer startPosition) {
        this.startPosition = startPosition;
    }

    public Integer getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Integer endPosition) {
        this.endPosition = endPosition;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    @Override
    public String toString() {
        return "SysDatanoteAnnotation [id=" + id + ", textId=" + textId + ", entityType=" + entityType + ", entityText="
                + entityText + ", startPosition=" + startPosition + ", endPosition=" + endPosition + ", importance="
                + importance + "]";
    }
} 