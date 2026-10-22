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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Thème Vampire Gothique : combat au corps à corps et, de temps en temps, se dissout en nuée
 * de chauves-souris (anim « special ») pour réapparaître derrière sa cible.
 */
public class ComteDraculaEntity extends Monster implements GeoEntity {
    public static final String ID = "comte_dracula";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int BAT_COOLDOWN = 20 * 8;
    /** Durée de l'anim « special » avant la réapparition. */
    private static final int BAT_DURATION = 18;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int batCooldown = BAT_COOLDOWN;
    private int batTicks = -1;

    public ComteDraculaEntity(EntityType<? extends ComteDraculaEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            heal(2.0F); // il se nourrit
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (batTicks >= 0) {
            level.sendParticles(ParticleTypes.SQUID_INK, getX(), getY() + 1.2, getZ(), 4, 0.4, 0.6, 0.4, 0.02);
            if (--batTicks < 0 && target != null) {
                reappearBehind(level, target);
            }
            return;
        }
        if (target != null && --batCooldown <= 0 && distanceToSqr(target) > 9) {
            batCooldown = BAT_COOLDOWN + getRandom().nextInt(80);
            batTicks = BAT_DURATION;
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.BAT_TAKEOFF, 1.2F, 0.7F);
        }
    }

    private void reappearBehind(ServerLevel level, LivingEntity target) {
        // randomTeleport change de signature en 26.3 : test de collision fait ici
        Vec3 behind = target.position().subtract(target.getLookAngle().multiply(2.0, 0.0, 2.0));
        Vec3 destination = level.noCollision(this, getBoundingBox().move(behind.subtract(position())))
                ? behind : target.position();
        teleportTo(destination.x, destination.y, destination.z);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 15, 0.4, 0.8, 0.4, 0.02);
        playSound(SoundEvents.BAT_AMBIENT, 1.0F, 0.6F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<ComteDraculaEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
