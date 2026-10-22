package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.entity.LuckyRocketEntity;
import net.minecraft.resources.Identifier;

/** Geo fusée : cube + flamme + fumée, texture 32x48 par thème (textures/entity/lucky_rocket/). */
public final class LuckyRocketGeoModel extends DefaultedEntityGeoModel<LuckyRocketEntity> {
    public LuckyRocketGeoModel() {
        super(Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "lucky_rocket"));
    }

    @Override
    public void addAdditionalStateData(LuckyRocketEntity animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(LuckyBlockGeoModel.THEME_ID, animatable.getTheme().id());
        renderState.addGeckolibData(LuckyBlockGeoModel.VARIANT, animatable.getVariant().ordinal());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String theme = renderState.getOrDefaultGeckolibData(LuckyBlockGeoModel.THEME_ID, "boulangerie");
        return Identifier.fromNamespaceAndPath(
                UnpredictableBlocks.MOD_ID,
                "textures/entity/lucky_rocket/lucky_" + theme + ".png"
        );
    }
}
