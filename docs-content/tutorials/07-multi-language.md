---
sidebar_position: 7
title: Multi-Language Support
description: Add multiple locales to your registration chains.
---

# Multi-Language Support

## What You Will Learn

In this tutorial you will learn how the Lang System writes display names into language files and how to extend it to additional locales. By the end you will be able to:

- Generate default English display names from registration chains.
- Add extra locales (e.g. `zh_cn`) using the `ProviderType` approach.
- Decide when to use custom Builder methods for language sugar.
- Avoid common pitfalls around chain type narrowing.

## Step 1 �?Use Built-In English Support

Every builder provides two ways to set the English display name:

| Call | Effect |
| --- | --- |
| `.lang("Display Name")` | Writes to `en_us.json` |
| `.defaultLang()` | Infers the display name from the registry path |

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .register();
```

:::tip
Use `.defaultLang()` when the registry name directly implies the correct display name (e.g. `copper_coin` �?"Copper Coin"). Use `.lang(...)` when you have a specific wording requirement.
:::

## Step 2 �?Add Extra Locales with ProviderType

The simplest way to add another locale is through the `ProviderType` approach. First define your locale's `ProviderType` (typically in your mod's init class):

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang/zh_cn",
                () -> c -> new RegistryLibLangProvider(c.parent(), c.output(), "zh_cn"));
```

Then use it in your registration chain:

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(MyMod.LANG_ZH_CN, "铜币")
    .register();
```

Both `en_us.json` and `zh_cn.json` are generated from the same chain.

## Step 3 �?Choose Your Extension Approach

There are two approaches for multi-language support. Choose the one that fits your project:

| Approach | Best for | Advantage | Limitation |
| --- | --- | --- | --- |
| `ProviderType` | Quickly adding one or more locales | No custom Builder type required | The call form is slightly longer |
| Custom Builder methods | Projects that use extra language helpers heavily | More natural call sites (e.g. `.langCn(...)`) | Requires overriding `RegistryCore` and the corresponding Builders |

For most projects, start with the `ProviderType` path. Only move to custom Builder methods if you find yourself repeating the same locale call across dozens of entries.

## Step 4 �?Understand Chain Type Narrowing

:::important
If you use a custom Builder approach, methods such as `.langCn(...)` usually need to be called while the chain is still returning **your custom Builder type**. Once the chain falls back to the base Builder type, those methods disappear at compile time.
:::

For example, this works:

```java
MY_CORE.item("gem", MyItem::new)   // returns MyItemBuilder
    .langCn("宝石")                  // custom method �?still MyItemBuilder
    .lang("Gem")                     // base method �?returns base builder
    .register();
```

But reordering would fail if `.lang(...)` narrows the type before `.langCn(...)` is reached. Place custom Builder methods **before** any call that returns the base type.

See the [Custom Builder](/tutorials/custom-builder) tutorial for the full pattern.

## Common Patterns

- For items, blocks, and fluids that all need the **same** locale, define one `ProviderType` constant and reuse it everywhere.
- When an outer fluid type and its bucket item both need multilingual support, the outer chain can use a custom Builder approach while the bucket callback uses `ProviderType`.
- Combine with [Recipes and Tags](/tutorials/recipes-tags) datagen in the same registration chain for a complete single-chain setup.

## Boundaries and Pitfalls

:::warning
- `.defaultLang()` is appropriate when the registry name can directly imply the display name. It is **not** appropriate when you already have a specific wording requirement.
- Extra-locale `ProviderType` values must correctly route back to their own provider type or output may go to the wrong target.
- Do not mix "default English" and "project-specific language sugar" as if they were the same responsibility in one chain.
:::

## Next Steps

- [Custom Builder](/tutorials/custom-builder) �?Build your own Builder subclass with language helpers.
- [Recipes and Tags](/tutorials/recipes-tags) �?Combine datagen with your registration chains.
- [API Reference](/reference/api-overview) �?Full API surface for `ProviderType` and `RegistryLibLangProvider`.
