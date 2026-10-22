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
# Version active (IDE / défaut = 26.1.2, voir stonecutter.active)
./gradlew :fabric:build
./gradlew :neoforge:build

# Une version précise (props dans versions/<mc>/gradle.properties)
./gradlew :fabric:build :neoforge:build \
  -Pminecraft_version=26.2 \
  -Pminecraft_dependency=~26.2 \
  -Pfabric_api_version=0.161.0+26.2 \
  -Pneoforge_version=26.2.0.93 \
  -Parchitectury_version=22.0.5 \
  -Ptarget_mc_version=26.2

# Toutes les versions supportées (26.1.2, 26.2, 26.3) → build/libs-multi/
./gradlew chiseledBuild
# alias: ./gradlew buildAllMinecraftVersions
```

JARs collectés : `build/libs-multi/<mc>/unpredictableblocks-{fabric|neoforge}-<mod>+<mc>.jar`.

**Important** — le JAR Fabric/NeoForge **embarque** le module `common` (classes + `fabric.mod.json` / assets). Un jar ~2 Ko = packaging cassé (Loader : « non-fabric mod »).

Dépendance runtime **Architectury API** obligatoire dans `mods/` :
- Fabric : `architectury-fabric` (même série MC, ex. 22.0.5 pour 26.2)
- NeoForge : `architectury-neoforge`

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
/unpredictableblocks place boulangerie 100
/unpredictableblocks givekit
```

## CI

`.github/workflows/ci.yml` — assets Python + compilation `:common`, `:fabric`, `:neoforge` en Java 25.

## Multi-version (Stonecutter)

Versions déclarées : **26.1.2**, **26.2**, **26.3**.

| Fichier | Rôle |
|---------|------|
| `versions/<mc>/gradle.properties` | pins MC / Fabric API / NeoForge / Architectury |
| `./gradlew chiseledBuild` | build les 3 versions → `build/libs-multi/<mc>/` |
| `McRefs` / `McAccess` | ponts d’API Mojmap (EntityTypes, structures, loot…) |
