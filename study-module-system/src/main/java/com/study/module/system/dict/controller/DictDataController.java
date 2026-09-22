package com.study.module.system.dict.controller;

import com.study.module.system.dict.dto.request.*;
import com.study.module.system.dict.service.*;
import com.study.common.core.domain.KeyValue;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.dict.dto.response.DictDataListResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 字典数据控制器
 */
@Api(tags = "字典数据")
@RestController
@RequestMapping("/api/dictData")
public class DictDataController {

    @Autowired
    DictDataListService dictDataListService;

    @Autowired
    DictDataCreateService dictDataCreateService;

    @Autowired
    DictDataUpdateService dictDataUpdateService;

    @Autowired
    DictDataDeleteService dictDataDeleteService;

    @Autowired
    DictDataEnableService dictDataEnableService;

    @Autowired
    DictDataSortService dictDataSortService;

    @Autowired
    DictDataOptionsService dictDataOptionsService;

    @ApiOperation("字典数据分页列表")
    @GetMapping("/dictDataPageList")
    @PreAuthorize("hasAuthority('system:dictData:dictDataPageList')")
    /**
     * 分页查询字典数据。
     */
    public Result<PageResult<DictDataListResp>> dictDataPageList(@Validated DictDataPageListReq request) {
        return ResultUtils.success(dictDataListService.dictDataPageList(request));
    }

    @ApiOperation("创建字典数据")
    @PostMapping("/createDictData")
    @PreAuthorize("hasAuthority('system:dictData:createDictData')")
    /**
     * 创建或保存字典数据。
     */
    public Result<Void> createDictData(@Validated @RequestBody DictDataCreateReq request) {
        dictDataCreateService.createDictData(request);
        return ResultUtils.success();
    }

    @ApiOperation("更新字典数据")
    @PostMapping("/updateDictData")
    @PreAuthorize("hasAuthority('system:dictData:updateDictData')")
    /**
     * 更新字典数据。
     */
    public Result<Void> updateDictData(@Validated @RequestBody DictDataUpdateReq request) {
        dictDataUpdateService.updateDictData(request);
        return ResultUtils.success();
    }

    @ApiOperation("删除字典数据")
    @PostMapping("/deleteDictData")
    @PreAuthorize("hasAuthority('system:dictData:deleteDictData')")
    /**
     * 删除字典数据。
     */
    public Result<Void> deleteDictData(@Validated @RequestBody DictDataIdReq request) {
        dictDataDeleteService.deleteDictData(request.getId());
        return ResultUtils.success();
    }

    @ApiOperation("启用字典数据")
    @PostMapping("/enableDictData")
    @PreAuthorize("hasAuthority('system:dictData:enableDictData')")
    /**
     * 执行 enableDictData 业务处理。
     */
    public Result<Void> enableDictData(@Validated @RequestBody DictDataIdReq request) {
        dictDataEnableService.enableDictData(request.getId());
        return ResultUtils.success();
    }

    @ApiOperation("字典数据下拉选项")
    @GetMapping("/dictDataOptions")
    //@PreAuthorize("hasAuthority('system:dictData:dictDataOptions')")
    /**
     * 查询字典数据选项。
     */
    public Result<List<KeyValue<String, String>>> dictDataOptions(@RequestParam @NotBlank(message = "字典数据类型不为空") String dictType) {
        return ResultUtils.success(dictDataOptionsService.dictDataOptions(dictType));
    }

    @ApiOperation("字典数据排序")
    @PostMapping("/dictDataSort")
    @PreAuthorize("hasAuthority('system:dictData:dictDataSort')")
    /**
     * 执行 dictDataSort 业务处理。
     */
    public Result<Void> dictDataSort(@Validated @RequestBody DictDataSortReq request) {
        dictDataSortService.dictDataSort(request);
        return ResultUtils.success();
    }
}
