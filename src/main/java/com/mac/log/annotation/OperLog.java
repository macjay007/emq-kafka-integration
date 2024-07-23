package com.mac.log.annotation;

import java.lang.annotation.*;

/**
*
* @author zj
* @Date 2024/7/23 10:10
**/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    String operModule() default "";

    String operType() default "";

    String operDesc() default "";
}
