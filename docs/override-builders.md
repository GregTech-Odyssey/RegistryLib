---
title: Override Builders
nav_order: 11
permalink: /override-builders/
---

# Override Builders

This tutorial shows how to subclass `RegistryCore` so that every builder your mod
creates already carries custom methods — for example a `.langCn("中文名")` shortcut
for Simplified-Chinese translations.

The result is a mod-local `ModRegistryCore` that you use in place of the plain
`RegistryCore`, with zero changes to the RegistryLib library itself.

---

## Why subclass?

| Approach | Call site |
|----------|-----------|
| **Standard ProviderType** (Approach 1) | `.lang(MyMod.LANG_ZH_CN, "铜币")` |
| **Subclassed RegistryCore** (this page) | `.langCn("铜币")` *(when you have a typed builder reference)* |

Use this approach when you register many entries and want the extra locale to feel
native rather than like a tag-on.

> **Java type-system note.** `RegistryCore.block("name", factory)` is declared to
> return `BlockBuilder<T, RegistryCore>`. Because Java generics are invariant,
> a subclass *cannot* safely override that return type to `ModBlockBuilder<T, ModRegistryCore>`
> without changing the library's API signature. The two-argument form
> `block(parent, "name", factory)`, however, *can* be safely overridden (both parent
> and override share the same generic `P` variable), so `langCn()` is accessible on
> the result of that call. For standard fluent chains the idiomatic choice remains
> `.lang(ModRegistryCore.LANG_ZH_CN, "...")`.

---

## Step 1 — Create ZhCnLangProvider

```java
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn");
    }

    /** Routes callbacks to our own ProviderType, not the default LANG. */
    @Override
    protected ProviderType<? extends RegistryLibLangProvider> getProviderType() {
        return ModRegistryCore.LANG_ZH_CN;
    }
}
```

---

## Step 2 — Create ModRegistryCore

Subclass `RegistryCore`. Declare `LANG_ZH_CN` here so it lives next to the builders
that use it, and override the three builder-factory hooks.

```java
public class ModRegistryCore extends RegistryCore {

    // ── Shared ProviderType ──────────────────────────────────────────────────

    public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
            ProviderType.registerClientProvider(
                    "lang_zh_cn",
                    () -> c -> new ZhCnLangProvider(c.parent(), c.output()));

    // ── Construction ─────────────────────────────────────────────────────────

    protected ModRegistryCore(String modid) {
        super(modid);
    }

    public static ModRegistryCore create(String modid) {
        var ret = new ModRegistryCore(modid);
        ModList.get().getModContainerById(modid)
                .ifPresent(c -> ret.registerEventListeners(c.getEventBus()));
        return ret;
    }

    // ── Builder-factory hooks ────────────────────────────────────────────────
    // Override these three protected methods so every .block() / .item() / .fluid()
    // call on this instance creates a Mod*Builder instead of the plain library type.

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

    // ── Covariant two-argument overrides ─────────────────────────────────────
    // The two-arg (parent, name, factory) form CAN be overridden covariantely
    // because the generic P is the same variable in parent and override.
    // Result: block(REGISTRYLIB, "name", factory) returns ModBlockBuilder directly.

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

---

## Step 3 — Create ModBlockBuilder

Extend `BlockBuilder` and add your locale method. The `create()` factory mirrors
the one in the library; the key addition is `.langCn()`.

```java
public class ModBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public static <T extends Block, P> ModBlockBuilder<T, P> create(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<BlockBehaviour.Properties, T> factory) {
        var b = new ModBlockBuilder<>(owner, parent, name, callback, factory);
        return (ModBlockBuilder<T, P>) b.defaultBlockstate().defaultLoot().defaultLang();
    }

    protected ModBlockBuilder(RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<BlockBehaviour.Properties, T> factory) {
        super(owner, parent, name, callback, factory);
    }

    /** Sets the Simplified-Chinese display name. Sugar for {@code lang(LANG_ZH_CN, name)}. */
    public ModBlockBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

