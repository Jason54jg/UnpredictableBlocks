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
import net.minecraft.world.phys.AABB;

/** Theme Complot : pyramide a oeil qui surveille (glow). */
public class PyramideOeilEntity extends Monster implements GeoEntity {
    public static final String ID = "pyramide_oeil";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int specialCd = 20 * 6;

    public PyramideOeilEntity(EntityType<? extends PyramideOeilEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            if (target instanceof Player p) {
                p.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 15, 0));
            }
            playSound(SoundEvents.ENDERMAN_STARE, 0.8F, 0.6F);
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--specialCd > 0) return;
        specialCd = 20 * 6 + getRandom().nextInt(40);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.ENDERMAN_AMBIENT, 1.0F, 0.5F);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.5, getZ(), 12, 0.4, 0.4, 0.4, 0.02);
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(8))) {
            p.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 20, 0));
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 3, 0));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar c) { ANIMS.<PyramideOeilEntity>register(c, null); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
