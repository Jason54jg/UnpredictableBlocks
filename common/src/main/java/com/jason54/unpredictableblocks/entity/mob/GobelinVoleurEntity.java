package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Variation Lucky (LOW) : gobelin qui chipe l'objet en main du joueur puis s'enfuit.
 * Le butin est rangé dans sa main (sauvegardé avec l'entité) et lâché à coup sûr à sa mort.
 */
public class GobelinVoleurEntity extends Monster implements GeoEntity {
    public static final String ID = "gobelin_voleur";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int fleeRepath;
    private int specialCd = 20 * 4;

    public GobelinVoleurEntity(EntityType<? extends GobelinVoleurEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean hasLoot() {
        return !getMainHandItem().isEmpty();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !hasLoot() && super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (!hit || hasLoot() || !(target instanceof Player player)) {
            return hit;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return true;
        }
        setItemSlot(EquipmentSlot.MAINHAND, held.copy());
        setGuaranteedDrop(EquipmentSlot.MAINHAND);
        setPersistenceRequired();
        held.setCount(0);
        setTarget(null);
        playSound(SoundEvents.ITEM_PICKUP, 1.0F, 0.7F);
        player.sendSystemMessage(Component.literal("§2Gobelin : §a« Merci pour le cadeau ! »"));
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (hasLoot() && --fleeRepath <= 0) {
            fleeRepath = 20;
            Player chaser = level.getNearestPlayer(this, 16.0);
            if (chaser != null) {
                double dx = getX() - chaser.getX();
                double dz = getZ() - chaser.getZ();
                double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                getNavigation().moveTo(getX() + dx / len * 10, getY(), getZ() + dz / len * 10, 1.5);
            }
        }
        if (--specialCd > 0) return;
        specialCd = 20 * 6 + getRandom().nextInt(60);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.VILLAGER_NO, 0.8F, 1.8F);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 1.2, getZ(), 2, 0.3, 0.2, 0.3, 0.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<GobelinVoleurEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
