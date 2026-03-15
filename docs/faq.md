---
title: 常见问题 FAQ
parent: 学习中心
nav_order: 2
permalink: /faq/
---

# 常见问题 FAQ

这一页收集最常见的“为什么没有发生”类问题，优先给出最短判断路径。

## 注册链相关

### 为什么我的条目没有出现？

先检查注册类是否真的被加载，其次确认链尾是否调用了 `.register()`。

### 为什么 `.attach(...)` 不能用于普通 `Item`？

因为 attachment 是 `CompositeItem` 的扩展点。普通 `Item` 没有对应的 attachment 生命周期。

### 为什么我写了 Block 但游戏里没有对应 BlockItem？

因为 Block 和 BlockItem 是两个独立注册对象。需要显式调用 `.simpleItem()` 或 `.item(...)`。

## datagen 与语言相关

### 为什么 `.lang(...)` 没有出现在我预期的语言文件里？

先确认你走的是 RegistryLib 的 datagen 管线，再确认对应页面中的 ProviderType 或默认语言配置是否正确。

### `.defaultLang()` 和 `.lang(...)` 应该选哪个？

当 registry name 足以推导出显示名时，用 `.defaultLang()`；当你需要特定展示文案时，用 `.lang(...)`。

## 结构与设计相关

### 什么时候应该用 Group？

当多个条目共享 lang 前缀、creative tab、block/item 属性默认值时，用 Group；单个孤立条目通常直接走 `RegistryCore`。

### 什么时候应该自定义 Builder？

当你反复需要同一套项目内语法糖或默认规则，并且这些规则无法只靠 Group 或普通链式调用表达时，再看 [Override Builders]({{ '/override-builders/' | relative_url }}).

## 继续阅读

- [故障排查]({{ '/troubleshooting/' | relative_url }})
- [术语表]({{ '/glossary/' | relative_url }})
- [API 参考]({{ '/api-reference/' | relative_url }})
