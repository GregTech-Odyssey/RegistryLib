---
sidebar_position: 9
title: Custom Builders
description: Create project-specific Builder types with custom syntax sugar.
---

# Custom Builders

This page explains how to turn project-specific syntax sugar or default rules into your own Builder types by overriding the Builder factory hooks in `RegistryCore`.

## When to Use It

- You keep repeating the same project-level rules across multiple registration chains.
- Group can only solve shared defaults, but your requirement is closer to "add new methods" or "change the default chaining experience".
- You are willing to maintain a project-specific Builder layer for a more native call style.

## Prefer Built-In Helpers First

Before adding a custom Builder subclass, check whether the base API already has the hook you need:

- Locale/lang helper APIs cover most extra-language call sites without chain type narrowing.
- `addRecipeData(...)` gives typed recipe datagen callbacks without a custom recipe wrapper.
- `existingItem(...)`, `existingBlock(...)`, `tagExisting(...)`, and batch tag helpers cover tag additions for vanilla or third-party objects.
- Lazy entry wrappers, including data component type entries, let you pass registered values through suppliers instead of forcing early resolution.

Use custom Builders when the project needs new fluent syntax or a project-specific compile-time return type. For isolated datagen callbacks or tags, the lighter helpers are usually easier to maintain.

## Quick Example

```java
public class ModBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public static <T extends Block, P> ModBlockBuilder<T, P> create(
            RegistryCore owner,
            P parent,
            String name,
            Function<BlockBehaviour.Properties, T> factory) {
        var builder = new ModBlockBuilder<>(owner, parent, name, factory);
        return (ModBlockBuilder<T, P>) builder.defaultBlockstate().defaultLoot().defaultLang();
    }

    public ModBlockBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

This is the real pattern used in the test project: the custom Builder adds `langCn(...)`, and its static `create(...)` factory preserves the same default chain that the standard path would have applied.

## Core Concepts

### Builder Injection Points

For Blocks and Items the public registration methods directly construct the Builders, so the right place to inject custom Builder types is a covariant override of those public methods:

- Override `block(parent, name, factory)` to swap in your `ModBlockBuilder`.
- Override `item(parent, name, factory, isComponentItem)` and the convenience overloads to swap in your `ModItemBuilder`.

For Fluids, `RegistryCore` exposes a protected factory hook:

- Override `newFluidBuilder(parent, name, fluidFactory)` to return your `ModFluidBuilder`.

:::note
The old `BuilderCallback` style is no longer the preferred extension point. Current RegistryLib versions keep extension hooks lighter: override the public registration methods when you need a custom compile-time Builder type, and use built-in helper APIs for common cases such as locale/lang sugar, recipe data callbacks, and existing-object tags before creating a custom Builder layer.
:::

### The Full Flow

In the test project, the extension has four moving parts that work together:

1. Declare a new provider-side capability, here `ModRegistryCore.LANG_ZH_CN` backed by `ZhCnLangProvider`.
2. Add custom Builder subclasses such as `ModBlockBuilder`, `ModItemBuilder`, and `ModFluidBuilder`.
3. Override the public `block(...)` and `item(...)` registration methods in a custom `RegistryCore` subclass so they instantiate those Builders. For Fluids, override `newFluidBuilder(...)` when needed.
4. Override the public registration methods that can legally return your subtype at compile time.

If you skip step 4, your runtime object may still be a custom Builder, but the compiler can fall back to the base `BlockBuilder`, `ItemBuilder`, or `FluidBuilder` type and your new sugar methods will disappear from the chain.

### Recommended Implementation Order

1. Implement the language-side or other datagen-side support types first.
2. Create `ModRegistryCore` and override the public registration methods, plus `newFluidBuilder` for fluids if your custom fluid Builder needs to be injected.
3. Create `ModBlockBuilder`, `ModItemBuilder`, and `ModFluidBuilder`.
4. Switch the project entry point from `RegistryCore.create(...)` to `ModRegistryCore.create(...)`.

:::important
Item entry points can be overridden covariantly in the test project, so `item("name", factory)` still exposes `langCn(...)`. For Blocks and Fluids, the safe compile-time path is the overload that keeps an explicit `parent`, such as `block(parent, name, factory)` or `fluid(parent, ...)`.
:::

## Step-by-Step Implementation

### Step 1: Add the Shared Capability

`ModRegistryCore` defines a project-wide `ProviderType` that can be reused by every custom Builder:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

This is what makes `langCn(...)` more than a string helper. It points at a real datagen target.

### Step 2: Replace `RegistryCore` with Your Own Subclass

```java
public class ModRegistryCore extends RegistryCore {

    protected ModRegistryCore(String modid) {
        super(modid);
    }

