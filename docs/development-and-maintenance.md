---
title: 工程与维护
nav_order: 7
permalink: /development-and-maintenance/
---

# 工程与维护

这一页收集仓库级信息：如何把 RegistryLib 作为依赖接入、如何本地开发、如何理解 API 注解约定，以及如何发布新版本。

## 作为依赖使用

RegistryLib 通过 GitHub Packages 发布。解析依赖时需要一个具备 `read:packages` 权限的 GitHub Token。

### 第一步：准备 GitHub Token

1. 登录 GitHub。
2. 进入 Settings → Developer settings → Personal access tokens → Tokens (classic)。
3. 生成一个仅包含 `read:packages` 的 token。

### 第二步：配置本机环境变量

```cmd
setx GITHUB_ACTOR your-github-username
setx GITHUB_TOKEN your-token
```

执行后重开终端，让新环境变量生效。

{: .important }
> `setx` 会把 token 明文写入当前用户环境变量。不要在共享机器或生产环境上用高权限 token，最小化权限范围。

### 第三步：配置仓库与依赖

推荐的 `settings.gradle`：

```groovy
dependencyResolutionManagement {
    repositories {
        maven {
            name = 'GitHubPackages-RegistryLib'
            url = uri('https://maven.pkg.github.com/GregTech-Odyssey/RegistryLib')
            credentials {
                username = System.getenv('GITHUB_ACTOR')
                        ?: settings.providers.gradleProperty('gpr.user').orNull
                password = System.getenv('GITHUB_TOKEN')
                        ?: settings.providers.gradleProperty('gpr.key').orNull
            }
        }
    }
}
```

`build.gradle`：

```groovy
dependencies {
    implementation 'com.gto.registrylib:registrylib:1.0.0'
}
```

精确版本以当前仓库发布记录为准。

## 本地开发

1. 克隆仓库。
2. 以 Gradle 工程导入 IDE。
3. 运行构建或对应运行任务验证环境。

### 常用 Gradle 任务

| 命令 | 用途 |
| --- | --- |
| `./gradlew runClient` | 启动 Minecraft 客户端 |
| `./gradlew runServer` | 启动独立服务端 |
| `./gradlew runData` | 运行数据生成 |
| `./gradlew build` | 构建 mod JAR |

## API 设计约定

Builder 方法使用 `@StandardAPI` 与 `@SyntaxSugar` 标识不同层级的 API 职责。

- `@StandardAPI` 表示核心契约入口。
- `@SyntaxSugar` 表示便捷语法糖，最终仍会委托到标准入口。

阅读 Builder API 时，优先把 `@StandardAPI` 视为长期稳定入口，再把 `@SyntaxSugar` 视为常见场景的快捷写法。

## 发布新版本

发布流程由 GitHub Actions 自动化完成。

1. 更新 `gradle.properties` 中的 `mod_version`。
2. 提交并推送代码。
3. 在 GitHub Actions 中运行发布工作流。

工作流会自动构建、发布并创建对应的 GitHub Release。