---
title: 5 分钟快速开始
parent: 学习中心
nav_order: 1
permalink: /quickstart/
---

# 5 分钟快速开始

本页的目标只有一个：让你在最短路径内看到 RegistryLib 的基本注册链已经工作。

## 适用场景 / 前置条件

- 你已经把 RegistryLib 作为依赖接入工程。
- 你有一个可用的 `RegistryCore` 实例，例如 `RegistryLibTest.REGISTRYLIB`。
- 你只想先跑通一个最小 Item，而不是一次理解全部系统。

## 最小示例

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

这条链同时完成三件事：声明注册名、提供工厂方法、生成英文显示名。

## 你应该如何验证

1. 确保这个字段位于会被类加载的注册类中。
2. 运行工程的常规构建或客户端启动流程。
3. 在游戏内或生成资源中确认 `copper_coin` 已出现，并且显示名为 `Copper Coin`。

{: .important }
> 只写字段定义但没有让所在类参与初始化，注册不会生效。这通常不是 RegistryLib 的问题，而是类加载路径没有被触发。

## 下一步怎么走

- 继续看 [注册 Items]({{ '/register-items/' | relative_url }})，把模型、tooltip、recipe 和 attachment 加进来。
- 如果你在接入依赖时就卡住了，回看 [工程与维护]({{ '/development-and-maintenance/' | relative_url }})。
- 如果你已经遇到“为什么没生成 / 为什么没显示”的问题，直接看 [故障排查]({{ '/troubleshooting/' | relative_url }})。
