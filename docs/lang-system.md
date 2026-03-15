---
title: Lang System
nav_order: 10
permalink: /lang-system/
---

# Lang System

RegistryLib generates `en_us.json` (and its upside-down companion `en_ud.json`) automatically
for every registered entry that calls `.lang(...)`. The same mechanism is fully extensible:
you can register **additional lang providers** for any locale — such as `zh_cn` — and attach
translations in the same fluent builder chain.

There are two ways to add extra locales. Choose the one that fits your project best.

---

## Built-in English Lang

Every builder exposes two overloads out of the box:

| Call | Behaviour |
|------|-----------|
| `.lang("Display Name")` | Writes the given name to `en_us.json` (and auto-inverts to `en_ud.json`). |
| `.defaultLang()` | Derives a display name from the registry path (e.g. `magic_ore` → `Magic Ore`). Only available on `BlockBuilder` and `FluidBuilder`. |

```java
// Item – explicit name
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .register();

// Block – auto-derived name
REGISTRYLIB.block("magic_ore", Block::new)
    .defaultLang()   // → "Magic Ore"
    .register();
```

---

## Approach 1 — ProviderType (Minimal Setup)

The fastest way: declare one `ProviderType` constant and call `.lang(type, text)` on any builder.
No changes to `RegistryCore` or builder classes are needed.

### Step 1 — Provider Subclass

Create a class that extends `RegistryLibLangProvider`, passing your locale to the protected
constructor:

```java
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn");
    }

    @Override
    protected ProviderType<? extends RegistryLibLangProvider> getProviderType() {
        return MyMod.LANG_ZH_CN;   // points back to the ProviderType below
    }
}
```

The protected constructor targets the given locale file.  
No upside-down companion is generated for non-English locales.

### Step 2 — Register a ProviderType

Declare a `public static final` constant in your mod's entry class:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

> `registerClientProvider` is a no-op when the game is **not** running datagen, so this
> constant is safe to declare unconditionally.

### Step 3 — Attach Translations

Pass your `ProviderType` as the first argument to `.lang(...)`:

```java
// Item
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(MyMod.LANG_ZH_CN, "铜币")
    .register();

// Block
REGISTRYLIB.block("magic_ore", Block::new)
    .lang("Magic Ore")
    .lang(MyMod.LANG_ZH_CN, "魔法矿石")
    .register();

// Fluid — outer fluid and nested bucket both support the overload
REGISTRYLIB.fluid("molten_iron", STILL, FLOW)
    .lang("Molten Iron")
    .lang(MyMod.LANG_ZH_CN, "熔融铁")
    .bucket(bucket -> bucket
        .lang("Molten Iron Bucket")
        .lang(MyMod.LANG_ZH_CN, "熔融铁桶"))
    .register();
```

This approach works with a plain `RegistryCore` instance and requires no additional infrastructure.

---

## Approach 2 — Subclass RegistryCore (Native Builder Methods)

If you want locale methods that feel native — e.g. `.langCn("铜币")` instead of
`.lang(MyMod.LANG_ZH_CN, "铜币")` — you can subclass `RegistryCore` and add custom builder
subclasses. This is more code upfront but yields a cleaner API for the rest of your mod.

The short version:

```java
// With ModRegistryCore + ModBlockBuilder from the tutorial:
REGISTRYLIB.block(REGISTRYLIB, "magic_ore", Block::new)
    .langCn("魔法矿石")
    .lang("Magic Ore")
    .register();
```

> The two-argument `block(parent, name, factory)` form is used here because Java's generic
> invariance prevents covariant return-type overriding of the one-argument convenience methods.
> See [Override Builders]({{ site.baseurl }}/override-builders/) for the full explanation and
> step-by-step guide.

---

## Full Example (Test Mod)

The bundled test mod demonstrates **both** approaches.

### Approach 2 — Simple* files (langCn)

The Simple* example files use the two-argument `(parent, name, factory)` form to obtain a
`Mod*Builder` and call `.langCn()` directly.

> `langCn()` must be the **first** call after the builder is obtained — before any method
> inherited from `BlockBuilder` / `ItemBuilder` / `FluidBuilder`, since those methods return
> the parent builder type and do not carry `langCn()`.

