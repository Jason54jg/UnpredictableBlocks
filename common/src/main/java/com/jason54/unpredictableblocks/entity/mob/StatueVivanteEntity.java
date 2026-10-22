package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Thème Art Moderne : sculpture géométrique façon ange qui pleure.
 * Ne bouge / n'attaque que quand personne ne la regarde ; se fige au regard.
 */
public class StatueVivanteEntity extends Monster implements GeoEntity {
    public static final String ID = "statue_vivante";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final double LOOK_DOT = 0.92;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean frozen = true;
    private int attackCooldown;
    private boolean assembled;

    public StatueVivanteEntity(EntityType<? extends StatueVivanteEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!assembled) {
            assembled = true;
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        boolean watched = isWatchedByPlayer(level);
        if (watched != frozen) {
            frozen = watched;
            if (frozen) {
                getNavigation().stop();
                setDeltaMovement(Vec3.ZERO);
                playSound(SoundEvents.STONE_PLACE, 0.6F, 0.5F);
            }
        }
        if (frozen) {
            setDeltaMovement(getDeltaMovement().multiply(0, 0.2, 0).add(0, Math.sin(tickCount * 0.05) * 0.002, 0));
            return;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            setDeltaMovement(getDeltaMovement().scale(0.7).add(0, Math.sin(tickCount * 0.1) * 0.01, 0));
            return;
        }
        Vec3 to = target.position().add(0, 0.5, 0).subtract(position());
        if (to.lengthSqr() > 4.0) {
            setDeltaMovement(to.normalize().scale(0.22));
            getLookControl().setLookAt(target);
        } else {
            setDeltaMovement(getDeltaMovement().scale(0.4));
            if (attackCooldown == 0) {
                doHurtTarget(level, target);
                attackCooldown = 25;
            }
        }
    }

    private boolean isWatchedByPlayer(ServerLevel level) {
        for (Player player : level.players()) {
            if (player.isSpectator() || distanceToSqr(player) > 400) {
                continue;
            }
            if (isLookingAtMe(player)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLookingAtMe(Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toEyes = getEyePosition().subtract(player.getEyePosition()).normalize();
        if (view.dot(toEyes) < LOOK_DOT) {
            return false;
        }
        return player.hasLineOfSight(this);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 0.9F, 0.7F);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<StatueVivanteEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