---

## Step 4 — Create ModItemBuilder and ModFluidBuilder

Repeat the same pattern for items and fluids:

```java
// ModItemBuilder — same pattern, extends ItemBuilder
public class ModItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {

    public static <T extends Item, P> ModItemBuilder<T, P> create(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            Function<Item.Properties, T> factory) {
        var b = new ModItemBuilder<>(owner, parent, name, callback, factory);
        return (ModItemBuilder<T, P>) b.defaultModel().defaultLang();
    }

    protected ModItemBuilder(RegistryCore owner, P parent, String name,
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
// ModFluidBuilder — extends FluidBuilder
public class ModFluidBuilder<T extends BaseFlowingFluid, P> extends FluidBuilder<T, P> {

    public static <T extends BaseFlowingFluid, P> ModFluidBuilder<T, P> create(
            RegistryCore owner, P parent, String name,
            BuilderCallback callback,
            FluidBuilder.FluidFactory<T> fluidFactory) {
        var b = new ModFluidBuilder<>(owner, parent, name, callback,
                FluidType::new, fluidFactory);
        return (ModFluidBuilder<T, P>) b.defaultLang().defaultSource()
                .defaultBlock().defaultBucket();
    }

    protected ModFluidBuilder(RegistryCore owner, P parent, String name,
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

---

## Step 5 — Use ModRegistryCore in your mod

Replace `RegistryCore.create(MOD_ID)` with `ModRegistryCore.create(MOD_ID)` in your
mod's entry class:

```java
@Mod(MyMod.MOD_ID)
public class MyMod {
    public static final String MOD_ID = "mymod";

    public static final ModRegistryCore REGISTRYLIB = ModRegistryCore.create(MOD_ID);

    static {
        REGISTRYLIB.defaultCreativeTab("main_tab").register();
        /* trigger static initializers … */
    }
}
```

---

## Using the API

### Standard fluent registration (Approach 1 style)

The no-arg convenience methods (`block("name", factory)`, `item("name", factory)`, etc.)
return the base library types at compile time. Use `.lang(ModRegistryCore.LANG_ZH_CN, "...")`
as you would in Approach 1 — all the zh_cn datagen still flows through the overridden
builder hooks, so nothing is lost.

```java
public static final BlockEntry<Block> MAGIC_ORE = MyMod.REGISTRYLIB
        .block("magic_ore", Block::new)
        .lang("Magic Ore")
        .lang(ModRegistryCore.LANG_ZH_CN, "魔法矿石")
        .register();
```

### Two-argument form to unlock langCn()

When you pass `REGISTRYLIB` as the parent, the covariant two-arg override kicks in
and the return type is `ModBlockBuilder`, giving access to `.langCn()` before any
inherited `BlockBuilder` method is called.

```java
public static final BlockEntry<Block> MAGIC_ORE = MyMod.REGISTRYLIB
        .block(MyMod.REGISTRYLIB, "magic_ore", Block::new)
        .langCn("魔法矿石")           // on ModBlockBuilder — works
        .lang("Magic Ore")            // returns BlockBuilder from here on
        .register();
```

> Call `.langCn()` **before** any method inherited from `BlockBuilder` (e.g. `.lang(String)`,
> `.simpleItem()`, `.tag(...)`) because those methods return `BlockBuilder` (the self-type
> bound at class declaration time), which does not carry `langCn()`.
> This is a Java generics limitation; see the note at the top of this page for details.

### Nested bucket builders

The `bucket(...)` callback receives an `ItemBuilder`, not a `ModItemBuilder`, so use
`.lang(ModRegistryCore.LANG_ZH_CN, "...")` inside bucket callbacks:

```java
REGISTRYLIB.fluid("molten_iron", STILL, FLOW)
    .lang("Molten Iron")
    .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁")
    .bucket(bucket -> bucket
        .lang("Molten Iron Bucket")
        .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁桶"))
    .register();
```

---

*Back to: [Lang System]({{ site.baseurl }}/lang-system/)*
