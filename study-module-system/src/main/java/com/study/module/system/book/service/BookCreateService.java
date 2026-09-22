package com.study.module.system.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.book.dto.request.CreateBookReq;
import com.study.module.system.book.entity.Book;

/**
 * 课本新增服务
 */
public interface BookCreateService extends IService<Book> {

    /**
     * 新增图书
     */
    void createBook(CreateBookReq request);
}
