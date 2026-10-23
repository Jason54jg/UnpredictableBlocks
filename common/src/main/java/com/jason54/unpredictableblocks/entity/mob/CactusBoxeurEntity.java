package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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

/** Variation Lucky (LOW) : cactus boxeur. Le frapper au corps à corps pique ; enchaîne parfois un combo. */
public class CactusBoxeurEntity extends Monster implements GeoEntity {
    public static final String ID = "cactus_boxeur";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final float THORNS = 2.0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int comboCd = 20 * 4;

    public CactusBoxeurEntity(EntityType<? extends CactusBoxeurEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        // Seulement au corps à corps : la source directe est l'attaquant lui-même (pas une flèche).
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker == source.getEntity()) {
            attacker.hurtServer(level, damageSources().thorns(this), THORNS);
        }
        return hurt;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--comboCd > 0) return;
        LivingEntity target = getTarget();
        if (target == null || distanceToSqr(target) > 6) {
            comboCd = 10;
            return;
        }
        comboCd = 20 * 6 + getRandom().nextInt(40);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.4F);
        target.hurtServer(level, damageSources().mobAttack(this), 3.0F);
        target.push((target.getX() - getX()) * 0.6, 0.4, (target.getZ() - getZ()) * 0.6);
        target.needsSync = true;
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<CactusBoxeurEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
