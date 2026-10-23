package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Variation Lucky (HIGH) : pinata lama qui fuit les coups et éclate en butin
 * (loot table {@code entities/pinata_lucky}).
 */
public class PinataLuckyEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "pinata_lucky";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int specialCd = 20 * 5;

    public PinataLuckyEntity(EntityType<? extends PinataLuckyEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--specialCd > 0) return;
        specialCd = 20 * 6 + getRandom().nextInt(60);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 0.8F, 1.2F + getRandom().nextFloat() * 0.4F);
        level.sendParticles(ParticleTypes.NOTE, getX(), getY() + 1.6, getZ(), 6, 0.5, 0.3, 0.5, 1.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (!hurt) {
            return false;
        }
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        level.sendParticles(ParticleTypes.FIREWORK, getX(), getY() + 1.0, getZ(), 8, 0.4, 0.4, 0.4, 0.1);
        if (isDeadOrDying()) {
            playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1.0, getZ(), 40, 0.6, 0.6, 0.6, 0.3);
        }
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<PinataLuckyEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
