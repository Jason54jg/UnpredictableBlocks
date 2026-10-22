package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.jason54.unpredictableblocks.entity.LuckyRocketEntity;
import com.jason54.unpredictableblocks.theme.LuckyVariant;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Rendu GeckoLib de la fusée (anim du style du thème), teinte/taille selon la variante du bloc cassé. */
public final class LuckyRocketRenderer extends GeoEntityRenderer<LuckyRocketEntity, EntityRenderState> {
    public LuckyRocketRenderer(EntityRendererProvider.Context context) {
        super(context, new LuckyRocketGeoModel());
    }

    @Override
    public int getRenderColor(LuckyRocketEntity animatable, Void relatedObject, float partialTick) {
        return animatable.getVariant().color();
    }

    // R de GeckoLib = intersection (état MC & GeoRenderState) : non exprimable ici, d'où le type brut.
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void scaleModelForRender(RenderPassInfo info, float widthScale, float heightScale) {
        float scale = LuckyVariant.byOrdinal((Integer) info.getOrDefaultGeckolibData(LuckyBlockGeoModel.VARIANT, 0)).scale();
        super.scaleModelForRender(info, widthScale * scale, heightScale * scale);
    }
}
