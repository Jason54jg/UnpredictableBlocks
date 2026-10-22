#!/usr/bin/env python3
"""Textes du journal des quetes (objectifs + indices + libelles UI), FR et EN.

Cles : quest.unpredictableblocks.<theme>.<critere> (objectif), quest.unpredictableblocks.<theme>.hint (indice).
Les criteres doivent correspondre a Quests.java. Lancer : python tools/quest_texts.py
"""
from __future__ import annotations

import json
import os
import time
from pathlib import Path

LANG = Path(__file__).resolve().parents[1] / "common/src/main/resources/assets/unpredictableblocks/lang"

# theme -> ([(critere, objectif FR, objectif EN)], indice FR, indice EN)
QUESTS = {
    "anachronisme": ([("kill_ravager", "Vaincre un Ravageur (ou le Toi du Futur)", "Defeat a Ravager (or Future You)")],
                     "Les Ravageurs accompagnent les raids : tuer un capitaine de pillards déclenche un raid au village.",
                     "Ravagers come with raids: kill a pillager captain, then enter a village."),
    "boulangerie": ([("kill_poultry", "Vaincre 10 poulets", "Defeat 10 chickens"),
                     ("have_bread", "Avoir 32 pains dans l'inventaire", "Hold 32 bread in your inventory")],
                    "Le blé pousse vite avec de la poudre d'os. Les deux objectifs sont nécessaires.",
                    "Bone meal speeds up wheat. Both objectives are required."),
    "bsod": ([("glitched", "Subir Cécité et Nausée en même temps", "Have Blindness and Nausea at the same time")],
             "Certains résultats du Lucky Block BSOD te « plantent » : casse-en un et croise les doigts.",
             "Some BSOD Lucky Block outcomes 'crash' you: break one and hope."),
    "volaille": ([("kill_golem", "Vaincre un Golem de fer", "Defeat an Iron Golem")],
                 "On en trouve dans les villages. Prévois un arc et de la distance.",
                 "Found in villages. Bring a bow and keep your distance."),
    "impots": ([("pay", "Dépenser 16 émeraudes près d'un villageois", "Spend 16 emeralds near a villager")],
               "Commerce avec des villageois : chaque émeraude dépensée à moins de 8 blocs compte.",
               "Trade with villagers: every emerald spent within 8 blocks counts."),
    "streamer": ([("streamer_zombies", "Vaincre 15 zombies", "Defeat 15 zombies")],
                 "La nuit en plaine, les zombies ne manquent pas.",
                 "Plains at night are full of zombies."),
    "proprete": ([("kills", "Vaincre 5 zombies", "Defeat 5 zombies")],
                 "Le grand nettoyage commence par les zombies.",
                 "The big clean-up starts with zombies."),
    "gravite_zero": ([("levitate", "Obtenir l'effet Lévitation", "Get the Levitation effect")],
                     "Les shulkers de l'End en tirent, et certains Lucky Blocks Gravité Zéro aussi.",
                     "End shulkers inflict it, and so do some Zero Gravity Lucky Blocks."),
    "fast_food": ([("ride", "Monter sur un cochon", "Ride a pig")],
                  "Il te faut une selle (coffres de donjon, pêche, commerce).",
                  "You need a saddle (dungeon chests, fishing, trading)."),
    "cauchemar_matin": ([("sleep", "Dormir dans un lit", "Sleep in a bed")],
                        "Un lit = 3 laines + 3 planches. Dors la nuit.",
                        "A bed = 3 wool + 3 planks. Sleep at night."),
    "film_action": ([("slow_fall", "Obtenir Chute lente", "Get Slow Falling")],
                    "Potion de chute lente : membrane de phantom + potion étrange.",
                    "Slow Falling potion: phantom membrane + awkward potion."),
    "jdr_d20": ([("mimic", "Vaincre un Enderman", "Defeat an Enderman")],
                "Ne le regarde pas dans les yeux… sauf si tu es prêt. Porte une citrouille pour te protéger.",
                "Don't look it in the eyes... unless you're ready. A carved pumpkin protects you."),
    "penurie_2020": ([("tp", "Avoir 64 papiers dans l'inventaire", "Hold 64 paper in your inventory")],
                     "Canne à sucre ×3 = 3 papiers. Fais une ferme au bord de l'eau.",
                     "3 sugar cane = 3 paper. Build a farm by the water."),
    "complot": ([("hat", "Porter un casque en or", "Wear a golden helmet")],
                "5 lingots d'or. Protège aussi des piglins.",
                "5 gold ingots. Also keeps piglins calm."),
    "lag_bug": ([("end", "Entrer dans l'End", "Enter the End")],
                "Trouve un fort avec des yeux de l'Ender et active le portail.",
                "Find a stronghold with Eyes of Ender and activate the portal."),
    "disco_80": ([("music", "Jouer un disque dans un jukebox", "Play a music disc in a jukebox")],
                 "Disques : coffres de donjon, ou un squelette qui tue un creeper.",
                 "Discs: dungeon chests, or a skeleton killing a creeper."),
    "plomberie": ([("chest", "Ouvrir un coffre pendant l'alerte plomberie", "Open a chest during the plumbing alert")],
                  "Casse un Lucky Block Plomberie : une fenêtre de temps s'ouvre, ouvre vite un coffre.",
                  "Break a Plumbing Lucky Block: a time window opens, open a chest quickly."),
    "fortune_cookie": ([("fortune_code", "Valider le code fortune dans le chat à temps", "Type the fortune code in chat in time")],
                       "Un Lucky Block Fortune Cookie peut donner un code : tape-le dans le chat en moins de 5 s.",
                       "A Fortune Cookie Lucky Block may give a code: type it in chat within 5 s."),
    "camping": ([("bear", "Vaincre un ours polaire", "Defeat a polar bear")],
                "Biomes enneigés. Les adultes protègent leurs petits !",
                "Snowy biomes. Adults protect their cubs!"),
    "gym_bro": ([("buff", "Avoir Force puis Lenteur III en 30 s", "Get Strength then Slowness III within 30 s")],
                "Bois une potion de force, puis une potion de lenteur forte (ou un tortue maître).",
                "Drink a Strength potion, then a strong Slowness potion (or Turtle Master)."),
    "art_moderne": ([("art", "Avoir une peinture dans l'inventaire", "Hold a painting")],
                    "Peinture = 8 bâtons + 1 laine.",
                    "Painting = 8 sticks + 1 wool."),
    "magie_comptoir": ([("wolf", "Vaincre un loup", "Defeat a wolf")],
                       "Forêts et taïgas. Attention, ils attaquent en meute.",
                       "Forests and taigas. Careful, they attack in packs."),
    "invasion_memes": ([("completed", "Donner 50 coups de Marteau BONK", "Land 50 BONK Hammer hits")],
                       "Le Marteau BONK s'obtient sur les Lucky Blocks Invasion de Mèmes. Tape n'importe quoi.",
                       "The BONK Hammer drops from Meme Invasion Lucky Blocks. Hit anything."),
    "pause_cafe": ([("five_espressos", "Boire 5 Espressos Ultra-Dosés sans avoir faim", "Drink 5 Ultra-Dosed Espressos without starving")],
                   "Si ta barre de faim est vide, le compteur repart à zéro. Mange avant !",
                   "If your hunger bar is empty, the counter resets. Eat first!"),
    "retard_vol": ([("three_valises", "Ouvrir 3 Valises Perdues", "Open 3 Lost Suitcases")],
                   "Les valises tombent des Lucky Blocks Retard de Vol.",
                   "Suitcases drop from Flight Delay Lucky Blocks."),
    "ananas_pizza": ([("survive_chefs", "Vaincre 10 Chefs Italiens sans manger d'ananas", "Defeat 10 Italian Chefs without eating pineapple")],
                     "Manger la Tranche du Débat fait échouer la quête jusqu'à la prochaine invocation.",
                     "Eating the Debate Slice fails the quest until the next summon."),
    "speedrunner": ([("end_speedrun", "Atteindre l'End en moins de 3 min après un Lucky Block Speedrunner", "Reach the End within 3 min of a Speedrunner Lucky Block")],
                    "Prépare tout (yeux de l'Ender, portail trouvé) AVANT de casser le bloc.",
                    "Prepare everything (Eyes of Ender, portal found) BEFORE breaking the block."),
    "piraterie_torrent": ([("treasure_download", "Utiliser le Disque Dur du Capitaine", "Use the Captain's Hard Drive")],
                          "Le disque dur tombe des Lucky Blocks Piraterie Torrent.",
                          "The hard drive drops from Torrent Piracy Lucky Blocks."),
    "asmr_fidget": ([("bubble_pop_50", "Casser 50 blocs de laine blanche ou de rayon de miel", "Break 50 white wool or honeycomb blocks")],
                    "Pose et casse la même laine en boucle : chaque cassage compte.",
                    "Place and break the same wool over and over: every break counts."),
    "vampire_gothique": ([("dracula_no_metal", "Vaincre le Comte Dracula sans armure en métal", "Defeat Count Dracula without metal armor")],
                         "Cuir ou rien : fer, or, maille, diamant et netherite comptent comme métal.",
                         "Leather or nothing: iron, gold, chain, diamond and netherite count as metal."),
    "multivers_sliders": ([("multivers_biomes", "Traverser 3 biomes pendant l'inversion de gravité", "Cross 3 biomes during the gravity flip")],
                          "Casse un Lucky Block Multivers puis cours : tu as 2 minutes.",
                          "Break a Multiverse Lucky Block, then run: you have 2 minutes."),
    "greve_transports": ([("minecart", "Monter dans un wagonnet", "Ride a minecart")],
                         "Wagonnet = 5 lingots de fer. Le Lucky Block Grève en donne parfois un.",
                         "Minecart = 5 iron ingots. The Strike Lucky Block sometimes gives one."),
    "meteo_folle": ([("struck", "Être frappé par la foudre", "Get struck by lightning")],
                    "Pendant un orage, reste à découvert… ou casse un Lucky Block Météo Folle malchanceux.",
                    "Stay outside during a thunderstorm... or break an unlucky Crazy Weather block."),
    "chat_internet": ([("tame_cats", "Apprivoiser 3 chats", "Tame 3 cats")],
                      "Les chats errent dans les villages : approche-toi doucement avec du poisson cru.",
                      "Cats roam villages: approach slowly holding raw fish."),
    "crypto_bulle": ([("hodl", "Avoir 64 émeraudes dans l'inventaire", "Hold 64 emeralds")],
                     "Commerce avec les villageois et ne vends rien pendant un krach !",
                     "Trade with villagers and don't sell during a crash!"),
}

