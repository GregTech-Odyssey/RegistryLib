---
title: Development and Maintenance
nav_order: 2
permalink: /development-and-maintenance/
---

# Development and Maintenance

This page collects the repository-level information needed by developers and maintainers, including
dependency setup, local development workflow, and release publishing.

It also documents the API annotation conventions used throughout RegistryLib so readers can quickly
distinguish core builder contracts from convenience shortcuts.

---

## Using as a Dependency

RegistryLib is published to GitHub Packages (Maven). A GitHub Personal Access Token is required to
resolve the package.

### Step 1: Generate a GitHub Personal Access Token

1. Log in to GitHub and go to **Settings → Developer settings → Personal access tokens → Tokens (classic)**
2. Click **Generate new token (classic)**
3. Check the scope: `read:packages`
4. Click **Generate token** and copy the token

### Step 2: Configure the Token on Your Machine

Recommended: set Windows user environment variables.

```cmd
setx GITHUB_ACTOR your-github-username
setx GITHUB_TOKEN your-token
```

Restart your terminal after running these commands.

Security notice:

- `setx` writes user-level environment variables that are readable by applications running under your account.
- The token is stored in plaintext in `HKEY_CURRENT_USER\Environment`.
- Avoid this approach on shared or production machines.
- Restricting the token to `read:packages` minimizes impact if exposed.

### Step 3: Add the Repository and Dependency to Your Project

Recommended `settings.gradle`:

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

`build.gradle`:

```groovy
dependencies {
    implementation 'com.gto.registrylib:registrylib:1.0.0'
}
```

Replace the version with the one you need. Available versions are listed on the GitHub Packages page.

---

## Local Development

1. Clone the repository.
2. Import it as a Gradle project in your IDE.
3. Run `./gradlew build` to build.

### Common Gradle Tasks

| Command | Description |
| --- | --- |
| `./gradlew runClient` | Launch the Minecraft client |
| `./gradlew runServer` | Launch a dedicated server |
| `./gradlew runData` | Run data generators |
| `./gradlew build` | Build the mod JAR |

---

## API Design Conventions

Builder methods are annotated with `@StandardAPI` and `@SyntaxSugar`.

- `@StandardAPI` marks the core API contract.
- `@SyntaxSugar` marks a convenience method that delegates to a `@StandardAPI` call.

When reading the builder APIs, treat `@StandardAPI` methods as the canonical entry points and
`@SyntaxSugar` methods as shortcuts for common cases.

---

## Publishing a New Version

The publish workflow is fully automated via GitHub Actions.

1. Update `mod_version` in `gradle.properties`.
2. Commit and push to the repository.
3. Open the repository on GitHub.
4. Go to **Actions → Gradle Package → Run workflow**.

The workflow will:

- Read the current `mod_version` and generate a tag such as `v1.0.0-build1`.
- Auto-increment if the tag already exists.
- Run `./gradlew build` and `./gradlew publish`.
- Create a corresponding GitHub Release with the built JAR attached.