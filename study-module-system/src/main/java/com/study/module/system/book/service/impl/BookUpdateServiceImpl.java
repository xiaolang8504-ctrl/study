package com.study.module.system.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.book.convert.BookConvert;
import com.study.module.system.book.dto.request.UpdateBookReq;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.mapper.BookMapper;
import com.study.module.system.book.service.BookService;
import com.study.module.system.book.service.BookUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 课本修改服务实现
 */
@Service
public class BookUpdateServiceImpl extends ServiceImpl<BookMapper, Book> implements BookUpdateService {

    @Autowired
    BookService bookService;

    /**
     * 修改图书
     */
    @Override
    public void updateBook(UpdateBookReq request) {
        bookService.checkBook(request.getId());
        Book book = BookConvert.INSTANCE.toBook(request);
        bookService.fillDictNames(book);
        book.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(book)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_BOOK_FAIL);
        }
    }
}
