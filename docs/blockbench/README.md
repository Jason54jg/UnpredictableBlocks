# Lucky Blocks — Blockbench + GeckoLib

## Ce qui est genere
| Fichier | Role |
|---|---|
| `geckolib/models/block/lucky_block.geo.json` | Bloc pose (cube seul, statique) |
| `geckolib/models/entity/lucky_rocket.geo.json` | Fusee : `root` → `block` + `exhaust` → `flame`, `smoke1-3` |
| `geckolib/animations/entity/lucky_rocket.animation.json` | `animation.lucky_rocket.launch` (2 s, hold) |
| `textures/entity/lucky_rocket/lucky_<id>.png` | 32×48 : texture du bloc + bande fumee (gauche) / flamme (droite) |
| `docs/blockbench/lucky_<id>.bbmodel` | Meme hierarchie + memes keyframes, pour previsualiser |

## Deroule in-game (cassage)
1. Le joueur casse le Lucky Block → l'event part, et une entite `lucky_rocket` apparait a sa place.
2. 0–0.5 s : tremblement + compression, fumee au sol.
3. 0.5 s : son de fusee, flamme, l'entite accelere vers le haut en vrillant, trainee de particules.
4. 2 s : nuage de fumee + etincelles, l'entite disparait.

## Blockbench
- **Le plus simple** : Fichier → Ouvrir `lucky_<id>.bbmodel` → onglet **Animate** → **launch** → **Play**.
- **Depuis les fichiers GeckoLib** : Fichier → Importer `lucky_rocket.geo.json`, puis onglet **Animate** →
  Importer une animation → `lucky_rocket.animation.json` (la geo ne contient pas les anims).

La montee elle-meme est faite en Java (l'entite bouge) : dans Blockbench le bloc vrille sur place.
Le `.bbmodel` joue `launch` en boucle ; in-game elle est en `hold_on_last_frame` mais l'entite est retiree a 2 s.
Si tu importes `lucky_rocket.animation.json` a la main, Blockbench la met en « hold » (arret a 2 s) :
regle Loop Mode sur « Loop » dans les proprietes de l'anim pour l'apercu.
Keyframes au format Bedrock `"t": [x, y, z]` : l'ancien format `{"vector": [...]}` s'importe a 0 dans Blockbench.

## Modifier l'anim
Ne pas exporter depuis Blockbench : edite `launch_keyframes()` / `ROCKET_BONES`
dans `tools/sync_geckolib_models.py` puis :
```bash
python tools/sync_geckolib_models.py
```
