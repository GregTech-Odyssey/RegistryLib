---
title: Override Builders
nav_order: 11
permalink: /override-builders/
---

# Override Builders

`RegistryCore` exposes three **protected factory hooks** that every
`.block()` / `.item()` / `.fluid()` call delegates to. By overriding
these hooks in a subclass you can substitute your own builder classes
 with any custom methods you need  without modifying the library.

This page uses **adding a `.langCn()` method** as the worked example, but the
pattern applies to any per-builder customisation (extra defaults, extra tags,
logging, etc.).

For how to *use* `.langCn()` once the setup is done, see
[Lang System  Approach 2]({{ site.baseurl }}/lang-system/#approach-2--custom-builder-method).

---

## How the Hook System Works

Every public registration method on `RegistryCore` goes through one of these
three overrideable factory methods:

```
RegistryCore.block("name", factory)
  internally calls: newBlockBuilder(parent, name, callback, factory)
  default impl:     BlockBuilder.create(...)
                         
  your override:    ModBlockBuilder.create(...)   inject here
```

```java
// The three hooks in RegistryCore  these are what you override:
protected <T extends Block, P> BlockBuilder<T, P> newBlockBuilder(
        P parent, String name, BuilderCallback callback,
        Function<BlockBehaviour.Properties, T> factory) { ... }

protected <T extends Item, P> ItemBuilder<T, P> newItemBuilder(
        P parent, String name, BuilderCallback callback,
        Function<Item.Properties, T> factory) { ... }

protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
        P parent, String name, BuilderCallback callback,
        FluidBuilder.FluidFactory<T> fluidFactory) { ... }
```

Override all three (or just the ones you need), return your own builder subclass,
and every `.block()` / `.item()` / `.fluid()` call on your `ModRegistryCore`
automatically produces the extended builder.

---

## Java Return-Type Constraint

The single-argument convenience methods
(`block("name", factory)`, `item("name", factory)`, `fluid("name", still, flow)`) are
declared to return `BlockBuilder<T, RegistryCore>` / `ItemBuilder<T, RegistryCore>` / etc.
Because Java generics are invariant, a subclass **cannot** override these to return
`ModBlockBuilder<T, ModRegistryCore>`  the types are incompatible.

The **two-argument** form (`block(P parent, String name, factory)`) **can** be safely
overridden with a covariant return type, because `P` is the same generic variable in
both the parent declaration and the override:

```java
// In ModRegistryCore  covariant override of the two-arg form:
@SuppressWarnings("unchecked")
@Override
public <T extends Block, P> ModBlockBuilder<T, P> block(
        P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
    return (ModBlockBuilder<T, P>) super.block(parent, name, factory);
    // safe: newBlockBuilder() always returns ModBlockBuilder at runtime
}
```

This means callers must use the two-argument form to get `ModBlockBuilder` back
at compile time:

```java
//  ModBlockBuilder  langCn() visible
REGISTRYLIB.block(REGISTRYLIB, "magic_ore", Block::new).langCn(...);

//  BlockBuilder  langCn() not visible (but still datagens correctly at runtime)
REGISTRYLIB.block("magic_ore", Block::new).langCn(...);
```

---

## Implementation  Step by Step

### Step 1  Create the Lang Provider

```java
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn");
    }

    /**
     * Routes callbacks to our own ProviderType.
     * Without this override, callbacks would be written into en_us instead of zh_cn.
     */
    @Override
    protected ProviderType<? extends RegistryLibLangProvider> getProviderType() {
        return ModRegistryCore.LANG_ZH_CN;
    }
}
```

### Step 2  Create ModRegistryCore

```java
public class ModRegistryCore extends RegistryCore {

    /** Shared ProviderType  declares the zh_cn datagen pipeline. */
    public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
            ProviderType.registerClientProvider(
                    "lang_zh_cn",
                    () -> c -> new ZhCnLangProvider(c.parent(), c.output()));

    protected ModRegistryCore(String modid) {
        super(modid);
    }

    public static ModRegistryCore create(String modid) {
        var ret = new ModRegistryCore(modid);
        ModList.get().getModContainerById(modid)
                .ifPresent(c -> ret.registerEventListeners(c.getEventBus()));
        return ret;
    }

    //  Hook overrides 

    @Override
    protected <T extends Block, P> BlockBuilder<T, P> newBlockBuilder(
            P parent, String name, BuilderCallback callback,
            Function<BlockBehaviour.Properties, T> factory) {
        return ModBlockBuilder.create(this, parent, name, callback, factory);
    }

    @Override
    protected <T extends Item, P> ItemBuilder<T, P> newItemBuilder(
            P parent, String name, BuilderCallback callback,
            Function<Item.Properties, T> factory) {
        return ModItemBuilder.create(this, parent, name, callback, factory);
    }

    @Override
    protected <T extends BaseFlowingFluid, P> FluidBuilder<T, P> newFluidBuilder(
            P parent, String name, BuilderCallback callback,
            FluidBuilder.FluidFactory<T> fluidFactory) {
        return ModFluidBuilder.create(this, parent, name, callback, fluidFactory);
    }

    //  Covariant two-argument overrides 
    // These expose the Mod*Builder return type to callers using the (parent, name, factory) form.

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Block, P> ModBlockBuilder<T, P> block(
            P parent, String name, Function<BlockBehaviour.Properties, T> factory) {
        return (ModBlockBuilder<T, P>) super.block(parent, name, factory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Item, P> ModItemBuilder<T, P> item(
            P parent, String name, Function<Item.Properties, T> factory) {
        return (ModItemBuilder<T, P>) super.item(parent, name, factory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> fluid(
            P parent, String name,
            Identifier stillTexture, Identifier flowingTexture,
            FluidBuilder.FluidFactory<T> fluidFactory) {
        return (ModFluidBuilder<T, P>)
                super.fluid(parent, name, stillTexture, flowingTexture, fluidFactory);
    }
}
```

### Step 3  Create ModBlockBuilder

The `create()` static factory **must mirror** the library's defaults
(`defaultBlockstate()`, `defaultLoot()`, `defaultLang()`)
before returning, otherwise these defaults are skipped.

```java
public class ModBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public static <T extends Block, P> ModBlockBuilder<T, P> create(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<BlockBehaviour.Properties, T> factory) {
        var b = new ModBlockBuilder<>(owner, parent, name, callback, factory);
        return (ModBlockBuilder<T, P>) b.defaultBlockstate().defaultLoot().defaultLang();
    }

    protected ModBlockBuilder(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<BlockBehaviour.Properties, T> factory) {
        super(owner, parent, name, callback, factory);
    }

    /** Example custom method  add any methods you need here. */
    public ModBlockBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

### Step 4  Create ModItemBuilder and ModFluidBuilder

Repeat the same pattern. The important requirement is that each `create()` method
applies the same defaults as the library's original `create()` for that builder type.

```java
public class ModItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {

    public static <T extends Item, P> ModItemBuilder<T, P> create(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<Item.Properties, T> factory) {
        var b = new ModItemBuilder<>(owner, parent, name, callback, factory);
        return (ModItemBuilder<T, P>) b.defaultModel().defaultLang();
    }

    protected ModItemBuilder(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<Item.Properties, T> factory) {
        super(owner, parent, name, callback, factory);
    }

    public ModItemBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

```java
public class ModFluidBuilder<T extends BaseFlowingFluid, P> extends FluidBuilder<T, P> {

    public static <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> create(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            FluidBuilder.FluidFactory<T> fluidFactory) {
        var b = new ModFluidBuilder<>(
                owner, parent, name, callback, FluidType::new, fluidFactory);
        return (ModFluidBuilder<T, P>) b.defaultLang().defaultSource()
                .defaultBlock().defaultBucket();
    }

    protected ModFluidBuilder(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            FluidBuilder.FluidTypeFactory typeFactory,
            FluidBuilder.FluidFactory<T> fluidFactory) {
        super(owner, parent, name, callback, typeFactory, fluidFactory);
    }

    public ModFluidBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

### Step 5  Wire up in your mod entry class

Replace `RegistryCore.create(MOD_ID)` with `ModRegistryCore.create(MOD_ID)`:

```java
@Mod(MyMod.MOD_ID)
public class MyMod {
    public static final String MOD_ID = "mymod";

    public static final ModRegistryCore REGISTRYLIB = ModRegistryCore.create(MOD_ID);

    static {
        REGISTRYLIB.defaultCreativeTab("main_tab").register();
        // trigger registration class static initializers...
    }
}
```

---

## Beyond Lang

The same pattern works for any per-builder functionality. Some examples:

```java
// Example: always apply a tag on every block registration
public ModBlockBuilder<T, P> alwaysMineableWithPickaxe() {
    tag(BlockTags.MINEABLE_WITH_PICKAXE);
    return this;
}

// Example: apply a shared default tooltip
public ModItemBuilder<T, P> betaTooltip() {
    tooltip(Component.literal("7[Beta]"));
    return this;
}
```

---

*Back to: [Lang System]({{ site.baseurl }}/lang-system/)*
