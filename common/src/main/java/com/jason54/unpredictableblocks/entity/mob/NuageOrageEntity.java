package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.compat.McRefs;
import com.jason54.unpredictableblocks.event.EntitySpawnHelper;
import net.minecraft.core.BlockPos;
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

/** Theme Meteo Folle : nuage orageux flottant qui frappe la foudre. */
public class NuageOrageEntity extends Monster implements GeoEntity {
    public static final String ID = "nuage_orage";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int strikeCd = 20 * 8;
    private int atkCd;

    public NuageOrageEntity(EntityType<? extends NuageOrageEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
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
        if (atkCd > 0) atkCd--;
        LivingEntity target = getTarget();
        if (target == null) {
            setDeltaMovement(getDeltaMovement().scale(0.8).add(0, Math.sin(tickCount * 0.07) * 0.012, 0));
        } else {
            Vec3 to = target.position().add(0, 2.5, 0).subtract(position());
            setDeltaMovement(to.normalize().scale(0.1));
            getLookControl().setLookAt(target);
            if (distanceToSqr(target) < 9 && atkCd == 0) {
                doHurtTarget(level, target);
                atkCd = 30;
            }
        }
        if (--strikeCd <= 0 && target != null) {
            strikeCd = 20 * 8 + getRandom().nextInt(60);
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.6F, 1.2F);
            BlockPos strike = target.blockPosition();
            var bolt = EntitySpawnHelper.create(McRefs.LIGHTNING_BOLT, level);
            if (bolt != null) {
                bolt.setPos(Vec3.atBottomCenterOf(strike));
                level.addFreshEntity(bolt);
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 20, 0.6, 0.4, 0.6, 0.1);
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
    public void registerControllers(AnimatableManager.ControllerRegistrar c) { ANIMS.<NuageOrageEntity>register(c, null); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
