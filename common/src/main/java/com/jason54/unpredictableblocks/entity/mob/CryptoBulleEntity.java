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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Theme Crypto Bulle : bulle flottante qui gonfle puis crash (drop emeraude ou rien). */
public class CryptoBulleEntity extends Monster implements GeoEntity {
    public static final String ID = "crypto_bulle";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int pumpCd = 20 * 6;
    private int atkCd;

    public CryptoBulleEntity(EntityType<? extends CryptoBulleEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 18.0);
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
        var target = getTarget();
        if (target == null) {
            setDeltaMovement(getDeltaMovement().scale(0.85).add(0, Math.sin(tickCount * 0.08) * 0.01, 0));
        } else {
            Vec3 to = target.position().add(0, 1, 0).subtract(position());
            setDeltaMovement(to.lengthSqr() > 4 ? to.normalize().scale(0.12) : getDeltaMovement().scale(0.5));
            if (to.lengthSqr() < 4 && atkCd == 0) { doHurtTarget(level, target); atkCd = 25; }
            getLookControl().setLookAt(target);
        }
        if (--pumpCd <= 0) {
            pumpCd = 20 * 6 + getRandom().nextInt(40);
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, 1.0F, 0.6F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1, getZ(), 10, 0.5, 0.5, 0.5, 0.05);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (getRandom().nextBoolean()) {
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    level, getX(), getY(), getZ(), new ItemStack(Items.EMERALD, 1 + getRandom().nextInt(4))));
        }
        playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 0.5F, 1.6F);
    }

    @Override
    public boolean causeFallDamage(double d, float m, DamageSource s) { return false; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar c) { ANIMS.<CryptoBulleEntity>register(c, null); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
