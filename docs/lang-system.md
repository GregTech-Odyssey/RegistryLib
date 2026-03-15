---
title: Lang System
nav_order: 10
permalink: /lang-system/
---

# Lang System

RegistryLib auto-generates `en_us.json` for every registered entry that calls `.lang(...)`.
The same mechanism is fully extensible —
you can attach extra lang providers for any locale in the same fluent builder chain.

---

## Built-in English

Every builder type exposes these overloads out of the box:

| Call | Result |
|------|--------|
| `.lang("Display Name")` | Writes to `en_us.json`. |
| `.defaultLang()` | Derives a name from the registry path (`magic_ore` → `Magic Ore`). Available on `BlockBuilder` and `FluidBuilder` only. |

An upside-down mirror (`en_ud.json`) can be enabled on the `RegistryCore` instance:

```java
public static final RegistryCore REGISTRYLIB =
        RegistryCore.create(MOD_ID).upsideDownLang(true);
```

{: .note }
> This is disabled by default.

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .register();

REGISTRYLIB.block("magic_ore", Block::new)
    .defaultLang()   // → "Magic Ore"
    .register();
```

---

## Adding Extra Locales

Two approaches are available; both write to the same locale file.

| | Approach 1 — ProviderType | Approach 2 — Custom Builder Method |
|-|---------------------------|---------------------------------|
| **One-time setup** | 1 provider class + 1 `ProviderType` constant | `ModRegistryCore` + `Mod*Builder` classes (see [Override Builders]({{ site.baseurl }}/override-builders/)) |
| **Call site** | `.lang(LANG_ZH_CN, "text")` anywhere | `.langCn("text")` — must come first in the chain |
| **Works in bucket callbacks** | Yes | No — use Approach 1 inside `.bucket(...)` |
| **Best for** | Quick addition, any number of builders | Many entries, native-feeling API |

---

## Approach 1 — ProviderType

### Setup

**1. Create a provider subclass** that sets the locale:

```java
public class ZhCnLangProvider extends RegistryLibLangProvider {

    public ZhCnLangProvider(RegistryCore owner, PackOutput packOutput) {
        super(owner, packOutput, "zh_cn");
    }

    @Override
    protected ProviderType<? extends RegistryLibLangProvider> getProviderType() {
        return MyMod.LANG_ZH_CN;  // points back to the constant below
    }
}
```

The protected constructor writes directly to the given locale file.
No upside-down companion is generated for non-English locales.

**2. Declare a `ProviderType` constant** in your mod entry class:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

`registerClientProvider` is a no-op outside of datagen, so the constant is safe to
declare unconditionally.

### Usage

Pass the `ProviderType` as the first argument to
`lang(ProviderType, String)`.
Call order does not matter; the callback is deferred to datagen.

```java
// Item
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(MyMod.LANG_ZH_CN, "铜币")
    .register();

// Block
REGISTRYLIB.block("magic_ore", Block::new)
    .initialProperties(() -> Blocks.IRON_ORE)
    .lang("Magic Ore")
    .lang(MyMod.LANG_ZH_CN, "魔法矿石")
    .register();

// Fluid — outer fluid type and nested bucket item both support the overload
REGISTRYLIB.fluid("molten_iron", STILL, FLOW)
    .lang("Molten Iron")
    .lang(MyMod.LANG_ZH_CN, "熔融铁")
    .bucket(bucket -> bucket
        .lang("Molten Iron Bucket")
        .lang(MyMod.LANG_ZH_CN, "熔融铁桶"))
    .register();
```

---

## Approach 2 — Custom Builder Method

This approach adds a `.langCn(String)` method directly to your builder classes by
subclassing `RegistryCore` and the three builder types. **One-time setup only** —
see [Override Builders]({{ site.baseurl }}/override-builders/) for step-by-step
implementation instructions.

Once the setup is complete, use the **two-argument `(parent, name, factory)` form**
to get the custom builder type back at compile time:

```java
// The one-arg form block("name", factory) returns BlockBuilder<T, RegistryCore> —
// no langCn(). Passing REGISTRYLIB as the explicit parent triggers the covariant
// override and returns ModBlockBuilder<T, P> instead.
REGISTRYLIB.block(REGISTRYLIB, "magic_ore", Block::new)
    .langCn("魔法矿石")      //  ← on ModBlockBuilder
    .lang("Magic Ore")       //  ← returns BlockBuilder from here on
    .initialProperties(() -> Blocks.IRON_ORE)
    .register();
