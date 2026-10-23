package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Variation Lucky (NEUTRAL) : fantôme en drap inoffensif. Il fait « Bouh ! » aux joueurs qui
 * s'approchent (anim « attack »), puis s'évapore après trois frayeurs (anim « special »).
 */
public class FantomeDrapEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "fantome_drap";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int MAX_SCARES = 3;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int scareCd = 20 * 2;
    private int scares;
    private int vanishTicks = -1;

    public FantomeDrapEntity(EntityType<? extends FantomeDrapEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (vanishTicks >= 0) {
            if (--vanishTicks <= 0) {
                level.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.8, getZ(), 20, 0.4, 0.6, 0.4, 0.05);
                discard();
            }
            return;
        }
        if (--scareCd > 0) return;
        Player victim = level.getNearestPlayer(this, 4.0);
        if (victim == null) {
            scareCd = 10;
            return;
        }
        scareCd = 20 * 6;
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.ENDERMAN_STARE, 0.6F, 1.6F);
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 3, 1));
        victim.sendSystemMessage(Component.literal("§fFantôme : §7« Bouuuh ! »"));
        if (++scares >= MAX_SCARES) {
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            vanishTicks = 20;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<FantomeDrapEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
