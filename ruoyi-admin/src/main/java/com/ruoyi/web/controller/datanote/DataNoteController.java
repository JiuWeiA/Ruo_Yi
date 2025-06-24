package com.ruoyi.web.controller.datanote;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.sql.SqlUtil;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.system.domain.SysDatanoteAnnotation;
import com.ruoyi.system.service.ISysDatanoteAnnotationService;

/**
 * 数据标注控制器
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/datanote/datanote")
public class DataNoteController extends BaseController {
    private String prefix = "datanote";
    
    @Autowired
    private ISysDatanoteAnnotationService annotationService;

    @RequiresPermissions("datanote:datanote:view")
    @GetMapping()
    public String datanote() {
        return prefix + "/datanote";
    }
    
    /**
     * 跳转到统计页面
     */
    @RequiresPermissions("datanote:datanote:view")
    @GetMapping("/toStatistics")
    public String toStatistics() {
        return prefix + "/statistics";
    }

    /**
     * 查询数据标注列表
     */
    @RequiresPermissions("datanote:datanote:list")
    @GetMapping("/list")
    @ResponseBody
    public TableDataInfo list() {
        // 这里实际应该连接到你的数据标注服务和数据库
        // 暂时返回空列表，需要根据实际业务扩展
        startPage();
        return getDataTable(null);
    }
    
    /**
     * 保存标注数据到数据库
     */
    @RequiresPermissions("datanote:datanote:save")
    @Log(title = "数据标注", businessType = BusinessType.INSERT)
    @PostMapping("/saveAnnotations")
    @ResponseBody
    public AjaxResult saveAnnotations(@RequestBody Map<String, Object> params) {
        try {
            // 获取请求参数
            String textId = (String) params.get("textId");
            String originalText = (String) params.get("originalText");
            List<Map<String, Object>> annotations = (List<Map<String, Object>>) params.get("annotations");
            
            // 数据验证
            if (textId == null || textId.trim().isEmpty()) {
                return error("文本编号不能为空");
            }
            
            if (annotations == null || annotations.isEmpty()) {
                return error("标注数据不能为空");
            }
            
            // 调用服务层保存数据
            int rows = annotationService.batchSaveAnnotations(textId, originalText, annotations);
            
            return toAjax(rows);
            
        } catch (Exception e) {
            logger.error("保存标注数据失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入CSV文件并自动标注内容
     */
    @RequiresPermissions("datanote:datanote:import")
    @Log(title = "数据标注导入", businessType = BusinessType.IMPORT)
    @PostMapping("/importCSV")
    @ResponseBody
    public AjaxResult importCSV(@RequestParam("files") MultipartFile[] files, @RequestParam("entityType") String entityType) {
        if (files == null || files.length == 0) {
            return error("请选择需要导入的文件");
        }
        
        if (entityType == null || entityType.trim().isEmpty()) {
            return error("请选择实体类型");
        }
        
        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;
        
        // 调试信息
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("导入调试信息:\n");
        debugInfo.append("选择的实体类型: ").append(entityType).append("\n");
        
        try {
            for (MultipartFile file : files) {
                // 获取文件名（不含路径）
                String fileName = file.getOriginalFilename();
                logger.info("正在处理文件: " + fileName);
                debugInfo.append("处理文件: ").append(fileName).append("\n");
                debugInfo.append("文件大小: ").append(file.getSize()).append(" 字节\n");
                
                // 尝试自动检测编码
                String encoding = detectEncoding(file);
                debugInfo.append("选择的文件编码: ").append(encoding).append("\n");
                
                // 使用Apache Commons CSV库来处理CSV文件
                try (Reader reader = new InputStreamReader(file.getInputStream(), Charset.forName(encoding))) {
                    
                    CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
                    
                    // 获取表头信息
                    Map<String, Integer> headerMap = csvParser.getHeaderMap();
                    debugInfo.append("CSV表头: ").append(String.join(", ", headerMap.keySet())).append("\n");
                    
                    // 寻找"诊断"、"检查结果"和"住院号"列
                    Integer diagnosisIndex = null;
                    Integer examResultIndex = null;
                    Integer idIndex = null;
                    
                    int i = 0;
                    for (String header : headerMap.keySet()) {
                        debugInfo.append("第").append(i).append("列标题: [").append(header).append("]\n");
                        if (header.equals("诊断")) {
                            diagnosisIndex = headerMap.get(header);
                            debugInfo.append("找到'诊断'列，索引: ").append(diagnosisIndex).append("\n");
                        } else if (header.equals("检查结果")) {
                            examResultIndex = headerMap.get(header);
                            debugInfo.append("找到'检查结果'列，索引: ").append(examResultIndex).append("\n");
                        } else if (header.equals("住院号")) {
                            idIndex = headerMap.get(header);
                            debugInfo.append("找到'住院号'列，索引: ").append(idIndex).append("\n");
                        }
                        i++;
                    }
                    
                    // 如果没找到诊断或检查结果列，则跳过此文件
                    if ((diagnosisIndex == null || diagnosisIndex < 0) && 
                        (examResultIndex == null || examResultIndex < 0)) {
                        String msg = "文件缺少诊断或检查结果列: " + fileName;
                        logger.error(msg);
                        debugInfo.append(msg).append("\n");
                        skipCount += 1;
                        continue;
                    }
                    
                    // 如果没有住院号列，也跳过
                    if (idIndex == null || idIndex < 0) {
                        String msg = "文件缺少住院号列: " + fileName;
                        logger.error(msg);
                        debugInfo.append(msg).append("\n");
                        skipCount += 1;
                        continue;
                    }
                    
                    // 读取每一行数据
                    int recordNum = 0;
                    for (CSVRecord record : csvParser) {
                        recordNum++;
                        
                        // 获取住院号作为textId
                        String textId = "";
                        if (idIndex < record.size()) {
                            textId = record.get(idIndex).trim();
                        }
                        
                        if (textId.isEmpty()) {
                            String msg = "第" + recordNum + "行住院号为空，跳过";
                            logger.warn(msg);
                            debugInfo.append(msg).append("\n");
                            skipCount++;
                            continue;
                        }
                        
                        // 获取需要标注的文本
                        String textToAnnotate = "";
                        
                        // 优先使用"诊断"列，如果没有再使用"检查结果"列
                        if (diagnosisIndex != null && diagnosisIndex >= 0 && diagnosisIndex < record.size() && !record.get(diagnosisIndex).isEmpty()) {
                            textToAnnotate = record.get(diagnosisIndex).trim();
                        } else if (examResultIndex != null && examResultIndex >= 0 && examResultIndex < record.size() && !record.get(examResultIndex).isEmpty()) {
                            textToAnnotate = record.get(examResultIndex).trim();
                        }
                        
                        if (textToAnnotate.isEmpty()) {
                            String msg = "第" + recordNum + "行没有可标注内容，跳过";
                            logger.warn(msg);
                            skipCount++;
                            continue;
                        }
                        
                        // 执行自动标注
                        List<Map<String, Object>> annotations = autoAnnotate(textToAnnotate, entityType);
                        
                        if (annotations.isEmpty()) {
                            String msg = "第" + recordNum + "行没有找到需要标注的内容，跳过";
                            logger.info(msg);
                            skipCount++;
                            continue;
                        }
                        
                        try {
                            // 保存标注数据
                            int result = annotationService.batchSaveAnnotations(textId, textToAnnotate, annotations);
                            if (result > 0) {
                                successCount++;
                            } else {
                                failCount++;
                                String msg = "第" + recordNum + "行保存失败，返回值: " + result;
                                logger.error(msg);
                            }
                        } catch (Exception e) {
                            failCount++;
                            String msg = "保存第" + recordNum + "行数据失败: " + e.getMessage();
                            logger.error(msg, e);
                        }
                    }
                }
            }
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("successCount", successCount);
            resultMap.put("skipCount", skipCount);
            resultMap.put("failCount", failCount);
            resultMap.put("debugInfo", debugInfo.toString());
            
            return AjaxResult.success("成功导入：" + successCount + " 条记录", resultMap);
            
        } catch (Exception e) {
            logger.error("导入CSV文件失败", e);
            debugInfo.append("导入异常: ").append(e.getMessage()).append("\n");
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("successCount", successCount);
            resultMap.put("skipCount", skipCount);
            resultMap.put("failCount", failCount);
            resultMap.put("debugInfo", debugInfo.toString());
            
            return AjaxResult.error("导入失败: " + e.getMessage(), resultMap);
        }
    }
    
    /**
     * 尝试自动检测文件编码
     */
    private String detectEncoding(MultipartFile file) {
        String[] encodingsToTry = {"UTF-8", "GBK", "GB2312", "ISO-8859-1"};
        
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
                return "UTF-8";  // UTF-8 with BOM
            }
            
            // 尝试不同的编码解析表头
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("使用自动编码检测模式\n");
            
            for (String encoding : encodingsToTry) {
                debugInfo.append("尝试使用编码: ").append(encoding).append("\n");
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName(encoding)))) {
                    String headerLine = reader.readLine();
                    if (headerLine == null) {
                        continue;
                    }
                    
                    debugInfo.append("CSV表头: ").append(headerLine).append("\n");
                    String[] headers = headerLine.split(",");
                    
                    boolean foundValidHeader = false;
                    for (int i = 0; i < headers.length; i++) {
                        debugInfo.append("第").append(i).append("列标题: [").append(headers[i]).append("]\n");
                        if (headers[i].equals("住院号") || headers[i].equals("检查结果") || headers[i].equals("诊断")) {
                            debugInfo.append("找到'").append(headers[i]).append("'列，索引: ").append(i).append("\n");
                            foundValidHeader = true;
                        }
                    }
                    
                    if (foundValidHeader) {
                        debugInfo.append("使用编码 ").append(encoding).append(" 成功识别表头\n");
                        logger.info(debugInfo.toString());
                        return encoding;
                    }
                } catch (Exception e) {
                    logger.warn("使用编码 " + encoding + " 解析失败", e);
                }
            }
            
            // 默认返回GBK编码
            logger.info("未能自动检测到编码，使用默认编码GBK");
            return "GBK";
            
        } catch (Exception e) {
            logger.error("检测文件编码失败", e);
            return "GBK";  // 默认使用GBK编码
        }
    }
    
    /**
     * 自动标注文本中与指定实体类型相关的内容
     */
    private List<Map<String, Object>> autoAnnotate(String text, String entityType) {
        List<Map<String, Object>> annotations = new ArrayList<>();
        
        // 调试日志
        logger.info("自动标注文本: [" + (text.length() > 50 ? text.substring(0, 50) + "..." : text) + "], 实体类型: " + entityType);
        
        // 根据实体类型定义不同的匹配模式
        String patternStr;
        switch (entityType) {
            case "胸腔积液":
                // 修改为更宽松的匹配模式
                patternStr = ".*?(胸腔积液|胸水|胸膜腔积液|胸腔少量积液).*?";
                break;
            case "气胸":
                // 修改为更宽松的匹配模式
                patternStr = ".*?(气胸|气胸征象|胸腔积气|液气胸).*?";
                break;
            case "肺部感染":
                // 修改为更宽松的匹配模式
                patternStr = ".*?(肺部感染|肺炎|肺部炎症|肺部病变).*?";
                break;
            default:
                // 如果是其他类型，寻找*标注的内容
                patternStr = "\\*(.*?)\\*|\\*\\*(.*?)\\*\\*|\\*\\*\\*(.*?)\\*\\*\\*";
                break;
        }
        
        logger.info("使用匹配模式: " + patternStr);
        
        // 从文本中查找匹配项
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            try {
                // 找到匹配的文本
                String matchedText = matcher.group();
                int startPos = matcher.start();
                int endPos = matcher.end();
                
                logger.info("找到匹配文本: [" + matchedText + "], 位置: " + startPos + "-" + endPos);
                
                // 检查是否已有*标注，如果没有则用*标注
                int importance = 1; // 默认轻度重要
                String importanceType = "轻度重要"; // 默认轻度重要
                if (matchedText.contains("***")) {
                    importance = 3; // 高度重要
                    importanceType = "高度重要";
                } else if (matchedText.contains("**")) {
                    importance = 2; // 中度重要
                    importanceType = "中度重要";
                } else {
                    // 没有标注，将匹配到的文本添加*标注
                    // 提取实际的实体文本
                    String entityText = matchedText;
                    
                    // 根据实体类型提取关键词
                    if (entityType.equals("胸腔积液")) {
                        Matcher entityMatcher = Pattern.compile("(胸腔积液|胸水|胸膜腔积液|胸腔少量积液)").matcher(matchedText);
                        if (entityMatcher.find()) {
                            entityText = entityMatcher.group();
                        }
                    } else if (entityType.equals("气胸")) {
                        Matcher entityMatcher = Pattern.compile("(气胸|气胸征象|胸腔积气|液气胸)").matcher(matchedText);
                        if (entityMatcher.find()) {
                            entityText = entityMatcher.group();
                        }
                    } else if (entityType.equals("肺部感染")) {
                        Matcher entityMatcher = Pattern.compile("(肺部感染|肺炎|肺部炎症|肺部病变)").matcher(matchedText);
                        if (entityMatcher.find()) {
                            entityText = entityMatcher.group();
                        }
                    }
                    
                    // 检查entityText是否在matchedText中
                    int entityIndex = matchedText.indexOf(entityText);
                    if (entityIndex >= 0) {
                        // 在原文中添加*标注
                        String annotatedText = text.substring(0, startPos) + 
                                            text.substring(startPos, startPos + entityIndex) + 
                                            "*" + entityText + "*" + 
                                            text.substring(startPos + entityIndex + entityText.length());
                        
                        // 更新文本
                        text = annotatedText;
                        
                        // 更新位置信息
                        int newStartPos = startPos + entityIndex;
                        int newEndPos = newStartPos + entityText.length() + 2; // +2 for the two asterisks
                        startPos = newStartPos;
                        endPos = newEndPos;
                    }
                }
                
                // 提取实际实体文本
                String entityText;
                
                // 根据实体类型提取关键词
                if (entityType.equals("胸腔积液")) {
                    Matcher entityMatcher = Pattern.compile("(胸腔积液|胸水|胸膜腔积液|胸腔少量积液)").matcher(matchedText);
                    if (entityMatcher.find()) {
                        entityText = entityMatcher.group();
                    } else {
                        entityText = matchedText.replaceAll("\\*", "");
                    }
                } else if (entityType.equals("气胸")) {
                    Matcher entityMatcher = Pattern.compile("(气胸|气胸征象|胸腔积气|液气胸)").matcher(matchedText);
                    if (entityMatcher.find()) {
                        entityText = entityMatcher.group();
                    } else {
                        entityText = matchedText.replaceAll("\\*", "");
                    }
                } else if (entityType.equals("肺部感染")) {
                    Matcher entityMatcher = Pattern.compile("(肺部感染|肺炎|肺部炎症|肺部病变)").matcher(matchedText);
                    if (entityMatcher.find()) {
                        entityText = entityMatcher.group();
                    } else {
                        entityText = matchedText.replaceAll("\\*", "");
                    }
                } else {
                    // 对于其他类型，去掉星号
                    entityText = matchedText.replaceAll("\\*", "");
                }
                
                logger.info("提取的实体文本: [" + entityText + "], 重要程度: " + importance);
                
                // 创建标注对象
                Map<String, Object> annotation = new HashMap<>();
                annotation.put("text", entityText);
                annotation.put("start", startPos);
                annotation.put("end", endPos);
                annotation.put("type", importanceType);  // 使用重要程度作为实体类型
                annotation.put("level", importance);
                annotation.put("category", entityType);  // 保存原始实体类型为类别
                
                annotations.add(annotation);
            } catch (Exception e) {
                logger.error("处理匹配文本时出错", e);
                // 继续处理下一个匹配项
            }
        }
        
        // 也检查文本中已有的星号标注
        try {
            Pattern starPattern = Pattern.compile("\\*(.*?)\\*|\\*\\*(.*?)\\*\\*|\\*\\*\\*(.*?)\\*\\*\\*");
            Matcher starMatcher = starPattern.matcher(text);
            
            while (starMatcher.find()) {
                String fullMatch = starMatcher.group();
                int startPos = starMatcher.start();
                int endPos = starMatcher.end();
                
                // 确定重要程度
                int importance;
                String importanceType;
                String content;
                
                if (fullMatch.startsWith("***") && fullMatch.length() >= 6) {
                    importance = 3;
                    importanceType = "高度重要";
                    content = fullMatch.substring(3, fullMatch.length() - 3);
                } else if (fullMatch.startsWith("**") && fullMatch.length() >= 4) {
                    importance = 2;
                    importanceType = "中度重要";
                    content = fullMatch.substring(2, fullMatch.length() - 2);
                } else if (fullMatch.startsWith("*") && fullMatch.length() >= 2) {
                    importance = 1;
                    importanceType = "轻度重要";
                    content = fullMatch.substring(1, fullMatch.length() - 1);
                } else {
                    // 无效的标注格式，跳过
                    continue;
                }
                
                logger.info("找到星号标注: [" + content + "], 重要程度: " + importance);
                
                // 检查是否重复
                boolean isDuplicate = false;
                for (Map<String, Object> existing : annotations) {
                    if (existing.get("text").equals(content)) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                if (!isDuplicate) {
                    Map<String, Object> annotation = new HashMap<>();
                    annotation.put("text", content);
                    annotation.put("start", startPos);
                    annotation.put("end", endPos);
                    annotation.put("type", importanceType);  // 使用重要程度作为实体类型
                    annotation.put("level", importance);
                    annotation.put("category", entityType);  // 保存原始实体类型为类别
                    
                    annotations.add(annotation);
                }
            }
        } catch (Exception e) {
            logger.error("处理星号标注时出错", e);
        }
        
        logger.info("总共找到 " + annotations.size() + " 个标注");
        return annotations;
    }
    
    /**
     * 获取统计数据
     */
    @RequiresPermissions("datanote:datanote:view")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics(@RequestParam(required = false) String textId, 
                                     @RequestParam(required = false) String entityType) {
        try {
            // 创建查询条件
            SysDatanoteAnnotation query = new SysDatanoteAnnotation();
            if (textId != null && !textId.trim().isEmpty()) {
                query.setTextId(textId);
            }
            if (entityType != null && !entityType.trim().isEmpty()) {
                query.setEntityType(entityType);
            }
            
            // 获取符合条件的标注数据
            List<SysDatanoteAnnotation> annotations = annotationService.selectAnnotationList(query);
            
            // 处理统计数据
            Map<String, Object> resultMap = new HashMap<>();
            
            // 1. 总标注数
            int totalCount = annotations.size();
            resultMap.put("totalCount", totalCount);
            
            // 2. 标注文本数
            long textCount = annotations.stream()
                    .map(SysDatanoteAnnotation::getTextId)
                    .distinct()
                    .count();
            resultMap.put("textCount", textCount);
            
            // 3. 平均每文本标注数
            double avgCount = textCount > 0 ? (double) totalCount / textCount : 0;
            resultMap.put("avgCount", String.format("%.2f", avgCount));
            
            // 4. 标注类型统计（使用实体文本）
            Map<String, Long> textStats = annotations.stream()
                    .filter(ann -> ann.getEntityText() != null && !ann.getEntityText().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            SysDatanoteAnnotation::getEntityText,
                            Collectors.counting()));
            resultMap.put("textStats", textStats);
            
            // 5. 标注类型统计（原始，使用实体类型）
            Map<String, Long> typeStats = annotations.stream()
                    .filter(ann -> ann.getEntityType() != null && !ann.getEntityType().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            SysDatanoteAnnotation::getEntityType,
                            Collectors.counting()));
            resultMap.put("typeStats", typeStats);
            
            // 6. 实体类别统计（使用实体类别）
            Map<String, Long> categoryStats = annotations.stream()
                    .filter(ann -> ann.getEntityCategory() != null && !ann.getEntityCategory().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            SysDatanoteAnnotation::getEntityCategory,
                            Collectors.counting()));
            resultMap.put("categoryStats", categoryStats);
            
            // 7. 重要程度统计
            Map<String, Long> importanceStats = annotations.stream()
                    .collect(Collectors.groupingBy(
                            ann -> String.valueOf(ann.getImportance()),
                            Collectors.counting()));
            resultMap.put("importanceStats", importanceStats);
            
            // 8. 标注趋势（按日期分组）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Long> trendStats = annotations.stream()
                    .collect(Collectors.groupingBy(
                            ann -> sdf.format(ann.getCreateTime()),
                            Collectors.counting()));
            resultMap.put("trendStats", trendStats);
            
            return AjaxResult.success(resultMap);
            
        } catch (Exception e) {
            logger.error("获取统计数据失败", e);
            return error("获取统计数据失败: " + e.getMessage());
        }
    }
} 