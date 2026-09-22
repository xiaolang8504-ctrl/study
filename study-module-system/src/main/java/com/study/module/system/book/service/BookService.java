package com.study.module.system.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.book.entity.Book;

/**
 * 课本公共服务
 */
public interface BookService extends IService<Book> {

    /**
     * 校验图书
     */
    Book checkBook(Long id);

    /**
     * 根据年级和科目字典键值填充名称
     */
    void fillDictNames(Book book);
}
