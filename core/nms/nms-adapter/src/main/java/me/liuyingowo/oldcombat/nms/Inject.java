package me.liuyingowo.oldcombat.nms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Inject {

    InjectType injectType() default InjectType.DEFAULT;

    enum InjectType {
        BRIDGE,
        EVENT,
        DEFAULT
    }
}
