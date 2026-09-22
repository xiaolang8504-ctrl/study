package com.study.common.core.enums;

import com.study.common.core.domain.ErrorCode;

/**
 * 业务错误码枚举常量
 * 统一格式：AA-BB-CCC，7位长度整形。
 * AA：代表服务，从21开始。
 * BB：代表模块，从01开始。
 * CCC：具体错误编号，从001开始。
 */
public interface ErrorCodeConstants {

    // 21-系统服务
    // 2101-账号模块
    ErrorCode ACCESS_DENIED = new ErrorCode(2101001, "无访问权限，请联系管理员进行配置");
    ErrorCode NO_LOGIN = new ErrorCode(2101002, "未登录或登录已失效");
    ErrorCode VERIFY_FAIL = new ErrorCode(2101003, "图形验证码错误");
    ErrorCode VERIFY_EXPIRE_FAIL = new ErrorCode(2101004, "图形验证码过期");
    ErrorCode LOGIN_FAIL = new ErrorCode(2101005, "用户名或密码错误");
    ErrorCode USER_NOT_EXIST = new ErrorCode(2101006, "用户信息不存在");
    ErrorCode CREATE_USER_FAIL = new ErrorCode(2101007, "创建用户失败");
    ErrorCode UPDATE_USER_FAIL = new ErrorCode(2101008, "更新用户信息失败");
    ErrorCode DELETE_USER_FAIL = new ErrorCode(2101009, "删除用户失败");
    ErrorCode USER_NAME_EXIST = new ErrorCode(2101010, "用户名已存在");
    ErrorCode ADMIN_CANNOT_DELETE = new ErrorCode(2101011, "管理员账号无法删除");
    ErrorCode NEW_PASSWORD_DIFFERENT_CONFIRMED_PASSWORD = new ErrorCode(2101012, "新密码与确认密码不一致");
    ErrorCode OLD_PASSWORD_ERROR = new ErrorCode(2101013, "原密码错误");
    ErrorCode UPDATE_PASSWORD_FAIL = new ErrorCode(2101014, "修改密码失败");
    ErrorCode USER_DISABLED = new ErrorCode(2101015, "账号被禁用，请联系管理员");
    ErrorCode EMAIL_CODE_ERROR = new ErrorCode(2101016, "邮箱验证码错误!");
    ErrorCode EMAIL_CODE_EXIST = new ErrorCode(2101017, "邮箱验证码不存在!");
    ErrorCode EMAIL_EXIST = new ErrorCode(2101018, "未获取到邮箱信息!");
    ErrorCode BIND_USER_FAIL = new ErrorCode(2101019, "绑定钉钉失败!");
    ErrorCode BIND_USER_EXIST = new ErrorCode(2101020, "绑定钉钉信息不存在!");
    ErrorCode SLIDER_CAPTCHA_VERIFY_FAIL = new ErrorCode(2101021, "验证失败,请重新向右拖动滑块填充拼图");
    // 2102-组织架构模块
    ErrorCode DEPT_NOT_EXIST = new ErrorCode(2102001, "部门信息不存在");
    ErrorCode CREATE_DEPT_FAIL = new ErrorCode(2102002, "创建部门失败");
    ErrorCode UPDATE_DEPT_FAIL = new ErrorCode(2102003, "更新部门信息失败");
    ErrorCode DELETE_DEPT_FAIL = new ErrorCode(2102004, "删除部门失败");
    ErrorCode DEPT_NAME_EXIST = new ErrorCode(2102005, "部门名称已存在");
    ErrorCode EXIST_SUB_DEPT = new ErrorCode(2102006, "请先删除该部门的下级部门");
    ErrorCode DEPT_USER_EXIST = new ErrorCode(2102007, "该部门存在对应的账号,请先移除此部门下的账号");
    // 2103-角色模块
    ErrorCode ROLE_NOT_EXIST = new ErrorCode(2103001, "角色信息不存在");
    ErrorCode CREATE_ROLE_FAIL = new ErrorCode(2103002, "创建角色失败");
    ErrorCode UPDATE_ROLE_FAIL = new ErrorCode(2103003, "更新角色信息失败");
    ErrorCode DELETE_ROLE_FAIL = new ErrorCode(2103004, "删除角色失败");
    ErrorCode ROLE_NAME_EXIST = new ErrorCode(2103005, "角色名已经存在");
    ErrorCode SET_ROLE_MENU_FAIL = new ErrorCode(2103006, "设置角色菜单权限失败");
    ErrorCode SET_ROLE_DATA_FAIL = new ErrorCode(2103007, "设置角色数据权限失败");
    ErrorCode ROLE_USER_EXIST = new ErrorCode(2103008, "该角色存在对应的账号,请先移除此角色下的账号");
    ErrorCode ROLE_USER_NAME_EXIST = new ErrorCode(2103009, "角色对应的账号不存在");
    ErrorCode BUS_SCOPE_NOT_EXIST = new ErrorCode(2103010, "数据业务不存在");

