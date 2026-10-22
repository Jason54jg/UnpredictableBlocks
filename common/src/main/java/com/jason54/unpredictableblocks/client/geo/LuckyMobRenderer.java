package com.jason54.unpredictableblocks.client.geo;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.jason54.unpredictableblocks.UnpredictableBlocks;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * Rendu GeckoLib commun aux mobs du mod : modèle, animations et texture trouvés par convention
 * ({@code geckolib/models/entity/<id>.geo.json}, {@code textures/entity/<id>.png}).
 */
public final class LuckyMobRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T, LivingEntityRenderState> {
    public LuckyMobRenderer(EntityRendererProvider.Context context, String id, float shadowRadius) {
        super(context, new DefaultedEntityGeoModel<>(Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, id)));
        this.shadowRadius = shadowRadius;
    }
}
