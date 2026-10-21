# Lucky Block Boulangerie Française — fiche de génération

## 1. Nom du Bloc & Rareté/Biome

- **ID** : `unpredictableblocks:lucky_boulangerie`
- **Rareté** : Commun (~60 % des spawns naturels)
- **Biomes** : tous biomes terrestres (poids Overworld 1.5)
- **Dimension** : Overworld principalement

## 2. Description visuelle détaillée (PNG 16×16 / 32×32)

- **Base** : cube pain baguette — croûte dorée (#C8A040) avec grignotines transversales tous les 4 px.
- **Motif central** : deux taches chocolat (#5C3317) en diagonale (pépites).
- **Sommet** : mini toque de chef blanche (#F5F5F5) avec bandeau bleu (#2040A0), 6×4 px sur la face top en 16×16.
- **Face avant** : point d’interrogation « ? » crémé (#FFE8B0) en relief, rappel Lucky Block.
- **32×32** : ajouter vapeur blanche semi-transparente (2 px) au-dessus de la toque + reflets specular 1 px sur la croûte.

## 3. Fiche des événements par niveau de chance

| Chance | Tier | Événement |
|--------|------|-----------|
| **+100** | HIGH | **Pluie de Croissants** — 64 pains/cookies, saturation infinie 2 min, loot table `gameplay/lucky/boulangerie/high` |
| **0** | NEUTRAL | **Attaque Pigeons Géants** — 6+ poulets/agressifs, malédiction saut/caquetage |
| **-100** | LOW | **Inondation de Café Chaud** — lave noire (blackstone) zone locale, Speed III + brûlure |
| Structure (HIGH/NEUTRAL) | — | **Four Géant 5×5×5** — `unpredictableblocks:four_geant` |

## 4. Quête thématique

- **Advancement** : `unpredictableblocks:quest/boulangerie`
- **Titre** : *Le Chef Manque d'Ingrédients*
- **Objectifs** : vaincre l’essaim de « Pigeons Géants » ; déposer 32 `minecraft:bread` (critère custom à brancher sur `minecraft:player_killed_entity` + `minecraft:inventory_changed`).
- **Récompenses** : loot `unpredictableblocks:rewards/quest_boulangerie` → item Lucky Block avec `Luck:100` ; déblocage craft **Baguette Magique**.

## 5. Configuration / code

**Loot table (high)** — `data/unpredictableblocks/loot_table/gameplay/lucky/boulangerie/high.json` :

```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:bread",
          "functions": [{ "function": "minecraft:set_count", "count": 64 }]
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [{ "type": "minecraft:item", "name": "unpredictableblocks:baguette_magique" }]
    }
  ]
}
```

**Résolution Java (Mojmap)** :

```java
LuckResolver.ResolvedOutcome outcome =
    LuckResolver.resolve(new LuckStat(blockLuck), PlayerLuckStorage.getModifier(player), level.random);
LuckyBlockEventDispatcher.dispatch(level, player, pos, LuckyTheme.BOULANGERIE, outcome);
```

**Spawn (extrait config)** — clé `themes.boulangerie` générée au premier lancement dans `config/unpredictableblocks.json`.

## 6. Objets custom & structure NBT

| Asset | Description |
|-------|-------------|
| `baguette_magique` | Bâton, Knockback X (data components / NBT enchant), lore FR |
| `four_geant.nbt` | 5×5×5 briques rouges, four central avec feu camp + coffre loot pain enchanté |
| Son | `unpredictableblocks:entity.pigeon.caquetage` (optionnel, à ajouter) |

Structure : exporter depuis structure block vers `common/src/main/resources/data/unpredictableblocks/structures/four_geant.nbt`.
