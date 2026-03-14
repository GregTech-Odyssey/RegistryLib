package com.gto.registrylib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a convenience shortcut (syntax sugar) that internally delegates to the {@link
 * StandardAPI standard multi-layer fluent API}.
 *
 * <p>Methods annotated with {@code @SyntaxSugar} are not part of the core API contract — they exist
 * solely to reduce boilerplate for common patterns.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyntaxSugar {
  /** Optional description of which standard API call(s) this shortcut wraps. */
  String value() default "";
}
