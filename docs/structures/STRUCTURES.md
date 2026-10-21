# Structures instantanées (NBT)

Les fichiers `.nbt` sont générés par :

```bash
pip install nbtlib
python3 tools/generate_structures.py
```

Sortie : `common/src/main/resources/data/unpredictableblocks/structures/<id>.nbt`

| ID structure | Thème | Notes build |
|--------------|-------|-------------|
| `four_geant` | Boulangerie | 5×5×5 briques, feu central |
| `drive_thru` | Fast-Food | Jaune/rouge, fenêtre service |
| `pyramide_pq` | Pénurie 2020 | Laine blanche, coffre sommet |
| `tapis_rouge` | Film Action | 20 blocs, armures or |
| `bunker_zone51` | Complot | Béton souterrain, redstone |
| `piste_disco` | Disco 80 | 7×7 lampes/verre clignotant |
| `chateau_briques` | Plomberie | Drapeau, coffre sommet |
| `galerie_absurde` | Art moderne | Quartz, banane item frame |
| `sanctuaire_doge` | Mèmes | Statue béton Doge |
| `pyramide_piège` | Anachronisme | Piège TNT / sable |
| `scene_spectacle` | Magie comptoir | Rideaux rouges |
| `table_poker_vip` | Casino | Laine verte, sièges cuir |
| `campement_inonde` | Camping | Tente + eau |
| `salle_gym` | Gym Bro | Banc fer/granite |
| `autel_mochi` | Fortune cookie | Rose/blanc |

Référencées par `StructurePlacer` lors des outcomes HIGH/NEUTRAL éligibles.
