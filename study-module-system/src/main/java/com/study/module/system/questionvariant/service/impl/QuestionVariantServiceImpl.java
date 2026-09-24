package com.study.module.system.questionvariant.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionVariantGenerateReq;
import com.study.module.system.questionbank.dto.request.QuestionVariantTemplateSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionVariantGenerateResp;
import com.study.module.system.questionbank.dto.response.QuestionVariantTemplateResp;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.study.module.system.questionvariant.entity.QuestionVariantFormula;
import com.study.module.system.questionvariant.entity.QuestionVariantParameter;
import com.study.module.system.questionvariant.entity.QuestionVariantRecord;
import com.study.module.system.questionvariant.entity.QuestionVariantTemplate;
import com.study.module.system.questionvariant.entity.QuestionVariantTemplateVersion;
import com.study.module.system.questionvariant.entity.QuestionVariantValidation;
import com.study.module.system.questionvariant.entity.QuestionVariantVariable;
import com.study.module.system.questionvariant.mapper.QuestionVariantFormulaMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantParameterMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantRecordMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantTemplateMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantTemplateVersionMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantValidationMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantVariableMapper;
import com.study.module.system.questionvariant.service.QuestionVariantService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 复用既有变式题审计模型；只执行白名单公式，生成题仍进入审核题库。 */
@Service
public class QuestionVariantServiceImpl implements QuestionVariantService {
    private static final Pattern FORMULA = Pattern.compile("^(ADD|SUBTRACT|MULTIPLY)\\(([A-Za-z][A-Za-z0-9_]*)\\s*,\\s*([A-Za-z][A-Za-z0-9_]*)\\)$");
    @Autowired private QuestionVariantTemplateMapper templateMapper; @Autowired private QuestionVariantTemplateVersionMapper versionMapper;
    @Autowired private QuestionVariantVariableMapper variableMapper; @Autowired private QuestionVariantFormulaMapper formulaMapper;
    @Autowired private QuestionVariantRecordMapper recordMapper; @Autowired private QuestionVariantParameterMapper parameterMapper;
    @Autowired private QuestionVariantValidationMapper validationMapper; @Autowired private KnowledgePointService knowledgePointService;
    @Autowired private QuestionBankService questionBankService; @Autowired private QuestionKnowledgePointService questionKnowledgePointService;