UI = {
    "fr": {
        "tab.all": "Toutes (%s)", "tab.common": "Communes (%s)", "tab.rare": "Rares (%s)",
        "tab.legendary": "Légend. (%s)", "tab.todo": "À faire (%s)",
        "rarity.0": "Commune", "rarity.1": "Rare", "rarity.2": "Légendaire",
        "suggested": "★ Conseillée", "objectives": "Objectifs", "hint": "Indice",
        "quest_done": "Quête terminée !", "empty": "Aucune quête ici.",
        "card_done": "Carte de collection : obtenue", "card_pending": "Carte : à obtenir (résultat HIGH)",
        "master": "Maîtrise : %s/%s cassages + quête", "master_done": "Thème maîtrisé !",
    },
    "en": {
        "tab.all": "All (%s)", "tab.common": "Common (%s)", "tab.rare": "Rare (%s)",
        "tab.legendary": "Legend. (%s)", "tab.todo": "To do (%s)",
        "rarity.0": "Common", "rarity.1": "Rare", "rarity.2": "Legendary",
        "suggested": "★ Suggested", "objectives": "Objectives", "hint": "Hint",
        "quest_done": "Quest complete!", "empty": "No quest here.",
        "card_done": "Collection card: obtained", "card_pending": "Card: not yet (HIGH outcome)",
        "master": "Mastery: %s/%s breaks + quest", "master_done": "Theme mastered!",
    },
}


