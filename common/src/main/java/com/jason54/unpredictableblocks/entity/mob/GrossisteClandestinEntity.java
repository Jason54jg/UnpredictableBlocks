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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Thème Pénurie 2020 : marchand furtif. Clic droit = objet rare contre des émeraudes ;
 * s'enfuit si on le frappe (anim « attack » = fuite, « special » = vente).
 */
public class GrossisteClandestinEntity extends PathfinderMob implements GeoEntity {
    public static final String ID = "grossiste_clandestin";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int PRICE = 8;
    private static final ItemStack[] WARES = {
            new ItemStack(Items.NETHERITE_SCRAP),
            new ItemStack(Items.DIAMOND, 2),
            new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.ENDER_PEARL, 4),
            new ItemStack(Items.TOTEM_OF_UNDYING),
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean fleeing;
    private int fleeTicks;

    public GrossisteClandestinEntity(EntityType<? extends GrossisteClandestinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (fleeing && --fleeTicks <= 0) {
            discard();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (fleeing) {
            return InteractionResult.FAIL;
        }
        if (!level().isClientSide()) {
            if (countEmeralds(player) < PRICE) {
                player.sendSystemMessage(Component.literal("§7Grossiste : §c" + PRICE + " émeraudes§7 pour la marchandise."));
                return InteractionResult.SUCCESS;
            }
            takeEmeralds(player, PRICE);
            ItemStack ware = WARES[getRandom().nextInt(WARES.length)].copy();
            McAccess.giveOrDrop(player, ware);
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            playSound(SoundEvents.VILLAGER_YES, 1.0F, 0.9F);
            player.sendSystemMessage(Component.literal("§aMarché conclu. Discrétion, camarade."));
        }
        return InteractionResult.SUCCESS;
    }

    private static int countEmeralds(Player player) {
        int n = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.EMERALD)) {
                n += s.getCount();
            }
        }
        return n;
    }

    private static void takeEmeralds(Player player, int amount) {
        int left = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && left > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.EMERALD)) {
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !fleeing) {
            fleeing = true;
            fleeTicks = 20 * 8;
            triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
            playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.2F);
            getNavigation().moveTo(getX() + (getRandom().nextDouble() - 0.5) * 16,
                    getY(), getZ() + (getRandom().nextDouble() - 0.5) * 16, 1.6);
        }
        return hurt;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<GrossisteClandestinEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
