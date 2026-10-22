package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Thème BSOD : écran planté qui flotte vers sa cible, la « plante » au contact (cécité + nausée)
 * et se téléporte par saccades (anim « special »). Pas de gravité, déplacement géré à la main.
 */
public class EcranBleuEntity extends Monster implements GeoEntity {
    public static final String ID = "ecran_bleu";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int GLITCH_COOLDOWN = 20 * 5;
    private static final double SPEED = 0.12;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int glitchCooldown = GLITCH_COOLDOWN;
    private int attackCooldown;

    public EcranBleuEntity(EntityType<? extends EcranBleuEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (target == null) {
            // Dérive lente en flottant sur place
            setDeltaMovement(getDeltaMovement().scale(0.8).add(0, Math.sin(tickCount * 0.1) * 0.005, 0));
            return;
        }
        Vec3 toTarget = target.position().add(0, 1.0, 0).subtract(position());
        if (toTarget.lengthSqr() > 2.0) {
            setDeltaMovement(toTarget.normalize().scale(SPEED));
        } else {
            setDeltaMovement(getDeltaMovement().scale(0.5));
            if (attackCooldown == 0) {
                doHurtTarget(level, target);
                attackCooldown = 30;
            }
        }
        getLookControl().setLookAt(target);
        if (--glitchCooldown <= 0) {
            glitchCooldown = GLITCH_COOLDOWN + getRandom().nextInt(60);
            glitch(level);
        }
    }

    private void glitch(ServerLevel level) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 0.6, getZ(), 12, 0.5, 0.4, 0.5, 0.1);
        double x = getX() + (getRandom().nextDouble() - 0.5) * 6;
        double z = getZ() + (getRandom().nextDouble() - 0.5) * 6;
        teleportTo(x, getY() + (getRandom().nextDouble() - 0.3), z);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 0.6F, 1.8F);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 3, 0));
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5, 0));
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<EcranBleuEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
