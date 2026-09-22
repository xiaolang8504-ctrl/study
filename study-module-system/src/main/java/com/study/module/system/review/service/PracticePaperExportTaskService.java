package com.study.module.system.review.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.review.dto.request.PracticePaperExportCreateReq;
import com.study.module.system.review.dto.response.PracticePaperExportTaskResp;
import com.study.module.system.review.entity.PracticePaperExportTask;
import java.util.List;
public interface PracticePaperExportTaskService extends IService<PracticePaperExportTask> {
    Long createPracticePaperExportTask(PracticePaperExportCreateReq request);
    List<PracticePaperExportTaskResp> practicePaperExportTaskList();
    PracticePaperExportTask checkPracticePaperExportTask(Long id);
}
