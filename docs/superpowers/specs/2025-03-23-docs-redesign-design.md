# RegistryLib 文档重构设计方案

**日期**: 2025-03-23
**状态**: 待审核
**作者**: Claude

## 1. 项目背景

### 1.1 当前问题

RegistryLib 现有文档使用 Jekyll + Just the Docs 主题，存在以下问题：

1. **视觉平淡** - 基础主题，缺乏品牌识别度
2. **缺少视觉元素** - 无图表、流程图、截图
3. **教程风格枯燥** - 技术准确但缺乏吸引力
4. **代码示例密集** - 缺少渐进式引导
5. **学习曲线陡峭** - 新手需要同时理解多个概念

### 1.2 目标受众

**混合受众**：同时服务 Minecraft Modding 新手和有经验的开发者，需要分层的学习路径。

### 1.3 设计目标

将文档从"技术正确但枯燥"转变为"友好亲切且易于上手"，具体目标：

- 新手能在 15 分钟内跑通第一个 mod
- 有经验者能快速找到所需 API
- 视觉设计温暖友好，支持暗色/亮色模式

## 2. 技术选型

### 2.1 文档框架

**选择**: Docusaurus

**理由**:
- React 组件化，易于定制
- 内置搜索、版本管理
- Markdown + MDX 支持
- 丰富的插件生态
- 更容易实现"友好亲切"风格

### 2.2 托管平台

**选择**: GitHub Pages

**理由**:
- 免费
- 与现有 Jekyll 方案一致
- 原生支持 Docusaurus 部署

### 2.3 迁移路径

1. 在项目中创建 `website/` 目录存放 Docusaurus 站点
2. 逐步迁移现有 Markdown 内容
3. 保持 `docs/` 目录作为源文件，`website/` 读取并构建

## 3. 信息架构

### 3.1 Diátaxis 框架

按 Diátaxis 文档系统重组为四种类型：

| 目录 | 类型 | 目的 | 内容示例 |
|------|------|------|----------|
| `tutorials/` | Tutorial | 学习导向 | 新手入门、进阶教程 |
| `how-to/` | How-to Guide | 任务导向 | 如何注册物品、如何添加配方 |
| `reference/` | Reference | 信息导向 | API 快速查询、Builder 方法列表 |
| `concepts/` | Explanation | 理解导向 | 什么是 RegistryLib、设计决策说明 |

### 3.2 新的文档结构

```
website/
├── docs/
│   ├── tutorials/
│   │   ├── beginner/
│   │   │   ├── 01-installation.md
│   │   │   ├── 02-first-item.md
│   │   │   ├── 03-first-block.md
│   │   │   └── 04-understanding-chain.md
│   │   └── intermediate/
│   │       ├── 01-group-system.md
│   │       ├── 02-tooltip-system.md
│   │       ├── 03-multi-language.md
│   │       └── 04-recipes-tags.md
│   ├── how-to/
│   │   ├── register-items.md
│   │   ├── register-blocks.md
│   │   ├── register-fluids.md
│   │   ├── register-advancements.md
│   │   └── add-recipes.md
│   ├── reference/
│   │   ├── api-quick-reference.md
│   │   ├── entry-types.md
│   │   ├── builder-methods.md
│   │   └── configuration-options.md
│   └── concepts/
│       ├── what-is-registrylib.md
│       ├── builder-pattern.md
│       ├── datagen-principles.md
│       └── design-decisions.md
├── src/
│   └── components/  # 自定义 React 组件
└── docusaurus.config.js
```

### 3.3 三层学习路径

#### 🌱 新手路径 (0 → 能用)

**目标**: 15 分钟内跑通第一个 mod

| 步骤 | 内容 | 预计时间 |
|------|------|----------|
| 1 | 安装与配置 | 3 分钟 |
| 2 | 你的第一个物品 | 5 分钟 |
| 3 | 你的第一个方块 | 5 分钟 |
| 4 | 理解注册链 | 2 分钟 |

#### 🚀 进阶路径 (能用 → 用好)

**目标**: 掌握高效开发模式

| 步骤 | 内容 |
|------|------|
| 1 | Group 批量管理 |
| 2 | Tooltip 系统 |
| 3 | 多语言支持 |
| 4 | 配方和标签 |

#### ⚡ 专家路径 (用好 → 精通)

**目标**: 深度定制和扩展

| 步骤 | 内容 |
|------|------|
| 1 | 自定义 Builder |
| 2 | 性能优化 |
| 3 | 源码架构 |
| 4 | 贡献代码 |

## 4. 视觉设计

### 4.1 配色方案

#### 亮色模式

| 用途 | 色值 | 说明 |
|------|------|------|
| 主色调 | `#fbbf24` | 温暖黄 |
| 背景 | `#fffbeb` | 浅黄背景 |
| 成功 | `#10b981` | 绿色 |
| 信息 | `#3b82f6` | 蓝色 |
| 警告 | `#ef4444` | 红色 |

#### 暗色模式

| 用途 | 色值 | 说明 |
|------|------|------|
| 主色调 | `#fbbf24` | 温暖黄（保持一致） |
| 背景 | `#1a1b26` | 深蓝紫（Tokyo Night 风格） |
| 文字 | `#c0caf5` | 浅蓝白 |
| 强调 | `#e0af68` | 金橙色 |

### 4.2 代码块样式

- 深色背景 `#292d3e`
- 语法高亮（类似 VS Code）
- 复制按钮
- 语言标签

### 4.3 特殊组件

