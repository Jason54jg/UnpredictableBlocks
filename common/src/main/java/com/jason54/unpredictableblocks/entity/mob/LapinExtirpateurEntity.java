package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Thème Magie de Comptoir : lapin en haut-de-forme. Neutre ; sort des objets / cartes de son chapeau,
 * puis disparaît dans un nuage pour réapparaître ailleurs.
 */
public class LapinExtirpateurEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "lapin_extirpateur";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int TRICK_COOLDOWN = 20 * 8;
    private static final ItemStack[] TRICKS = {
            new ItemStack(Items.PAPER),
            new ItemStack(Items.GOLD_NUGGET),
            new ItemStack(Items.RABBIT_FOOT),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.MAP),
            new ItemStack(Items.NAME_TAG),
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int trickCooldown = TRICK_COOLDOWN / 2;
    private int vanishTicks = -1;

    public LapinExtirpateurEntity(EntityType<? extends LapinExtirpateurEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (vanishTicks >= 0) {
            level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 6, 0.3, 0.4, 0.3, 0.02);
            if (--vanishTicks < 0) {
                reappear(level);
            }
            return;
        }
        if (--trickCooldown > 0) {
            return;
        }
        trickCooldown = TRICK_COOLDOWN + getRandom().nextInt(40);
        if (getRandom().nextBoolean()) {
            pullFromHat(level);
        } else {
            startVanish();
        }
    }

    private void pullFromHat(ServerLevel level) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.ENDER_CHEST_OPEN, 0.7F, 1.4F);
        ItemStack drop = TRICKS[getRandom().nextInt(TRICKS.length)].copy();
        var item = new net.minecraft.world.entity.item.ItemEntity(level, getX(), getY() + 1.2, getZ(), drop);
        level.addFreshEntity(item);
        level.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.2, getZ(), 12, 0.3, 0.4, 0.3, 0.5);
    }

    private void startVanish() {
        vanishTicks = 16;
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.2F);
        setInvisible(true);
    }

    private void reappear(ServerLevel level) {
        double x = getX() + (getRandom().nextDouble() - 0.5) * 10;
        double z = getZ() + (getRandom().nextDouble() - 0.5) * 10;
        Vec3 dest = new Vec3(x, getY(), z);
        if (level.noCollision(this, getBoundingBox().move(dest.subtract(position())))) {
            teleportTo(dest.x, dest.y, dest.z);
        }
        setInvisible(false);
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 20, 0.4, 0.5, 0.4, 0.02);
        playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.8F, 0.9F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<LapinExtirpateurEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
