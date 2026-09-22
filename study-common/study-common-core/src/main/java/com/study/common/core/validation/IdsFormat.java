package com.study.common.core.validation;

import org.springframework.util.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * IDS 格式约束
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = IdsFormat.Validator.class)
public @interface IdsFormat {

    String message() default "IDS 格式错误";

    boolean distinct() default true;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<IdsFormat, String> {

        private boolean distinct;

        /**
         * 初始化相关业务数据。
         */
        public void initialize(IdsFormat constraint) {
            this.distinct = constraint.distinct();
        }

        /**
         * 校验相关业务数据。
         */
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isEmpty(value)) {
                return true;
            }
            if (!value.matches("^[1-9][0-9]*(,[1-9][0-9]*)*$")) {
                return false;
            }
            if (!distinct) {
                return true;
            }
            String[] strings = value.split(",");
            return strings.length == Arrays.stream(strings).distinct().count();
        }
    }
}