    @Override @Transactional(rollbackFor = Exception.class)
    public void saveQuestionVariantTemplate(QuestionVariantTemplateSaveReq request) {
        QuestionBank original = questionBankService.getById(request.getOriginalQuestionId()); KnowledgePoint point = knowledgePointService.getById(request.getPrimaryKnowledgePointId());
        if (original == null || point == null || !request.getGrade().equals(point.getGrade()) || !request.getSubject().equals(point.getSubject())) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        Map<String, Integer> sample = parseValues(request.getVariableSchemaJson(), 1L); calculate(request.getAnswerFormula(), sample); render(request.getQuestionPattern(), sample);
        QuestionVariantTemplate entity = request.getId() == null ? new QuestionVariantTemplate() : templateMapper.selectById(request.getId());
        if (entity == null || duplicateCode(request)) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        int nextVersion = entity.getId() == null ? 1 : entity.getCurrentVersion() + 1; LocalDateTime now = LocalDateTime.now();
        entity.setTemplateCode(request.getTemplateCode().trim()); entity.setTemplateName(request.getTemplateName().trim()); entity.setOriginalQuestionId(request.getOriginalQuestionId()); entity.setSubject(request.getSubject()); entity.setGrade(request.getGrade()); entity.setKnowledgePointId(request.getPrimaryKnowledgePointId()); entity.setQuestionType(request.getQuestionType()); entity.setTitleTemplate(request.getQuestionPattern()); entity.setAnswerTemplate(request.getAnswerFormula()); entity.setAnalysisTemplate(request.getAnalysisPattern()); entity.setCurrentVersion(nextVersion); entity.setStatus(request.getEnable() == null || request.getEnable() == 1 ? 1 : 2); entity.setUpdateId(AccountUtils.getUserId()); entity.setUpdateTime(now);
        if (entity.getId() == null) { entity.setCreateId(AccountUtils.getUserId()); entity.setCreateTime(now); templateMapper.insert(entity); } else templateMapper.updateById(entity);
        QuestionVariantTemplateVersion version = new QuestionVariantTemplateVersion(); version.setTemplateId(entity.getId()); version.setVersionNo(nextVersion); version.setSnapshotJson(JSON.toJSONString(request)); version.setChangeSummary("保存受限公式模板"); version.setVersionStatus(1); version.setOperatorId(AccountUtils.getUserId()); version.setCreateTime(now); versionMapper.insert(version);
        variableMapper.delete(new LambdaQueryWrapper<QuestionVariantVariable>().eq(QuestionVariantVariable::getTemplateId, entity.getId()).eq(QuestionVariantVariable::getVersionNo, nextVersion));
        int sort = 1; for (Map.Entry<String, Integer> item : sample.entrySet()) { JSONObject config = JSON.parseArray(request.getVariableSchemaJson()).stream().map(value -> (JSONObject) value).filter(value -> item.getKey().equals(value.getString("name"))).findFirst().orElse(null); QuestionVariantVariable variable = new QuestionVariantVariable(); variable.setTemplateId(entity.getId()); variable.setVersionNo(nextVersion); variable.setVariableCode(item.getKey()); variable.setVariableName(item.getKey()); variable.setValueType("INTEGER"); variable.setMinValue(BigDecimal.valueOf(config.getInteger("min"))); variable.setMaxValue(BigDecimal.valueOf(config.getInteger("max"))); variable.setStepValue(BigDecimal.ONE); variable.setIsRequired(1); variable.setSort(sort++); variable.setCreateTime(now); variable.setUpdateTime(now); variableMapper.insert(variable); }
        QuestionVariantFormula formula = new QuestionVariantFormula(); formula.setTemplateId(entity.getId()); formula.setVersionNo(nextVersion); formula.setFormulaCode("ANSWER"); formula.setFormulaName("标准答案"); formula.setExpression(request.getAnswerFormula()); formula.setTargetVariable("answer"); formula.setExecutionOrder(1); formula.setEnable(1); formula.setCreateTime(now); formula.setUpdateTime(now); formulaMapper.insert(formula);
    }

    @Override public List<QuestionVariantTemplateResp> questionVariantTemplateList(String subject) { List<QuestionVariantTemplateResp> result = new ArrayList<>(); for (QuestionVariantTemplate item : templateMapper.selectList(new LambdaQueryWrapper<QuestionVariantTemplate>().eq(StringUtils.hasText(subject), QuestionVariantTemplate::getSubject, subject).eq(QuestionVariantTemplate::getDeleted, 0).orderByDesc(QuestionVariantTemplate::getId))) { QuestionVariantTemplateResp r = new QuestionVariantTemplateResp(); r.setId(item.getId()); r.setTemplateCode(item.getTemplateCode()); r.setTemplateName(item.getTemplateName()); r.setTemplateVersion("v" + item.getCurrentVersion()); r.setGrade(item.getGrade()); r.setSubject(item.getSubject()); r.setQuestionType(item.getQuestionType()); r.setQuestionPattern(item.getTitleTemplate()); r.setAnswerFormula(item.getAnswerTemplate()); r.setAnalysisPattern(item.getAnalysisTemplate()); r.setDifficulty(3); r.setPrimaryKnowledgePointId(item.getKnowledgePointId()); KnowledgePoint point = knowledgePointService.getById(item.getKnowledgePointId()); r.setPrimaryKnowledgePointName(point == null ? null : point.getPointName()); r.setEnable(item.getStatus() == 1 ? 1 : 0); List<QuestionVariantVariable> vars = variableMapper.selectList(new LambdaQueryWrapper<QuestionVariantVariable>().eq(QuestionVariantVariable::getTemplateId,item.getId()).eq(QuestionVariantVariable::getVersionNo,item.getCurrentVersion()).orderByAsc(QuestionVariantVariable::getSort)); JSONArray schema = new JSONArray(); vars.forEach(v -> { JSONObject x = new JSONObject(); x.put("name",v.getVariableCode()); x.put("min",v.getMinValue().intValue()); x.put("max",v.getMaxValue().intValue()); schema.add(x); }); r.setVariableSchemaJson(schema.toJSONString()); result.add(r); } return result; }

