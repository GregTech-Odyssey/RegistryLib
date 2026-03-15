---
title: Lang System
parent: 核心系统
nav_order: 3
permalink: /lang-system/
---

# Lang System

Lang System 负责把注册链中的显示名写入语言文件，并允许你把同一套链式写法扩展到额外 locale。

## 何时使用

- 你要为条目生成默认英文显示名。
- 你想在同一条注册链里追加 `zh_cn` 等其他 locale。
- 你想把“项目内常用语言方法”包装成自定义 Builder 语法糖。

## 快速例子

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(MyMod.LANG_ZH_CN, "铜币")
    .register();
```

这个例子使用 ProviderType 方案，在同一条 Item 注册链里同时写入 `en_us` 和 `zh_cn`。

## 核心概念

### 内置英文支持

| 调用 | 作用 |
| --- | --- |
| `.lang("Display Name")` | 写入 `en_us.json` |
| `.defaultLang()` | 从 registry path 推导显示名 |

### 两种扩展方案

| 方案 | 适合什么场景 | 优点 | 限制 |
| --- | --- | --- | --- |
| ProviderType | 只想快速追加 locale | 不需要自定义 Builder 类型 | 调用形式略长 |
| 自定义 Builder 方法 | 项目里会大量使用额外语言方法 | 调用体验更原生 | 需要覆写 `RegistryCore` 与对应 Builder |

### 倒置语言

可以在 `RegistryCore` 上开启 upside-down 英文镜像，但默认关闭。

## 常见组合

- 外层流体类型与桶物品混合使用多语言时，通常外层可用自定义 Builder 方案，桶回调内部继续用 ProviderType 方案。
- 对多数项目来说，先实现 ProviderType 方案，再决定是否值得为常用 locale 做语法糖，是更稳妥的路径。

{: .important }
> 如果你走自定义 Builder 方案，像 `.langCn(...)` 这类方法通常必须在拿到自定义 Builder 后尽早调用；一旦链式返回类型退回基础 Builder，编译期就看不到该方法了。

## 边界与坑

- `.defaultLang()` 适合 registry name 可直接推导展示名的场景，不适合你本来就有明确文案要求的条目。
- 额外 locale 的 ProviderType 必须正确回指自己的 provider 类型，否则写入目标可能跑偏。
- 不要在同一条链上混淆“默认英文”与“项目特定语法糖”这两层职责。

## 相关链接

- [核心系统]({{ '/systems-overview/' | relative_url }})
- [注册 Items]({{ '/register-items/' | relative_url }})
- [Override Builders]({{ '/override-builders/' | relative_url }})
