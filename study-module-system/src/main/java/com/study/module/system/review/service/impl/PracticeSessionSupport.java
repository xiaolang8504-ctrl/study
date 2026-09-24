package com.study.module.system.review.service.impl;

import com.alibaba.fastjson.JSON;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionBankImage;
import com.study.module.system.review.dto.response.PracticeQuestionResp;
import com.study.module.system.review.dto.response.PracticeQuestionImageResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.constants.PracticeQuestionSource;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专项练习响应组装工具
 */
final class PracticeSessionSupport {

    private static final Set<String> AUTO_JUDGE_QUESTION_TYPES = new HashSet<>(Arrays.asList(
            "选择题", "判断题", "填空题", "单选题", "多选题"));

    private PracticeSessionSupport() {
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions) {
        return buildDetail(session, sessionQuestions, wrongQuestions, Collections.emptyList());
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions,
                                                 List<QuestionBank> bankQuestions) {
        return buildDetail(session, sessionQuestions, wrongQuestions, bankQuestions, false);
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions,
                                                 List<QuestionBank> bankQuestions,
                                                 boolean includeAnswers) {
        return buildDetail(session, sessionQuestions, wrongQuestions, bankQuestions,
                Collections.emptyList(), Collections.emptyList(), includeAnswers);
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions,
                                                 List<QuestionBank> bankQuestions,
                                                 List<QuestionCapturePage> capturePages,
                                                 List<QuestionBankImage> bankImages,
                                                 boolean includeAnswers) {
        Map<Long, WrongQuestion> questionMap = wrongQuestions.stream()
                .collect(Collectors.toMap(WrongQuestion::getId, question -> question, (left, right) -> left));
        Map<Long, QuestionBank> bankQuestionMap = bankQuestions.stream()
                .collect(Collectors.toMap(QuestionBank::getId, question -> question, (left, right) -> left));
        Map<Long, QuestionCapturePage> capturePageMap = capturePages.stream()
                .collect(Collectors.toMap(QuestionCapturePage::getId, page -> page, (left, right) -> left));
        Map<Long, List<QuestionBankImage>> bankImageMap = bankImages.stream()
                .sorted(Comparator.comparing(QuestionBankImage::getSort,
                        Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.groupingBy(QuestionBankImage::getQuestionId));
        PracticeSessionDetailResp response = new PracticeSessionDetailResp();
        response.setSessionId(session.getId());
        response.setTitle(session.getTitle());
        response.setGenerationReason(session.getGenerationReason());
        response.setBlankLineCount(session.getBlankLineCount() == null ? 3 : session.getBlankLineCount());
        response.setAnswerPosition(StringUtils.hasText(session.getAnswerPosition())
                ? session.getAnswerPosition() : "END");
        response.setImageMode(StringUtils.hasText(session.getImageMode())
                ? session.getImageMode() : "ORIGINAL");
        response.setPaperVersion(session.getPaperVersion() == null ? 1 : session.getPaperVersion());
        response.setColumnCount(session.getColumnCount() == null ? 1 : session.getColumnCount());
        response.setPracticeType(session.getPracticeType());
        response.setQuestionCount(session.getQuestionCount());
        response.setAnsweredCount(session.getAnsweredCount());
        response.setCorrectCount(session.getCorrectCount());
        response.setWrongCount(session.getWrongCount());
        response.setAccuracyRate(session.getAccuracyRate());
        response.setStatus(session.getStatus());
        response.setQuestionList(sessionQuestions.stream()
                .map(item -> buildQuestion(item, questionMap.get(item.getWrongQuestionId()),
                        bankQuestionMap.get(item.getBankQuestionId()), capturePageMap,
                        bankImageMap, response.getImageMode(), includeAnswers))
                .collect(Collectors.toList()));
        return response;
    }

    private static PracticeQuestionResp buildQuestion(PracticeSessionQuestion sessionQuestion,
                                                      WrongQuestion question,
                                                      QuestionBank bankQuestion,
                                                      Map<Long, QuestionCapturePage> capturePageMap,
                                                      Map<Long, List<QuestionBankImage>> bankImageMap,
                                                      String imageMode,
                                                      boolean includeAnswers) {
        PracticeQuestionResp response = new PracticeQuestionResp();
        response.setSessionQuestionId(sessionQuestion.getId());
        response.setWrongQuestionId(sessionQuestion.getWrongQuestionId());
        response.setQuestionSource(sessionQuestion.getQuestionSource());
        response.setSourceReason(sessionQuestion.getSourceReason());
        response.setBankQuestionId(sessionQuestion.getBankQuestionId());
        response.setQuestionTitle(sessionQuestion.getQuestionTitleSnapshot());
        response.setQuestionContent(sessionQuestion.getQuestionContentSnapshot());
        response.setContentFormat(StringUtils.hasText(sessionQuestion.getContentFormatSnapshot())
                ? sessionQuestion.getContentFormatSnapshot() : "TEXT");
        response.setOptionsJson(sessionQuestion.getOptionsJsonSnapshot());
        if (sessionQuestion.getAssetSnapshotJson() != null) {
            response.setPaperImageList("TEXT_ONLY".equals(imageMode) ? Collections.emptyList()
                    : JSON.parseArray(sessionQuestion.getAssetSnapshotJson(), PracticeQuestionImageResp.class));
        }
        if (includeAnswers || sessionQuestion.getAnswerTime() != null) {
            response.setCorrectAnswer(sessionQuestion.getCorrectAnswerSnapshot());
            response.setAnalysis(sessionQuestion.getAnalysisSnapshot());
        }
        response.setSubjectName(sessionQuestion.getSubjectName());
        response.setLearningPoint(sessionQuestion.getLearningPoint());
        response.setErrorLabelSnapshot(sessionQuestion.getErrorLabelSnapshot());
        response.setDifficulty(sessionQuestion.getDifficulty());
        response.setAnswered(sessionQuestion.getAnswerTime() != null);
        response.setStudentAnswer(sessionQuestion.getStudentAnswer());
        response.setIsCorrect(sessionQuestion.getIsCorrect());
        response.setDurationSeconds(sessionQuestion.getDurationSeconds());
        response.setAnswerTime(sessionQuestion.getAnswerTime());
        if (question != null) {
            if (!StringUtils.hasText(response.getQuestionContent())) {
                response.setQuestionContent(question.getQuestionContent());
            }
            response.setQuestionTypeName(question.getQuestionTypeName());
            if (!StringUtils.hasText(sessionQuestion.getContentFormatSnapshot())) {
                response.setContentFormat(StringUtils.hasText(question.getContentFormat())
                        ? question.getContentFormat() : "TEXT");
                response.setOptionsJson(question.getOptionsJson());
            }
            response.setImageUrl(question.getImageUrl());
            response.setImageUrl2(question.getImageUrl2());
            response.setImageUrl3(question.getImageUrl3());
            response.setImageUrl4(question.getImageUrl4());
            if (response.getPaperImageList() == null) {
                response.setPaperImageList(buildWrongQuestionImages(question,
                        capturePageMap.get(question.getCapturePageId()), imageMode));
            }
            if (includeAnswers || sessionQuestion.getAnswerTime() != null) {
                if (!StringUtils.hasText(response.getCorrectAnswer())) {
                    response.setCorrectAnswer(question.getCorrectAnswer());
                }
                if (!StringUtils.hasText(response.getAnalysis())) {
                    response.setAnalysis(question.getAnalysis());
                }
            }
            if (!StringUtils.hasText(response.getQuestionTitle())) {
                response.setQuestionTitle(question.getQuestionTitle());
            }
            response.setJudgeMode(isAutoJudge(question.getQuestionTypeName(), null) ? "AUTO" : "SELF");
            response.setAutoJudge(isAutoJudge(question.getQuestionTypeName(), null));
        }
        if (bankQuestion != null) {
            if (!StringUtils.hasText(response.getQuestionContent())) {
                response.setQuestionContent(bankQuestion.getQuestionContent());
            }
            response.setQuestionTypeName(bankQuestion.getQuestionTypeName());
            response.setImageUrls(bankQuestion.getImageUrls());
            if (!StringUtils.hasText(sessionQuestion.getContentFormatSnapshot())) {
                response.setContentFormat(bankQuestion.getContentFormat());
                response.setOptionsJson(bankQuestion.getOptionsJson());
            }
            response.setJudgeMode(bankQuestion.getJudgeMode());
            fillBankImages(response, bankQuestion.getImageUrls());
            if (response.getPaperImageList() == null) {
                response.setPaperImageList(buildBankQuestionImages(bankQuestion,
                        bankImageMap.get(bankQuestion.getId()), imageMode));
            }
            if (includeAnswers || sessionQuestion.getAnswerTime() != null) {
                if (!StringUtils.hasText(response.getCorrectAnswer())) {
                    response.setCorrectAnswer(bankQuestion.getCorrectAnswer());
                }
                if (!StringUtils.hasText(response.getAnalysis())) {
                    response.setAnalysis(bankQuestion.getAnalysis());
                }
            }
            if (!StringUtils.hasText(response.getQuestionTitle())) {
                response.setQuestionTitle(bankQuestion.getQuestionTitle());
            }
            response.setAutoJudge(isAutoJudge(bankQuestion.getQuestionTypeName(), bankQuestion.getJudgeMode()));
        }
        if (!StringUtils.hasText(response.getQuestionSource())) {
            response.setQuestionSource(response.getBankQuestionId() == null
                    ? PracticeQuestionSource.WRONG_QUESTION : PracticeQuestionSource.QUESTION_BANK);
        }
        return response;
    }

    private static List<PracticeQuestionImageResp> buildWrongQuestionImages(
            WrongQuestion question, QuestionCapturePage capturePage, String imageMode) {
        if ("TEXT_ONLY".equals(imageMode)) {
            return Collections.emptyList();
        }
        if (capturePage != null && capturePage.getImageFileId() != null) {
            boolean useGrayscale = "GRAYSCALE".equals(imageMode)
                    && Integer.valueOf(2).equals(capturePage.getCleanStatus())
                    && capturePage.getCleanedFileId() != null;
            Long selectedFileId = useGrayscale
                    ? capturePage.getCleanedFileId() : capturePage.getImageFileId();
            PracticeQuestionImageResp image = fileImage(Math.toIntExact(selectedFileId),
                    "wrongQuestion", useGrayscale ? "GRAYSCALE" : "ORIGINAL");
            image.setLeftPosition(question.getCaptureLeftPosition());
            image.setTopPosition(question.getCaptureTopPosition());
            image.setWidth(question.getCaptureWidth());
            image.setHeight(question.getCaptureHeight());
            return Collections.singletonList(image);
        }
        List<PracticeQuestionImageResp> images = new ArrayList<>();
        addLegacyImage(images, question.getImageUrl(), "wrongQuestion");
        addLegacyImage(images, question.getImageUrl2(), "wrongQuestion");
        addLegacyImage(images, question.getImageUrl3(), "wrongQuestion");
        addLegacyImage(images, question.getImageUrl4(), "wrongQuestion");
        return images;
    }

    private static List<PracticeQuestionImageResp> buildBankQuestionImages(
            QuestionBank question, List<QuestionBankImage> storedImages, String imageMode) {
        if ("TEXT_ONLY".equals(imageMode)) {
            return Collections.emptyList();
        }
        List<PracticeQuestionImageResp> images = new ArrayList<>();
        if (storedImages != null && !storedImages.isEmpty()) {
            for (QuestionBankImage storedImage : storedImages) {
                PracticeQuestionImageResp image = storedImage.getFileId() == null
                        ? urlImage(storedImage.getImageUrl(), "QUESTION")
                        : fileImage(storedImage.getFileId(), "questionBank", "QUESTION");
                if (image != null) {
                    images.add(image);
                }
            }
            return images;
        }
        if (StringUtils.hasText(question.getImageUrls())) {
            for (String imageUrl : question.getImageUrls().split(",")) {
                addLegacyImage(images, imageUrl, "questionBank");
            }
        }
        return images;
    }

    private static void addLegacyImage(List<PracticeQuestionImageResp> images,
                                       String value, String uploadType) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        String normalized = value.trim();
        Integer fileId = parseFileId(normalized);
        PracticeQuestionImageResp image = fileId == null
                ? urlImage(normalized, "QUESTION")
                : fileImage(fileId, uploadType, "QUESTION");
        if (image != null) {
            images.add(image);
        }
    }

