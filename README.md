# RegistryLib

A Minecraft mod built with NeoForge.

- **Minecraft**: 26.1-snapshot.11
- **NeoForge**: 26.1
- **Gradle**: 9.0
- **Java**: 25

---

## Using as a Dependency

RegistryLib is published to GitHub Packages (Maven). A GitHub Personal Access Token is required to resolve the package.

### Step 1: Generate a GitHub Personal Access Token

1. Log in to GitHub and go to **Settings → Developer settings → Personal access tokens → Tokens (classic)**
2. Click **Generate new token (classic)**
3. Check the scope: `read:packages`
4. Click **Generate token** and copy the token (it is only shown once — save it somewhere safe)

### Step 2: Configure the Token on Your Machine

**Recommended: Set Windows user environment variables**

Run the following in CMD (replace with your actual values):

```cmd
setx GITHUB_ACTOR your-github-username
setx GITHUB_TOKEN your-token
```

**Restart your terminal** after running these commands for the variables to take effect.

> **Security Notice**:
> - `setx` writes **user-level** environment variables, which are readable by all applications running under your user account. This carries a token exposure risk if malicious software runs on the same machine.
> - The token is stored in plaintext in the Windows registry (`HKEY_CURRENT_USER\Environment`).
> - **Not recommended on shared or production machines** — use a dedicated secrets manager instead.
> - Limiting the token to only `read:packages` scope minimizes the impact of any potential exposure.

### Step 3: Add the Repository and Dependency to Your Project

**`settings.gradle`** (recommended — keeps repository config in one place):

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

**`build.gradle`**:

```groovy
dependencies {
    implementation 'com.gto.registrylib:registrylib:1.0.0'
}
```

Replace the version with the version you need. All available versions can be found on the [GitHub Packages page](https://github.com/GregTech-Odyssey/RegistryLib/packages).

---

## Publishing a New Version (Maintainers)

The publish workflow is fully automated via GitHub Actions:

1. Update `mod_version` in `gradle.properties` (e.g. `1.0.0`)
2. Commit and push to the repository
3. Go to the GitHub repository page → **Actions → Gradle Package → Run workflow**, then click **Run workflow**

Actions will automatically:
- Read the current `mod_version` and generate a tag such as `v1.0.0-build1`
- If the tag already exists, auto-increment to `v1.0.0-build2`, `v1.0.0-build3`, etc.
- Run `./gradlew build` and `./gradlew publish` to publish the package to GitHub Packages
- Create a corresponding GitHub Release with the built JAR attached

---

## Local Development

1. Clone the repository
2. Import as a Gradle project in your IDE
3. Run `./gradlew build` to build

### Common Gradle Tasks

| Command | Description |
|---|---|
| `./gradlew runClient` | Launch the Minecraft client |
| `./gradlew runServer` | Launch a dedicated server |
| `./gradlew runData` | Run data generators |
| `./gradlew build` | Build the mod JAR |

---

## API Design Conventions

Builder methods are annotated with `@StandardAPI` (core API contract) or `@SyntaxSugar` (shortcut that delegates to a `@StandardAPI` call). See the annotation Javadocs for full definitions.

---

## License

This project is based on [Registrate](https://github.com/tterrag1098/Registrate) by tterrag, an absolutely brilliant piece of work — its elegant design makes registry management in Minecraft mods a genuine pleasure to use. The thoughtful API, the fluent builder patterns, and the sheer amount of boilerplate it eliminates are a testament to tterrag's craftsmanship. We are deeply grateful for this incredible open-source contribution to the modding community.

Licensed under [MPL-2.0](LICENSE). Portions of the source code are derived from or constitute Modifications of Registrate and are therefore subject to the Mozilla Public License, v. 2.0.
