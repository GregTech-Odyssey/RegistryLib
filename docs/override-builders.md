---
title: Override Builders
parent: 高级主题
nav_order: 1
permalink: /override-builders/
---

# Override Builders

这一页解释如何通过覆写 `RegistryCore` 的 Builder 工厂钩子，把项目内的语法糖或默认规则做成自己的 Builder 类型。

## 何时使用

- 你在多个注册链里反复写同一套项目级规则。
- Group 只能解决默认值复用，但你的需求更像“新增方法”或“改变默认建链体验”。
- 你愿意为更原生的调用方式维护一层项目自定义 Builder。

## 快速例子

```java
public class ModBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public ModBlockBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

这个例子展示了最常见的目标：把额外 locale 封装成项目内部的 Builder 语法糖。

## 核心概念

### 三个工厂钩子

`RegistryCore` 的公开注册入口最终都会经过三个可覆写工厂方法：

- `newBlockBuilder(...)`
- `newItemBuilder(...)`
- `newFluidBuilder(...)`

你覆写这些方法后，就可以把默认 Builder 替换成自己的子类。

### 推荐实现顺序

1. 先写语言或其他 datagen 侧的支持类型。
2. 创建 `ModRegistryCore`，覆写 Builder 工厂方法。
3. 创建 `ModBlockBuilder`、`ModItemBuilder`、`ModFluidBuilder`。
4. 在项目入口里把 `RegistryCore.create(...)` 切换成 `ModRegistryCore.create(...)`。

{: .important }
> 单参数简写入口通常返回基础 Builder 类型。想在编译期拿到你自己的 Builder 子类型，通常需要使用带显式 `parent` 的重载形式。

## 常见组合

- 多语言语法糖：例如 `langCn(...)`、`langTw(...)`。
- 项目内固定 tag 或 tooltip：例如每个机器类方块自动追加统一标签或提示。
- 项目级默认模型 / 默认 tab：适合确实跨越多个模块都稳定存在的规则。

## 边界与坑

- 自定义 Builder 不是为了替代 Group。Group 擅长共享默认值，自定义 Builder 擅长新增语法糖和改变编译期返回类型。
- 自定义 `create()` 工厂时，要确认自己没有丢掉基础 Builder 原本会应用的默认行为。
- 这类扩展会提高项目内部抽象层级，只在规则足够稳定、重复度足够高时才值得做。

## 相关链接

- [高级主题]({{ '/advanced-topics/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [API 参考]({{ '/api-reference/' | relative_url }})
