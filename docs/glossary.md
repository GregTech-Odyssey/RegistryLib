---
title: 术语表
parent: 学习中心
nav_order: 4
permalink: /glossary/
---

# 术语表

## RegistryCore

RegistryLib 的主入口，负责创建 `item(...)`、`block(...)`、`fluid(...)`、`blockEntity(...)` 等注册链。

## Builder

链式配置对象，例如 `ItemBuilder`、`BlockBuilder`、`FluidBuilder`。它负责累积配置，并在 `.register()` 时提交。

## Entry

注册结果的包装类型，例如 `ItemEntry`、`BlockEntry`、`FluidEntry`。它是你在后续代码里引用已注册对象的常见入口。

## Group

包裹在 `RegistryCore` 外层的一组共享默认值。适用于一批条目共同继承 lang 前缀、creative tab 或属性修饰器的场景。

## datagen

数据生成流程，包括语言、模型、配方、掉落、Advancement 等资源输出。

## CompositeItem / attachment

`CompositeItem` 是支持可复用 attachment 的 Item 类型。attachment 用于封装右键行为、tooltip 贡献、tick 行为等扩展逻辑。

## RootNode / SubNode

Tooltip System 中的两个核心概念。`RootNode` 决定内容渲染到哪个区域，`SubNode` 表示实际渲染的文字或自定义元素。
