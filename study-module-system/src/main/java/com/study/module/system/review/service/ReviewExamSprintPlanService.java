package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.ReviewExamSprintSaveReq;
import com.study.module.system.review.dto.response.ReviewExamSprintResp;

public interface ReviewExamSprintPlanService {
    ReviewExamSprintResp reviewExamSprint(String subject);
    ReviewExamSprintResp saveReviewExamSprint(ReviewExamSprintSaveReq request);
}
