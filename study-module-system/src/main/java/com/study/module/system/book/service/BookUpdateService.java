package com.study.module.system.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.book.dto.request.UpdateBookReq;
import com.study.module.system.book.entity.Book;

/**
 * 课本修改服务
 */
public interface BookUpdateService extends IService<Book> {

    /**
     * 修改图书
     */
    void updateBook(UpdateBookReq request);
}
