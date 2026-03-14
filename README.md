# RegistryLib

A Minecraft mod built with NeoForge.

- **Minecraft**: 26.1-snapshot.11
- **NeoForge**: 26.1
- **Gradle**: 9.0
- **Java**: 25

---

## 作为依赖使用（在你的 Mod 中引入 RegistryLib）

RegistryLib 发布在 GitHub Packages（Maven），需要配置 GitHub Personal Access Token 才能拉取。

### 第一步：生成 GitHub Personal Access Token

1. 登录 GitHub，进入 **Settings → Developer settings → Personal access tokens → Tokens (classic)**
2. 点击 **Generate new token (classic)**
3. 勾选权限：`read:packages`
4. 点击 **Generate token**，复制生成的 token（只显示一次，请妥善保存）

### 第二步：在本机配置 Token

**推荐方式：写入 Gradle 用户全局配置文件**

找到（或创建）`~/.gradle/gradle.properties` 文件（Windows 路径：`C:\Users\你的用户名\.gradle\gradle.properties`），添加：

```properties
gpr.user=你的GitHub用户名
gpr.key=你生成的token
```

> **安全提示**：不要将 token 写入项目内的 `gradle.properties`，也不要提交到版本控制。

### 第三步：在你的项目中添加仓库和依赖

**`settings.gradle`**（推荐写在这里统一管理）：

```groovy
dependencyResolutionManagement {
    repositories {
        maven {
            name = 'GitHubPackages-RegistryLib'
            url = uri('https://maven.pkg.github.com/GregTech-Odyssey/RegistryLib')
            credentials {
                username = settings.providers.gradleProperty('gpr.user').orNull
                        ?: System.getenv('USERNAME')
                password = settings.providers.gradleProperty('gpr.key').orNull
                        ?: System.getenv('TOKEN')
            }
        }
    }
}
```

**`build.gradle`**：

```groovy
dependencies {
    implementation 'com.gto.registrylib:registrylib:1.0.0'
}
```

将版本号替换为你需要的实际版本，可在 [GitHub Packages 页面](https://github.com/GregTech-Odyssey/RegistryLib/packages) 查看所有可用版本。

---

## 发布新版本（维护者）

发布流程由 GitHub Actions 全自动完成，只需：

1. 更新 `gradle.properties` 中的 `mod_version`
2. 提交并推送到仓库
3. 在 GitHub 仓库页面创建一个新的 **Release**（填写 Tag 和说明后点击 Publish）

Actions 会自动执行 `./gradlew build` 和 `./gradlew publish`，将包发布到 GitHub Packages。

---

## 本地开发

1. Clone 仓库
2. 用 IDE 作为 Gradle 项目导入
3. 运行 `./gradlew build` 构建

### 常用 Gradle 任务

| 命令 | 说明 |
|---|---|
| `./gradlew runClient` | 运行 Minecraft 客户端 |
| `./gradlew runServer` | 运行专用服务端 |
| `./gradlew runData` | 运行数据生成器 |
| `./gradlew build` | 构建 Mod JAR |

---

## API Design Conventions

Builder methods are annotated with `@StandardAPI` (core API contract) or `@SyntaxSugar` (shortcut that delegates to a `@StandardAPI` call). See the annotation Javadocs for full definitions.

---

## License

This project is based on [Registrate](https://github.com/tterrag1098/Registrate) by tterrag, licensed under [MPL-2.0](LICENSE). Portions of the source code are derived from or constitute Modifications of Registrate and are therefore subject to the Mozilla Public License, v. 2.0.
