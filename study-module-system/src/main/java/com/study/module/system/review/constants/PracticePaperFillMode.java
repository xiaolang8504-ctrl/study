package com.study.module.system.review.constants;

/**
 * 纸面练习卷重复回填策略。
 */
public final class PracticePaperFillMode {

    /** 新建一次独立纸面作答。 */
    public static final String NEW = "NEW";

    /** 覆盖该练习卷最近一次纸面回填。 */
    public static final String OVERWRITE = "OVERWRITE";

    private PracticePaperFillMode() {
    }

    public static boolean valid(String mode) {
        return NEW.equals(mode) || OVERWRITE.equals(mode);
    }
}
