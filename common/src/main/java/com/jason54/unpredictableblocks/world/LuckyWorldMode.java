package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Type de monde choisi à la création (bouton « Type de monde ») via les world_preset du mod.
 * <p>
 * Chaque preset donne à l'overworld un {@code dimension_type} dédié (copie de l'overworld vanilla) :
 * c'est ce type qui sert de marqueur, car le preset lui-même n'est pas sauvegardé dans le monde.
 */
public enum LuckyWorldMode {
    NORMAL(null),
    /** Monde vanilla, Lucky Blocks beaucoup plus fréquents. */
    DENSE("lucky_dense"),
    /** Monde plat avec une grille de Lucky Blocks. */
    FLAT("lucky_flat"),
    /** Île dans le vide + générateur de Lucky Blocks. */
    SKYBLOCK("lucky_skyblock");

    private final ResourceKey<DimensionType> dimensionType;

    LuckyWorldMode(String path) {
        this.dimensionType = path == null ? null
                : ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, path));
    }

    /** Mode du monde (lu sur l'overworld : nether/end gardent leurs types vanilla). */
    public static LuckyWorldMode of(ServerLevel level) {
        Holder<DimensionType> type = level.getServer().overworld().dimensionTypeRegistration();
        for (LuckyWorldMode mode : values()) {
            if (mode.dimensionType != null && type.is(mode.dimensionType)) {
                return mode;
            }
        }
        return NORMAL;
    }
}
