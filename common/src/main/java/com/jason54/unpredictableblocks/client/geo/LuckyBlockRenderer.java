package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.theme.LuckyVariant;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/** BER GeckoLib du bloc posé (statique ; l'envol est rendu par {@link LuckyRocketRenderer}). Teinte/taille selon la variante. */
public final class LuckyBlockRenderer extends GeoBlockRenderer<LuckyBlockEntity, BlockEntityRenderState> {
    public LuckyBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new LuckyBlockGeoModel());
    }

    @Override
    public int getRenderColor(LuckyBlockEntity animatable, Void relatedObject, float partialTick) {
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
