package com.jason54.unpredictableblocks.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.registry.ModEntities;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Lucky Block qui décolle comme une fusée quand on le casse (purement visuel).
 * <p>
 * Le mouvement est calculé à l'identique côté serveur et client depuis {@code tickCount}
 * (comme les feux d'artifice), l'anim GeckoLib « launch » gère tremblement / vrille / flamme,
 * et les particules donnent la traînée de fumée. Jamais sauvegardée ({@code noSave}).
 */
public class LuckyRocketEntity extends Entity implements GeoEntity {
    /** Durée totale = longueur de animation.lucky_rocket.launch (2 s). */
    public static final int LIFETIME = 40;
    /** Tremblement au sol avant décollage (0.5 s, aligné sur l'anim). */
    public static final int WINDUP_TICKS = 10;
    private static final double ACCELERATION = 0.04;
    private static final double MAX_SPEED = 0.8;

    private static final EntityDataAccessor<String> THEME =
            SynchedEntityData.defineId(LuckyRocketEntity.class, EntityDataSerializers.STRING);
    private static final RawAnimation LAUNCH_ANIM =
            RawAnimation.begin().thenPlayAndHold("animation.lucky_rocket.launch");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public LuckyRocketEntity(EntityType<? extends LuckyRocketEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    /** Remplace le bloc cassé : même position, même thème. */
    public static void launch(ServerLevel level, BlockPos pos, LuckyTheme theme) {
        LuckyRocketEntity rocket = new LuckyRocketEntity(ModEntities.LUCKY_ROCKET.get(), level);
        rocket.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        rocket.entityData.set(THEME, theme.id());
        level.addFreshEntity(rocket);
    }

    public LuckyTheme getTheme() {
        String id = entityData.get(THEME);
        for (LuckyTheme candidate : LuckyTheme.values()) {
            if (candidate.id().equals(id)) {
                return candidate;
            }
        }
        return LuckyTheme.BOULANGERIE;
    }

    @Override
    public void tick() {
        super.tick();
        int flying = tickCount - WINDUP_TICKS;
        if (flying > 0) {
            setPos(getX(), getY() + Math.min(ACCELERATION * flying, MAX_SPEED), getZ());
        }

        Level level = level();
        if (level.isClientSide()) {
            spawnTrail(level, flying);
            return;
        }
        if (flying == 0) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 1.5F, 0.7F);
        }
        if (tickCount >= LIFETIME && level instanceof ServerLevel serverLevel) {
            double y = getY() + 0.5;
            serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), y, getZ(), 30, 0.4, 0.4, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.FIREWORK, getX(), y, getZ(), 20, 0.2, 0.2, 0.2, 0.15);
            level.playSound(null, getX(), y, getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.0F, 1.0F);
            discard();
        }
    }

    /** Fumée au sol pendant le tremblement, puis traînée flamme + fumée sous le bloc. */
    private void spawnTrail(Level level, int flying) {
        RandomSource random = level.getRandom();
        double x = getX();
        double y = getY();
        double z = getZ();
        if (flying <= 0) {
            if (random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.SMOKE,
                        x + (random.nextDouble() - 0.5), y + 0.05, z + (random.nextDouble() - 0.5),
                        0.0, 0.02, 0.0);
            }
            return;
        }
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.FLAME,
                    x + (random.nextDouble() - 0.5) * 0.3, y - 0.2, z + (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.02, -0.15, (random.nextDouble() - 0.5) * 0.02);
        }
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x + (random.nextDouble() - 0.5) * 0.4, y - 0.4, z + (random.nextDouble() - 0.5) * 0.4,
                (random.nextDouble() - 0.5) * 0.03, -0.05, (random.nextDouble() - 0.5) * 0.03);
        level.addParticle(ParticleTypes.LARGE_SMOKE,
                x, y - 0.6, z,
                (random.nextDouble() - 0.5) * 0.05, -0.1, (random.nextDouble() - 0.5) * 0.05);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("launch", state -> state.setAndContinue(LAUNCH_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(THEME, LuckyTheme.BOULANGERIE.id());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
