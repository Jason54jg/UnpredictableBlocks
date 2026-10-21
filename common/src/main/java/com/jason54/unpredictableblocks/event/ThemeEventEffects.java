package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

/**
 * Effets instantanés (buffs, spawns, malus) complémentaires aux loot tables.
 */
public final class ThemeEventEffects {
    private ThemeEventEffects() {}

    public static void apply(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckTier tier
    ) {
        switch (theme) {
            case BOULANGERIE -> applyBoulangerie(level, player, origin, tier);
            case BSOD -> applyBsod(level, player, tier);
            case VOLAILLE -> applyVolaille(level, origin, tier);
            case STREAMER -> applyStreamer(level, origin, tier);
            case PROPRETE -> applyProprete(level, player, tier);
            case GRAVITE_ZERO -> applyGravite(level, player, tier);
            case INVASION_MEMES -> applyMemes(level, player, tier);
            default -> { /* loot + structures suffisent pour l'instant */ }
        }
    }

    private static void applyBoulangerie(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20 * 120, 0));
        } else if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 8; i++) {
                var chicken = EntityType.CHICKEN.create(level);
                if (chicken != null) {
                    chicken.moveTo(Vec3.atBottomCenterOf(origin).add(level.random.nextInt(5) - 2, 0, level.random.nextInt(5) - 2));
                    level.addFreshEntity(chicken);
                }
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 2));
            player.setSecondsOnFire(5);
        }
    }

    private static void applyBsod(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 4));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 30, 4));
        } else if (tier == LuckTier.LOW) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 10, 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 10, 0));
        }
    }

    private static void applyVolaille(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            for (int i = 0; i < 20; i++) {
                spawnPassive(level, origin, EntityType.CHICKEN);
            }
        } else if (tier == LuckTier.LOW) {
            var golem = EntityType.IRON_GOLEM.create(level);
            if (golem != null) {
                golem.moveTo(Vec3.atBottomCenterOf(origin));
                golem.setHealth(500);
                level.addFreshEntity(golem);
            }
        }
    }

    private static void applyStreamer(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            for (int i = 0; i < 15; i++) {
                Zombie zombie = EntityType.ZOMBIE.create(level);
                if (zombie != null) {
                    zombie.moveTo(Vec3.atBottomCenterOf(origin).add(level.random.nextInt(8) - 4, 0, level.random.nextInt(8) - 4));
                    level.addFreshEntity(zombie);
                }
            }
        }
    }

    private static void applyProprete(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50),
                            e -> e instanceof net.minecraft.world.entity.monster.Monster)
                    .forEach(net.minecraft.world.entity.Entity::discard);
        } else if (tier == LuckTier.LOW) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 0));
        }
    }

    private static void applyGravite(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 60, 4));
        } else if (tier == LuckTier.LOW) {
            player.hurt(level.damageSources().fellOutOfWorld(), 4);
        }
    }

    private static void applyMemes(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 4));
        }
    }

    private static void spawnPassive(ServerLevel level, BlockPos origin, EntityType<?> type) {
        var entity = type.create(level);
        if (entity != null) {
            entity.moveTo(Vec3.atBottomCenterOf(origin).add(level.random.nextDouble() * 4 - 2, 0, level.random.nextDouble() * 4 - 2));
            level.addFreshEntity(entity);
        }
    }
}
