package com.study.module.system.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.book.entity.Book;

/**
 * 课本删除服务
 */
public interface BookDeleteService extends IService<Book> {

    /**
     * 删除图书
     */
    void deleteBook(Long id);
}