# Messages des variations bonus (LuckyVariations) et des variantes de bloc (LuckyVariant) : message.unpredictableblocks.*
MESSAGES = {
    "variation.xp_rain": ("Variation : pluie d'XP !", "Variation: XP rain!"),
    "variation.gold_rain": ("Variation : pluie d'or !", "Variation: gold rain!"),
    "variation.lucky_aura": ("Variation : aura chanceuse !", "Variation: lucky aura!"),
    "variation.gift_block": ("Variation : un Lucky Block en cadeau !", "Variation: a free Lucky Block!"),
    "variation.diamond": ("Variation : pierre précieuse !", "Variation: a gem!"),
    "variation.emerald_dust": ("Variation : poussière d'émeraude !", "Variation: emerald dust!"),
    "variation.zoo": ("Variation : mini-zoo !", "Variation: mini zoo!"),
    "variation.flowers": ("Variation : ça fleurit !", "Variation: flowers bloom!"),
    "variation.blink": ("Variation : téléportation surprise !", "Variation: surprise teleport!"),
    "variation.concert": ("Variation : concert improvisé !", "Variation: impromptu concert!"),
    "variation.snack": ("Variation : petit goûter !", "Variation: snack time!"),
    "variation.bouncy": ("Variation : jambes à ressort !", "Variation: springy legs!"),
    "variation.anvil": ("Variation : attention à la tête !", "Variation: mind your head!"),
    "variation.cobwebs": ("Variation : toiles d'araignée !", "Variation: cobwebs!"),
    "variation.silverfish": ("Variation : poissons d'argent !", "Variation: silverfish!"),
    "variation.hungry": ("Variation : fringale !", "Variation: the munchies!"),
    "variation.hiccup": ("Variation : hoquet gravitationnel !", "Variation: gravity hiccup!"),
    "variation.butterfingers": ("Variation : mains glissantes !", "Variation: butterfingers!"),
    "variant.golden": ("§6Lucky Block DORÉ ! Chance +30, butin ×2.", "§6GOLDEN Lucky Block! Luck +30, loot ×2."),
    "variant.cursed": ("§5Lucky Block MAUDIT ! Chance −30, butin ×2, 2 variations.", "§5CURSED Lucky Block! Luck −30, loot ×2, 2 variations."),
    "variant.giant": ("§bLucky Block GÉANT ! Butin ×3.", "§bGIANT Lucky Block! Loot ×3."),
}


