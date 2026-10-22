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

/**
 * Theme Gym Bro : bodybuilder qui frappe fort, puis se flex (Force) avant une crampe
 * (Lenteur) sur les joueurs proches — utile pour la quete buff.
 */
public class BodybuilderEntity extends Monster implements GeoEntity {
    public static final String ID = "bodybuilder";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int FLEX_COOLDOWN = 20 * 10;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int flexCooldown = FLEX_COOLDOWN / 2;

    public BodybuilderEntity(EntityType<? extends BodybuilderEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
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
            target.push(0, 0.55, 0);
            target.needsSync = true;
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.8F);
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--flexCooldown > 0) {
            return;
        }
        flexCooldown = FLEX_COOLDOWN;
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.PLAYER_LEVELUP, 0.6F, 0.5F);
        addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 8, 1));
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 1.5, getZ(), 8, 0.4, 0.4, 0.4, 0.02);
        // Crampe collective : Force puis Lenteur pour la quete gym
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(6))) {
            p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 5, 0));
            p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 8, 2));
            p.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cCrampe post-flex !"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<BodybuilderEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
