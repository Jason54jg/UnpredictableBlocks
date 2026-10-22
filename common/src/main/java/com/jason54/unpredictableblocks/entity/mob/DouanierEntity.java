package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.compat.McAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Thème Retard de Vol : uniforme bleu + tampon. Prélève une « taxe » (1 objet) au clic / au contact,
 * et frappe au tampon si on insiste (après un coup reçu).
 */
public class DouanierEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "douanier";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int TAX_COOLDOWN = 20 * 12;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int taxCooldown = TAX_COOLDOWN / 3;
    private boolean aggressive;

    public DouanierEntity(EntityType<? extends DouanierEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--taxCooldown > 0 || aggressive) {
            return;
        }
        Player near = level.getNearestPlayer(this, 2.5);
        if (near != null && !near.isCreative() && !near.getMainHandItem().isEmpty()) {
            collectTax(near);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && !player.isCreative()) {
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.VILLAGER_TRADE, 1.0F, 0.8F);
            if (taxCooldown <= 0) {
                collectTax(player);
            } else {
                player.sendSystemMessage(Component.literal("§bDouane : contrôle en cours…"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    private void collectTax(Player player) {
        taxCooldown = TAX_COOLDOWN;
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            McAccess.dropMainHand(player);
            player.sendSystemMessage(Component.literal("§6Taxe aéroportuaire : 1 objet confisqué."));
            playSound(SoundEvents.ITEM_PICKUP, 1.0F, 0.5F);
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ItemStack taken = stack.split(1);
                if (!taken.isEmpty() && level() instanceof ServerLevel serverLevel) {
                    var item = new net.minecraft.world.entity.item.ItemEntity(
                            serverLevel, getX(), getY() + 1.0, getZ(), taken);
                    serverLevel.addFreshEntity(item);
                    player.sendSystemMessage(Component.literal("§6Taxe aéroportuaire : 1 objet confisqué."));
                    playSound(SoundEvents.ITEM_PICKUP, 1.0F, 0.5F);
                    return;
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        playSound(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F, 0.7F);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof Player player) {
            aggressive = true;
            setTarget(player);
            triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        }
        return hurt;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<DouanierEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
