package com.study.module.system.wrongquestion.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.study.module.system.wrongquestion.config.WrongQuestionOcrConfig;
import com.study.module.system.wrongquestion.domain.WrongQuestionOcrData;
import com.study.module.system.wrongquestion.service.WrongQuestionImageOcrService;
import com.study.api.dto.response.FileData;
import com.study.api.provider.FileProvider;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 飞桨Studio A4错题图片识别服务
 */
@Service
@Slf4j
public class WrongQuestionImageOcrServiceImpl implements WrongQuestionImageOcrService {

    private static final int MAX_ERROR_RESPONSE_LENGTH = 4000;
    private static final Pattern QUESTION_START_PATTERN = Pattern.compile("^\\s*\\d{1,2}[\\.、）)]\\s*.*");
    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^\\s*#{1,6}\\s*");
    private static final Pattern CHINESE_SECTION_PREFIX_PATTERN = Pattern.compile("^\\s*[一二三四五六七八九十]+[、.．)]\\s*");
    private static final Pattern QUESTION_TYPE_LABEL_PATTERN = Pattern.compile("^\\s*(题型|类型)\\s*[:：]\\s*");
    private static final Pattern CHOICE_OPTION_PATTERN = Pattern.compile("(?m)(^|\\s)([A-D])[\\.．、）):：]\\s*");
    private static final Pattern HTML_IMAGE_SRC_PATTERN = Pattern.compile("(<img\\b[^>]*\\bsrc=[\"'])([^\"']+)([\"'][^>]*>)", Pattern.CASE_INSENSITIVE);
    private static final List<String> QUESTION_TYPE_LIST = Arrays.asList(
            "选择题", "填空题", "判断题", "计算题", "应用题", "证明题", "阅读理解", "作文", "实验题", "其他"
    );
    private static final String LINE_SEPARATOR = "\n";

    @Autowired
    WrongQuestionOcrConfig wrongQuestionOcrConfig;

    @DubboReference
    FileProvider fileProvider;

