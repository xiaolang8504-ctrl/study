package com.study.module.system.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.book.convert.BookConvert;
import com.study.module.system.book.dto.response.BookDetailResp;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.mapper.BookMapper;
import com.study.module.system.book.service.BookDetailService;
import com.study.module.system.book.service.BookService;
import com.study.api.dto.response.FileData;
import com.study.api.provider.FileProvider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 课本详情服务实现
 */
@Service
public class BookDetailServiceImpl extends ServiceImpl<BookMapper, Book> implements BookDetailService {

    @Autowired
    BookService bookService;

    @DubboReference
    FileProvider fileProvider;

    /**
     * 查询图书详情
     */
    @Override
    public BookDetailResp bookDetail(Long id) {
        BookDetailResp response = BookConvert.INSTANCE.toBookDetailResp(bookService.checkBook(id));
        fillFileData(response);
        return response;
    }

    /**
     * 填充文件信息
     */
    private void fillFileData(BookDetailResp response) {
        Integer fileId = parseFileId(response.getImageUrl());
        if (fileId == null) {
            return;
        }
        FileData fileData = fileProvider.getFile(UploadType.BOOK, fileId);
        if (fileData != null) {
            response.setFileName(fileData.getOriginName());
            response.setFileExtension(fileData.getFileExtension());
            response.setFileSize(fileData.getFileSize());
        }
    }

    /**
     * 解析文件编号
     */
    private Integer parseFileId(String fileId) {
        try {
            return fileId == null ? null : Integer.valueOf(fileId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
