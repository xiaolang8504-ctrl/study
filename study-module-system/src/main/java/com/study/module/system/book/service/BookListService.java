package com.study.module.system.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.book.dto.request.BookPageListReq;
import com.study.module.system.book.dto.response.BookPageListResp;
import com.study.module.system.book.entity.Book;
import com.study.common.core.domain.dto.PageResult;

/**
 * 课本列表服务
 */
public interface BookListService extends IService<Book> {

    /**
     * 分页查询图书
     */
    PageResult<BookPageListResp> bookPageList(BookPageListReq request);
}
