---
title: Tooltip System
parent: 核心系统
nav_order: 2
permalink: /tooltip-system/
---

# Tooltip System

Tooltip System 用来组织超出 vanilla 单行描述能力的 tooltip 内容。它的核心价值是排序、分区、分盒渲染，以及把自定义视觉元素纳入同一套收集流程。

## 何时使用

- 你只写一行静态说明已经不够。
- 你需要按优先级插入多段 tooltip。
- 你想把次级信息放进独立 tooltip box。
- 你要让 attachment 或自定义节点参与 tooltip 组装。

## 快速例子

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:detail_box", 10, true);

item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
            true, false);
    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
});
```

这个例子会在默认 tooltip 区域插入一行标题，并在下方额外渲染一个独立信息框。

## 核心概念

### `SubNode`

`SubNode` 是最小渲染单元，可以是一行文本，也可以是进度条、图标等自定义元素。

### `RootNode`

`RootNode` 决定一组 `SubNode` 渲染到哪里：

| 模式 | `separateBox` | 效果 |
| --- | --- | --- |
| Inline | `false` | 内容追加到 vanilla tooltip 内部 |
| Separate | `true` | 内容绘制到独立 tooltip box |

### `TooltipNodeCollector`

收集器负责把多个来源的节点汇总起来，包括 Item 自己的 tooltip 回调、BlockItem 的 tooltip，以及 attachment 提供的节点。

{: .note }
> 只需要一句固定说明时，优先使用 `tooltip(Component)`。Tooltip System 适合处理排序、布局或多来源组合，不适合替代所有简单提示。

## 常见组合

- Item 的基本信息留在默认 root，调试信息或次级信息写入独立 `RootNodeRef`。
- `CompositeItemAttachment` 通过 `collectTooltipNodes(...)` 贡献独立节点。
- Block 通过 `.item(item -> ...)` 复用与普通 Item 相同的 tooltip 管线。

## 边界与坑

- `separatorAbove` 和 `separatorBelow` 只是表达布局意图，不是绝对逐像素控制。
- 自定义 `SubNode` 时，`getWidth()` 与 `getHeight()` 必须准确，否则布局会错位。
- tooltip 文本如果需要本地化，优先使用 `Component.translatable(...)`，不要长期依赖 `Component.literal(...)`。

## 相关链接

- [核心系统]({{ '/systems-overview/' | relative_url }})
- [注册 Items]({{ '/register-items/' | relative_url }})
- [注册 Blocks]({{ '/register-blocks/' | relative_url }})
