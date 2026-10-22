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

/** Theme Invasion Memes : cube qui spam emotional damage. */
public class CubeMemeEntity extends Monster implements GeoEntity {
    public static final String ID = "cube_meme";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int specialCd = 20 * 4;

    public CubeMemeEntity(EntityType<? extends CubeMemeEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.5)
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
            target.push(0, 0.55, 0);
            target.needsSync = true;
            playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.5F);
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--specialCd > 0) return;
        specialCd = 20 * 4 + getRandom().nextInt(40);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.8F);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1.0, getZ(), 20, 0.5, 0.5, 0.5, 0.1);
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(7))) {
            p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 2, 0));
            p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 3, 0));
            p.setDeltaMovement(p.getDeltaMovement().add(0, 0.6, 0));
            p.needsSync = true;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar c) { ANIMS.<CubeMemeEntity>register(c, null); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
