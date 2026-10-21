# Build UnpredictableBlocks

## Prérequis

- **JDK 25** — JVM Gradle **et** toolchain de compilation (Minecraft 26.1+)
- **Gradle 9.8+** (wrapper inclus : `./gradlew`)

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64   # exemple Linux
./gradlew :common:compileJava :fabric:compileJava :neoforge:compileJava
```

## Toolchain 26.1 (non obfusqué)

| Module | Plugin |
|--------|--------|
| `common`, `fabric` | `dev.architectury.loom-no-remap` 1.17+ (sans `mappings`, `implementation` au lieu de `modImplementation`) |
| `neoforge` | `net.neoforged.moddev` 2.0.x + `neoForge { version = "26.1.2.xxx" }` |

- **Architectury API** : `22.0.5+`
- **Fabric API** : alignée sur `26.1.2` (voir `gradle.properties`)
- **NeoForge** : version complète type `26.1.2.122` (pas `26.1.2` seul)

## Commandes

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew chiseledBuild   # Stonecutter — variantes MC configurées
```

## Assets procéduraux

```bash
pip install -r tools/requirements.txt
python3 tools/generate_textures.py
python3 tools/generate_item_textures.py
python3 tools/generate_structures.py
python3 tools/enrich_loot_tables.py   # uniquement les loot tables placeholder
```

## Tests en jeu (ops)

```
/unpredictableblocks event boulangerie high
/unpredictableblocks casino
/unpredictableblocks place casino 100
/unpredictableblocks givekit
```

## CI

`.github/workflows/ci.yml` — assets Python + compilation `:common`, `:fabric`, `:neoforge` en Java 25.
