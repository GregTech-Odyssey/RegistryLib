package com.gto.registrylib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a standard API entry point.
 *
 * <p>Covers three method categories:
 *
 * <ul>
 *   <li><b>Leaf configuration</b> — configures the current entry, returns {@code this} builder.
 *   <li><b>Sub-resource configuration</b> — accepts a {@link java.util.function.Consumer} of a
 *       nested builder, automatically registers the sub-entry after the consumer completes, returns
 *       the parent builder.
 *   <li><b>Terminal</b> — {@code register()} finalises the entry and returns a {@code
 *       RegistryEntry}.
 * </ul>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface StandardAPI {
  /** Optional description of the API behavior represented by this method. */
  String value() default "";
}
