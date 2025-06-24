package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.system.domain.SysDatanoteAnnotation;
import com.ruoyi.system.mapper.SysDatanoteAnnotationMapper;
import com.ruoyi.system.service.ISysDatanoteAnnotationService;

/**
 * 数据标注服务实现
 * 
 * @author ruoyi
 */
@Service
public class SysDatanoteAnnotationServiceImpl implements ISysDatanoteAnnotationService {
    @Autowired
    private SysDatanoteAnnotationMapper annotationMapper;

    /**
     * 查询数据标注
     * 
     * @param id 数据标注ID
     * @return 数据标注
     */
    @Override
    public SysDatanoteAnnotation selectAnnotationById(Long id) {
        return annotationMapper.selectAnnotationById(id);
    }

    /**
     * 查询数据标注列表
     * 
     * @param annotation 数据标注
     * @return 数据标注
     */
    @Override
    public List<SysDatanoteAnnotation> selectAnnotationList(SysDatanoteAnnotation annotation) {
        return annotationMapper.selectAnnotationList(annotation);
    }

    /**
     * 查询指定文本ID的标注
     * 
     * @param textId 文本ID
     * @return 数据标注集合
     */
    @Override
    public List<SysDatanoteAnnotation> selectAnnotationsByTextId(String textId) {
        return annotationMapper.selectAnnotationsByTextId(textId);
    }

    /**
     * 新增数据标注
     * 
     * @param annotation 数据标注
     * @return 结果
     */
    @Override
    public int insertAnnotation(SysDatanoteAnnotation annotation) {
        annotation.setCreateBy(ShiroUtils.getLoginName());
        annotation.setCreateTime(DateUtils.getNowDate());
        return annotationMapper.insertAnnotation(annotation);
    }

    /**
     * 批量保存标注数据
     * 
     * @param textId 文本ID
     * @param originalText 原始文本
     * @param annotations 标注数据列表
     * @return 结果
     */
    @Override
    @Transactional
    public int batchSaveAnnotations(String textId, String originalText, List<Map<String, Object>> annotations) {
        // 先删除该文本ID下的所有标注
        annotationMapper.deleteAnnotationByTextId(textId);
        
        // 批量插入新的标注
        List<SysDatanoteAnnotation> annotationList = new ArrayList<>();
        for (Map<String, Object> ann : annotations) {
            SysDatanoteAnnotation annotation = new SysDatanoteAnnotation();
            annotation.setTextId(textId);
            annotation.setOriginalText(originalText);
            annotation.setEntityText((String) ann.get("text"));
            annotation.setEntityType((String) ann.get("type"));
            
            // 处理位置信息
            Integer start = ((Number) ann.get("start")).intValue();
            Integer end = ((Number) ann.get("end")).intValue();
            annotation.setStartPosition(start);
            annotation.setEndPosition(end);
            
            // 处理重要程度
            Integer level = ((Number) ann.get("level")).intValue();
            annotation.setImportance(level);
            
            // 设置创建信息
            annotation.setCreateBy(ShiroUtils.getLoginName());
            annotation.setCreateTime(DateUtils.getNowDate());
            
            annotationList.add(annotation);
        }
        
        int rows = 0;
        if (!annotationList.isEmpty()) {
            // 使用批量插入提高效率
            rows = annotationMapper.batchInsertAnnotation(annotationList);
        }
        
        return rows;
    }

    /**
     * 修改数据标注
     * 
     * @param annotation 数据标注
     * @return 结果
     */
    @Override
    public int updateAnnotation(SysDatanoteAnnotation annotation) {
        annotation.setUpdateBy(ShiroUtils.getLoginName());
        annotation.setUpdateTime(DateUtils.getNowDate());
        return annotationMapper.updateAnnotation(annotation);
    }

    /**
     * 删除数据标注对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteAnnotationByIds(String ids) {
        return annotationMapper.deleteAnnotationByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除数据标注信息
     * 
     * @param id 数据标注ID
     * @return 结果
     */
    @Override
    public int deleteAnnotationById(Long id) {
        return annotationMapper.deleteAnnotationById(id);
    }
    
    /**
     * 删除文本ID的所有标注
     * 
     * @param textId 文本ID
     * @return 结果
     */
    @Override
    public int deleteAnnotationByTextId(String textId) {
        return annotationMapper.deleteAnnotationByTextId(textId);
    }
} 