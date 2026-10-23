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

/**
 * Variation Lucky (LOW) : Lucky Block en armure. Coups d'épée lourds et coup de bouclier qui repousse ;
 * peut lâcher une arme légendaire (loot table {@code entities/chevalier_lucky}).
 */
public class ChevalierLuckyEntity extends Monster implements GeoEntity {
    public static final String ID = "chevalier_lucky";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int bashCd = 20 * 5;

    public ChevalierLuckyEntity(EntityType<? extends ChevalierLuckyEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.9F);
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--bashCd > 0) return;
        LivingEntity target = getTarget();
        if (target == null || distanceToSqr(target) > 9) {
            bashCd = 10;
            return;
        }
        bashCd = 20 * 7 + getRandom().nextInt(40);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.8F);
        addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 3, 1));
        Vec3 push = target.position().subtract(position()).normalize().scale(1.4);
        target.setDeltaMovement(push.x, 0.5, push.z);
        target.needsSync = true;
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.3, 0.3, 0.2);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<ChevalierLuckyEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
