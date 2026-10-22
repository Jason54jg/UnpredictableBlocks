package com.jason54.unpredictableblocks.entity.mob;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Part de pizza lancée par le Chef Italien. Côté client c'est une boule de neige qui affiche l'objet
 * « Tranche du Débat » (type d'entité vanilla, aucun rendu à enregistrer) ; côté serveur elle blesse.
 */
public final class PizzaSlice extends Snowball {
    private static final float DAMAGE = 4.0F;

    public PizzaSlice(Level level, LivingEntity thrower, ItemStack slice) {
        super(level, thrower, slice);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (level() instanceof ServerLevel serverLevel) {
            hitResult.getEntity().hurtServer(serverLevel, damageSources().thrown(this, getOwner()), DAMAGE);
            serverLevel.sendParticles(ParticleTypes.LAVA, getX(), getY(), getZ(), 3, 0.2, 0.2, 0.2, 0);
        }
    }
}
