#!/usr/bin/env python3
"""Detailed multi-line item/block tooltips for en_us / en_gb / fr_fr."""
from __future__ import annotations

import json
from pathlib import Path

LANG = Path(__file__).resolve().parents[1] / "common/src/main/resources/assets/unpredictableblocks/lang"

# item_id -> (desc, usage, effect, quest)  — empty string skips key
ITEMS_EN = {
    "luck_emerald_dust": (
        "Glittering dust infused with good fortune.",
        "Right-click to consume.",
        "Permanently adds +10 to your Luck (-100 to +100).",
        "Higher Luck biases Lucky Block breaks toward HIGH outcomes.",
    ),
    "luck_blaze_curse": (
        "Cursed powder that invites misfortune.",
        "Right-click to consume.",
        "Permanently subtracts 10 from your Luck.",
        "Low Luck makes LOW (bad) Lucky outcomes more likely.",
    ),
    "baguette_magique": (
        "A crusty stick of destiny. Smells like bakery and violence.",
        "Melee weapon — hit entities to BONK them away.",
        "Comes with absurd Knockback X. Yes, ten.",
        "Theme gear: Boulangerie / Invasion de Memes vibes.",
    ),
    "cle_usb_hacker": (
        "Suspicious USB. Do not plug into real PCs.",
        "Right-click while holding to overclock yourself.",
        "Haste III + Speed II for 30 seconds.",
        "Theme gear: Lag & Bug / Streamer.",
    ),
    "marteau_bonk": (
        "The legendary BONK hammer. Ethics optional.",
        "Hit mobs or players to BONK.",
        "Knockback V. Loud. Satisfying.",
        "Quest Invasion de Memes: land 50 BONK hits.",
    ),
    "casque_5g": (
        "Tin-foil helmet of absolute skepticism.",
        "Wear on your head.",
        "High Protection. Helps shrug off nasty status effects.",
        "Theme gear: Theorie du Complot.",
    ),
    "shaker_prot": (
        "Protein shaker filled with pure serotonin (allegedly).",
        "Right-click to drink. Consumed. Cooldown applies.",
        "Strength III, Speed II and Hunger II for 90 seconds.",
        "Theme gear: Go Muscu.",
    ),
    "cle_molette_legendaire": (
        "A wrench that has fixed more realities than pipes.",
        "Use as a tool / weapon.",
        "Efficiency IV, Unbreaking III, Knockback II.",
        "Theme gear: Plomberie.",
    ),
    "lucky_mystere": (
        "A sealed Lucky Block with no fixed theme.",
        "Right-click to place a random theme (of 31). 30s cooldown.",
        "The placed block behaves like a normal Lucky Block of that theme.",
        "Great for hunting missing themes or cards.",
    ),
    "journal_quetes": (
        "Your personal ledger of absurd accomplishments.",
        "Right-click to open the quest journal UI.",
        "Shows quest / card / master progress per theme.",
        "Also available via the Quests button in your inventory.",
    ),
    "carte_collection": (
        "Trading card for Lucky themes. Fill the album.",
        "Right-click for live stats if a theme is tagged.",
        "Blank cards can be themed by Lucky HIGH rewards.",
        "Collect all 31 for the Card Collector advancement.",
    ),
    "espresso_ultra_dose": (
        "Barista-grade rocket fuel. Legal in most dimensions.",
        "Right-click to drink (consumed).",
        "Haste III + Speed III + Nausea I for a short while.",
        "Pause Cafe quest: drink 5 without starving to death.",
    ),
    "valise_perdue": (
        "Someone's luggage. Definitely not yours. Probably.",
        "Right-click to open (consumed).",
        "Gives a shulker box and leather armor pieces.",
        "Retard de Vol quest: recover 3 lost suitcases.",
    ),
    "tranche_debat": (
        "A controversial pizza slice. Pineapple optional. Or not.",
        "Right-click to eat and broadcast the debate.",
        "Restores hunger. Starts the Ananas Pizza event chain.",
        "Ananas Pizza quest: survive Italian chefs without eating pineapple.",
    ),
    "chrono_world_record": (
        "A stopwatch that refuses to waste ticks.",
        "Right-click to dash 15 blocks in your look direction.",
        "Instant reposition — no cooldown listed, don't softlock yourself.",
        "Speedrunner theme gear / Any% energy.",
    ),
    "disque_dur_capitaine": (
        "External HDD full of \"totally legal\" treasure maps.",
        "Right-click to \"download\" (consumed).",
        "Gives a map + book, applies Weakness briefly.",
        "Piraterie & Torrent quest: download the digital treasure map.",
    ),
    "pop_it_supreme": (
        "The ultimate fidget. Infinite satisfying pops.",
        "Right-click to squeeze.",
        "Clears Poison & Nausea, grants Regeneration briefly.",
        "ASMR & Fidget theme gear.",
    ),
    "fiole_sang_noble": (
        "A gothic vial. Best consumed at night.",
        "Right-click to drink (consumed).",
        "Heals 3 hearts + Strength 30s. Burns you in sunlight!",
        "Vampire & Gothique theme gear.",
    ),
    "zapper_dimensions": (
        "A remote that zaps nearby entities into the ceiling.",
        "Right-click to zap (consumed).",
        "Teleports a nearby entity ~12 blocks upward.",
        "Multivers / Sliders theme gear.",
    ),
}

