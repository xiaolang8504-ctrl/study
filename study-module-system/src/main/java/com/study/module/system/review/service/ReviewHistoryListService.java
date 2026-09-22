package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.ReviewHistoryPageListReq;
import com.study.module.system.review.dto.response.ReviewHistoryHomeResp;

/**
 * 复习历史列表服务
 */
public interface ReviewHistoryListService {

    /**
     * 分页查询复习历史
     */
    ReviewHistoryHomeResp reviewHistoryPageList(ReviewHistoryPageListReq request);
}