    /**
     * 识别A4图片中的错题
     */
    @Override
    public List<WrongQuestionOcrData> recognize(Long imageFileId, Long userId) {
        if (!BooleanUtil.isTrue(wrongQuestionOcrConfig.getEnabled()) || StrUtil.isBlank(wrongQuestionOcrConfig.getToken())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_NOT_CONFIGURED);
        }
        Integer fileId = Math.toIntExact(imageFileId);
        FileData fileData = fileProvider.getFile(wrongQuestionOcrConfig.getUploadType(), fileId);
        String imageUrl = fileProvider.downloadUrlByUserId(fileId, wrongQuestionOcrConfig.getUploadType(), userId);
        String content = recognizeFileText(imageUrl, buildFileName(fileData, imageFileId));
        if (StrUtil.isBlank(content)) {
            return Collections.emptyList();
        }
        return splitQuestionContent(content);
    }

    /**
     * 识别图片版面，并保留 OCR 服务返回的题块坐标与检测置信度。
     * 坐标会归一化为 0-10000，供采集页在不同尺寸的原图、净化图上复用。
     */
    @Override
    public List<WrongQuestionOcrData> recognizeLayout(Long imageFileId, Long userId) {
        if (!BooleanUtil.isTrue(wrongQuestionOcrConfig.getEnabled()) || StrUtil.isBlank(wrongQuestionOcrConfig.getToken())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_NOT_CONFIGURED);
        }
        Integer fileId = Math.toIntExact(imageFileId);
        FileData fileData = fileProvider.getFile(wrongQuestionOcrConfig.getUploadType(), fileId);
        String imageUrl = fileProvider.downloadUrlByUserId(fileId, wrongQuestionOcrConfig.getUploadType(), userId);
        return recognizeFileLayout(imageUrl, buildFileName(fileData, imageFileId));
    }

    /**
     * 调用飞桨Studio OCR识别文件文本
     */
    private String recognizeFileText(String fileUrl, String fileName) {
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            byte[] fileBytes = IoUtil.readBytes(inputStream);
            // OCR 服务采用异步任务协议：先上传原图，再轮询任务，最后下载识别结果。
            String jobId = submitOcrJob(fileBytes, fileName);
            String jsonlUrl = waitOcrJobDone(jobId);
            return fetchMarkdownText(jsonlUrl);
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("飞桨OCR识别异常，fileName={}", fileName, e);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
    }

    /**
     * 调用同一 OCR 任务链路，并从结果中解析版面块。
     */
    private List<WrongQuestionOcrData> recognizeFileLayout(String fileUrl, String fileName) {
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            byte[] fileBytes = IoUtil.readBytes(inputStream);
            String jobId = submitOcrJob(fileBytes, fileName);
            String jsonlUrl = waitOcrJobDone(jobId);
            return fetchLayoutData(jsonlUrl);
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("飞桨OCR版面识别异常，fileName={}", fileName, e);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
    }

    /**
     * 提交OCR任务
     */
    private String submitOcrJob(byte[] fileBytes, String fileName) throws IOException {
        String boundary = "----StudyWrongQuestionOcr" + System.currentTimeMillis();
        HttpURLConnection connection = openConnection(wrongQuestionOcrConfig.getJobUrl(), "POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            writeFormField(outputStream, boundary, "model", wrongQuestionOcrConfig.getModel());
            writeFormField(outputStream, boundary, "optionalPayload", buildOptionalPayload().toJSONString());
            writeFileField(outputStream, boundary, "file", fileName, fileBytes);
            outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        }
        HttpResponse response = readResponse(connection);
        if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
            logOcrErrorResponse("提交任务", null, response);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
        JSONObject responseJson = parseOcrResponse("解析任务提交结果", null, response);
        JSONObject data = responseJson.getJSONObject("data");
        if (data == null || StrUtil.isBlank(data.getString("jobId"))) {
            logOcrErrorResponse("任务提交结果缺少jobId", null, response);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
        return data.getString("jobId");
    }

    /**
     * 轮询OCR任务直到完成
     */
    private String waitOcrJobDone(String jobId) throws IOException, InterruptedException {
        HttpResponse lastResponse = null;
        for (int i = 0; i < wrongQuestionOcrConfig.getMaxPollTimes(); i++) {
            HttpURLConnection connection = openConnection(wrongQuestionOcrConfig.getJobUrl() + "/" + jobId, "GET");
            HttpResponse response = readResponse(connection);
            lastResponse = response;
            if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
                logOcrErrorResponse("轮询任务", jobId, response);
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
            }
            JSONObject responseJson = parseOcrResponse("解析任务轮询结果", jobId, response);
            JSONObject data = responseJson.getJSONObject("data");
            if (data == null) {
                logOcrErrorResponse("任务轮询结果缺少data", jobId, response);
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
            }
            String state = data.getString("state");
            if ("done".equals(state)) {
                JSONObject resultUrl = data.getJSONObject("resultUrl");
                if (resultUrl == null || StrUtil.isBlank(resultUrl.getString("jsonUrl"))) {
                    logOcrErrorResponse("任务完成结果缺少jsonUrl", jobId, response);
                    throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
                }
                return resultUrl.getString("jsonUrl");
            }
            if ("failed".equals(state)) {
                logOcrErrorResponse("任务执行失败", jobId, response);
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
            }
            // 非终态按配置间隔等待，限制轮询频率，避免压垮外部 OCR 服务。
            Thread.sleep(wrongQuestionOcrConfig.getPollInterval());
        }
        if (lastResponse != null) {
            logOcrErrorResponse("任务轮询超时", jobId, lastResponse);
        }
        log.error("飞桨OCR任务轮询超时，jobId={}, maxPollTimes={}, pollInterval={}",
                jobId, wrongQuestionOcrConfig.getMaxPollTimes(), wrongQuestionOcrConfig.getPollInterval());
        throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
    }

    /**
     * 下载jsonl结果并提取Markdown文本
     */
    private String fetchMarkdownText(String jsonlUrl) throws IOException {
        HttpURLConnection connection = openConnection(jsonlUrl, "GET", false);
        HttpResponse response = readResponse(connection);
        if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
            logOcrErrorResponse("下载OCR结果", null, response);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
        List<String> markdownTextList = new ArrayList<>();
        // 返回文件为 JSON Lines，每行都是独立结果，单行格式异常应终止本次导入。
        String[] lines = response.getBody().split("\\R");
        for (String line : lines) {
            if (StrUtil.isBlank(line)) {
                continue;
            }
            JSONObject lineJson;
            try {
                lineJson = JSON.parseObject(line);
            } catch (Exception e) {
                log.error("飞桨OCR结果行解析失败，responseBody={}", formatResponseBody(line), e);
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
            }
            JSONObject result = lineJson.getJSONObject("result");
            if (result == null) {
                continue;
            }
            JSONArray layoutParsingResults = result.getJSONArray("layoutParsingResults");
            collectMarkdownText(layoutParsingResults, markdownTextList, jsonlUrl);
        }
        return String.join(LINE_SEPARATOR, markdownTextList);
    }

    /**
     * 读取 JSONL 中的版面块；旧模型结果没有版面字段时退回 Markdown 拆题，避免采集任务无可确认内容。
     */
    private List<WrongQuestionOcrData> fetchLayoutData(String jsonlUrl) throws IOException {
        HttpURLConnection connection = openConnection(jsonlUrl, "GET", false);
        HttpResponse response = readResponse(connection);
        if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
            logOcrErrorResponse("下载OCR版面结果", null, response);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
        List<WrongQuestionOcrData> dataList = new ArrayList<>();
        List<String> markdownTextList = new ArrayList<>();
        for (String line : response.getBody().split("\\R")) {
            if (StrUtil.isBlank(line)) {
                continue;
            }
            JSONObject lineJson;
            try {
                lineJson = JSON.parseObject(line);
            } catch (Exception e) {
                log.error("飞桨OCR版面结果行解析失败，responseBody={}", formatResponseBody(line), e);
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
            }
            JSONObject result = lineJson.getJSONObject("result");
            if (result == null) {
                continue;
            }
            JSONArray layoutParsingResults = result.getJSONArray("layoutParsingResults");
            collectLayoutData(layoutParsingResults, dataList);
            collectMarkdownText(layoutParsingResults, markdownTextList, jsonlUrl);
        }
        if (CollUtil.isNotEmpty(dataList)) {
            return dataList;
        }
        String markdownText = String.join(LINE_SEPARATOR, markdownTextList);
        return StrUtil.isBlank(markdownText) ? Collections.emptyList() : splitQuestionContent(markdownText);
    }

    /**
     * 将 OCR 版面块转换为采集题块。
     */
    private void collectLayoutData(JSONArray layoutParsingResults, List<WrongQuestionOcrData> dataList) {
        if (layoutParsingResults == null) {
            return;
        }
        for (int i = 0; i < layoutParsingResults.size(); i++) {
            JSONObject layoutResult = layoutParsingResults.getJSONObject(i);
            JSONObject prunedResult = layoutResult.getJSONObject("prunedResult");
            if (prunedResult == null) {
                continue;
            }
            int pageWidth = Math.max(prunedResult.getIntValue("width"), 1);
            int pageHeight = Math.max(prunedResult.getIntValue("height"), 1);
            JSONArray detectionBoxes = getDetectionBoxes(prunedResult, layoutResult);
            JSONArray parsingList = prunedResult.getJSONArray("parsing_res_list");
            if (parsingList == null) {
                continue;
            }
            for (int j = 0; j < parsingList.size(); j++) {
                JSONObject block = parsingList.getJSONObject(j);
                String content = StrUtil.trim(block.getString("block_content"));
                int[] box = readBox(block.get("block_bbox"));
                if (StrUtil.isBlank(content) || box == null) {
                    continue;
                }
                WrongQuestionOcrData data = buildOcrData(content, resolveQuestionTypeLine(content));
                data.setLeftPosition(normalizeCoordinate(box[0], pageWidth));
                data.setTopPosition(normalizeCoordinate(box[1], pageHeight));
                data.setWidth(normalizeLength(box[2] - box[0], pageWidth));
                data.setHeight(normalizeLength(box[3] - box[1], pageHeight));
                data.setConfidence(resolveConfidence(box, detectionBoxes));
                dataList.add(data);
            }
        }
    }

    /**
     * 兼容不同 OCR 版本将检测框放在 prunedResult 或 layoutParsingResult 的差异。
     */
    private JSONArray getDetectionBoxes(JSONObject prunedResult, JSONObject layoutResult) {
        JSONObject layoutDetection = prunedResult.getJSONObject("layout_det_res");
        if (layoutDetection == null) {
            layoutDetection = layoutResult.getJSONObject("layout_det_res");
        }
        return layoutDetection == null ? null : layoutDetection.getJSONArray("boxes");
    }

    /**
     * 读取 [left, top, right, bottom] 形式的 OCR 框，并规整为合法边界。
     */
    private int[] readBox(Object rawBox) {
        JSONArray values = rawBox instanceof JSONArray ? (JSONArray) rawBox : JSON.parseArray(JSON.toJSONString(rawBox));
        if (values == null || values.size() < 4) {
            return null;
        }
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = Integer.MIN_VALUE, bottom = Integer.MIN_VALUE;
        for (int i = 0; i + 1 < values.size(); i += 2) {
            Number x = values.getObject(i, Number.class);
            Number y = values.getObject(i + 1, Number.class);
            if (x == null || y == null) {
                return null;
            }
            left = Math.min(left, Math.round(x.floatValue()));
            top = Math.min(top, Math.round(y.floatValue()));
            right = Math.max(right, Math.round(x.floatValue()));
            bottom = Math.max(bottom, Math.round(y.floatValue()));
        }
        return right > left && bottom > top ? new int[]{left, top, right, bottom} : null;
    }

    /**
     * 采用 IoU 最大的版面检测框置信度；没有匹配检测框时使用中性置信度。
     */
    private int resolveConfidence(int[] blockBox, JSONArray detectionBoxes) {
        float highestIou = 0F;
        float confidence = 0.5F;
        if (detectionBoxes != null) {
            for (int i = 0; i < detectionBoxes.size(); i++) {
                JSONObject detection = detectionBoxes.getJSONObject(i);
                int[] detectionBox = readBox(detection.get("coordinate"));
                if (detectionBox == null) {
                    continue;
                }
                float iou = calculateIou(blockBox, detectionBox);
                if (iou > highestIou) {
                    highestIou = iou;
                    confidence = detection.getFloatValue("score");
                }
            }
        }
        return Math.max(0, Math.min(100, Math.round(confidence * 100)));
    }

    /**
     * 执行 calculateIou 辅助处理。
     */
    private float calculateIou(int[] first, int[] second) {
        int left = Math.max(first[0], second[0]);
        int top = Math.max(first[1], second[1]);
        int right = Math.min(first[2], second[2]);
        int bottom = Math.min(first[3], second[3]);
        int intersection = Math.max(0, right - left) * Math.max(0, bottom - top);
        int firstArea = (first[2] - first[0]) * (first[3] - first[1]);
        int secondArea = (second[2] - second[0]) * (second[3] - second[1]);
        int union = firstArea + secondArea - intersection;
        return union == 0 ? 0F : (float) intersection / union;
    }

    /**
     * 标准化并计算业务数据。
     */
    private int normalizeCoordinate(int value, int total) {
        return Math.max(0, Math.min(10000, Math.round(value * 10000F / total)));
    }

    /**
     * 标准化并计算业务数据。
     */
    private int normalizeLength(int value, int total) {
        return Math.max(1, Math.min(10000, Math.round(Math.max(1, value) * 10000F / total)));
    }

    /**
     * 解析飞桨OCR响应，并在响应格式异常时记录原始内容
     */
    private JSONObject parseOcrResponse(String stage, String jobId, HttpResponse response) {
        try {
            JSONObject responseJson = JSON.parseObject(response.getBody());
            if (responseJson == null) {
                logOcrErrorResponse(stage + "响应为空", jobId, response);
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
            }
            return responseJson;
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("飞桨OCR{}响应解析失败，jobId={}, statusCode={}, responseBody={}",
                    stage, jobId, response.getStatusCode(), formatResponseBody(response.getBody()), e);
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_OCR_INVOKE_FAIL);
        }
    }

    /**
     * 记录飞桨OCR异常响应
     */
    private void logOcrErrorResponse(String stage, String jobId, HttpResponse response) {
        log.error("飞桨OCR{}失败，jobId={}, statusCode={}, responseBody={}",
                stage, jobId, response.getStatusCode(), formatResponseBody(response.getBody()));
    }

    /**
     * 限制响应日志长度并转义换行，避免污染日志格式
     */
    private String formatResponseBody(String responseBody) {
        String normalizedBody = StrUtil.nullToEmpty(responseBody)
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return StrUtil.maxLength(normalizedBody, MAX_ERROR_RESPONSE_LENGTH);
    }

    /**
     * 提取Markdown文本
     */
    private void collectMarkdownText(JSONArray layoutParsingResults, List<String> markdownTextList, String jsonlUrl) {
        if (layoutParsingResults == null) {
            return;
        }
        for (int i = 0; i < layoutParsingResults.size(); i++) {
            JSONObject layoutParsingResult = layoutParsingResults.getJSONObject(i);
            JSONObject markdown = layoutParsingResult.getJSONObject("markdown");
            if (markdown == null || StrUtil.isBlank(markdown.getString("text"))) {
                continue;
            }
            markdownTextList.add(resolveMarkdownImageSrc(markdown.getString("text"), jsonlUrl));
        }
    }

    /**
     * 将OCR Markdown里的相对图片地址转换为可访问的绝对地址
     */
    private String resolveMarkdownImageSrc(String markdownText, String jsonlUrl) {
        Matcher matcher = HTML_IMAGE_SRC_PATTERN.matcher(markdownText);
        StringBuffer buffer = new StringBuffer();
        URI baseUri = URI.create(jsonlUrl);
        while (matcher.find()) {
            String imageSrc = matcher.group(2);
            String resolvedImageSrc = imageSrc;
            if (StrUtil.isNotBlank(imageSrc) && !isAbsoluteImageSrc(imageSrc)) {
                resolvedImageSrc = baseUri.resolve(imageSrc).toString();
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(1) + resolvedImageSrc + matcher.group(3)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 判断图片地址是否已经是绝对地址
     */
    private boolean isAbsoluteImageSrc(String imageSrc) {
        return imageSrc.startsWith("/")
                || imageSrc.startsWith("data:")
                || imageSrc.startsWith("http://")
                || imageSrc.startsWith("https://")
                || imageSrc.startsWith("//");
    }

    /**
     * OCR可选参数
     */
    private JSONObject buildOptionalPayload() {
        Map<String, Object> optionalPayload = new HashMap<>();
        optionalPayload.put("useDocOrientationClassify", BooleanUtil.isTrue(wrongQuestionOcrConfig.getUseDocOrientationClassify()));
        optionalPayload.put("useDocUnwarping", BooleanUtil.isTrue(wrongQuestionOcrConfig.getUseDocUnwarping()));
        optionalPayload.put("useChartRecognition", BooleanUtil.isTrue(wrongQuestionOcrConfig.getUseChartRecognition()));
        return new JSONObject(optionalPayload);
    }

    /**
     * 获取OCR提交文件名，保留原始后缀便于服务端判断文件类型
     */
    private String buildFileName(FileData fileData, Long fileId) {
        if (fileData != null && StrUtil.isNotBlank(fileData.getOriginName())) {
            return fileData.getOriginName();
        }
        if (fileData != null && StrUtil.isNotBlank(fileData.getFileExtension())) {
            return "wrong-question-" + fileId + "." + fileData.getFileExtension();
        }
        return "wrong-question-" + fileId;
    }

    /**
     * 打开HTTP连接
     */
    private HttpURLConnection openConnection(String url, String method) throws IOException {
        return openConnection(url, method, true);
    }

    /**
     * 打开HTTP连接
     */
    private HttpURLConnection openConnection(String url, String method, boolean authorization) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(wrongQuestionOcrConfig.getConnectTimeout());
        connection.setReadTimeout(wrongQuestionOcrConfig.getReadTimeout());
        if (authorization) {
            connection.setRequestProperty("Authorization", "bearer " + wrongQuestionOcrConfig.getToken());
        }
        return connection;
    }

    /**
     * 写普通表单字段
     */
    private void writeFormField(OutputStream outputStream, String boundary, String name, String value) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 写文件字段
     */
    private void writeFileField(OutputStream outputStream, String boundary, String name, String fileName, byte[] fileBytes) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.write(fileBytes);
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 读取HTTP响应
     */
    private HttpResponse readResponse(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        InputStream inputStream = statusCode >= HttpURLConnection.HTTP_BAD_REQUEST
                ? connection.getErrorStream()
                : connection.getInputStream();
        String responseBody = "";
        if (inputStream != null) {
            try (InputStream currentInputStream = inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                IoUtil.copy(currentInputStream, outputStream);
                responseBody = outputStream.toString(StandardCharsets.UTF_8.name());
            }
        }
        return new HttpResponse(statusCode, responseBody);
    }

    /**
     * 按题号拆分，并根据题型标题维护当前题目类型
     */
    private List<WrongQuestionOcrData> splitQuestionContent(String content) {
        String normalizedContent = StrUtil.trim(content);
        List<WrongQuestionOcrData> ocrDataList = new ArrayList<>();
        String currentQuestionType = "其他";
        StringBuilder questionBlock = new StringBuilder();
        String questionBlockType = currentQuestionType;
        String[] lines = normalizedContent.split("\\R");
        for (String line : lines) {
            String questionType = resolveQuestionTypeLine(line);
            if (StrUtil.isNotBlank(questionType)) {
                if (StrUtil.isNotBlank(questionBlock)) {
                    ocrDataList.add(buildOcrData(questionBlock.toString(), questionBlockType));
                    questionBlock.setLength(0);
                }
                currentQuestionType = questionType;
                continue;
            }
            if (QUESTION_START_PATTERN.matcher(line).matches() && StrUtil.isNotBlank(questionBlock)) {
                ocrDataList.add(buildOcrData(questionBlock.toString(), questionBlockType));
                questionBlock.setLength(0);
            }
            if (StrUtil.isBlank(questionBlock)) {
                questionBlockType = currentQuestionType;
            } else {
                questionBlock.append(LINE_SEPARATOR);
            }
            questionBlock.append(line);
        }
        if (StrUtil.isNotBlank(questionBlock)) {
            ocrDataList.add(buildOcrData(questionBlock.toString(), questionBlockType));
        }
        if (CollUtil.isEmpty(ocrDataList)) {
            ocrDataList.add(buildOcrData(normalizedContent, currentQuestionType));
        }
        return ocrDataList;
    }

    /**
     * 构造错题识别数据
     */
    private WrongQuestionOcrData buildOcrData(String questionContent, String questionType) {
        String content = StrUtil.trim(questionContent);
        WrongQuestionOcrData ocrData = new WrongQuestionOcrData();
        ocrData.setQuestionType(resolveQuestionBlockType(content, questionType));
        ocrData.setQuestionTitle(buildQuestionTitle(content));
        ocrData.setQuestionContent(content);
        return ocrData;
    }

    /**
     * 根据题块内容兜底识别题型，含A/B/C/D四个选项时按选择题处理
     */
    private String resolveQuestionBlockType(String questionContent, String questionType) {
        if (hasChoiceOptions(questionContent)) {
            return "选择题";
        }
        return StrUtil.blankToDefault(questionType, "其他");
    }

    /**
     * 识别A、B、C、D四个选项标记
     */
    private boolean hasChoiceOptions(String questionContent) {
        Set<String> optionSet = new HashSet<>();
        Matcher matcher = CHOICE_OPTION_PATTERN.matcher(questionContent);
        while (matcher.find()) {
            optionSet.add(matcher.group(2));
            if (optionSet.size() == 4) {
                return true;
            }
        }
        return false;
    }

    /**
     * 识别题型标题行，兼容Markdown标题、中文章节标题和题型标签
     */
    private String resolveQuestionTypeLine(String line) {
        if (StrUtil.isBlank(line)) {
            return null;
        }
        String normalizedLine = normalizeQuestionTypeLine(line);
        if (normalizedLine.length() > 20) {
            return null;
        }
        for (String questionType : QUESTION_TYPE_LIST) {
            if (normalizedLine.contains(questionType)) {
                return questionType;
            }
        }
        return null;
    }

    /**
     * 去掉题型样式中的Markdown、序号和括号符号，便于匹配题型名称
     */
    private String normalizeQuestionTypeLine(String line) {
        String normalizedLine = MARKDOWN_HEADING_PATTERN.matcher(StrUtil.trim(line)).replaceFirst("");
        normalizedLine = QUESTION_TYPE_LABEL_PATTERN.matcher(normalizedLine).replaceFirst("");
        normalizedLine = CHINESE_SECTION_PREFIX_PATTERN.matcher(normalizedLine).replaceFirst("");
        return normalizedLine
                .replace("【", "")
                .replace("】", "")
                .replace("[", "")
                .replace("]", "")
                .replace("（", "")
                .replace("）", "")
                .replace("(", "")
                .replace(")", "")
                .replace("：", "")
                .replace(":", "")
                .trim();
    }

    /**
     * 使用首行作为标题，避免列表标题过长
     */
    private String buildQuestionTitle(String questionContent) {
        String firstLine = questionContent.split("\\R", 2)[0];
        return StrUtil.maxLength(firstLine, 80);
    }

    /**
     * HTTP响应
     */
    private static class HttpResponse {

        private final int statusCode;

        private final String body;

        private HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        /**
         * 获取响应状态码
         */
        private int getStatusCode() {
            return statusCode;
        }

        /**
         * 获取响应内容
         */
        private String getBody() {
            return body;
        }
    }
}
