package com.study.module.system.book.controller;

import com.study.module.system.book.dto.request.BookIdReq;
import com.study.module.system.book.dto.request.BookPageListReq;
import com.study.module.system.book.dto.request.CreateBookReq;
import com.study.module.system.book.dto.request.UpdateBookReq;
import com.study.module.system.book.dto.response.BookDetailResp;
import com.study.module.system.book.dto.response.BookPageListResp;
import com.study.module.system.book.service.BookCreateService;
import com.study.module.system.book.service.BookDeleteService;
import com.study.module.system.book.service.BookDetailService;
import com.study.module.system.book.service.BookListService;
import com.study.module.system.book.service.BookUpdateService;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课本管理前端控制器
 */
@Api(tags = "课本管理")
@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    BookListService bookListService;

    @Autowired
    BookDetailService bookDetailService;

    @Autowired
    BookCreateService bookCreateService;

    @Autowired
    BookUpdateService bookUpdateService;

    @Autowired
    BookDeleteService bookDeleteService;

    @ApiOperation("课本分页列表")
    //@PreAuthorize("hasAuthority('system:book:bookPageList')")
    /**
     * 分页查询分页数据。
     */
    @GetMapping("/bookPageList")
    public Result<PageResult<BookPageListResp>> bookPageList(@Validated BookPageListReq request) {
        return ResultUtils.success(bookListService.bookPageList(request));
    }

    @ApiOperation("课本详情")
    //@PreAuthorize("hasAuthority('system:book:bookDetail')")
    /**
     * 查询相关业务数据详情。
     */
    @GetMapping("/bookDetail")
    public Result<BookDetailResp> bookDetail(@Validated BookIdReq request) {
        return ResultUtils.success(bookDetailService.bookDetail(request.getId()));
    }

    @ApiOperation("新增课本")
    //@PreAuthorize("hasAuthority('system:book:createBook')")
    /**
     * 创建或保存相关业务数据。
     */
    @PostMapping("/createBook")
    public Result<Void> createBook(@RequestBody @Validated CreateBookReq request) {
        bookCreateService.createBook(request);
        return ResultUtils.success();
    }

    @ApiOperation("修改课本")
    //@PreAuthorize("hasAuthority('system:book:updateBook')")
    /**
     * 更新相关业务数据。
     */
    @PostMapping("/updateBook")
    public Result<Void> updateBook(@RequestBody @Validated UpdateBookReq request) {
        bookUpdateService.updateBook(request);
        return ResultUtils.success();
    }

    @ApiOperation("删除课本")
    //@PreAuthorize("hasAuthority('system:book:deleteBook')")
    /**
     * 删除相关业务数据。
     */
    @PostMapping("/deleteBook")
    public Result<Void> deleteBook(@RequestBody @Validated BookIdReq request) {
        bookDeleteService.deleteBook(request.getId());
        return ResultUtils.success();
    }
}
