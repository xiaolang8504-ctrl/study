package com.study.module.system.file.service;

import com.study.module.system.file.dto.request.BatchUploadReq;
import com.study.module.system.file.dto.request.UploadReq;
import com.study.module.system.file.dto.response.FileUploadPolicyResp;
import com.study.module.system.file.dto.response.UploadFileResp;
import java.util.List;

/**
 * 上传服务
 */
public interface UploadService {

    /**
     * 上传签名生成
     */
    FileUploadPolicyResp policy(String uploadType);

    /**
     * 上传文件
     */
    UploadFileResp upload(UploadReq request);

    /**
     * 批量上传文件
     */
    List<UploadFileResp> batchUploadFile(BatchUploadReq request);
}
