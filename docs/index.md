---
title: RegistryLib
nav_order: 1
permalink: /
---

# RegistryLib

RegistryLib 是一个面向 NeoForge 的 fluent registration 库，目标是把常见内容注册从分散的样板代码收拢成一条可组合、可生成资源、可维护的构建链。

如果你第一次接触这个项目，先走“学习中心”；如果你已经知道自己要注册什么内容，直接进入“内容指南”或“API 参考”。

## 你会在这里找到什么

| 入口 | 适合谁 | 你会得到什么 |
| --- | --- | --- |
| [学习中心]({{ '/start-here/' | relative_url }}) | 第一次使用 RegistryLib 的开发者 | 5 分钟跑通、FAQ、排错、术语表 |
| [内容指南]({{ '/content-guides/' | relative_url }}) | 已经知道要注册 Item、Block、Fluid 等内容的人 | 按内容类型组织的教程页 |
| [核心系统]({{ '/systems-overview/' | relative_url }}) | 想理解 Group、Tooltip、Lang 等横切能力的人 | 决策思路、组合方式与边界 |
| [API 参考]({{ '/api-reference/' | relative_url }}) | 需要快速查入口、Builder 族和常用链路的人 | 速查表与职责对照 |
| [高级主题]({{ '/advanced-topics/' | relative_url }}) | 需要自定义 Builder 或理解内部实现策略的人 | 扩展与优化指南 |

{: .note }
> 文档统一使用中文讲解，类型名、方法名、类名保留英文，以便直接对应源码和 IDE 提示。

## 推荐阅读路径

1. 先看 [5 分钟快速开始]({{ '/quickstart/' | relative_url }}) 跑通一个最小 Item。
2. 再根据内容类型进入 [注册 Items]({{ '/register-items/' | relative_url }})、[注册 Blocks]({{ '/register-blocks/' | relative_url }}) 或 [注册 Fluids 和 Buckets]({{ '/register-fluids-and-buckets/' | relative_url }})。
3. 当你开始复用默认值或组织复杂 tooltip 时，再补 [Group System]({{ '/group-system/' | relative_url }})、[Tooltip System]({{ '/tooltip-system/' | relative_url }})、[Lang System]({{ '/lang-system/' | relative_url }})。
4. 需要定制 Builder 时，继续看 [Override Builders]({{ '/override-builders/' | relative_url }})。

## 仓库与工程信息

- [工程与维护]({{ '/development-and-maintenance/' | relative_url }})：依赖接入、本地开发、发布流程与 API 约定。
- [GitHub Repository](https://github.com/GregTech-Odyssey/RegistryLib)