    // 2104-菜单权限模块
    ErrorCode MENU_NOT_EXIST = new ErrorCode(2104001, "菜单信息不存在");
    ErrorCode CREATE_MENU_FAIL = new ErrorCode(2104002, "创建菜单失败");
    ErrorCode UPDATE_MENU_FAIL = new ErrorCode(2104003, "更新菜单信息失败");
    ErrorCode DELETE_MENU_FAIL = new ErrorCode(2104004, "删除菜单失败");
    ErrorCode MENU_CODE_EXIST = new ErrorCode(2104005, "菜单编码已经存在");
    ErrorCode EXIST_SUB_MENU = new ErrorCode(2104006, "请先删除该菜单的下级菜单");
    // 2105-API模块
    ErrorCode RES_NOT_EXIST = new ErrorCode(2105001, "API信息不存在");
    ErrorCode CREATE_RES_FAIL = new ErrorCode(2105002, "创建API失败");
    ErrorCode UPDATE_RES_FAIL = new ErrorCode(2105003, "更新API信息失败");
    ErrorCode DELETE_RES_FAIL = new ErrorCode(2105004, "删除API失败");
    ErrorCode RES_CODE_EXIST = new ErrorCode(2105005, "API编码已经存在");
    ErrorCode EXIST_SUB_RES = new ErrorCode(2105006, "请先删除该API的下级API");
    // 2106-字典模块
    ErrorCode DICT_NOT_EXIST = new ErrorCode(2106001, "字典信息不存在");
    ErrorCode DICT_CREATE_FAIL = new ErrorCode(2106002, "创建字典失败");
    ErrorCode DICT_UPDATE_FAIL = new ErrorCode(2106003, "更新字典信息失败");
    ErrorCode DICT_DELETE_FAIL = new ErrorCode(2106004, "删除字典失败");
    ErrorCode DICT_TYPE_EXIST = new ErrorCode(2106005, "字典类型已经存在");
    ErrorCode DICT_TYPE_NOT_EXIST = new ErrorCode(2106006, "字典类型不存在");
    ErrorCode EXIST_DICT_DATA = new ErrorCode(2106007, "该类型下存在字典数据，无法删除。");
    ErrorCode UPDATE_DICT_TYPE_FAIL = new ErrorCode(2106008, "更新字典类型失败");
    ErrorCode DICT_DATA_NOT_EXIST = new ErrorCode(2106009, "字典数据信息不存在");
    ErrorCode DICT_DATA_CREATE_FAIL = new ErrorCode(2106010, "创建字典数据失败");
    ErrorCode DICT_DATA_UPDATE_FAIL = new ErrorCode(2106011, "更新字典数据信息失败");
    ErrorCode DICT_DATA_DELETE_FAIL = new ErrorCode(2106012, "删除字典数据失败");
    ErrorCode DICT_DATA_ENABLE_FAIL = new ErrorCode(2106013, "启用/禁用字典数据失败");
    ErrorCode DICT_DATA_SORT_FAIL = new ErrorCode(2106014, "上移/下移字典数据失败");
    ErrorCode DICT_DATA_NOT_MOVE = new ErrorCode(2106015, "当前位置无法移动");
    ErrorCode DICT_DATA_LABEL_EXIST = new ErrorCode(2106016, "该字典类型下字典标签已存在");
    ErrorCode DICT_DATA_VALUE_EXIST = new ErrorCode(2106017, "该字典类型下字典键值已存在");
    ErrorCode INVALID_DICT_DATA_IDS = new ErrorCode(2106018, "存在无效的字典数据");
    // 2107-消息模块
    ErrorCode MSG_NOT_EXIST = new ErrorCode(2107001, "消息信息不存在");
    ErrorCode CREATE_MSG_FAIL = new ErrorCode(2107002, "创建消息失败");
    ErrorCode BATCH_CREATE_MSG_FAIL = new ErrorCode(2107003, "批量创建消息失败");
    ErrorCode BATCH_DELETE_MSG_FAIL = new ErrorCode(2107004, "批量删除消息失败");
    ErrorCode SET_HAS_READ_FAIL = new ErrorCode(2107005, "标记消息为已读失败");
    ErrorCode MSG_TYPE_NOT_EXIST = new ErrorCode(2107006, "消息类型未配置");
    // 2108-操作日志模块
    ErrorCode OPERATE_LOG_TYPE_NOT_EXIST = new ErrorCode(2108001, "操作日志类型未配置");
    ErrorCode CREATE_OPERATE_LOG_FAIL = new ErrorCode(2108002, "创建操作日志失败");
    // 2112-错题归档模块
    ErrorCode WRONG_QUESTION_NOT_EXIST = new ErrorCode(2112001, "错题信息不存在");
    ErrorCode CREATE_WRONG_QUESTION_FAIL = new ErrorCode(2112002, "创建错题失败");
    ErrorCode UPDATE_WRONG_QUESTION_FAIL = new ErrorCode(2112003, "更新错题失败");
    ErrorCode DELETE_WRONG_QUESTION_FAIL = new ErrorCode(2112004, "删除错题失败");
    ErrorCode UPDATE_WRONG_QUESTION_IMAGE_FAIL = new ErrorCode(2112005, "修改错图失败");
    ErrorCode WRONG_QUESTION_OCR_NOT_CONFIGURED = new ErrorCode(2112006, "错题图片识别服务未配置");
    ErrorCode WRONG_QUESTION_IMPORT_EMPTY = new ErrorCode(2112007, "未识别到错题内容");
    ErrorCode WRONG_QUESTION_IMPORT_FAIL = new ErrorCode(2112008, "导入错题失败");
    ErrorCode WRONG_QUESTION_OCR_INVOKE_FAIL = new ErrorCode(2112009, "错题图片识别失败");
    ErrorCode WRONG_QUESTION_STATUS_INVALID = new ErrorCode(2112010, "错题状态无效");
    ErrorCode WRONG_QUESTION_STATUS_FLOW_INVALID = new ErrorCode(2112011, "错题状态流转不允许");
    ErrorCode CREATE_WRONG_QUESTION_CORRECTION_RECORD_FAIL = new ErrorCode(2112012, "提交错题订正记录失败");
    // 2113-课本管理模块
    ErrorCode BOOK_NOT_EXIST = new ErrorCode(2113001, "课本信息不存在");
    ErrorCode CREATE_BOOK_FAIL = new ErrorCode(2113002, "创建课本失败");
    ErrorCode UPDATE_BOOK_FAIL = new ErrorCode(2113003, "更新课本失败");
    ErrorCode DELETE_BOOK_FAIL = new ErrorCode(2113004, "删除课本失败");
    // 2114-作业管理模块
    ErrorCode HOME_WORK_NOT_EXIST = new ErrorCode(2114001, "作业信息不存在");
    ErrorCode CREATE_HOME_WORK_FAIL = new ErrorCode(2114002, "创建作业失败");
    ErrorCode UPDATE_HOME_WORK_FAIL = new ErrorCode(2114003, "更新作业失败");
    ErrorCode DELETE_HOME_WORK_FAIL = new ErrorCode(2114004, "删除作业失败");
    // 2115-智能复习模块
    ErrorCode REVIEW_ITEM_NOT_EXIST = new ErrorCode(2115001, "复习任务不存在或无权操作");
    ErrorCode REVIEW_ITEM_NOT_AVAILABLE = new ErrorCode(2115002, "复习任务当前不可操作");
    ErrorCode REVIEW_ANSWER_NOT_REVEALED = new ErrorCode(2115003, "请先查看答案再提交反馈");
    ErrorCode REVIEW_FEEDBACK_CONFLICT = new ErrorCode(2115004, "复习任务已更新，请刷新后重试");
    ErrorCode REVIEW_FEEDBACK_FAIL = new ErrorCode(2115005, "保存复习反馈失败");
    ErrorCode REVIEW_PLAN_NOT_EXIST = new ErrorCode(2115006, "复习计划不存在");
    ErrorCode REVIEW_PLAN_SETTING_FAIL = new ErrorCode(2115007, "保存复习计划设置失败");
    ErrorCode REVIEW_REMINDER_NOT_EXIST = new ErrorCode(2115008, "复习提醒不存在或无权操作");
    ErrorCode REVIEW_REMINDER_READ_FAIL = new ErrorCode(2115009, "复习提醒设为已读失败");
    ErrorCode REVIEW_ANSWER_TICKET_FAIL = new ErrorCode(2115010, "生成答案解锁凭证失败，请重试");
    ErrorCode REVIEW_REMINDER_SEND_FAIL = new ErrorCode(2115011, "复习提醒发送失败");
    ErrorCode REVIEW_ANSWER_RESULT_REQUIRED = new ErrorCode(2115012, "请确认本次作答是否正确");
    // 2117-家庭监护模块
    ErrorCode GUARDIAN_BINDING_NOT_EXIST = new ErrorCode(2117001, "监护绑定不存在或无权操作");
    ErrorCode GUARDIAN_INVITATION_INVALID = new ErrorCode(2117002, "邀请码无效、已过期或已被使用");
    ErrorCode GUARDIAN_BINDING_STATUS_INVALID = new ErrorCode(2117003, "监护绑定当前状态不允许该操作");
    ErrorCode GUARDIAN_BINDING_DUPLICATE = new ErrorCode(2117004, "该家长与学生已存在有效的监护绑定");
    ErrorCode GUARDIAN_BINDING_SELF_FORBIDDEN = new ErrorCode(2117005, "不能与自己的账号建立监护绑定");
    ErrorCode GUARDIAN_ASSISTED_CAPTURE_NOT_EXIST = new ErrorCode(2117006, "家长代上传任务不存在或无权操作");
    ErrorCode GUARDIAN_ASSISTED_CAPTURE_STATUS_INVALID = new ErrorCode(2117007, "家长代上传任务当前状态不允许该操作");
    // 2116-精品题库模块
    ErrorCode QUESTION_BANK_NOT_EXIST = new ErrorCode(2116001, "题库题目不存在");
    ErrorCode QUESTION_BANK_SAVE_FAIL = new ErrorCode(2116002, "保存题库题目失败");
    ErrorCode QUESTION_BANK_DELETE_FAIL = new ErrorCode(2116003, "删除题库题目失败");
    ErrorCode QUESTION_BANK_REVIEW_FAIL = new ErrorCode(2116004, "审核题库题目失败");
    ErrorCode KNOWLEDGE_POINT_NOT_EXIST = new ErrorCode(2116005, "知识点不存在");
    ErrorCode KNOWLEDGE_POINT_SAVE_FAIL = new ErrorCode(2116006, "保存知识点失败");
    ErrorCode QUESTION_PRACTICE_NOT_EXIST = new ErrorCode(2116007, "相似题练习记录不存在");
    ErrorCode QUESTION_PRACTICE_SUBMITTED = new ErrorCode(2116008, "该题已经提交，请勿重复作答");
    ErrorCode PRACTICE_SESSION_NOT_EXIST = new ErrorCode(2116015, "练习会话不存在");
    ErrorCode PRACTICE_SESSION_QUESTION_NOT_EXIST = new ErrorCode(2116016, "练习题目不存在");
    ErrorCode PRACTICE_SESSION_FINISHED = new ErrorCode(2116017, "练习已完成，请勿重复提交");
    ErrorCode PRACTICE_SESSION_QUESTION_INSUFFICIENT = new ErrorCode(2116023, "符合当前组卷条件的题目不足，请调整筛选条件或减少题量");
    ErrorCode PRACTICE_PAPER_EXPORT_NOT_EXIST = new ErrorCode(2116024, "练习卷导出任务不存在或无权操作");
    ErrorCode PRACTICE_PAPER_EXPORT_FORMAT_INVALID = new ErrorCode(2116025, "练习卷导出格式不支持");
    ErrorCode PRACTICE_PAPER_EXPORT_FAILED = new ErrorCode(2116026, "练习卷导出失败，请稍后重试");
    ErrorCode QUESTION_BANK_DUPLICATE = new ErrorCode(2116009, "题库中已存在相同或高度重复的题目");
    ErrorCode QUESTION_EXPERIMENT_TIME_INVALID = new ErrorCode(2116010, "实验开始时间必须早于结束时间");
    ErrorCode QUESTION_EXPERIMENT_NOT_EXIST = new ErrorCode(2116011, "A/B实验配置不存在");
    ErrorCode QUESTION_ANSWER_NOT_VIEWED = new ErrorCode(2116012, "请先查看标准答案后再进行自评");
    ErrorCode QUESTION_PRACTICE_APPEAL_EXIST = new ErrorCode(2116013, "该作答已提交申诉，请勿重复提交");
    ErrorCode QUESTION_PRACTICE_APPEAL_NOT_EXIST = new ErrorCode(2116014, "作答申诉不存在");
    ErrorCode KNOWLEDGE_POINT_HAS_CHILDREN = new ErrorCode(2116015, "请先删除该知识点的下级节点");
    ErrorCode KNOWLEDGE_POINT_IN_USE = new ErrorCode(2116016, "知识点已被题目引用，无法删除");
    ErrorCode KNOWLEDGE_POINT_DELETE_FAIL = new ErrorCode(2116017, "知识点删除失败");
    ErrorCode KNOWLEDGE_POINT_PARENT_INVALID = new ErrorCode(2116018, "父级知识点无效或存在循环层级");
    ErrorCode KNOWLEDGE_POINT_NAME_EXIST = new ErrorCode(2116019, "同一父级下已存在同名知识点");
    ErrorCode QUESTION_BANK_OPTIONS_INVALID = new ErrorCode(2116020, "题目选项格式不正确");
    ErrorCode QUESTION_BANK_IMPORT_EMPTY = new ErrorCode(2116021, "A4文件中未识别到题目内容");
    ErrorCode QUESTION_REPORT_DUPLICATE = new ErrorCode(2116022, "该题目问题已提交，请勿重复举报");