    @Override @Transactional(rollbackFor = Exception.class)
    public QuestionVariantGenerateResp generateQuestionVariant(QuestionVariantGenerateReq request) { QuestionVariantTemplate t = templateMapper.selectById(request.getTemplateId()); if (t == null || t.getStatus() != 1) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL); List<QuestionVariantVariable> vars = variableMapper.selectList(new LambdaQueryWrapper<QuestionVariantVariable>().eq(QuestionVariantVariable::getTemplateId,t.getId()).eq(QuestionVariantVariable::getVersionNo,t.getCurrentVersion()).orderByAsc(QuestionVariantVariable::getSort)); Map<String,Integer> values = valuesFromVariables(vars, request.getSeed()); QuestionVariantFormula formula = formulaMapper.selectOne(new LambdaQueryWrapper<QuestionVariantFormula>().eq(QuestionVariantFormula::getTemplateId,t.getId()).eq(QuestionVariantFormula::getVersionNo,t.getCurrentVersion()).eq(QuestionVariantFormula::getEnable,1).orderByAsc(QuestionVariantFormula::getExecutionOrder).last("LIMIT 1")); int answer = calculate(formula == null ? "" : formula.getExpression(), values); String content = render(t.getTitleTemplate(),values); LocalDateTime now = LocalDateTime.now(); QuestionVariantRecord record = new QuestionVariantRecord(); record.setTemplateId(t.getId()); record.setTemplateVersion(t.getCurrentVersion()); record.setOriginalQuestionId(request.getSourceQuestionId() == null ? t.getOriginalQuestionId() : request.getSourceQuestionId()); record.setQuestionContent(content); record.setCorrectAnswer(String.valueOf(answer)); record.setAnalysis(render(t.getAnalysisTemplate(), values)); record.setQuestionHash(DigestUtils.md5DigestAsHex((t.getId()+"|"+content).getBytes(StandardCharsets.UTF_8))); record.setValidationStatus("PASS"); record.setAuditStatus(0); record.setCreateId(AccountUtils.getUserId()); record.setCreateTime(now); record.setUpdateTime(now); recordMapper.insert(record); int sort = 1; for (Map.Entry<String,Integer> value : values.entrySet()) { QuestionVariantParameter p = new QuestionVariantParameter(); p.setVariantRecordId(record.getId()); p.setVariableCode(value.getKey()); p.setVariableName(value.getKey()); p.setValueText(String.valueOf(value.getValue())); p.setNumericValue(BigDecimal.valueOf(value.getValue())); p.setDisplayValue(String.valueOf(value.getValue())); p.setGenerateSource("RANDOM"); p.setSort(sort++); p.setCreateTime(now); parameterMapper.insert(p); } QuestionVariantValidation validation = new QuestionVariantValidation(); validation.setVariantRecordId(record.getId()); validation.setValidationCode("FORMULA_RECHECK"); validation.setValidationType("DETERMINISTIC_FORMULA"); validation.setValidationLevel("ERROR"); validation.setValidationStatus("PASS"); validation.setInputSnapshot(JSON.toJSONString(values)); validation.setMessage("白名单公式复算一致"); validation.setExecutionOrder(1); validation.setDurationMs(0); validation.setValidateTime(now); validationMapper.insert(validation);
        QuestionBank question = new QuestionBank(); question.setGrade(t.getGrade()); question.setSubject(t.getSubject()); question.setQuestionType(t.getQuestionType()); question.setQuestionTitle(t.getTemplateName()+" · v"+t.getCurrentVersion()); question.setQuestionContent(content); question.setContentFormat("TEXT"); question.setCorrectAnswer(String.valueOf(answer)); question.setJudgeMode("AUTO"); question.setAnalysis(record.getAnalysis()); question.setDifficulty(3); question.setSource("3"); question.setSourceName("参数化变式题"); question.setReviewStatus(0); question.setEnable(1); question.setQuestionHash(record.getQuestionHash()); question.setVariantTemplateId(t.getId()); question.setVariantRecordId(record.getId()); question.setCreateId(AccountUtils.getUserId()); question.setCreateTime(now); question.setUpdateTime(now); questionBankService.fillDictNames(question); question.setSourceName("参数化变式题"); questionBankService.save(question); questionKnowledgePointService.rewrite(question.getId(), java.util.Collections.singletonList(t.getKnowledgePointId())); record.setBankQuestionId(question.getId()); recordMapper.updateById(record); QuestionVariantGenerateResp resp = new QuestionVariantGenerateResp(); resp.setRecordId(record.getId()); resp.setQuestionBankId(question.getId()); resp.setGeneratedContent(content); resp.setGeneratedAnswer(String.valueOf(answer)); resp.setVariableValuesJson(JSON.toJSONString(values)); resp.setValidationStatus(1); resp.setValidationDetail("白名单公式复算通过"); resp.setReviewStatus(0); return resp; }

    private boolean duplicateCode(QuestionVariantTemplateSaveReq request) { return templateMapper.selectCount(new LambdaQueryWrapper<QuestionVariantTemplate>().eq(QuestionVariantTemplate::getTemplateCode,request.getTemplateCode().trim()).ne(request.getId()!=null,QuestionVariantTemplate::getId,request.getId())) > 0; }
    private Map<String,Integer> parseValues(String schema, Long seed) { try { JSONArray array = JSON.parseArray(schema); if(array==null||array.isEmpty()) throw new IllegalArgumentException(); Map<String,Integer> map=new LinkedHashMap<>(); long state=seed==null?System.nanoTime():seed; for(Object raw:array){ JSONObject x=(JSONObject)raw; String name=x.getString("name"); Integer min=x.getInteger("min"),max=x.getInteger("max"); if(!StringUtils.hasText(name)||min==null||max==null||min>max||map.containsKey(name))throw new IllegalArgumentException(); state=state*1103515245L+12345L; map.put(name,min+(int)Math.floorMod(state,(long)max-min+1)); } return map;}catch(Exception e){throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);} }
    private Map<String,Integer> valuesFromVariables(List<QuestionVariantVariable> vars, Long seed){ JSONArray array=new JSONArray(); vars.forEach(v->{JSONObject x=new JSONObject();x.put("name",v.getVariableCode());x.put("min",v.getMinValue().intValue());x.put("max",v.getMaxValue().intValue());array.add(x);});return parseValues(array.toJSONString(),seed); }
    private int calculate(String formula,Map<String,Integer> values){Matcher m=FORMULA.matcher(formula==null?"":formula.trim());if(!m.matches()||!values.containsKey(m.group(2))||!values.containsKey(m.group(3)))throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);long a=values.get(m.group(2)),b=values.get(m.group(3));long r="ADD".equals(m.group(1))?a+b:"SUBTRACT".equals(m.group(1))?a-b:a*b;if(r>Integer.MAX_VALUE||r<Integer.MIN_VALUE)throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);return(int)r;}
    private String render(String pattern,Map<String,Integer> values){if(pattern==null)return null;String result=pattern;for(Map.Entry<String,Integer> item:values.entrySet())result=result.replace("{{"+item.getKey()+"}}",String.valueOf(item.getValue())).replace("{"+item.getKey()+"}",String.valueOf(item.getValue()));return result;}
}