ITEMS_FR = {
    "luck_emerald_dust": (
        "Poussiere scintillante chargee de bonne fortune.",
        "Clic droit pour consommer.",
        "Ajoute definitivement +10 a ta Chance (-100 a +100).",
        "Une Chance elevee favorise les resultats HIGH des Lucky Blocks.",
    ),
    "luck_blaze_curse": (
        "Poudre maudite qui attire la malchance.",
        "Clic droit pour consommer.",
        "Retire definitivement 10 points de Chance.",
        "Une Chance basse rend les resultats LOW (mauvais) plus frequents.",
    ),
    "baguette_magique": (
        "Baguette croustillante du destin. Sent le four et la violence.",
        "Arme de melee — frappe pour envoyer valser.",
        "Knockback X (oui, dix). Absurde.",
        "Gear theme Boulangerie / Invasion de Memes.",
    ),
    "cle_usb_hacker": (
        "Cle USB douteuse. Ne pas brancher sur un vrai PC.",
        "Clic droit pour t'overclocker.",
        "Hate III + Vitesse II pendant 30 secondes.",
        "Gear theme Lag & Bug / Streamer.",
    ),
    "marteau_bonk": (
        "Le marteau BONK legendaire. Ethique optionnelle.",
        "Frappe mobs ou joueurs pour BONK.",
        "Knockback V. Bruyant. Satisfaisant.",
        "Quete Invasion de Memes : 50 coups BONK.",
    ),
    "casque_5g": (
        "Casque en papier alu de scepticisme absolu.",
        "Equipe-le sur la tete.",
        "Haute Protection. Aide contre les effets nefastes.",
        "Gear theme Theorie du Complot.",
    ),
    "shaker_prot": (
        "Shaker de proteines a la serotonine pure (cense).",
        "Clic droit pour boire (consomme). Cooldown.",
        "Force III, Vitesse II et Faim II pendant 90 secondes.",
        "Gear theme Go Muscu.",
    ),
    "cle_molette_legendaire": (
        "Cle a molette qui a repare plus de realites que de tuyaux.",
        "Utilisable comme outil / arme.",
        "Efficiency IV, Unbreaking III, Knockback II.",
        "Gear theme Plomberie.",
    ),
    "lucky_mystere": (
        "Lucky Block scelle sans theme fixe.",
        "Clic droit : place 1 theme au hasard (31). Recharge 30s.",
        "Le bloc place se comporte comme un Lucky Block normal de ce theme.",
        "Utile pour chasser themes ou cartes manquants.",
    ),
    "journal_quetes": (
        "Ton registre personnel d'exploits absurdes.",
        "Clic droit : ouvre l'UI du journal des quetes.",
        "Affiche progression quete / carte / maitre par theme.",
        "Aussi via le bouton Quetes dans l'inventaire.",
    ),
    "carte_collection": (
        "Carte a collectionner des themes Lucky. Remplis l'album.",
        "Clic droit pour les stats si un theme est tague.",
        "Les cartes vierges se thematisent via recompenses HIGH.",
        "Collectionne les 31 pour l'advancement Album.",
    ),
    "espresso_ultra_dose": (
        "Carburant barista. Legal dans la plupart des dimensions.",
        "Clic droit pour boire (consomme).",
        "Hate III + Vitesse III + Nausee I un moment.",
        "Quete Pause Cafe : 5 espressos sans mourir de faim.",
    ),
    "valise_perdue": (
        "Les bagages de quelqu'un. Pas les tiens. Probablement.",
        "Clic droit pour ouvrir (consomme).",
        "Donne un shulker et des pieces d'armure en cuir.",
        "Quete Retard de Vol : retrouver 3 valises.",
    ),
    "tranche_debat": (
        "Tranche de pizza controversee. Ananas optionnel. Ou pas.",
        "Clic droit : mange et lance le debat en chat.",
        "Restaure la faim. Declenche la chaine Ananas Pizza.",
        "Quete Ananas Pizza : survivre aux chefs sans manger d'ananas.",
    ),
    "chrono_world_record": (
        "Chronometre qui refuse de perdre un tick.",
        "Clic droit : dash de 15 blocs dans ton regard.",
        "Reposition instantanee — ne te softlock pas.",
        "Gear theme Speedrunner / vibes Any%.",
    ),
    "disque_dur_capitaine": (
        "Disque dur plein de cartes au tresor \"totalement legales\".",
        "Clic droit pour \"telecharger\" (consomme).",
        "Donne carte + livre, Faiblesse courte.",
        "Quete Piraterie & Torrent : telecharger la carte numerique.",
    ),
    "pop_it_supreme": (
        "Le fidget ultime. Pops infinis et satisfaisants.",
        "Clic droit pour presser.",
        "Enleve Poison & Nausee, Regeneration courte.",
        "Gear theme ASMR & Fidget.",
    ),
    "fiole_sang_noble": (
        "Fiole gothique. Meilleure la nuit.",
        "Clic droit pour boire (consomme).",
        "Soigne 3 coeurs + Force 30s. Brule au soleil !",
        "Gear theme Vampire & Gothique.",
    ),
    "zapper_dimensions": (
        "Telecommande qui zap les entites vers le plafond.",
        "Clic droit pour zapper (consomme).",
        "Teleporte une entite proche ~12 blocs vers le haut.",
        "Gear theme Multivers / Sliders.",
    ),
}

