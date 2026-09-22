package com.study.module.system.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.mapper.BookMapper;
import com.study.module.system.book.service.BookDeleteService;
import com.study.module.system.book.service.BookService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 课本删除服务实现
 */
@Service
public class BookDeleteServiceImpl extends ServiceImpl<BookMapper, Book> implements BookDeleteService {

    @Autowired
    BookService bookService;

    /**
     * 删除图书
     */
    @Override
    public void deleteBook(Long id) {
        bookService.checkBook(id);
        if (!this.removeById(id)) {
            throw new LogicException(ErrorCodeConstants.DELETE_BOOK_FAIL);
        }
    }
}