| 组件 | 亮色模式 | 暗色模式 | 用途 |
|------|----------|----------|------|
| 💡 小贴士 | 黄色背景 + 边框 | 深色背景 + 金色边框 | 实用建议 |
| 🤔 常见问题 | 蓝色背景 + 边框 | 深色背景 + 蓝色边框 | FAQ 预览 |
| ⚠️ 警告 | 红色背景 + 边框 | 深色背景 + 红色边框 | 重要注意事项 |
| 🚀 下一步 | 紫色背景 + 边框 | 深色背景 + 紫色边框 | 学习引导 |

## 5. 交互功能

### 5.1 核心功能

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 🌙 暗/亮模式切换 | 用户偏好保存到 localStorage | P0 |
| 📋 代码复制 | 一键复制代码块 | P0 |
| 🔍 全局搜索 | 快捷键 Ctrl+K | P0 |
| 📖 面包屑导航 | 显示当前位置 | P0 |
| ↔️ 上一篇/下一篇 | 页面底部翻页 | P0 |
| 📊 目录大纲 | 右侧显示页面结构 | P1 |
| 📝 编辑此页 | 跳转 GitHub 编辑 | P1 |

### 5.2 Docusaurus 插件

```javascript
// docusaurus.config.js
plugins: [
  '@docusaurus/plugin-content-docs',
  '@docusaurus/plugin-client-redirects',  // 旧链接重定向
  '@docusaurus/theme-search-algolia',     // 搜索（可选）
]
```

## 6. 内容风格指南

### 6.1 写作风格

#### 避免

- 冷冰冰的技术描述
- "显而易见..."
- "只需..."
- 大段无解释的代码
- 假设读者已知晓

#### 推荐

- 温暖友好的语气
- "接下来我们..."
- "这里你可能想问..."
- 代码带行内注释
- 解释"为什么"不只是"怎么做"

### 6.2 教程页面模板

```markdown
---
title: [动词开头的标题]
---

# 🎯 [标题]

**预计时间**: X 分钟 | **难度**: 🌱 新手 / 🚀 进阶 / ⚡ 专家

## 👋 你将学到

- 要点 1
- 要点 2
- 要点 3

## 最小可运行示例

\`\`\`java
// 代码示例（带注释）
\`\`\`

## 逐步解释

1. 第一步...
2. 第二步...

## ✅ 试试看

运行 `./gradlew runClient` 验证结果。

## 🚀 下一步

继续学习 [下一个主题](链接)
```

### 6.3 参考页面模板

```markdown
---
title: [API/方法名称]
---

# 📋 [标题]

一句话说明用途。

## 方法列表

| 方法 | 参数 | 返回值 | 用途 |
|------|------|--------|------|
| xxx  | ...  | ...    | ... |

## 示例

\`\`\`java
// 1-3行简短示例
\`\`\`

## 相关链接

- [详细教程](链接)
```

### 6.4 视觉元素

| 类型 | 用途 | 工具 |
|------|------|------|
| 📊 流程图 | 注册流程、Builder 链 | Mermaid / Excalidraw |
| 🗂️ 架构图 | 类关系、系统边界 | Mermaid / Excalidraw |
| 📸 截图 | 游戏内效果、IDE 配置 | 实际截图 |

## 7. 迁移计划

### 7.1 内容映射

| 现有文件 | 新位置 | 类型 |
|----------|--------|------|
| `quickstart.md` | `tutorials/beginner/01-installation.md` + `02-first-item.md` | Tutorial |
| `register-items.md` | `how-to/register-items.md` | How-to |
| `register-blocks.md` | `how-to/register-blocks.md` | How-to |
| `register-fluids-and-buckets.md` | `how-to/register-fluids.md` | How-to |
| `register-advancements.md` | `how-to/register-advancements.md` | How-to |
| `group-system.md` | `tutorials/intermediate/01-group-system.md` | Tutorial |
| `tooltip-system.md` | `tutorials/intermediate/02-tooltip-system.md` | Tutorial |
| `lang-system.md` | `tutorials/intermediate/03-multi-language.md` | Tutorial |
| `api-reference.md` | `reference/api-quick-reference.md` | Reference |
| `override-builders.md` | `tutorials/expert/01-custom-builder.md` | Tutorial |
| `faq.md` | 保留在根目录 | Support |
| `troubleshooting.md` | 保留在根目录 | Support |
| `glossary.md` | 保留在根目录 | Support |

### 7.2 需要新建的内容

| 新文件 | 内容 |
|--------|------|
| `concepts/what-is-registrylib.md` | 项目定位和核心理念 |
| `concepts/builder-pattern.md` | Builder 模式详解 |
| `concepts/design-decisions.md` | 设计决策说明 |
| `tutorials/beginner/04-understanding-chain.md` | 注册链原理解析 |

## 8. 成功指标

| 指标 | 目标 |
|------|------|
| 新手完成第一个 mod 时间 | < 15 分钟 |
| 页面加载时间 | < 2 秒 |
| 搜索响应时间 | < 500ms |
| 移动端可用性 | 良好 |
| 无障碍访问 | WCAG 2.1 AA |

## 9. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 迁移工作量大 | 中 | 分阶段迁移，保持旧链接可用 |
| Docusaurus 学习曲线 | 低 | 文档完善，社区活跃 |
| 自定义组件开发 | 中 | 优先使用现成主题，按需定制 |

## 10. 附录

### A. 参考资源

- [Diátaxis 文档系统](https://diataxis.fr/)
- [Docusaurus 官方文档](https://docusaurus.io/)
- [优秀文档案例](https://vuejs.org/, https://react.dev/, https://tailwindcss.com/)

### B. 技术栈版本

| 依赖 | 版本 |
|------|------|
| Node.js | >= 18.0 |
| Docusaurus | 3.x |
| React | 18.x |
