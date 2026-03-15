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

## Adding Extra Locales

Any locale can be added by:

1. **Creating a provider subclass** that extends `RegistryLibLangProvider` with your locale code.
2. **Registering a `ProviderType`** via `ProviderType.registerClientProvider`.
3. **Calling `.lang(type, "text")`** on any builder.

### Step 1 — Provider Subclass

```java
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn"); // locale passed to parent
    }
}
```

The protected constructor `RegistryLibLangProvider(owner, packOutput, locale)` writes directly to
the given locale file. No upside-down companion is generated for non-English locales.

### Step 2 — Register a ProviderType

Declare a `public static final` field in your mod's entry class so every registration file can
access it:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

> `registerClientProvider` is a no-op when the game is **not** running datagen, so this field
> is safe to declare unconditionally. The factory lambda is only invoked during `runData`.

### Step 3 — Attach Translations

Pass your `ProviderType` as the first argument to `.lang(...)`:

```java
// Item
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(RegistryLibTest.LANG_ZH_CN, "铜币")
    .register();

// Block
REGISTRYLIB.block("magic_ore", Block::new)
    .lang("Magic Ore")
    .lang(RegistryLibTest.LANG_ZH_CN, "魔法矿石")
    .register();

// Fluid — outer fluid and nested bucket both support the overload
REGISTRYLIB.fluid("molten_iron", STILL, FLOW)
    .lang("Molten Iron")
    .lang(RegistryLibTest.LANG_ZH_CN, "熔融铁")
    .bucket(bucket -> bucket
        .lang("Molten Iron Bucket")
        .lang(RegistryLibTest.LANG_ZH_CN, "熔融铁桶"))
    .register();
```

---

## Full Example (Test Mod)

The test mod ships `ZhCnLangProvider` and demonstrates the pattern across all entry types.

**`RegistryLibTest.java`** — one-time setup:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

**`SimpleItemExample.java`**:

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .lang(RegistryLibTest.LANG_ZH_CN, "铜币")
        .register();
```

**`SimpleBlockExample.java`**:

```java
public static final BlockEntry<Block> DECORATIVE_STONE = RegistryLibTest.REGISTRYLIB
        .block("decorative_stone", Block::new)
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .lang(RegistryLibTest.LANG_ZH_CN, "装饰石")
        .simpleItem()
        .register();
```

**`SimpleFluidExample.java`**:

```java
public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
        RegistryLibTest.REGISTRYLIB
                .fluid("acid", FLUID_STILL, FLUID_FLOW)
                .lang("Acid")
                .lang(RegistryLibTest.LANG_ZH_CN, "酸液")
                .clientExtension(FLUID_STILL, FLUID_FLOW)
                .register();
```

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

### `RegistryLibLangProvider`

| Constructor | Description |
|-------------|-------------|
| `(RegistryCore, PackOutput)` | Built-in constructor; targets `en_us` + `en_ud`. |
| `protected (RegistryCore, PackOutput, String locale)` | Subclass entrypoint; targets the given locale, no upside-down companion. |
