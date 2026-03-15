package com.gto.registrylib.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as part of the core fluent API contract.
 *
 * <p>
 * Every {@code @StandardAPI} method belongs to one of three categories:
 *
 * <ul>
 * <li><b>Leaf configuration</b> — configures a single property of the current entry and returns
 * {@code this} builder, enabling method chaining.
 * 
 * <pre>{@code
 * block.properties(p -> p.strength(3.5F))
 *         .lang("Crusher")
 * }</pre>
 * 
 * <li><b>Sub-resource configuration</b> — accepts a {@link java.util.function.Consumer Consumer}
 * scoped to a nested builder. The sub-entry is automatically registered after the consumer
 * completes; the method returns the <em>parent</em> builder so chaining continues at the
 * outer level.
 * 
 * <pre>{@code
 * block.item(item -> item.tab(MY_TAB))  // returns BlockBuilder, not ItemBuilder
 *      .blockEntity(be -> ...);
 * }</pre>
 * 
 * <li><b>Terminal</b> — {@code register()} finalises the entry, submits it to the registry, and
 * returns a {@code RegistryEntry} handle. It must be the last call in a builder chain.
 * Consumer-scoped entry points (e.g. {@code RegistryCore.block(name, factory, config ->)})
 * call {@code register()} implicitly.
 * </ul>
 *
 * <p>
 * {@code @SyntaxSugar} methods are <em>not</em> marked {@code @StandardAPI} — see {@link
 * SyntaxSugar} for the distinction.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface StandardAPI {

    /** Optional description of the API behavior represented by this method. */
    String value() default "";
}
