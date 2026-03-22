# GuizhanLib for Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/net.guizhanss/guizhanlib-kt-all.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/net.guizhanss/guizhanlib-kt-all)

The extended [GuizhanLib](https://github.com/ybw0014/guizhanlib) for Kotlin Slimefun/Rebar addon development.

Adopted and extended part of [sf4k](https://github.com/Seggan/sf4k).

## Usage

You have to apply kotlin `stdlib` and `reflect` dependencies to your project on your own.

Then, add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("net.guizhanss:guizhanlib-kt-all:[VERSION]")
}
```

If you only need a specific package, use the specific package name instead of `all`.

### Snapshots

If you want to use snapshot versions, add the following to your `repositories`:

```kotlin
repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}
```

## Manuals

- [Kommand](./KOMMAND_MANUAL.md) for the DSL style command framework
