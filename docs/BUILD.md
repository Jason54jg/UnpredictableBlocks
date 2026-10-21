# Build UnpredictableBlocks

## Prérequis

- **JDK 21**
- **Gradle Wrapper** : `./gradlew` (inclus dans le repo)

## Commandes

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
```

Stonecutter active la version MC via `gradle.properties` (`minecraft_version=26.1.2`).

## CI

GitHub Actions (`.github/workflows/ci.yml`) :

1. Régénère textures / structures et signale un diff éventuel.
2. Tente un build Gradle (peut échouer si les artefacts Maven MC 26.x ne sont pas encore publics — `continue-on-error`).

## Dépannage Stonecutter

Si `modImplementation()` est introuvable sur `:common`, lance le build depuis la variante versionnée Stonecutter ou mets à jour le plugin selon [stonecutter.kikugie.dev](https://stonecutter.kikugie.dev).