    private static Integer parseFileId(String value) {
        String normalized = value;
        int protocolSeparator = normalized.lastIndexOf('/');
        if (normalized.startsWith("file://") && protocolSeparator >= 0) {
            normalized = normalized.substring(protocolSeparator + 1);
        }
        try {
            int fileId = Integer.parseInt(normalized);
            return fileId > 0 ? fileId : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static PracticeQuestionImageResp fileImage(Integer fileId, String uploadType,
                                                        String imageType) {
        PracticeQuestionImageResp image = new PracticeQuestionImageResp();
        image.setFileId(fileId);
        image.setUploadType(uploadType);
        image.setImageType(imageType);
        return image;
    }

    private static PracticeQuestionImageResp urlImage(String imageUrl, String imageType) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        PracticeQuestionImageResp image = new PracticeQuestionImageResp();
        image.setImageUrl(imageUrl.trim());
        image.setImageType(imageType);
        return image;
    }

    private static boolean isAutoJudge(String questionTypeName, String judgeMode) {
        return "AUTO".equals(judgeMode) || AUTO_JUDGE_QUESTION_TYPES.contains(questionTypeName);
    }

    /**
     * 更新业务数据。
     */
    private static void fillBankImages(PracticeQuestionResp response, String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return;
        }
        String[] images = imageUrls.split(",");
        if (images.length > 0) {
            response.setImageUrl(images[0].trim());
        }
        if (images.length > 1) {
            response.setImageUrl2(images[1].trim());
        }
        if (images.length > 2) {
            response.setImageUrl3(images[2].trim());
        }
        if (images.length > 3) {
            response.setImageUrl4(images[3].trim());
        }
    }
}