LUCKY_FLAVOR_EN = {
    "anachronisme": "A block ripped from the wrong century.",
    "boulangerie": "Warm crust, cold RNG. Bakery chaos in a cube.",
    "bsod": "Fatal error aesthetic. Please do not restart yet.",
    "volaille": "Maximum poultry density. Cluck responsibly.",
    "impots": "Form 1099-LUCKY. Bureaucracy tastes like emeralds.",
    "streamer": "Chat is already donating bad ideas.",
    "proprete": "Hospital-grade clean. Slightly terrifying.",
    "gravite_zero": "Newton called. He wants his laws back.",
    "fast_food": "Order up: one large side of consequences.",
    "cauchemar_matin": "Alarm clock trauma, block-shaped.",
    "film_action": "Stunt double not included.",
    "jdr_d20": "The DM is smiling. That is never good.",
    "penurie_2020": "Shelves empty. Panic optional but likely.",
    "complot": "They don't want you to break this block.",
    "lag_bug": "50 ms ping to reality itself.",
    "disco_80": "Mirror ball optional. Funk mandatory.",
    "plomberie": "Drip. Drip. Drip. Your problem now.",
    "fortune_cookie": "Paper prophecy with crunchy RNG.",
    "camping": "Bring bug spray and a will to live.",
    "gym_bro": "Bro science meets block science.",
    "art_moderne": "Critics hate it. That's the point.",
    "magie_comptoir": "Discount spells, premium weirdness.",
    "invasion_memes": "Upload complete. Resistance futile.",
    "pause_cafe": "Break room energy. Extra foam.",
    "retard_vol": "Boarding group: chaos.",
    "ananas_pizza": "Culinary war crime or masterpiece?",
    "speedrunner": "PB or die trying.",
    "piraterie_torrent": "Yo-ho-ho and a bottle of bandwidth.",
    "asmr_fidget": "Soft sounds. Loud outcomes.",
    "vampire_gothique": "Cape sold separately. Night preferred.",
    "multivers_sliders": "Slide carefully between selves.",
}

