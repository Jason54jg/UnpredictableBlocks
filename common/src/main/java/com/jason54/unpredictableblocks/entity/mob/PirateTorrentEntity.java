package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.compat.McAccess;
import com.jason54.unpredictableblocks.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
 * Theme Piraterie Torrent : pirate qui « telecharge » (vole) l'objet en main,
 * et lache un Disque Dur du Capitaine a la mort.
 */
public class PirateTorrentEntity extends Monster implements GeoEntity {
    public static final String ID = "pirate_torrent";
    private static final MobAnimations ANIMS = MobAnimations.of(ID);
    private static final int DOWNLOAD_COOLDOWN = 20 * 10;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int downloadCooldown = DOWNLOAD_COOLDOWN / 2;

    public PirateTorrentEntity(EntityType<? extends PirateTorrentEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim(MobAnimations.ACTION, MobAnimations.ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof Player player && downloadCooldown <= 0 && !player.getMainHandItem().isEmpty()) {
            downloadCooldown = DOWNLOAD_COOLDOWN;
            triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
            McAccess.dropMainHand(player);
            player.sendSystemMessage(Component.literal("§4Telechargement illicite — objet pirate !"));
            playSound(SoundEvents.UI_TOAST_IN, 1.0F, 0.7F);
            level.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1, getZ(), 15, 0.4, 0.5, 0.4, 0.4);
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (downloadCooldown > 0) {
            downloadCooldown--;
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        var loot = new net.minecraft.world.entity.item.ItemEntity(
                level, getX(), getY() + 0.5, getZ(), new ItemStack(ModItems.DISQUE_DUR_CAPITAINE.get()));
        level.addFreshEntity(loot);
        triggerAnim(MobAnimations.ACTION, MobAnimations.SPECIAL);
        playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 0.8F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ANIMS.<PirateTorrentEntity>register(controllers, null);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