    // 22-文件服务
    // 2201-文件模块
    ErrorCode FILE_NOT_EXIST = new ErrorCode(2201001, "文件信息不存在");
    ErrorCode CREATE_FILE_FAIL = new ErrorCode(2201002, "创建文件失败");
    ErrorCode BATCH_CREATE_FILE_FAIL = new ErrorCode(2201003, "批量创建文件失败");
    ErrorCode INVALID_FILE_IDS = new ErrorCode(2201004, "存在无效文件");
    ErrorCode FILE_PATH_NOT_EXIST = new ErrorCode(2201005, "文件路径不存在");
    ErrorCode FILE_NAME_LENGTH_TOO_LONG = new ErrorCode(2201006, "文件名过长，请确保不超过50个字符。");
    ErrorCode UPLOAD_FILE_TOO_LARGE = new ErrorCode(2201007, "上传的文件大小超过限制");
    ErrorCode FILE_EXT_ERROR = new ErrorCode(2201008, "文件后缀名不支持");
    ErrorCode FILE_SIGNATURE_INVALID = new ErrorCode(2201009, "签名已过期或失效");
    ErrorCode FILE_PREVIEW_CONFIG_NOT_EXIST = new ErrorCode(2201010, "文件预览配置不存在");
    ErrorCode FILE_UPLOAD_TYPE_CONFIG_NOT_EXIST = new ErrorCode(2201011, "文件上传类型配置不存在");
    ErrorCode UPLOAD_FILE_FAIL = new ErrorCode(2201012, "上传文件失败");
    ErrorCode DOWNLOAD_FILE_FAIL = new ErrorCode(2201013, "下载文件失败");
    ErrorCode BRIDGE_INVOKE_SERVICE_FAILED = new ErrorCode(2201014, "调用外部接口失败，请稍后尝试。");
    ErrorCode FILE_DELETE_FAIL = new ErrorCode(2201015, "文件删除失败");
    ErrorCode FILE_CONTENT_INVALID = new ErrorCode(2201016, "文件实际内容与文件类型不匹配");
    ErrorCode FILE_VIRUS_DETECTED = new ErrorCode(2201017, "文件安全扫描未通过");
    ErrorCode FILE_VIRUS_SCAN_FAILED = new ErrorCode(2201018, "文件安全扫描服务不可用，请稍后重试");
}