LUCKY_FLAVOR_FR = {
    "anachronisme": "Un bloc arrache au mauvais siecle.",
    "boulangerie": "Croute chaude, RNG froide. Chaos boulanger.",
    "bsod": "Esthetique ecran bleu. Ne redemarre pas encore.",
    "volaille": "Densite poulet maximale. Cot cot responsable.",
    "impots": "Formulaire 1099-LUCKY. La paperasse a gout d'emeraude.",
    "streamer": "Le chat donate deja de mauvaises idees.",
    "proprete": "Proprete niveau hopital. Legerement terrifiant.",
    "gravite_zero": "Newton a appele. Il veut ses lois.",
    "fast_food": "Commande : une grande portion de consequences.",
    "cauchemar_matin": "Trauma reveil, en forme de bloc.",
    "film_action": "Cascadeur non inclus.",
    "jdr_d20": "Le MJ sourit. Ce n'est jamais bon signe.",
    "penurie_2020": "Rayons vides. Panique optionnelle mais probable.",
    "complot": "Ils ne veulent pas que tu casses ce bloc.",
    "lag_bug": "50 ms de ping vers la realite.",
    "disco_80": "Boule a facettes optionnelle. Funk obligatoire.",
    "plomberie": "Goutte. Goutte. Goutte. A toi de gerer.",
    "fortune_cookie": "Prophetie en papier, RNG croustillante.",
    "camping": "Prends de l'anti-moustique et de la volonte.",
    "gym_bro": "La bro-science rencontre la block-science.",
    "art_moderne": "Les critiques detestent. C'est le but.",
    "magie_comptoir": "Sorts discount, bizarrerie premium.",
    "invasion_memes": "Upload termine. Resistance futile.",
    "pause_cafe": "Energie salle de pause. Mousse en plus.",
    "retard_vol": "Groupe d'embarquement : chaos.",
    "ananas_pizza": "Crime culinaire ou chef-d'oeuvre ?",
    "speedrunner": "PB ou mort en essayant.",
    "piraterie_torrent": "Ohé et une bouteille de bande passante.",
    "asmr_fidget": "Sons doux. Resultats bruyants.",
    "vampire_gothique": "Cape vendue separement. Nuit preferee.",
    "multivers_sliders": "Glisse prudemment entre tes moi.",
}


def put_item(out: dict, prefix: str, item_id: str, lines: tuple[str, str, str, str]) -> None:
    desc, usage, effect, quest = lines
    base = f"{prefix}.{item_id}"
    out[f"{base}.desc"] = desc
    out[f"{base}.usage"] = usage
    out[f"{base}.effect"] = effect
    out[f"{base}.quest"] = quest


def build(lang: str) -> dict:
    out: dict[str, str] = {}
    items = ITEMS_EN if lang == "en" else ITEMS_FR
    flavor = LUCKY_FLAVOR_EN if lang == "en" else LUCKY_FLAVOR_FR

    for item_id, lines in items.items():
        put_item(out, "item.unpredictableblocks", item_id, lines)

    if lang == "en":
        out["tooltip.unpredictableblocks.rarity.common"] = "Rarity: Common"
        out["tooltip.unpredictableblocks.rarity.rare"] = "Rarity: Rare"
        out["tooltip.unpredictableblocks.rarity.legendary"] = "Rarity: Legendary"
        out["tooltip.unpredictableblocks.stored_luck"] = "Stored luck on this block: %s"
        out["tooltip.unpredictableblocks.lucky_break"] = "Place & break to trigger a themed random event (HIGH / NEUTRAL / LOW)."
        out["tooltip.unpredictableblocks.lucky_quest"] = "Theme quest: %s"
        out["tooltip.unpredictableblocks.lucky_luck_hint"] = "Your Luck stat tilts the outcome. Sneak + emerald dust in off-hand to bet."
        usage = "Place in the world, then break it."
        effect = "Rolls a themed event and loot based on your Luck tier."
    else:
        out["tooltip.unpredictableblocks.rarity.common"] = "Rarete : Commune"
        out["tooltip.unpredictableblocks.rarity.rare"] = "Rarete : Rare"
        out["tooltip.unpredictableblocks.rarity.legendary"] = "Rarete : Legendaire"
        out["tooltip.unpredictableblocks.stored_luck"] = "Chance stockee sur ce bloc : %s"
        out["tooltip.unpredictableblocks.lucky_break"] = "Place puis casse pour declencher un evenement theme (HIGH / NEUTRAL / LOW)."
        out["tooltip.unpredictableblocks.lucky_quest"] = "Quete du theme : %s"
        out["tooltip.unpredictableblocks.lucky_luck_hint"] = "Ta Chance oriente le resultat. Sneak + poussiere d'emeraude en off-hand pour parier."
        usage = "Place dans le monde, puis casse-le."
        effect = "Declenche un evenement et du loot selon ton palier de Chance."

    for theme, desc in flavor.items():
        base = f"block.unpredictableblocks.lucky_{theme}"
        out[f"{base}.desc"] = desc
        out[f"{base}.usage"] = usage
        out[f"{base}.effect"] = effect
        # quest line shown dynamically from advancement; keep a static hint too
        out[f"{base}.quest"] = (
            "Complete the theme quest after the break for rewards."
            if lang == "en"
            else "Complete la quete du theme apres le cassage pour les recompenses."
        )

    return out


def merge(path: Path, extras: dict) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    data.update(extras)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{path.name}: {len(data)} keys")


def main() -> None:
    merge(LANG / "en_us.json", build("en"))
    merge(LANG / "en_gb.json", build("en"))
    merge(LANG / "fr_fr.json", build("fr"))


if __name__ == "__main__":
    main()