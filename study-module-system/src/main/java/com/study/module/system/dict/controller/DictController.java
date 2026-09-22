package com.study.module.system.dict.controller;

import com.study.module.system.dict.service.*;
import com.study.common.core.domain.KeyValue;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.dict.dto.request.DictCreateReq;
import com.study.module.system.dict.dto.request.DictIdReq;
import com.study.module.system.dict.dto.request.DictPageListReq;
import com.study.module.system.dict.dto.request.DictUpdateReq;
import com.study.module.system.dict.dto.response.DictListResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 */
@Api(tags = "字典")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    DictListService dictListService;

    @Autowired
    DictCreateService dictCreateService;

    @Autowired
    DictUpdateService dictUpdateService;

    @Autowired
    DictDeleteService dictDeleteService;

    @Autowired
    DictOptionsService dictOptionsService;

    @ApiOperation("字典分页列表")
    @GetMapping("/dictList")
    @PreAuthorize("hasAuthority('system:dict:dictList')")
    /**
     * 执行 dictList 业务处理。
     */
    public Result<PageResult<DictListResp>> dictList(DictPageListReq request) {
        return ResultUtils.success(dictListService.dictList(request));
    }

    @ApiOperation("创建字典")
    @PostMapping("/createDict")
    @PreAuthorize("hasAuthority('system:dict:createDict')")
    /**
     * 创建或保存字典。
     */
    public Result<Void> createDict(@Validated @RequestBody DictCreateReq request) {
        dictCreateService.createDict(request);
        return ResultUtils.success();
    }

    @ApiOperation("更新字典")
    @PostMapping("/updateDict")
    @PreAuthorize("hasAuthority('system:dict:updateDict')")
    /**
     * 更新字典。
     */
    public Result<Void> updateDict(@Validated @RequestBody DictUpdateReq request) {
        dictUpdateService.updateDict(request);
        return ResultUtils.success();
    }

    @ApiOperation("删除字典")
    @PostMapping("/deleteDict")
    @PreAuthorize("hasAuthority('system:dict:deleteDict')")
    /**
     * 删除字典。
     */
    public Result<Void> deleteDict(@Validated @RequestBody DictIdReq request) {
        dictDeleteService.deleteDict(request.getId());
        return ResultUtils.success();
    }

    @ApiOperation("字典下拉选项")
    @PostMapping("/dictOptions")
    //@PreAuthorize("hasAuthority('system:dict:dictOptions')")
    /**
     * 查询字典选项。
     */
    public Result<List<KeyValue<Long, String>>> dictOptions() {
        return ResultUtils.success(dictOptionsService.dictOptions());
    }
}
