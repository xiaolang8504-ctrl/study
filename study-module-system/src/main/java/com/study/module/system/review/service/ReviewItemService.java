package com.study.module.system.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.review.entity.ReviewItem;

/**
 * 复习项目公共服务
 */
public interface ReviewItemService extends IService<ReviewItem> {

    /**
     * 校验当前用户复习任务
     */
    ReviewItem checkReviewItem(Long id, Long userId);
}
