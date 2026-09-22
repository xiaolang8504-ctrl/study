package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.mapper.ReviewItemMapper;
import com.study.module.system.review.service.ReviewItemService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;

/**
 * 复习项目公共服务实现
 */
@Service
public class ReviewItemServiceImpl extends ServiceImpl<ReviewItemMapper, ReviewItem>
        implements ReviewItemService {

    /**
     * 校验复习项
     */
    @Override
    public ReviewItem checkReviewItem(Long id, Long userId) {
        ReviewItem reviewItem = lambdaQuery()
                .eq(ReviewItem::getId, id)
                .eq(ReviewItem::getUserId, userId)
                .one();
        if (reviewItem == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ITEM_NOT_EXIST);
        }
        return reviewItem;
    }
}
