package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.renderer.GeoEntityRenderer;
import com.jason54.unpredictableblocks.entity.LuckyRocketEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Rendu GeckoLib de la fusée (anim « launch »). */
public final class LuckyRocketRenderer extends GeoEntityRenderer<LuckyRocketEntity, EntityRenderState> {
    public LuckyRocketRenderer(EntityRendererProvider.Context context) {
        super(context, new LuckyRocketGeoModel());
    }
}
