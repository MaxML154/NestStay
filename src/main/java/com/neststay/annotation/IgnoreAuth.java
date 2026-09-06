package com.neststay.annotation;

import java.lang.annotation.*;

/** 忽略UserToken验证 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {}