def write_atomic(path: Path, text: str) -> None:
    """Ecrit via un fichier temporaire + remplacement : Windows refuse parfois l'ouverture directe (Errno 22) si l'IDE lit le fichier."""
    tmp = path.with_suffix(path.suffix + ".tmp")
    with open(tmp, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)
    for attempt in range(10):
        try:
            os.replace(tmp, path)
            return
        except OSError:
            time.sleep(0.3 * (attempt + 1))
    raise OSError(f"Impossible de remplacer {path} (fichier verrouille ?)")


def main() -> None:
    for file, lang in (("fr_fr.json", "fr"), ("en_us.json", "en"), ("en_gb.json", "en")):
        path = LANG / file
        data = json.loads(path.read_text(encoding="utf-8"))
        for key, (fr, en) in MESSAGES.items():
            data[f"message.unpredictableblocks.{key}"] = fr if lang == "fr" else en
        for theme, (steps, hint_fr, hint_en) in QUESTS.items():
            for criterion, fr, en in steps:
                data[f"quest.unpredictableblocks.{theme}.{criterion}"] = fr if lang == "fr" else en
            data[f"quest.unpredictableblocks.{theme}.hint"] = hint_fr if lang == "fr" else hint_en
        for key, text in UI[lang].items():
            data[f"screen.unpredictableblocks.quest_journal.{key}"] = text
        write_atomic(path, json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    print(f"OK {len(QUESTS)} quetes x 3 langues")


if __name__ == "__main__":
    main()
