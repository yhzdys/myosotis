package com.yhzdys.myosotis.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyosotisValue {

    String namespace() default "";

    String configKey() default "";

    String defaultValue() default "";
}
