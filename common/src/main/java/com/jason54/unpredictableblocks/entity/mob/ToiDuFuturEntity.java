package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
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
import org.jetbrains.annotations.Nullable;

/**
 * Thème Anachronisme : allié chromé au jetpack. Suit le joueur, attaque les monstres proches
 * et survole les obstacles (anim « special » = vol, « attack » = tir laser).
 */
public class ToiDuFuturEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "toi_du_futur";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int LASER_COOLDOWN = 20 * 3;
    private static final int JETPACK_COOLDOWN = 20 * 6;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable UUID allyOwner;
    private int laserCooldown;
    private int jetpackCooldown = JETPACK_COOLDOWN / 2;
    private int flyTicks;

    public ToiDuFuturEntity(EntityType<? extends ToiDuFuturEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    public void setAllyOwner(Player player) {
        this.allyOwner = player.getUUID();
    }

    public @Nullable Player getAllyOwner() {
        if (allyOwner == null || !(level() instanceof ServerLevel server)) {
            return null;
        }
        Entity e = server.getEntity(allyOwner);
        return e instanceof Player p ? p : null;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof ToiDuFuturEntity) && super.canAttack(target);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        Player owner = getAllyOwner();
        if (owner != null && getTarget() == null && distanceToSqr(owner) > 16) {
            getNavigation().moveTo(owner, 1.2);
        }
        if (flyTicks > 0) {
            flyTicks--;
            setNoGravity(true);
            setDeltaMovement(getDeltaMovement().x, Math.max(0.08, getDeltaMovement().y), getDeltaMovement().z);
            level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 2, 0.15, 0.05, 0.15, 0.01);
            if (flyTicks == 0) {
                setNoGravity(false);
            }
        } else if (--jetpackCooldown <= 0 && horizontalCollision) {
            jetpackCooldown = JETPACK_COOLDOWN;
            flyTicks = 25;
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.5F, 1.4F);
        }
        LivingEntity target = getTarget();
        if (target != null && --laserCooldown <= 0 && distanceToSqr(target) > 9 && distanceToSqr(target) < 100) {
            laserCooldown = LASER_COOLDOWN;
            fireLaser(level, target);
        }
    }

    private void fireLaser(ServerLevel level, LivingEntity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.GUARDIAN_ATTACK, 0.8F, 1.6F);
        Vec3 from = getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 0.1) {
            return;
        }
        dir = dir.normalize();
        for (double d = 0; d < len; d += 0.4) {
            Vec3 p = from.add(dir.scale(d));
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        target.hurtServer(level, damageSources().mobAttack(this), 5.0F);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && player.getUUID().equals(allyOwner)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (allyOwner != null) {
            output.putString("AllyOwner", allyOwner.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        String s = input.getStringOr("AllyOwner", "");
        if (!s.isEmpty()) {
            try {
                allyOwner = UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {
                allyOwner = null;
            }
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<ToiDuFuturEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
