package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Theme Speedrunner : bot ultra-rapide qui dash vers sa cible (anim special). */
public class BotSpeedrunnerEntity extends Monster implements GeoEntity {
    public static final String ID = "bot_speedrunner";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int DASH_COOLDOWN = 20 * 4;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int dashCooldown = DASH_COOLDOWN / 2;

    public BotSpeedrunnerEntity(EntityType<? extends BotSpeedrunnerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.42)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.2));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (target == null || --dashCooldown > 0) {
            return;
        }
        if (distanceToSqr(target) < 9 || distanceToSqr(target) > 200) {
            return;
        }
        dashCooldown = DASH_COOLDOWN;
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        Vec3 dest = target.position().subtract(target.getLookAngle().multiply(1.2, 0, 1.2));
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 10, 0.3, 0.2, 0.3, 0.05);
        teleportTo(dest.x, dest.y, dest.z);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 0.8F, 1.8F);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 12, 0.3, 0.3, 0.3, 0.05);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<BotSpeedrunnerEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
