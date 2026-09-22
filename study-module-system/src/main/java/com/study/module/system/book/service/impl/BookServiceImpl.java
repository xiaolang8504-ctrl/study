package com.study.module.system.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.constants.BookDictType;
import com.study.module.system.book.mapper.BookMapper;
import com.study.module.system.book.service.BookService;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 课本公共服务实现
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    DictDataService dictDataService;

    /**
     * 校验图书
     */
    @Override
    public Book checkBook(Long id) {
        Book book = this.getById(id);
        if (book == null) {
            throw new LogicException(ErrorCodeConstants.BOOK_NOT_EXIST);
        }
        return book;
    }

    /**
     * 填充字典名称
     */
    @Override
    public void fillDictNames(Book book) {
        DictData grade = checkDictData(BookDictType.GRADE, book.getGrade());
        DictData subject = checkDictData(BookDictType.SUBJECT, book.getSubject());
        book.setGradeName(grade.getDictLabel());
        book.setSubjectName(subject.getDictLabel());
    }

    /**
     * 校验字典数据
     */
    private DictData checkDictData(String dictType, String dictValue) {
        return dictDataService.checkDictData(dictType, dictValue, ErrorCodeConstants.INVALID_DICT_DATA_IDS);
    }
}
