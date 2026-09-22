package com.study.module.system.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.book.convert.BookConvert;
import com.study.module.system.book.dto.request.CreateBookReq;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.mapper.BookMapper;
import com.study.module.system.book.service.BookCreateService;
import com.study.module.system.book.service.BookService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * 课本新增服务实现
 */
@Service
public class BookCreateServiceImpl extends ServiceImpl<BookMapper, Book> implements BookCreateService {

    @Autowired
    BookService bookService;

    /**
     * 新增图书
     */
    @Override
    public void createBook(CreateBookReq request) {
        Book book = BookConvert.INSTANCE.toBook(request);
        bookService.fillDictNames(book);
        LocalDateTime now = LocalDateTime.now();
        book.setCreateUserId(AccountUtils.getUserId());
        book.setCreateTime(now);
        book.setUpdateTime(now);
        if (!this.save(book)) {
            throw new LogicException(ErrorCodeConstants.CREATE_BOOK_FAIL);
        }
    }
}
