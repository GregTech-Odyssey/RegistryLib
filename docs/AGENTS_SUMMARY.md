---
title: AGENTS_SUMMARY
nav_exclude: true
search_exclude: true
---

# RegistryLib Docs Agent Summary

## 参与角色

- Agent A：Tutorial-first，关注新手路径、案例驱动、常见坑。
- Agent B：Reference-first，关注 API 边界、链接稳定性、参考完整性。
- Agent C：Docs UX & Just the Docs，关注导航、模板、一致性、视觉可扫读性。

## Round 1：独立诊断的共识

三位 agent 的共同判断如下：

1. 缺少明确的起步入口，新手必须在多页之间来回跳转。
2. 现有页面职责混杂，教程、系统说明和参考信息没有清晰分层。
3. 中英混写导致阅读体验不一致。
4. callout 使用不稳定，尤其是风险信息有堆叠趋势。
5. 缺少 FAQ、故障排查、术语表和 API 速查页。

## Round 2：主要争议点

1. 顶层导航是否会因为新增页面而膨胀。
2. special-optimizations 是否应该拆页，还是原地重写。
3. 教程页里是否还需要保留常用 API 速览区块。
4. AGENTS_SUMMARY 与 STYLE_GUIDE 是否应该出现在主导航。
5. 首页应该强调项目定位还是学习路径入口。

## Round 2：收敛决策

- 采用父页聚合，压缩顶层导航为：首页、学习中心、内容指南、核心系统、API 参考、高级主题、工程与维护。
- 教程页保留“常用 API 速览”，但严格限制为 3-5 个方法，不与 API 参考重复。
- special-optimizations 保留文件名，但重写为 RegistryLib 相关优化策略页。
- AGENTS_SUMMARY 与 STYLE_GUIDE 保存在 docs/，但不进入主导航。
- 首页以学习路径为主，保留简洁的项目定位说明。

## 最终信息架构

```text
首页
学习中心
  quickstart
  faq
  troubleshooting
  glossary
内容指南
  register-items
  register-blocks
  register-block-entities-and-renderers
  register-fluids-and-buckets
  register-advancements
核心系统
  group-system
  tooltip-system
  lang-system
API 参考
高级主题
  override-builders
  special-optimizations
工程与维护
```

## Round 3：签字结果

### Agent A

我同意最终方案。

理由：学习路径完整、模板稳定、入门页与 FAQ / Troubleshooting 分工明确，适合从 0 到 1 的教学节奏。

### Agent B

我同意最终方案。

理由：保留现有核心 URL，新增页承担缺失职能，API 参考与教程页边界清晰，可在不破坏链接稳定性的前提下重构。

### Agent C

我同意最终方案。

理由：顶层导航收敛、父页聚合合理、callout 和模板规则可执行，Just the Docs 下的扫描体验与维护体验都得到改善。

## 一致结论

三位 agent 最终一致同意：

- 全量重写 docs/ 下现有 Markdown 页面。
- 新增学习中心、系统父页、API 参考与必要辅助页。
- 统一中文技术写作风格与版式规则。
- 在不伪造 API 事实的前提下，以“先跑通，再扩展，再查阅”为主线重建文档体系。
