package com.gto.registrylib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a convenience shortcut that delegates to one or more {@link StandardAPI} calls.
 *
 * <p>
 * A {@code @SyntaxSugar} method collapses a fixed, commonly-used {@code @StandardAPI} invocation
 * into a single no-argument (or minimal-argument) call. The exact expansion is always documented in
 * {@link #value()}.
 *
 * <pre>{@code
 * block.simpleItem();   // @SyntaxSugar("item($ -> {})")
 * block.defaultLoot();  // @SyntaxSugar("loot(RegistryLibBlockLootTables::dropSelf)")
 * }</pre>
 *
 * <p>
 * {@code @SyntaxSugar} methods are <b>not</b> part of the API contract:
 *
 * <ul>
 * <li>They may be added or removed without being considered a breaking change.
 * <li>They offer no additional behaviour over the underlying {@code @StandardAPI} call.
 * <li>When the default does not fit, use the underlying {@code @StandardAPI} method directly.
 * </ul>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface SyntaxSugar {

    /** Optional description of which standard API call(s) this shortcut wraps. */
    String value() default "";
}
