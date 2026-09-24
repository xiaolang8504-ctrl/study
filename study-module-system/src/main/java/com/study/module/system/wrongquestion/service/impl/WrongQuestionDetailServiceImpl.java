package com.study.module.system.wrongquestion.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionDetailResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionCorrectionRecordService;
import com.study.module.system.wrongquestion.service.WrongQuestionDetailService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.study.module.system.wrongquestion.service.WrongQuestionAssetService;
import com.study.module.system.wrongquestion.service.WrongQuestionCorrectionDraftService;
import com.study.module.system.wrongquestion.service.WrongQuestionDuplicateService;
import com.study.module.system.wrongquestion.service.WrongQuestionOrganizeService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.api.provider.FileProvider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 初中生错题详情服务
 */
@Service
public class WrongQuestionDetailServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionDetailService {

    private static final Pattern WRONG_QUESTION_FILE_SRC_PATTERN = Pattern.compile("(<img\\b[^>]*\\bsrc=[\"'])wrong-question-file://(\\d+)([\"'][^>]*>)", Pattern.CASE_INSENSITIVE);

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    KnowledgePointService knowledgePointService;

    @Autowired
    WrongQuestionCorrectionRecordService wrongQuestionCorrectionRecordService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    @Autowired
    WrongQuestionAssetService wrongQuestionAssetService;

    @Autowired
    WrongQuestionCorrectionDraftService wrongQuestionCorrectionDraftService;

    @Autowired
    WrongQuestionDuplicateService wrongQuestionDuplicateService;

    @Autowired
    WrongQuestionOrganizeService wrongQuestionOrganizeService;

    @DubboReference
    FileProvider fileProvider;

    /**
     * 错题详情
     */
    @Override
    public WrongQuestionDetailResp wrongQuestionDetail(Long id) {
        WrongQuestion wrongQuestion = wrongQuestionService.checkWrongQuestion(id);
        WrongQuestionDetailResp response = WrongQuestionConvert.INSTANCE.toWrongQuestionDetailResp(wrongQuestion);
        List<Long> pointIds = wrongQuestionKnowledgePointService.resolvePointIds(wrongQuestion);
        response.setKnowledgePointIds(pointIds);
        response.setKnowledgePointNames(pointIds.isEmpty() ? java.util.Collections.emptyList()
                : knowledgePointService.listByIds(pointIds).stream().map(item -> item.getPointName()).collect(Collectors.toList()));
        response.setTagNames(wrongQuestionOrganizeService.resolveTagNames(
                java.util.Collections.singletonList(id)).getOrDefault(id, java.util.Collections.emptyList()));
        response.setQuestionContent(resolveContentImageUrl(response.getQuestionContent(), wrongQuestion.getCreateId()));
        response.setWrongAnswer(resolveContentImageUrl(response.getWrongAnswer(), wrongQuestion.getCreateId()));
        response.setCorrectAnswer(resolveContentImageUrl(response.getCorrectAnswer(), wrongQuestion.getCreateId()));
        response.setWrongReason(resolveContentImageUrl(response.getWrongReason(), wrongQuestion.getCreateId()));
        response.setKeyHint(resolveContentImageUrl(response.getKeyHint(), wrongQuestion.getCreateId()));
        response.setSolutionSteps(resolveContentImageUrl(response.getSolutionSteps(), wrongQuestion.getCreateId()));
        response.setCommonMistake(resolveContentImageUrl(response.getCommonMistake(), wrongQuestion.getCreateId()));
        response.setAnalysis(resolveContentImageUrl(response.getAnalysis(), wrongQuestion.getCreateId()));
        response.setLatestCorrectionRecord(wrongQuestionCorrectionRecordService.latestCorrectionRecord(id));
        response.setCorrectionRecordList(wrongQuestionCorrectionRecordService.correctionRecordList(id));
        response.setTimelineList(wrongQuestionTimelineService.timelineList(id));
        response.setAssetList(wrongQuestionAssetService.wrongQuestionAssetList(id));
        response.setCorrectionDraft(wrongQuestionCorrectionDraftService.correctionDraftDetail(id));
        response.setOccurrenceList(wrongQuestionDuplicateService.wrongQuestionOccurrenceList(id));
        return response;
    }

    /**
     * 将导入时保存的稳定文件ID转换为当前可访问的下载地址
     */
    private String resolveContentImageUrl(String content, Long ownerUserId) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        Matcher matcher = WRONG_QUESTION_FILE_SRC_PATTERN.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String imageSrc = matcher.group(0);
            try {
                String downloadUrl = fileProvider.downloadUrlByUserId(Integer.valueOf(matcher.group(2)),
                        UploadType.WRONG_QUESTION, ownerUserId);
                imageSrc = matcher.group(1) + downloadUrl + matcher.group(3);
            } catch (Exception ignored) {
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(imageSrc));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
