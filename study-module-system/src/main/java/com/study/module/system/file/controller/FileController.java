package com.study.module.system.file.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.dto.request.BatchUploadReq;
import com.study.module.system.file.dto.request.FileIdReq;
import com.study.module.system.file.dto.request.FileUploadTypeReq;
import com.study.module.system.file.dto.request.UploadReq;
import com.study.module.system.file.dto.request.QuestionImageCleanReq;
import com.study.module.system.file.dto.request.QuestionImageDeleteReq;
import com.study.module.system.file.dto.request.QuestionImagePageListReq;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.file.dto.response.FileUploadPolicyResp;
import com.study.module.system.file.dto.response.UploadFileResp;
import com.study.module.system.file.dto.response.QuestionImageCleanResp;
import com.study.module.system.file.dto.response.QuestionImagePageListResp;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.UploadService;
import com.study.module.system.file.service.QuestionImageManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 文件控制层
 */
@Api(tags = "文件")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    UploadService uploadService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FileService fileService;

    @Autowired
    DownloadService downloadService;

    @Autowired
    QuestionImageManageService questionImageManageService;

    @ApiOperation(value = "上传签名生成")
    @GetMapping(value = "/policy")
    @PreAuthorize("isAuthenticated()")
    /**
     * 执行 policy 业务处理。
     */
    public Result<FileUploadPolicyResp> policy(@Valid FileUploadTypeReq request) {
        FileUploadPolicyResp result = uploadService.policy(request.getUploadType());
        return ResultUtils.success(result);
    }

    @ApiOperation("上传文件")
    @PostMapping(value = "/uploadFile")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "form", dataType = "__file", name = "file", value = "附件", required = true)})
    @PreAuthorize("isAuthenticated()")
    /**
     * 执行 uploadFile 业务处理。
     */
    public Result<UploadFileResp> uploadFile(@Valid UploadReq request) {
        return ResultUtils.success(uploadService.upload(request));
    }

    /**
     * 下载文件(权限已放行)
     */
    @ApiOperation("下载文件")
    @GetMapping(value = "/downloadFile")
    // @PreAuthorize("hasAuthority('file:file:downloadFile')")
    public void downloadFile(@RequestParam @Valid @NotBlank(message = "下载TOKEN不为空") String signature, HttpServletResponse response) {
        downloadService.download(signature, response);
    }

    @ApiOperation("获取下载地址")
    @GetMapping(value = "/downloadUrl")
    @PreAuthorize("isAuthenticated()")
    public Result<String> downloadUrl(@Valid FileIdReq request,
                                      @RequestParam @Valid @NotBlank(message = "上传类型不为空") String uploadType) {
        return ResultUtils.success(downloadService.downUrl(request.getFileId(), uploadType));
    }

    /**
     * 执行 batchUploadFile 业务处理。
     */
    @ApiOperation("批量上传文件")
    @PostMapping(value = "/batchUploadFile")
    @PreAuthorize("isAuthenticated()")
    public Result<List<UploadFileResp>> batchUploadFile(@Validated @RequestBody BatchUploadReq request) {
        return ResultUtils.success(uploadService.batchUploadFile(request));
    }

    @ApiOperation("题目图片分页列表")
    @PreAuthorize("hasAuthority('system:file:questionImagePageList')")
    @GetMapping("/questionImagePageList")
    public Result<PageResult<QuestionImagePageListResp>> questionImagePageList(
            @Validated QuestionImagePageListReq request) {
        return ResultUtils.success(questionImageManageService.questionImagePageList(request));
    }

    @ApiOperation("删除选中的题目孤立图片")
    @PreAuthorize("hasAuthority('system:file:deleteOrphanQuestionImage')")
    @PostMapping("/deleteOrphanQuestionImage")
    public Result<QuestionImageCleanResp> deleteOrphanQuestionImage(
            @RequestBody @Validated QuestionImageDeleteReq request) {
        return ResultUtils.success(questionImageManageService.deleteOrphanQuestionImage(request.getFileIds()));
    }

    @ApiOperation("清理过期的题目孤立图片")
    @PreAuthorize("hasAuthority('system:file:cleanOrphanQuestionImage')")
    @PostMapping("/cleanOrphanQuestionImage")
    public Result<QuestionImageCleanResp> cleanOrphanQuestionImage(
            @RequestBody @Validated QuestionImageCleanReq request) {
        return ResultUtils.success(questionImageManageService.cleanOrphanQuestionImage(request.getRetentionHours()));
    }

}
