package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import net.minecraft.resources.Identifier;

/** Geo model unique : texture dynamiquement choisie selon le thème du Lucky Block. */
public final class LuckyBlockGeoModel extends DefaultedBlockGeoModel<LuckyBlockEntity> {
    public static final DataTicket<String> THEME_ID = DataTicket.create("lucky_theme", String.class);

    public LuckyBlockGeoModel() {
        super(Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "lucky_block"));
    }

    @Override
    public void addAdditionalStateData(LuckyBlockEntity animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(THEME_ID, animatable.getTheme().id());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String theme = renderState.getOrDefaultGeckolibData(THEME_ID, "boulangerie");
        return Identifier.fromNamespaceAndPath(
                UnpredictableBlocks.MOD_ID,
                "textures/block/lucky_" + theme + ".png"
        );
    }
}
