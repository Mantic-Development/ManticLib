package de.exlll.configlib.annotation;

import com.google.common.annotations.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the field won't be serialized if it's value is null or empty.
 */
@Beta
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreIfEmpty {

    // or if value equals default value
    boolean orIsDefault() default false;


}
