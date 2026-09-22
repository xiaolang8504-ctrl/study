package com.study.common.core.validation;

import org.apache.commons.lang3.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 常量约束
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ConstantValue.ValidatorNumber.class, ConstantValue.ValidatorString.class})
public @interface ConstantValue {

    String message() default "无效的常量值";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 获取常量定义类型。
     */
    Class<?> value();

    class ValidatorNumber implements ConstraintValidator<ConstantValue, Integer> {

        private Class<?> clazz;

        /**
         * 初始化相关业务数据。
         */
        @Override
        public void initialize(ConstantValue annotation) {
            clazz = annotation.value();
        }

        /**
         * 校验相关业务数据。
         */
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            if (null == value) {
                return true;
            }

            try {
                for (Field field : clazz.getFields()) {
                    if (value.equals(field.get(null))) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return false;
        }
    }

    class ValidatorString implements ConstraintValidator<ConstantValue, String> {

        private Class<?> clazz;

        /**
         * 初始化相关业务数据。
         */
        @Override
        public void initialize(ConstantValue annotation) {
            clazz = annotation.value();
        }

        /**
         * 校验相关业务数据。
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isBlank(value)) {
                return true;
            }

            try {
                for (Field field : clazz.getFields()) {
                    if (value.equals(field.get(null))) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return false;
        }
    }
}
