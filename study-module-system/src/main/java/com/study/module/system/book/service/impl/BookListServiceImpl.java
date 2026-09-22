package com.study.module.system.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.module.system.book.convert.BookConvert;
import com.study.module.system.book.dto.request.BookPageListReq;
import com.study.module.system.book.dto.response.BookPageListResp;
import com.study.module.system.book.entity.Book;
import com.study.module.system.book.mapper.BookMapper;
import com.study.module.system.book.service.BookListService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.study.api.dto.response.FileData;
import com.study.api.provider.FileProvider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 课本列表服务实现
 */
@Service
public class BookListServiceImpl extends ServiceImpl<BookMapper, Book> implements BookListService {

    @DubboReference
    FileProvider fileProvider;

    /**
     * 分页查询图书
     */
    @Override
    public PageResult<BookPageListResp> bookPageList(BookPageListReq request) {
        Page<Book> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, buildQueryWrapper(request));
        PageResult<BookPageListResp> response = PageUtils.wrap(page, BookConvert.INSTANCE::toBookPageListResp);
        fillFileData(response);
        return response;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Book> buildQueryWrapper(BookPageListReq request) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getGrade())) {
            queryWrapper.eq(Book::getGrade, request.getGrade());
        }
        if (StringUtils.hasText(request.getSubject())) {
            queryWrapper.eq(Book::getSubject, request.getSubject());
        }
        if (StringUtils.hasText(request.getKeyWord())) {
            queryWrapper.and(wrapper -> wrapper.like(Book::getTitle, request.getKeyWord())
                    .or()
                    .like(Book::getContent, request.getKeyWord()));
        }
        return queryWrapper.orderByDesc(Book::getId);
    }

    /**
     * 填充文件信息
     */
    private void fillFileData(PageResult<BookPageListResp> response) {
        String fileIds = response.getList().stream()
                .map(BookPageListResp::getImageUrl)
                .filter(this::isFileId)
                .collect(Collectors.joining(","));
        if (!StringUtils.hasText(fileIds)) {
            return;
        }
        Map<Integer, FileData> fileDataMap = fileProvider.getFileList(UploadType.BOOK, fileIds).stream()
                .collect(Collectors.toMap(FileData::getId, Function.identity(), (first, second) -> first));
        response.getList().forEach(item -> {
            if (!isFileId(item.getImageUrl())) {
                return;
            }
            FileData fileData = fileDataMap.get(Integer.valueOf(item.getImageUrl()));
            if (fileData != null) {
                item.setFileName(fileData.getOriginName());
                item.setFileExtension(fileData.getFileExtension());
                item.setFileSize(fileData.getFileSize());
            }
        });
    }

    /**
     * 判断是否为文件编号
     */
    private boolean isFileId(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
