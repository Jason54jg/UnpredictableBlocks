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
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
            case ANACHRONISME -> applyAnachronisme(level, player, origin, tier);
            case BOULANGERIE -> applyBoulangerie(level, player, origin, tier);
            case BSOD -> applyBsod(level, player, tier);
            case VOLAILLE -> applyVolaille(level, origin, tier);
            case IMPOTS -> applyImpots(level, player, tier);
            case STREAMER -> applyStreamer(level, origin, tier);
            case PROPRETE -> applyProprete(level, player, tier);
            case GRAVITE_ZERO -> applyGravite(level, player, tier);
            case FAST_FOOD -> applyFastFood(level, player, tier);
            case CAUCHEMAR_MATIN -> applyCauchemar(level, player, tier);
            case FILM_ACTION -> applyFilmAction(level, player, tier);
            case JDR_D20 -> applyJdr(level, player, tier);
            case PENURIE_2020 -> applyPenurie(level, player, tier);
            case COMPLOT -> applyComplot(level, origin, tier);
            case LAG_BUG -> applyLagBug(level, origin, tier);
            case DISCO_80 -> applyDisco(level, origin, tier);
            case PLOMBERIE -> applyPlomberie(level, player, origin, tier);
            case FORTUNE_COOKIE -> applyFortuneCookie(level, player, tier);
            case CAMPING -> applyCamping(level, origin, tier);
            case GYM_BRO -> applyGymBro(level, player, tier);
            case ART_MODERNE -> applyArtModerne(level, origin, tier);
            case MAGIE_COMPTOIR -> applyMagie(level, origin, tier);
            case CASINO -> applyCasino(level, player, tier);
            case INVASION_MEMES -> applyMemes(level, player, tier);
        }
    }

    private static void applyAnachronisme(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            spawnMonster(level, origin, EntityType.RAVAGER);
        } else if (tier == LuckTier.LOW) {
            player.getInventory().clearContent();
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE_SWORD));
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FLINT));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 60, 2));
        }
    }

    private static void applyBoulangerie(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20 * 120, 0));
        } else if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 8; i++) {
                spawnPassive(level, origin, EntityType.CHICKEN);
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 30, 2));
            player.setSecondsOnFire(5);
        }
    }

    private static void applyBsod(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 30, 4));
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 30, 4));
        } else if (tier == LuckTier.NEUTRAL) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 3, 0));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 10, 0));
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 10, 0));
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
        } else {
            playerCaquetage(level, origin);
        }
    }

    private static void playerCaquetage(ServerLevel level, BlockPos origin) {
        for (int i = 0; i < 4; i++) {
            spawnPassive(level, origin, EntityType.CHICKEN);
        }
    }

    private static void applyImpots(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD, 16));
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT, 16));
        } else if (tier == LuckTier.LOW) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 30, 1));
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
        } else if (tier == LuckTier.HIGH) {
            level.explode(null, origin.getX(), origin.getY(), origin.getZ(), 0.0f, Level.ExplosionInteraction.NONE);
        }
    }

    private static void applyProprete(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            clearHostiles(level, player);
        } else if (tier == LuckTier.NEUTRAL) {
            fillTemporaryWool(level, player.blockPosition(), 3);
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 0));
            freezeFloor(level, player.blockPosition(), 7);
        }
    }

    private static void applyGravite(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 60, 4));
        } else if (tier == LuckTier.NEUTRAL) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 2.5, 0));
        } else {
            player.hurt(level.damageSources().fellOutOfWorld(), 6);
        }
    }

    private static void applyFastFood(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.getFoodData().eat(20, 1.0f);
        } else if (tier == LuckTier.NEUTRAL) {
            player.getFoodData().eat(20, 1.0f);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 20, 3));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 20, 0));
        } else {
            spawnMonster(level, player.blockPosition(), EntityType.ZOMBIE);
        }
    }

    private static void applyCauchemar(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            player.getFoodData().setFoodLevel(0);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 60, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 60, 2));
        } else if (tier == LuckTier.HIGH && player.getRespawnPosition() != null) {
            var bed = player.getRespawnPosition();
            player.teleportTo(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5);
        }
    }

    private static void applyFilmAction(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 2.0, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 30, 0));
        } else if (tier == LuckTier.NEUTRAL) {
            level.explode(player, player.getX(), player.getY(), player.getZ(), 4.0f, Level.ExplosionInteraction.TNT);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 5, 4));
        }
    }

    private static void applyJdr(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            level.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(), "effect give @s minecraft:poison 10 1");
            var stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                stack.setDamageValue(stack.getMaxDamage());
            }
        } else if (tier == LuckTier.HIGH) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD));
        }
    }

    private static void applyPenurie(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER, 64));
        } else if (tier == LuckTier.LOW) {
            stripPaperProducts(player);
        }
    }

    private static void applyComplot(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            for (int i = 0; i < 3; i++) {
                EnderMan man = EntityType.ENDERMAN.create(level);
                if (man != null) {
                    man.moveTo(Vec3.atBottomCenterOf(origin).add(i, 0, 0));
                    level.addFreshEntity(man);
                }
            }
        } else if (tier == LuckTier.NEUTRAL) {
            flatten(level, origin, 10);
        }
    }

    private static void applyLagBug(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            hollowChunkSlice(level, origin);
        } else if (tier == LuckTier.HIGH) {
            duplicateNearbyAnimals(level, origin);
        }
    }

    private static void applyDisco(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH || tier == LuckTier.NEUTRAL) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos floor = origin.offset(dx, -1, dz);
                    if (level.getBlockState(floor).isSolidRender(level, floor)) {
                        level.setBlockAndUpdate(floor, Blocks.GLOWSTONE.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void applyPlomberie(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 45, 1));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 45, 0));
        } else if (tier == LuckTier.NEUTRAL) {
            player.teleportTo(origin.getX(), origin.getY() - 30, origin.getZ());
        } else {
            for (int i = 0; i < 10; i++) {
                Silverfish fish = EntityType.SILVERFISH.create(level);
                if (fish != null) {
                    fish.moveTo(Vec3.atBottomCenterOf(origin));
                    level.addFreshEntity(fish);
                }
            }
        }
    }

    private static void applyFortuneCookie(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 180, 4));
        } else if (tier == LuckTier.LOW) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 15, 1));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 0));
        }
    }

    private static void applyCamping(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            spawnMonster(level, origin, EntityType.POLAR_BEAR);
        } else if (tier == LuckTier.HIGH) {
            level.setBlock(origin, Blocks.CAMPFIRE.defaultBlockState(), 3);
        }
    }

    private static void applyGymBro(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 30, 3));
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 30, 2));
        } else if (tier == LuckTier.NEUTRAL) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 20, 3));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 45, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 45, 3));
        }
    }

    private static void applyArtModerne(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 32; i++) {
                BlockPos p = origin.offset(level.random.nextInt(8) - 4, level.random.nextInt(3), level.random.nextInt(8) - 4);
                level.setBlockAndUpdate(p, Blocks.WHITE_CONCRETE.defaultBlockState());
            }
        }
    }

    private static void applyMagie(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 30; i++) {
                spawnPassive(level, origin, EntityType.RABBIT);
            }
        } else if (tier == LuckTier.LOW) {
            spawnMonster(level, origin, EntityType.WOLF);
        }
    }

    private static void applyCasino(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD, 32));
            player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 16));
        } else if (tier == LuckTier.LOW) {
            stripCurrency(player);
        }
    }

    private static void applyMemes(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 20, 4));
        } else if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 4; i++) {
                spawnMonster(level, player.blockPosition(), EntityType.SKELETON);
            }
        }
    }

    private static void clearHostiles(ServerLevel level, ServerPlayer player) {
        level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50),
                        e -> e instanceof net.minecraft.world.entity.monster.Monster)
                .forEach(net.minecraft.world.entity.Entity::discard);
    }

    private static void fillTemporaryWool(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    level.setBlockAndUpdate(center.offset(dx, dy, dz), Blocks.WHITE_WOOL.defaultBlockState());
                }
            }
        }
    }

    private static void freezeFloor(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos floor = center.offset(dx, -1, dz);
                level.setBlockAndUpdate(floor, Blocks.ICE.defaultBlockState());
            }
        }
    }

    private static void flatten(ServerLevel level, BlockPos origin, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 1; dy <= 16; dy++) {
                    level.setBlockAndUpdate(origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void hollowChunkSlice(ServerLevel level, BlockPos origin) {
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int dy = 0; dy < 32; dy++) {
                    level.setBlockAndUpdate(origin.offset(dx, -dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void duplicateNearbyAnimals(ServerLevel level, BlockPos origin) {
        level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(origin).inflate(16),
                e -> e.getType() == EntityType.COW || e.getType() == EntityType.PIG || e.getType() == EntityType.SHEEP)
                .forEach(e -> {
                    var copy = e.getType().create(level);
                    if (copy != null) {
                        copy.moveTo(e.getX() + 1, e.getY(), e.getZ());
                        level.addFreshEntity(copy);
                    }
                });
    }

    private static void spawnPassive(ServerLevel level, BlockPos origin, EntityType<?> type) {
        var entity = type.create(level);
        if (entity != null) {
            entity.moveTo(Vec3.atBottomCenterOf(origin).add(level.random.nextDouble() * 4 - 2, 0, level.random.nextDouble() * 4 - 2));
            level.addFreshEntity(entity);
        }
    }

    private static void stripCurrency(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT) || stack.is(net.minecraft.world.item.Items.EMERALD)) {
                player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    private static void stripPaperProducts(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(net.minecraft.world.item.Items.PAPER)
                    || stack.is(net.minecraft.world.item.Items.BOOK)
                    || stack.is(net.minecraft.world.item.Items.BOOKSHELF)) {
                player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    private static void spawnMonster(ServerLevel level, BlockPos origin, EntityType<?> type) {
        var entity = type.create(level);
        if (entity != null) {
            entity.moveTo(Vec3.atBottomCenterOf(origin));
            level.addFreshEntity(entity);
        }
    }
}
