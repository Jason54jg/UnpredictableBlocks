package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Theme Lag Bug : fantome glitch qui flotte, se teleporte et melange la hotbar au contact.
 */
public class HackerFantomeEntity extends Monster implements GeoEntity {
    public static final String ID = "hacker_fantome";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int GLITCH_COOLDOWN = 20 * 5;
    private static final double SPEED = 0.14;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int glitchCooldown = GLITCH_COOLDOWN;
    private int attackCooldown;

    public HackerFantomeEntity(EntityType<? extends HackerFantomeEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            setDeltaMovement(getDeltaMovement().scale(0.8).add(0, Math.sin(tickCount * 0.12) * 0.008, 0));
            return;
        }
        Vec3 to = target.position().add(0, 1.0, 0).subtract(position());
        if (to.lengthSqr() > 2.5) {
            setDeltaMovement(to.normalize().scale(SPEED));
        } else {
            setDeltaMovement(getDeltaMovement().scale(0.4));
            if (attackCooldown == 0) {
                doHurtTarget(level, target);
                attackCooldown = 28;
            }
        }
        getLookControl().setLookAt(target);
        if (--glitchCooldown <= 0) {
            glitchCooldown = GLITCH_COOLDOWN + getRandom().nextInt(40);
            glitch(level);
        }
    }

    private void glitch(ServerLevel level) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1, getZ(), 16, 0.5, 0.6, 0.5, 0.08);
        teleportTo(getX() + (getRandom().nextDouble() - 0.5) * 8,
                getY() + (getRandom().nextDouble() - 0.2),
                getZ() + (getRandom().nextDouble() - 0.5) * 8);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 0.7F, 1.6F);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof ServerPlayer player) {
            shuffleHotbar(player);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cDesync — hotbar melangee !"));
        }
        return hit;
    }

    private static void shuffleHotbar(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < 8; i++) {
            int j = player.getRandom().nextInt(9);
            ItemStack a = inv.getItem(i);
            inv.setItem(i, inv.getItem(j));
            inv.setItem(j, a);
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<HackerFantomeEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
