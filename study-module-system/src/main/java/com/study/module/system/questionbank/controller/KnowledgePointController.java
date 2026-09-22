package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.questionbank.dto.request.KnowledgePointSaveReq;
import com.study.module.system.questionbank.dto.request.KnowledgePointIdReq;
import com.study.module.system.questionbank.dto.response.KnowledgePointListResp;
import com.study.module.system.questionbank.service.KnowledgePointListService;
import com.study.module.system.questionbank.service.KnowledgePointSaveService;
import com.study.module.system.questionbank.service.KnowledgePointTreeService;
import com.study.module.system.questionbank.service.KnowledgePointDeleteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 知识点接口
 */
@Api(tags = "知识点")
@RestController
@RequestMapping("/api/knowledgePoint")
public class KnowledgePointController {

    @Autowired
    KnowledgePointListService knowledgePointListService;

    @Autowired
    KnowledgePointSaveService knowledgePointSaveService;

    @Autowired
    KnowledgePointTreeService knowledgePointTreeService;

    @Autowired
    KnowledgePointDeleteService knowledgePointDeleteService;

    @ApiOperation("知识点列表")
    @PreAuthorize("hasAuthority('system:questionBank:knowledgePointList')")
    @GetMapping("/knowledgePointList")
    public Result<List<KnowledgePointListResp>> knowledgePointList(@RequestParam(required = false) String grade,
                                                                   @RequestParam(required = false) String subject) {
        return ResultUtils.success(knowledgePointListService.knowledgePointList(grade, subject));
    }

    @ApiOperation("保存知识点")
    @PreAuthorize("hasAuthority('system:questionBank:saveKnowledgePoint')")
    /**
     * 创建或保存知识点。
     */
    @PostMapping("/saveKnowledgePoint")
    public Result<Void> saveKnowledgePoint(@RequestBody @Validated KnowledgePointSaveReq request) {
        knowledgePointSaveService.saveKnowledgePoint(request);
        return ResultUtils.success();
    }

    @ApiOperation("知识点树")
    @PreAuthorize("hasAuthority('system:questionBank:knowledgePointTree')")
    @GetMapping("/knowledgePointTree")
    public Result<List<KnowledgePointListResp>> knowledgePointTree(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String subject) {
        return ResultUtils.success(knowledgePointTreeService.knowledgePointTree(grade, subject));
    }

    @ApiOperation("删除知识点")
    @PreAuthorize("hasAuthority('system:questionBank:deleteKnowledgePoint')")
    /**
     * 删除知识点。
     */
    @PostMapping("/deleteKnowledgePoint")
    public Result<Void> deleteKnowledgePoint(@RequestBody @Validated KnowledgePointIdReq request) {
        knowledgePointDeleteService.deleteKnowledgePoint(request.getId());
        return ResultUtils.success();
    }
}
