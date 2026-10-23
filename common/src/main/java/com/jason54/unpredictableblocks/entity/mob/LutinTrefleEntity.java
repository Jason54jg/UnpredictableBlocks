package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Variation Lucky (HIGH) : lutin à marmite d'or. Il fuit le joueur, sème des pépites à chaque coup
 * et disparaît au bout d'une minute s'il n'est pas attrapé.
 */
public class LutinTrefleEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "lutin_trefle";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int LIFETIME = 20 * 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicks = LIFETIME;
    private int specialCd = 20 * 4;

    public LutinTrefleEntity(EntityType<? extends LutinTrefleEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 7.0F, 1.1, 1.5));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--lifeTicks <= 0) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.6, getZ(), 20, 0.4, 0.5, 0.4, 0.1);
            playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.5F);
            discard();
            return;
        }
        if (--specialCd > 0) return;
        specialCd = 20 * 5 + getRandom().nextInt(60);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.NOTE_BLOCK_HARP.value(), 0.8F, 1.6F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (!hurt) {
            return false;
        }
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.8F);
        level.addFreshEntity(new ItemEntity(level, getX(), getY() + 0.5, getZ(),
                new ItemStack(Items.GOLD_NUGGET, 1 + getRandom().nextInt(3))));
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<LutinTrefleEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