    public static ModRegistryCore create(String modid) {
        return new ModRegistryCore(modid);
    }
}
```

Your project entry point should now be created through `ModRegistryCore.create(...)`, not `RegistryCore.create(...)`. Otherwise your Builder hooks never run.

### Step 3: Override the Builder Injection Points

For Blocks and Items, override the public registration methods in your `ModRegistryCore` subclass:

```java
@Override
public <T extends Block, P> ModBlockBuilder<T, P> block(
        P parent,
        String name,
        Function<BlockBehaviour.Properties, T> factory) {
    return ModBlockBuilder.create(this, parent, name, factory);
}

@Override
public <T extends Item, P> ModItemBuilder<T, P> item(
        P parent,
        String name,
        Function<Item.Properties, T> factory,
        boolean isComponentItem) {
    return ModItemBuilder.create(this, parent, name, factory, isComponentItem);
}
```

For Fluids, override `newFluidBuilder(...)` when you need to replace the default fluid Builder:

```java
@Override
protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
        P parent,
        String name,
        FluidBuilder.FluidFactory<T> fluidFactory) {
    return ModFluidBuilder.create(this, parent, name, fluidFactory);
}
```

This is the runtime swap. Public registration entry points call these methods before the chain is returned.

### Step 4: Preserve the Default Bootstrap Chain

Your custom static `create(...)` methods should keep the same baseline behavior the project expects:

- `ModBlockBuilder.create(...)` ends with `defaultBlockstate().defaultLoot().defaultLang()`.
- `ModItemBuilder.create(...)` ends with `defaultModel().defaultLang()`.
- `ModFluidBuilder.create(...)` ends with `defaultLang().defaultSource().defaultBlock().defaultBucket()`.

If you replace the Builder type but forget these defaults, the syntax sugar compiles, but the generated resources or related registrations no longer match the normal project behavior.

:::important
The custom Builder factory is part of the contract. If you only add `langCn(...)` and forget the inherited default chain, your project will silently regress in datagen or related object creation.
:::

### Step 5: Expose the Subtype Where Java Allows It

The test project overrides the public methods that can return a more specific Builder type safely:

```java
@Override
public <T extends Block, P> ModBlockBuilder<T, P> block(
        P parent,
        String name,
        Function<BlockBehaviour.Properties, T> factory) {
    return (ModBlockBuilder<T, P>) super.block(parent, name, factory);
}

@Override
public <T extends Item> ModItemBuilder<T, RegistryCore> item(
        String name,
        Function<Item.Properties, T> factory) {
    return item(this, name, factory, false);
}

@Override
public <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> fluid(
        P parent,
        String name,
        Identifier stillTexture,
        Identifier flowingTexture,
        FluidBuilder.FluidFactory<T> fluidFactory) {
    return (ModFluidBuilder<T, P>) super.fluid(parent, name, stillTexture, flowingTexture, fluidFactory);
}
```

The important nuance is the one noted in `ModRegistryCore`: Java generic invariance prevents some shorthand forms from being overridden with the exact project-specific subtype you would like. That is why the explicit-`parent` overloads matter for Blocks and Fluids.

### Step 6: Use the Right Entry Point in Real Code

Item chains in the test project can use the short form directly:

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .langCn("Copper Coin (zh_cn)")
        .lang("Copper Coin")
        .register();
```

For Blocks and Fluids, use the explicit-`parent` overload when you want the custom Builder API to remain visible:

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block(RegistryLibTest.REGISTRYLIB, "decorative_stone", Block::new)
        .langCn("Decorative Stone (zh_cn)")
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> ACID = RegistryLibTest.REGISTRYLIB
        .fluid(RegistryLibTest.REGISTRYLIB, "acid", STILL, FLOW, BaseFlowingFluid.Flowing::new)
        .langCn("Acid (zh_cn)")
        .lang("Acid")
        .register();
```

### Step 7: Call Custom Sugar Before You Fall Back to the Base Type

In the block and fluid examples above, `langCn(...)` appears before methods such as `initialProperties(...)`. That ordering is intentional.

Once a chain step returns the base Builder type, the compiler no longer sees your project-specific methods unless you explicitly override those fluent methods too. In practice, this means custom sugar should usually be placed early in the chain.

## Common Combinations

- Multilingual syntax sugar such as `langCn(...)` and `langTw(...)`.
- Project-level fixed tags or tooltip rules, such as machine-family Blocks automatically receiving a shared label or hint.
- Project-level default models or default tabs when the rule is genuinely stable across modules.

## Boundaries and Pitfalls

- Custom Builders are not meant to replace Group. Group is good at shared defaults, while custom Builders are good at new syntax sugar and custom compile-time return types.
- When creating a custom `create()` factory, verify that you did not drop important default behavior from the base Builder path.
- Verify the compile-time type, not only the runtime type. If your IDE no longer offers `langCn(...)`, you probably entered through a shorthand overload that returned the base Builder type.
- For Blocks and Fluids, keep the explicit `parent` overload in mind as part of the API design, not as an incidental workaround.
- This kind of extension raises the abstraction level inside the project, so it is only worth doing when the rule is stable and heavily repeated.

## Related Pages

- [Multi-Language Support](/tutorials/multi-language)
- [Group System](/tutorials/group-system)
- [API Overview](/reference/api-overview)
