package com.trace.common;

import java.lang.annotation.*;

/**
 * 操作日志注解，标注在Controller方法上自动记录日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作描述 */
    String value() default "";
}
