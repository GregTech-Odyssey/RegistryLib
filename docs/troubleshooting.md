---
title: 故障排查
parent: 学习中心
nav_order: 3
permalink: /troubleshooting/
---

# 故障排查

这一页按症状组织，而不是按 API 组织。你遇到问题时，先从症状往回缩小范围。

## 症状：注册对象根本不存在

优先检查三件事：

1. 所在类是否被实际加载。
2. 注册链是否以 `.register()` 收尾。
3. 是否使用了正确的 `RegistryCore` 或 `Group` 实例。

## 症状：Block 出现了，但没有 BlockItem

通常是漏写 `.simpleItem()` 或 `.item(...)`。如果你已经显式移除了 tab，也要确认不是展示位置的问题。

## 症状：服务端报 client class 相关错误

最常见于 `BlockEntity` renderer 或其他 client-only 类型被提前引用。检查是否保持了 `Supplier` 惰性加载写法。

{: .warning }
> 只要某个 client class 出现在服务端初始化路径上，就可能触发 `ClassNotFoundException`。不要把 renderer 直接实例化到公共静态字段里。

## 症状：流体表现不对

先确认 still / flow 纹理路径，再确认是否选错了 `clientExtension(...)` 重载。灰度纹理通常搭配三参数版本，已带颜色信息的纹理通常搭配两参数版本。

## 症状：多语言没有进入预期 locale

如果你走 ProviderType 方案，确认 provider 是否返回了正确的 `ProviderType`；如果你走自定义 Builder 方案，确认你拿到的真的是自定义 Builder 类型，而不是回退到了基础 Builder。

## 还找不到答案？

- 先回到对应教程页确认最小链路是否成立。
- 再看 [FAQ]({{ '/faq/' | relative_url }}) 与 [工程与维护]({{ '/development-and-maintenance/' | relative_url }})。
