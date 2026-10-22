package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.compat.McAccess;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Thème Chats d'Internet : chat à lunettes apprivoisable (morue / saumon, une chance sur trois).
 * Sauvage, il s'approche et fait tomber l'objet que tu tiens. Apprivoisé, il te suit et s'assoit.
 */
public class ChatInternetEntity extends TamableAnimal implements GeoEntity {
    public static final String ID = "chat_internet";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ChatInternetEntity(EntityType<? extends ChatInternetEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new KnockItemGoal(this));
        goalSelector.addGoal(4, new TemptGoal(this, 0.9, this::isFood, true));
        goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 8.0F, 3.0F));
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.SALMON);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isTame() && isFood(stack)) {
            if (!level().isClientSide()) {
                usePlayerItem(player, hand, stack);
                if (getRandom().nextInt(3) == 0) {
                    tame(player);
                    setOrderedToSit(true);
                    triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL); // s'étire, content
                    level().broadcastEntityEvent(this, (byte) 7); // cœurs
                } else {
                    level().broadcastEntityEvent(this, (byte) 6); // fumée
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (isTame() && isOwnedBy(player) && !isFood(stack)) {
            if (!level().isClientSide()) {
                setOrderedToSit(!isOrderedToSit());
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<ChatInternetEntity>register(controllers, this::isInSittingPose);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** Chat sauvage : vient au contact d'un joueur proche et fait tomber l'objet tenu (toutes les 40 s au plus). */
    private static final class KnockItemGoal extends Goal {
        private static final int COOLDOWN = 20 * 40;
        private final ChatInternetEntity cat;
        private Player victim;
        private int cooldown = COOLDOWN / 2;

        KnockItemGoal(ChatInternetEntity cat) {
            this.cat = cat;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cat.isTame() || --cooldown > 0) {
                return false;
            }
            victim = cat.level().getNearestPlayer(cat, 8.0);
            return victim != null && !victim.getMainHandItem().isEmpty() && !victim.isCreative();
        }

        @Override
        public boolean canContinueToUse() {
            return victim != null && victim.isAlive() && cat.distanceToSqr(victim) < 144 && cooldown <= 0;
        }

        @Override
        public void tick() {
            cat.getLookControl().setLookAt(victim);
            cat.getNavigation().moveTo(victim, 1.2);
            if (cat.distanceToSqr(victim) < 2.5) {
                cat.triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
                McAccess.dropMainHand(victim);
                cat.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 0.6F);
                cooldown = COOLDOWN;
            }
        }

        @Override
        public void stop() {
            victim = null;
            cat.getNavigation().stop();
        }
    }
}