**`SimpleItemExample.java`**:

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item(RegistryLibTest.REGISTRYLIB, "copper_coin", Item::new) // two-arg → ModItemBuilder
        .langCn("铜币")              // Approach 2: ModItemBuilder.langCn()
        .lang("Copper Coin")
        .register();
```

**`SimpleBlockExample.java`**:

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block(RegistryLibTest.REGISTRYLIB, "decorative_stone", Block::new) // → ModBlockBuilder
        .langCn("装饰石")            // Approach 2: must come before initialProperties / lang
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

**`SimpleFluidExample.java`**:

```java
// Five-arg fluid(parent, name, still, flow, factory) returns ModFluidBuilder.
// clientExtension is already registered internally; no explicit call needed.
public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
        RegistryLibTest.REGISTRYLIB
                .fluid(RegistryLibTest.REGISTRYLIB, "acid",
                        FLUID_STILL, FLUID_FLOW, BaseFlowingFluid.Flowing::new)
                .langCn("酸液")      // Approach 2: ModFluidBuilder.langCn()
                .lang("Acid")
                .register();
```

---

### Approach 1 — Full* files (lang with ProviderType)

The Full* example files use the standard one-argument form and pass `ModRegistryCore.LANG_ZH_CN`
explicitly. This works with any builder regardless of call ordering.

**`ModRegistryCore.java`** — declares the ProviderType:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

**`FullBlockExample.java`** (excerpt):

```java
public static final BlockEntry<Block> MAGIC_ORE = RegistryLibTest.REGISTRYLIB
        .block("magic_ore", Block::new)
        .initialProperties(() -> Blocks.IRON_ORE)
        .lang("Magic Ore")
        .lang(ModRegistryCore.LANG_ZH_CN, "魔法矿石")   // Approach 1
        .loot(...)
        .register();
```

**`FullFluidExample.java`** (excerpt, showing bucket nesting):

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
        RegistryLibTest.REGISTRYLIB
                .fluid("molten_iron", FLUID_STILL, FLUID_FLOW)
                .lang("Molten Iron")
                .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁")   // Approach 1
                .bucket(bucket -> bucket
                    .lang("Molten Iron Bucket")
                    .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁桶"))   // Approach 1 in bucket
                .register();
```

Bucket sub-builders (`ItemBuilder`) are not replaced by `ModItemBuilder`, so Approach 1
remains the correct choice inside `.bucket(...)` callbacks.

---

## API Reference

### `AbstractBuilder`

| Method | Description |
|--------|-------------|
| `lang(Function<T,String> keyProvider)` | Writes auto-derived English name to `en_us`. |
| `lang(Function<T,String> keyProvider, String name)` | Writes `name` under the given key to `en_us`. |
| `lang(ProviderType<? extends RegistryLibLangProvider> type, Function<T,String> keyProvider, String name)` | Writes `name` under the given key to **any** lang provider. |

### `BlockBuilder` / `ItemBuilder`

| Method | Description |
|--------|-------------|
| `lang(String name)` | Sugar for `lang(T::getDescriptionId, name)` → `en_us`. |
| `lang(ProviderType<...> type, String name)` | Sugar for `lang(type, T::getDescriptionId, name)`. |

### `FluidBuilder`

| Method | Description |
|--------|-------------|
| `lang(String name)` | Sugar for fluid type description id → `en_us`. |
| `defaultLang()` | Derives name from `sourceName` → `en_us`. |
| `lang(ProviderType<...> type, String name)` | Sugar for fluid type description id → any locale. |

### `ModBlockBuilder` / `ModItemBuilder` / `ModFluidBuilder` *(test mod)*

| Method | Description |
|--------|-------------|
| `langCn(String name)` | Sugar for `lang(ModRegistryCore.LANG_ZH_CN, name)`. Available when the builder was obtained via the two-argument `block(parent, name, factory)` / `item(parent, name, factory)` / `fluid(parent, name, ...)` form on a `ModRegistryCore` instance. |

### `RegistryLibLangProvider`

| Constructor | Description |
|-------------|-------------|
| `(RegistryCore, PackOutput)` | Built-in constructor; targets `en_us` + `en_ud`. |
| `protected (RegistryCore, PackOutput, String locale)` | Subclass entrypoint; targets the given locale, no upside-down companion. |

---

*See also: [Override Builders]({{ site.baseurl }}/override-builders/) for step-by-step instructions on adding native locale methods to your builders.*
