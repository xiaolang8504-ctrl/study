-- 旧“已掌握”缺少答案曝光前独立作答与跨间隔证据，先备份再回到“已订正”。
-- 上线前备份数据库；恢复脚本见 docs/releases/20260921-p0-release.md。
CREATE TABLE IF NOT EXISTS `review_legacy_mastery_reset` (
    `wrong_question_id` bigint NOT NULL,
    `user_id` bigint DEFAULT NULL,
    `previous_status` tinyint NOT NULL,
    `previous_mastered_time` datetime DEFAULT NULL,
    `reset_time` datetime NOT NULL,
    PRIMARY KEY (`wrong_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版掌握状态回退审计';

INSERT IGNORE INTO `review_legacy_mastery_reset`
(`wrong_question_id`, `user_id`, `previous_status`, `previous_mastered_time`, `reset_time`)
SELECT q.`id`, q.`create_id`, q.`status`, i.`mastered_time`, NOW()
FROM `sys_wrong_question` q
LEFT JOIN `sys_review_item` i ON i.`wrong_question_id` = q.`id` AND i.`user_id` = q.`create_id`
WHERE q.`status` = 2
  AND NOT EXISTS (
      SELECT 1 FROM `sys_review_record` r
      WHERE r.`wrong_question_id` = q.`id`
        AND r.`user_id` = q.`create_id`
        AND r.`algorithm_version` = 'stage-v2-evidence'
        AND r.`is_independent` = 1
        AND r.`is_correct` = 1
        AND r.`correct_streak_after` >= 3
  );

UPDATE `sys_wrong_question` q
JOIN `review_legacy_mastery_reset` b ON b.`wrong_question_id` = q.`id`
SET q.`status` = 1, q.`update_time` = NOW()
WHERE q.`status` = 2;

UPDATE `sys_review_item` i
JOIN `review_legacy_mastery_reset` b ON b.`wrong_question_id` = i.`wrong_question_id`
SET i.`mastered_time` = NULL, i.`correct_streak` = 0, i.`update_time` = NOW(), i.`version` = i.`version` + 1
WHERE i.`mastered_time` IS NOT NULL OR i.`correct_streak` > 0;
