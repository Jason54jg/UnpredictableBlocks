package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Thème JDR D20 : faux coffre-dé. Dort jusqu'à interaction / coup ; mord ensuite.
 * À la mort, le dé roule (1–20) et détermine le butin.
 */
public class MimiqueD20Entity extends Monster implements GeoEntity {
    public static final String ID = "mimique_d20";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean awakened;

    public MimiqueD20Entity(EntityType<? extends MimiqueD20Entity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return awakened && super.canAttack(target);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            awaken(player);
        }
        return InteractionResult.SUCCESS;
    }

    private void awaken(Player player) {
        if (awakened) {
            return;
        }
        awakened = true;
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.CHEST_OPEN, 1.0F, 0.6F);
        setTarget(player);
        player.sendSystemMessage(Component.literal("§cLe coffre… a des dents !"));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!awakened && source.getEntity() instanceof Player player) {
            awaken(player);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!awakened && target instanceof Player player) {
            awaken(player);
        }
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.0F, 0.8F);
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!awakened) {
            getNavigation().stop();
            setDeltaMovement(getDeltaMovement().multiply(0, 1, 0));
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int roll = 1 + getRandom().nextInt(20);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.5, getZ(), 20, 0.4, 0.4, 0.4, 0.1);
        playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 0.5F + roll * 0.05F);
        if (source.getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("§dD20 → §l" + roll));
        }
        var loot = new net.minecraft.world.entity.item.ItemEntity(
                level, getX(), getY() + 0.5, getZ(), lootForRoll(roll));
        level.addFreshEntity(loot);
    }

    private static ItemStack lootForRoll(int roll) {
        if (roll == 20) {
            return new ItemStack(Items.NETHERITE_INGOT);
        }
        if (roll >= 15) {
            return new ItemStack(Items.DIAMOND, 1 + (roll - 15));
        }
        if (roll >= 10) {
            return new ItemStack(Items.GOLD_INGOT, roll - 8);
        }
        if (roll >= 5) {
            return new ItemStack(Items.IRON_INGOT, roll);
        }
        if (roll == 1) {
            return new ItemStack(Items.POISONOUS_POTATO);
        }
        return new ItemStack(Items.COAL, roll);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<MimiqueD20Entity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
