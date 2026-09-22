package com.study.common.core.validation;

import org.springframework.util.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * IDS 数量约束
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = IdsSize.Validator.class)
public @interface IdsSize {

    String message() default "数量需在{min}到{max}范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    class Validator implements ConstraintValidator<IdsSize, String> {

        private int min;

        private int max;

        /**
         * 初始化相关业务数据。
         */
        public void initialize(IdsSize constraint) {
            min = constraint.min();
            max = constraint.max();
        }

        /**
         * 校验相关业务数据。
         */
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isEmpty(value)) {
                return true;
            }

            int length = value.split(",").length;

            return min <= length && length <= max;
        }
    }
}
