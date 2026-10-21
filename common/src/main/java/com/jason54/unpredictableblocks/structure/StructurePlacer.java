package com.jason54.unpredictableblocks.structure;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class StructurePlacer {
    private StructurePlacer() {}

    public static void tryPlaceForTheme(ServerLevel level, BlockPos origin, LuckyTheme theme, LuckTier tier) {
        String structureName = structureFor(theme, tier);
        if (structureName == null) {
            return;
        }
        Identifier id = Identifier.fromNamespaceAndPath("unpredictableblocks", structureName);
        StructureTemplate template = level.getStructureManager().get(id).orElse(null);
        if (template == null) {
            return;
        }
        BlockPos placeAt = origin.offset(-template.getSize().getX() / 2, 0, -template.getSize().getZ() / 2);
        template.placeInWorld(level, placeAt, placeAt, new StructurePlaceSettings(), level.getRandom(), 2);
    }

    private static String structureFor(LuckyTheme theme, LuckTier tier) {
        if (tier != LuckTier.HIGH && tier != LuckTier.NEUTRAL) {
            return null;
        }
        return switch (theme) {
            case BOULANGERIE -> "four_geant";
            case FAST_FOOD -> "drive_thru";
            case PENURIE_2020 -> "pyramide_pq";
            case FILM_ACTION -> "tapis_rouge";
            case COMPLOT -> "bunker_zone51";
            case DISCO_80 -> "piste_disco";
            case PLOMBERIE -> "chateau_briques";
            case ART_MODERNE -> "galerie_absurde";
            case INVASION_MEMES -> "sanctuaire_doge";
            case ANACHRONISME -> "pyramide_piege";
            case MAGIE_COMPTOIR -> "scene_spectacle";
            case CASINO -> "table_poker_vip";
            case CAMPING -> "campement_inonde";
            case GYM_BRO -> "salle_gym";
            case FORTUNE_COOKIE -> "autel_mochi";
            case PAUSE_CAFE -> "barista_corner";
            case RETARD_VOL -> "tour_controle";
            case ANANAS_PIZZA -> "pizzeria_debat";
            case SPEEDRUNNER -> "stronghold_skip";
            case PIRATERIE_TORRENT -> "epave_pirate";
            case ASMR_FIDGET -> "bulle_asmr";
            case VAMPIRE_GOTHIQUE -> "manoir_gothique";
            case MULTIVERS_SLIDERS -> "portail_sliders";
            default -> null;
        };
    }
}
