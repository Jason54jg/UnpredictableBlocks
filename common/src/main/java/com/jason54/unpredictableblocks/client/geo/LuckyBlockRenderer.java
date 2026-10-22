package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.renderer.GeoBlockRenderer;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/** BER GeckoLib du bloc posé (statique ; l'envol est rendu par {@link LuckyRocketRenderer}). */
public final class LuckyBlockRenderer extends GeoBlockRenderer<LuckyBlockEntity, BlockEntityRenderState> {
    public LuckyBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new LuckyBlockGeoModel());
    }
}
