---
title: Lang System
parent: Core Systems
nav_order: 3
permalink: /lang-system/
---

# Lang System

The Lang System writes display names from registration chains into language files and lets you extend the same chaining style to additional locales.

## When to Use It

- You want to generate the default English display name for an entry.
- You want to add `zh_cn` or other locales in the same registration chain.
- You want to wrap frequently used language methods into project-specific Builder syntax sugar.

## Quick Example

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(MyMod.LANG_ZH_CN, "Copper Coin (zh_cn)")
    .register();
```

This example uses the `ProviderType` approach to write both `en_us` and `zh_cn` in the same Item registration chain.

## Core Concepts

### Built-In English Support

| Call | Effect |
| --- | --- |
| `.lang("Display Name")` | Writes to `en_us.json` |
| `.defaultLang()` | Infers the display name from the registry path |

### Two Extension Approaches

| Approach | Best for | Advantage | Limitation |
| --- | --- | --- | --- |
| `ProviderType` | Quickly adding one or more locales | No custom Builder type required | The call form is slightly longer |
| Custom Builder methods | Projects that use extra language helpers heavily | More natural call sites | Requires overriding `RegistryCore` and the corresponding Builders |

### Upside-Down Language

You can enable upside-down English mirroring on `RegistryCore`, but it is off by default.

## Common Combinations

- When the outer fluid type and the bucket item both need multilingual support, the outer chain can use a custom Builder approach while the bucket callback continues using `ProviderType`.
- For most projects, implementing the `ProviderType` path first and only later deciding whether common locales deserve syntax sugar is the safer path.

{: .important }
> If you use a custom Builder approach, methods such as `.langCn(...)` usually need to be called while the chain is still returning your custom Builder type. Once the chain falls back to the base Builder type, those methods disappear at compile time.

## Boundaries and Pitfalls

- `.defaultLang()` is appropriate when the registry name can directly imply the display name. It is not appropriate when you already have a specific wording requirement.
- Extra-locale `ProviderType` values must correctly route back to their own provider type or output may go to the wrong target.
- Do not mix “default English” and “project-specific language sugar” as if they were the same responsibility in one chain.

## Related Links

- [Core Systems]({{ '/systems-overview/' | relative_url }})
- [Registering Items]({{ '/register-items/' | relative_url }})
- [Override Builders]({{ '/override-builders/' | relative_url }})
