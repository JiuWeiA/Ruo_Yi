package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.system.domain.SysDatanoteAnnotation;

/**
 * 数据标注服务接口
 * 
 * @author ruoyi
 */
public interface ISysDatanoteAnnotationService {
    /**
     * 查询数据标注
     * 
     * @param id 数据标注ID
     * @return 数据标注
     */
    public SysDatanoteAnnotation selectAnnotationById(Long id);

    /**
     * 查询数据标注列表
     * 
     * @param annotation 数据标注
     * @return 数据标注集合
     */
    public List<SysDatanoteAnnotation> selectAnnotationList(SysDatanoteAnnotation annotation);

    /**
     * 查询指定文本ID的标注
     * 
     * @param textId 文本ID
     * @return 数据标注集合
     */
    public List<SysDatanoteAnnotation> selectAnnotationsByTextId(String textId);

    /**
     * 新增数据标注
     * 
     * @param annotation 数据标注
     * @return 结果
     */
    public int insertAnnotation(SysDatanoteAnnotation annotation);

    /**
     * 批量保存标注数据
     * 
     * @param textId 文本ID
     * @param originalText 原始文本
     * @param annotations 标注数据列表
     * @return 结果
     */
    public int batchSaveAnnotations(String textId, String originalText, List<Map<String, Object>> annotations);

    /**
     * 修改数据标注
     * 
     * @param annotation 数据标注
     * @return 结果
     */
    public int updateAnnotation(SysDatanoteAnnotation annotation);

    /**
     * 批量删除数据标注
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteAnnotationByIds(String ids);

    /**
     * 删除数据标注信息
     * 
     * @param id 数据标注ID
     * @return 结果
     */
    public int deleteAnnotationById(Long id);
    
    /**
     * 删除文本ID的所有标注
     * 
     * @param textId 文本ID
     * @return 结果
     */
    public int deleteAnnotationByTextId(String textId);
} 