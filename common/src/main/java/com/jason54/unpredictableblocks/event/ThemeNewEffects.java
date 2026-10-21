package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.compat.McRefs;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.quest.QuestExtendedHandler;
import com.jason54.unpredictableblocks.quest.QuestNames;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Thèmes 25–32 (directive §3). */
public final class ThemeNewEffects {
    private ThemeNewEffects() {}

    public static void apply(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckTier tier
    ) {
        switch (theme) {
            case PAUSE_CAFE -> pauseCafe(player, tier);
            case RETARD_VOL -> retardVol(level, player, origin, tier);
            case ANANAS_PIZZA -> ananasPizza(level, player, origin, tier);
            case SPEEDRUNNER -> speedrunner(level, player, tier);
            case PIRATERIE_TORRENT -> piraterie(level, player, origin, tier);
            case ASMR_FIDGET -> asmr(level, player, tier);
            case VAMPIRE_GOTHIQUE -> vampire(player, tier);
            case MULTIVERS_SLIDERS -> multivers(level, player, origin, tier);
            default -> {}
        }
    }

    private static void pauseCafe(ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 120, 4));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 120, 2));
            player.sendSystemMessage(Component.literal("§6Overdose d'énergie pratique — minage ultra-rapide 2 min."));
        }
    }

    private static void retardVol(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = 0; dy <= 4; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        level.setBlockAndUpdate(origin.offset(dx, dy, dz), Blocks.GLASS.defaultBlockState());
                    }
                }
            }
            var douanier = EntitySpawnHelper.create(McRefs.VILLAGER, level);
            if (douanier != null) {
                douanier.setCustomName(Component.literal("Douanier"));
                douanier.setPos(Vec3.atBottomCenterOf(origin));
                level.addFreshEntity(douanier);
            }
            player.sendSystemMessage(Component.literal("§bVol annulé — périmètre de sécurité verre."));
        }
    }

    private static void ananasPizza(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            QuestExtendedHandler.onChefsSpawned(player);
            for (int i = 0; i < 10; i++) {
                var chef = EntitySpawnHelper.create(McRefs.VILLAGER, level);
                if (chef != null) {
                    chef.setCustomName(Component.literal(QuestNames.CHEF_ITALIEN));
                    chef.setPos(Vec3.atBottomCenterOf(origin).add(i % 5, 0, i / 5));
                    level.addFreshEntity(chef);
                }
            }
            player.sendSystemMessage(Component.literal("§cLe débat enflammé — chefs à la baguette !"));
        }
    }

    private static void speedrunner(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.teleportTo(player.getX() + 64, player.getY(), player.getZ() + 64);
            player.sendSystemMessage(Component.literal("§eSkip de donjon — téléportation vers le trésor (approx.)."));
        }
    }

    private static void piraterie(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            BlockPos.betweenClosedStream(origin.offset(-8, -2, -8), origin.offset(8, 3, 8))
                    .forEach(pos -> {
                        var state = level.getBlockState(pos);
                        if (state.is(Blocks.CHEST) || state.is(Blocks.FURNACE) || state.is(Blocks.BARREL)) {
                            level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState());
                        }
                    });
            player.sendSystemMessage(Component.literal("§4Ransomware — coffres & fours verrouillés (zone)."));
        }
    }

    private static void asmr(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 25, 0));
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.setBlockAndUpdate(player.blockPosition().offset(dx, 2, dz), Blocks.GLASS.defaultBlockState());
                }
            }
            player.sendSystemMessage(Component.literal("§fBulle de savon géante — lévitation douce."));
        }
    }

    private static void vampire(ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 90, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 90, 2));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 90, 1));
            player.sendSystemMessage(Component.literal("§5Pacte des ténèbres — forme d'ombre."));
        } else if (tier == LuckTier.NEUTRAL && player.level() instanceof ServerLevel level) {
            var dracula = EntitySpawnHelper.create(McRefs.ZOMBIE, level);
            if (dracula != null) {
                dracula.setCustomName(Component.literal(QuestNames.COMTE_DRACULA));
                dracula.setCustomNameVisible(true);
                dracula.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                dracula.setPos(player.position().add(2, 0, 0));
                level.addFreshEntity(dracula);
                player.sendSystemMessage(Component.literal("§5Le Comte Dracula exige un duel sans armure métallique."));
            }
        }
    }

    private static void multivers(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.LOW) {
            PlayerQuestData.startMultiversTrack(player, 20L * 120);
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(origin).inflate(16))
                    .forEach(e -> e.setDeltaMovement(e.getDeltaMovement().add(0, 1.2, 0)));
            for (int y = origin.getY(); y < origin.getY() + 20; y++) {
                level.setBlockAndUpdate(new BlockPos(origin.getX(), y, origin.getZ()), Blocks.AIR.defaultBlockState());
            }
            player.sendSystemMessage(Component.literal("§dInversion gravité — traversez 3 biomes en 2 min !"));
        } else if (tier == LuckTier.HIGH) {
            var target = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(origin).inflate(8))
                    .stream().filter(e -> e != player).findFirst();
            target.ifPresent(e -> e.teleportTo(e.getX(), e.getY() + 20, e.getZ()));
            player.sendSystemMessage(Component.literal("§bZapper : entité projetée ailleurs."));
        }
    }
}