```

### Call ordering constraint

`langCn()` **must be the first call** after the builder is obtained. Once any method
inherited from `BlockBuilder` / `ItemBuilder` / `FluidBuilder` is called (e.g.
`.lang(String)`, `.properties(...)`, `.simpleItem()`), the chain's static type reverts
to the parent builder and `.langCn()` is no longer visible.

```java
// ✅ correct — langCn() before any inherited method
REGISTRYLIB.item(REGISTRYLIB, "copper_coin", Item::new)
    .langCn("铜币")
    .lang("Copper Coin")
    .register();

// ❌ wrong — .lang(String) returns ItemBuilder, langCn() no longer available
REGISTRYLIB.item(REGISTRYLIB, "copper_coin", Item::new)
    .lang("Copper Coin")
    .langCn("铜币")   // compile error
    .register();
```

### Fluids

Use the five-argument `fluid(parent, name, still, flow, factory)` form:

```java
REGISTRYLIB.fluid(REGISTRYLIB, "acid", STILL, FLOW, BaseFlowingFluid.Flowing::new)
    .langCn("酸液")
    .lang("Acid")
    .register();
```

### Bucket sub-builders

The `.bucket(consumer)` callback receives an `ItemBuilder` (not `ModItemBuilder`),
because the library's `FluidBuilder` passes a plain builder to that slot.
Use **Approach 1** inside bucket callbacks:

```java
REGISTRYLIB.fluid(REGISTRYLIB, "molten_iron", STILL, FLOW, BaseFlowingFluid.Flowing::new)
    .langCn("熔融铁")                              // Approach 2 on outer fluid
    .lang("Molten Iron")
    .bucket(bucket -> bucket
        .lang("Molten Iron Bucket")
        .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁桶"))  // Approach 1 inside bucket
    .register();
```

---

## API Reference

### `AbstractBuilder`

| Method | Description |
|--------|-------------|
| `lang(Function<T,String> keyProvider)` | Auto-derives English name and writes to `en_us`. |
| `lang(Function<T,String> keyProvider, String name)` | Writes `name` under the key to `en_us`. |
| `lang(ProviderType<? extends RegistryLibLangProvider>, Function<T,String> keyProvider, String name)` | Writes `name` under the key to **any** registered lang provider. |

### `BlockBuilder` / `ItemBuilder`

| Method | Description |
|--------|-------------|
| `lang(String name)` | Sugar — writes `name` under `T::getDescriptionId` to `en_us`. |
| `lang(ProviderType<...> type, String name)` | Sugar — writes `name` under `T::getDescriptionId` to the given provider. |

### `FluidBuilder`

| Method | Description |
|--------|-------------|
| `lang(String name)` | Writes `name` for the fluid type to `en_us`. |
| `defaultLang()` | Derives name from the source fluid's registry path → `en_us`. |
| `lang(ProviderType<...> type, String name)` | Writes `name` for the fluid type to the given provider. |

### `ModBlockBuilder` / `ModItemBuilder` / `ModFluidBuilder` *(test mod — Approach 2)*

| Method | Available when | Description |
|--------|---------------|-------------|
| `langCn(String name)` | Immediately after `block(parent, ...)` / `item(parent, ...)` / `fluid(parent, ...)` on a `ModRegistryCore` | Sugar for `lang(ModRegistryCore.LANG_ZH_CN, name)`. |

### `RegistryLibLangProvider`

| Constructor | Description |
|-------------|-------------|
| `(RegistryCore, PackOutput)` | Targets `en_us`. |
| `protected (RegistryCore, PackOutput, String locale)` | Subclass entry point; targets the given locale, no upside-down companion. |

---

*For one-time setup of Approach 2, see [Override Builders]({{ site.baseurl }}/override-builders/).*
