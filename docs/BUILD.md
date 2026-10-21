# Build UnpredictableBlocks

## Prérequis

- **JDK 25** (Minecraft 26.1.2 l’exige via Loom)
- **Gradle Wrapper** : `./gradlew`

## Commandes

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew chiseledBuild   # Stonecutter — toutes les variantes MC configurées
```

## Stonecutter / Gradle

Chaque module (`common`, `fabric`, `neoforge`) déclare **Loom + Architectury** avec versions explicites dans son `build.gradle` (compatibilité évaluation Stonecutter).

## Tests en jeu (ops)

```
/unpredictableblocks event boulangerie high
/unpredictableblocks casino
/unpredictableblocks place casino 100
/unpredictableblocks givekit
```

## CI

`.github/workflows/ci.yml` — regénération des assets + build Gradle (Java 25).
