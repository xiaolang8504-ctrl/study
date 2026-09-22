package com.study.common.core.validation;

import org.springframework.util.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 组合常量约束
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {AssembleConstant.Validator.class})
public @interface AssembleConstant {

    String message() default "无效的组合常量";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 获取常量定义类型。
     */
    Class<?> value();

    class Validator implements ConstraintValidator<AssembleConstant, String> {

        private Class<?> clazz;

        /**
         * 初始化相关业务数据。
         */
        @Override
        public void initialize(AssembleConstant annotation) {
            clazz = annotation.value();
        }

        /**
         * 校验相关业务数据。
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isEmpty(value)) {
                return true;
            }
            try {
                List<Integer> idList = new ArrayList<>();
                for (Field field : clazz.getDeclaredFields()) {
                    idList.add((Integer) field.get(null));
                }
                for (String idStr : value.split(",")) {
                    if (!idList.contains(Integer.parseInt(idStr))) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }
}
