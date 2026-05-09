---
sidebar_position: 7
title: Multi-Language Support
description: Add multiple locales to your registration chains.
---

# Multi-Language Support

This tutorial explains how RegistryLib writes display names and other text into language files, and how to add additional locales such as `zh_cn`.

## What You Will Learn

- Generate default English display names from registration chains.
- Add raw lang entries for non-registered text such as tooltips and creative tab titles.
- Add extra locale files with `locale(...)`, `lang(...)`, and `withLangAlias(...)`.
- Decide when custom Builder methods are worth the maintenance cost.

## Step 1 - Use Built-In English Support

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

Use `.defaultLang()` when the registry name directly implies the correct display name, such as `copper_coin` -> "Copper Coin". Use `.lang(...)` when you have a specific wording requirement.

## Step 2 - Add Raw Text

Some text is not tied to a registered object. Use core lang helpers for tooltips, UI labels, creative tab titles, and other global text:

```java
public static final Component API_TOOLTIP =
        REGISTRYLIB.lang("tooltip.example.api", "RegistryLib API example");
```

The helper returns `Component.translatable(key)` and schedules the lang entry during datagen.

## Step 3 - Add Extra Locales

The shortest current path is `locale(...)`:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        REGISTRYLIB.locale("zh_cn");

REGISTRYLIB.lang(LANG_ZH_CN, "tooltip.example.api", "RegistryLib API example (zh_cn)");
```

You can also use the locale string directly:

```java
REGISTRYLIB.lang("zh_cn", "tooltip.example.api", "RegistryLib API example (zh_cn)");
```

Then use the provider in registration chains:

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(LANG_ZH_CN, "Copper Coin (zh_cn)")
    .register();
```

Both `en_us.json` and `zh_cn.json` are generated from the same code path.

## Step 4 - Reuse Existing Custom Providers

If your project already has a custom provider class, register it directly and let the provider override `getProviderType()` to return the same constant:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

Then alias the locale helper to that existing provider:

```java
protected ModRegistryCore(String modid) {
    super(modid);
    withLangAlias("zh_cn", LANG_ZH_CN);
}
```

This prevents `locale("zh_cn")` from creating a second provider for the same output file.

## Step 5 - Choose Your Extension Approach

| Approach | Best for | Advantage | Limitation |
| --- | --- | --- | --- |
| Locale/lang helpers | Quickly adding one or more locales | No custom Builder type required | Requires a current RegistryLib version |
| `ProviderType` | Explicit provider routing | Works with the base Builder API | The call form is slightly longer |
| Custom Builder methods | Heavily repeated project-specific syntax | Natural call sites such as `.langCn(...)` | Requires maintaining custom Builder types |

For most projects, start with `locale(...)`, `lang(...)`, and `withLangAlias(...)`. Move to custom Builder methods only when a project-specific call style is repeated enough to justify the extra type work.

## Step 6 - Understand Chain Type Narrowing

:::important
If you use a custom Builder approach, methods such as `.langCn(...)` usually need to be called while the chain is still returning your custom Builder type. Once the chain falls back to the base Builder type, those methods disappear at compile time.
:::

For example:

```java
MY_CORE.item("gem", MyItem::new)   // returns MyItemBuilder
    .langCn("Gem (zh_cn)")         // custom method, still MyItemBuilder
    .lang("Gem")                   // base method, may return base builder
    .register();
```

Place custom Builder methods before any call that returns the base type, unless you have overridden that fluent method to preserve your subtype.

## Common Patterns

- Define one locale/provider constant and reuse it for all items, blocks, fluids, tooltips, and tabs.
- Use `withLangAlias(...)` when a custom provider already exists for a locale.
- Use core `lang(...)` helpers for global keys that are not registry-object names.
- Combine lang entries with [Recipes and Tags](/tutorials/recipes-tags) datagen in the same registration chain.

## Boundaries and Pitfalls

:::warning
- `.defaultLang()` is appropriate when the registry name can directly imply the display name. It is not appropriate when you already have a specific wording requirement.
- If you hand-write a custom lang provider, make sure it routes back to its own provider type. Otherwise callbacks may run against the wrong locale.
- Do not mix default English naming and project-specific language sugar as if they were the same responsibility.
:::

## Next Steps

- [Custom Builder](/tutorials/custom-builder) - Build your own Builder subclass with language helpers.
- [Recipes and Tags](/tutorials/recipes-tags) - Combine datagen with your registration chains.
- [API Reference](/reference/api-overview) - Full API surface for lang providers and helper methods.
