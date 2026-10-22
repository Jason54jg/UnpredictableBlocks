package com.jason54.unpredictableblocks.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.registry.ModEntities;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.theme.LuckyVariant;
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
import net.minecraft.world.phys.Vec3;

/**
 * Lucky Block cassé, animé avant de disparaître (purement visuel) : fusée selon le thème, ou explosion,
 * enfoncement, disparition, rebonds ({@link BreakAnimation}).
 * <p>
 * Le mouvement est calculé à l'identique côté serveur et client depuis {@code tickCount}
 * (comme les feux d'artifice). Chaque thème a son {@link RocketStyle} : trajectoire, anim GeckoLib
 * et particules. Jamais sauvegardée ({@code noSave}).
 */
public class LuckyRocketEntity extends Entity implements GeoEntity {
    /** Durée totale = longueur de animation.lucky_rocket.launch (2 s). */
    public static final int LIFETIME = 40;
    /** Tremblement au sol avant décollage (0.5 s, aligné sur l'anim). */
    public static final int WINDUP_TICKS = 10;

    private static final EntityDataAccessor<String> THEME =
            SynchedEntityData.defineId(LuckyRocketEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(LuckyRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION =
            SynchedEntityData.defineId(LuckyRocketEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    /** Point de départ (position de spawn), pour une trajectoire identique serveur/client. */
    private Vec3 origin;
    /** Serveur : événements du cassage (butin, effets, structures), lancés à la fin de l'animation. */
    private Runnable onFinish;

    public LuckyRocketEntity(EntityType<? extends LuckyRocketEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    /** Remplace le bloc cassé : même position, même thème. */
    public static void launch(ServerLevel level, BlockPos pos, LuckyTheme theme, LuckyVariant variant, Runnable onFinish) {
        LuckyRocketEntity rocket = new LuckyRocketEntity(ModEntities.LUCKY_ROCKET.get(), level);
        rocket.onFinish = onFinish;
        rocket.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        rocket.entityData.set(THEME, theme.id());
        rocket.entityData.set(VARIANT, variant.ordinal());
        rocket.entityData.set(ANIMATION, BreakAnimation.roll(level.getRandom()).ordinal());
        if (!level.addFreshEntity(rocket)) {
            rocket.finish();
        }
    }

    /** Lance une seule fois les événements du cassage. */
    private void finish() {
        Runnable events = onFinish;
        onFinish = null;
        if (events != null) {
            events.run();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        // Chunk déchargé / monde fermé avant la fin de l'animation : les récompenses ne sont pas perdues
        if (!level().isClientSide()) {
            finish();
        }
        super.remove(reason);
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

    public LuckyVariant getVariant() {
        return LuckyVariant.byOrdinal(entityData.get(VARIANT));
    }

    public BreakAnimation getBreakAnimation() {
        return BreakAnimation.byOrdinal(entityData.get(ANIMATION));
    }

    public RocketStyle.Look getLook() {
        return RocketStyle.look(getTheme());
    }

    @Override
    public void tick() {
        super.tick();
        if (origin == null) {
            origin = position();
        }
        RocketStyle.Look look = getLook();
        BreakAnimation animation = getBreakAnimation();
        if (animation != BreakAnimation.ROCKET) {
            tickOtherAnimation(look, animation);
            return;
        }
        int flying = tickCount - WINDUP_TICKS;
        Vec3 offset = look.style().offset(flying);
        setPos(origin.x + offset.x, origin.y + offset.y, origin.z + offset.z);

        Level level = level();
        if (level.isClientSide()) {
            spawnTrail(level, look, flying);
            return;
        }
        if (flying == 0) {
            level.playSound(null, getX(), getY(), getZ(),
                    look.style().hasFlame() ? SoundEvents.FIREWORK_ROCKET_LAUNCH : SoundEvents.ILLUSIONER_CAST_SPELL,
                    SoundSource.BLOCKS, 1.5F, 0.7F);
        }
        if (tickCount >= LIFETIME && level instanceof ServerLevel serverLevel) {
            double y = getY() + 0.5;
            serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), y, getZ(), 30, 0.4, 0.4, 0.4, 0.05);
            serverLevel.sendParticles(look.trail(), getX(), y, getZ(), 25, 0.5, 0.5, 0.5, 0.0);
            serverLevel.sendParticles(look.accent(), getX(), y, getZ(), 15, 0.3, 0.3, 0.3, 0.1);
            level.playSound(null, getX(), y, getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.0F, 1.0F);
            discard();
        }
    }

    /** Animations autres que la fusée : trajectoire de {@link BreakAnimation}, particules et fin propres à chacune. */
    private void tickOtherAnimation(RocketStyle.Look look, BreakAnimation animation) {
        Vec3 offset = animation.offset(tickCount, getId());
        setPos(origin.x + offset.x, origin.y + offset.y, origin.z + offset.z);
        Level level = level();
        RandomSource random = level.getRandom();
        double x = getX();
        double y = getY() + 0.5;
        double z = getZ();
        if (level.isClientSide()) {
            switch (animation) {
                case POP -> level.addParticle(look.accent(), x + (random.nextDouble() - 0.5), y + (random.nextDouble() - 0.5),
                        z + (random.nextDouble() - 0.5), 0, 0.02, 0);
                case SINK -> level.addParticle(ParticleTypes.POOF, x + (random.nextDouble() - 0.5) * 1.2, origin.y + 0.1,
                        z + (random.nextDouble() - 0.5) * 1.2, 0, 0.03, 0);
                case VANISH -> level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5) * 1.4,
                        y + (random.nextDouble() - 0.5) * 1.4, z + (random.nextDouble() - 0.5) * 1.4, 0, 0, 0);
                case BOUNCE -> {
                    if (offset.y < 0.1) {
                        level.addParticle(look.trail(), x, getY() + 0.05, z, 0, 0.05, 0);
                    }
                }
                default -> {}
            }
            return;
        }
        if (tickCount == 1) {
            level.playSound(null, x, y, z, switch (animation) {
                case POP -> SoundEvents.CREEPER_PRIMED;
                case SINK -> SoundEvents.GRAVEL_BREAK;
                case VANISH -> SoundEvents.ILLUSIONER_PREPARE_MIRROR;
                default -> SoundEvents.SLIME_JUMP;
            }, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
        if (animation == BreakAnimation.BOUNCE && tickCount % 13 == 0) {
            level.playSound(null, x, y, z, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.8F, 1.4F);
        }
        if (tickCount >= animation.lifetime() && level instanceof ServerLevel serverLevel) {
            switch (animation) {
                case POP -> {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 2, 0.2, 0.2, 0.2, 0);
                    serverLevel.sendParticles(look.trail(), x, y, z, 40, 0.6, 0.6, 0.6, 0);
                    level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.6F, 1.4F);
                }
                case SINK -> serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, origin.y + 0.2, z, 8, 0.3, 0.1, 0.3, 0.01);
                case VANISH -> {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 30, 0.3, 0.3, 0.3, 0.15);
                    level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8F, 1.6F);
                }
                default -> serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 15, 0.3, 0.3, 0.3, 0.05);
            }
            serverLevel.sendParticles(look.accent(), x, y, z, 10, 0.4, 0.4, 0.4, 0.05);
            discard();
        }
    }

    /** Fumée au sol pendant le tremblement, puis traînée (couleur du thème + accent + flamme selon le style). */
    private void spawnTrail(Level level, RocketStyle.Look look, int flying) {
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
        if (look.style().hasFlame()) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.FLAME,
                        x + (random.nextDouble() - 0.5) * 0.3, y - 0.2, z + (random.nextDouble() - 0.5) * 0.3,
                        (random.nextDouble() - 0.5) * 0.02, -0.15, (random.nextDouble() - 0.5) * 0.02);
            }
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    x, y - 0.6, z,
                    (random.nextDouble() - 0.5) * 0.05, -0.1, (random.nextDouble() - 0.5) * 0.05);
        }
        level.addParticle(look.trail(),
                x + (random.nextDouble() - 0.5) * 0.5, y - 0.3, z + (random.nextDouble() - 0.5) * 0.5,
                0.0, -0.05, 0.0);
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x + (random.nextDouble() - 0.5) * 0.4, y - 0.4, z + (random.nextDouble() - 0.5) * 0.4,
                (random.nextDouble() - 0.5) * 0.03, -0.05, (random.nextDouble() - 0.5) * 0.03);
        if (random.nextInt(2) == 0) {
            level.addParticle(look.accent(),
                    x + (random.nextDouble() - 0.5) * 0.8, y + random.nextDouble() * 0.8, z + (random.nextDouble() - 0.5) * 0.8,
                    0.0, 0.02, 0.0);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("launch", state -> state.setAndContinue(
                getBreakAnimation() == BreakAnimation.ROCKET ? getLook().style().animation() : getBreakAnimation().animation())));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(THEME, LuckyTheme.BOULANGERIE.id());
        builder.define(VARIANT, LuckyVariant.NORMAL.ordinal());
        builder.define(ANIMATION, BreakAnimation.ROCKET.ordinal());
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
