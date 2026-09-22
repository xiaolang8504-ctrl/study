package com.study.module.system.book.convert;

import com.study.module.system.book.dto.request.CreateBookReq;
import com.study.module.system.book.dto.request.UpdateBookReq;
import com.study.module.system.book.dto.response.BookDetailResp;
import com.study.module.system.book.dto.response.BookPageListResp;
import com.study.module.system.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 课本转换类
 */
@Mapper
public interface BookConvert {

    BookConvert INSTANCE = Mappers.getMapper(BookConvert.class);

    List<BookPageListResp> toBookPageListResp(List<Book> bookList);

    BookDetailResp toBookDetailResp(Book book);

    /**
     * 将新增课本请求转换为课本实体。
     */
    Book toBook(CreateBookReq request);

    /**
     * 将修改课本请求转换为课本实体。
     */
    Book toBook(UpdateBookReq request);
}
