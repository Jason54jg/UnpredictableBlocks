package com.jason54.unpredictableblocks.entity.mob;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Boule de neige du Bonhomme de Neige Fou : type d'entité vanilla (aucun rendu à enregistrer),
 * mais côté serveur elle blesse et ralentit.
 */
public final class IceSnowball extends Snowball {
    private static final float DAMAGE = 2.0F;

    public IceSnowball(Level level, LivingEntity thrower, ItemStack snowball) {
        super(level, thrower, snowball);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (level() instanceof ServerLevel serverLevel) {
            hitResult.getEntity().hurtServer(serverLevel, damageSources().thrown(this, getOwner()), DAMAGE);
            if (hitResult.getEntity() instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 2, 1));
            }
            serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 4, 0.2, 0.2, 0.2, 0);
        }
    }
}
