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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Theme Multivers Sliders : barre portail qui zap sa cible ailleurs. */
public class SliderMultiversEntity extends Monster implements GeoEntity {
    public static final String ID = "slider_multivers";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int zapCd = 20 * 7;
    private int atkCd;

    public SliderMultiversEntity(EntityType<? extends SliderMultiversEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
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
        if (atkCd > 0) atkCd--;
        LivingEntity target = getTarget();
        if (target == null) {
            setDeltaMovement(getDeltaMovement().scale(0.85).add(0, Math.sin(tickCount * 0.09) * 0.008, 0));
            return;
        }
        Vec3 to = target.position().add(0, 0.8, 0).subtract(position());
        setDeltaMovement(to.normalize().scale(0.11));
        getLookControl().setLookAt(target);
        if (to.lengthSqr() < 6 && atkCd == 0) {
            doHurtTarget(level, target);
            atkCd = 28;
        }
        if (--zapCd <= 0) {
            zapCd = 20 * 7 + getRandom().nextInt(40);
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.8F);
            double ox = (getRandom().nextDouble() - 0.5) * 16;
            double oz = (getRandom().nextDouble() - 0.5) * 16;
            target.teleportTo(target.getX() + ox, target.getY() + 1, target.getZ() + oz);
            level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1, target.getZ(), 30, 0.5, 0.8, 0.5, 0.2);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean causeFallDamage(double d, float m, net.minecraft.world.damagesource.DamageSource s) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar c) { ANIMS.<SliderMultiversEntity>register(c, null); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
