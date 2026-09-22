package com.study.module.system.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.book.dto.response.BookDetailResp;
import com.study.module.system.book.entity.Book;

/**
 * 课本详情服务
 */
public interface BookDetailService extends IService<Book> {

    /**
     * 查询图书详情
     */
    BookDetailResp bookDetail(Long id);
}
