package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.compat.McRefs;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.quest.QuestTriggers;
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

/** Thèmes 25–36 (directive §3 + extension 33–36). */
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
            case GREVE_TRANSPORTS -> greve(level, player, origin, tier);
            case METEO_FOLLE -> meteo(level, player, origin, tier);
            case CHAT_INTERNET -> chats(level, player, origin, tier);
            case CRYPTO_BULLE -> crypto(level, player, tier);
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
            QuestTriggers.onChefsSpawned(player);
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

    private static void greve(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.getInventory().add(new ItemStack(Items.MINECART));
            player.getInventory().add(new ItemStack(Items.RAIL, 32));
            player.getInventory().add(new ItemStack(Items.POWERED_RAIL, 6));
            player.sendSystemMessage(Component.literal("§aService minimum assuré : voici de quoi rouler toi-même."));
        } else if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 4; i++) {
                var villager = EntitySpawnHelper.create(McRefs.VILLAGER, level);
                if (villager != null) {
                    villager.setPos(Vec3.atBottomCenterOf(origin).add(level.getRandom().nextDouble() * 4 - 2, 0, level.getRandom().nextDouble() * 4 - 2));
                    villager.setCustomName(Component.literal("Manifestant"));
                    level.addFreshEntity(villager);
                }
            }
            player.sendSystemMessage(Component.literal("§eManifestation spontanée devant le Lucky Block !"));
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 60, 2));
            player.sendSystemMessage(Component.literal("§cGrève générale : tu rentres à pied (et lentement)."));
        }
    }

    private static void meteo(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            level.getServer().setWeatherParameters(20 * 600, 0, false, false);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 1));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    origin.getX() + 0.5, origin.getY() + 2, origin.getZ() + 0.5, 40, 3, 1, 3, 0);
            player.sendSystemMessage(Component.literal("§bGrand soleil pour 10 minutes !"));
        } else if (tier == LuckTier.NEUTRAL) {
            level.getServer().setWeatherParameters(0, 20 * 120, true, true);
            player.sendSystemMessage(Component.literal("§7Orage soudain — rentre les poules."));
        } else {
            BlockPos strike = origin.offset(level.getRandom().nextInt(7) - 3, 0, level.getRandom().nextInt(7) - 3);
            var bolt = EntitySpawnHelper.create(McRefs.LIGHTNING_BOLT, level);
            if (bolt != null) {
                bolt.setPos(Vec3.atBottomCenterOf(strike));
                level.addFreshEntity(bolt);
            }
            player.sendSystemMessage(Component.literal("§eLa foudre tombe tout près…"));
        }
    }

    private static void chats(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            var cat = EntitySpawnHelper.create(McRefs.CAT, level);
            if (cat != null) {
                cat.setPos(Vec3.atBottomCenterOf(origin));
                cat.tame(player);
                level.addFreshEntity(cat);
            }
            player.getInventory().add(new ItemStack(Items.COD, 8));
            player.sendSystemMessage(Component.literal("§aUn chat t'a adopté. Il est à toi (ou l'inverse)."));
        } else if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 4; i++) {
                var cat = EntitySpawnHelper.create(McRefs.CAT, level);
                if (cat != null) {
                    cat.setPos(Vec3.atBottomCenterOf(origin).add(level.getRandom().nextDouble() * 4 - 2, 0, level.getRandom().nextDouble() * 4 - 2));
                    level.addFreshEntity(cat);
                }
            }
            player.getInventory().add(new ItemStack(Items.STRING, 6));
            player.sendSystemMessage(Component.literal("§eLes chats d'Internet débarquent. Apprivoise-les avec du poisson."));
        } else {
            com.jason54.unpredictableblocks.compat.McAccess.dropMainHand(player);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 15, 1));
            player.sendSystemMessage(Component.literal("§cLe chat a fait tomber ton objet de la table. Il ne regrette rien."));
        }
    }

    private static void crypto(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.getInventory().add(new ItemStack(Items.EMERALD, 16 + level.getRandom().nextInt(17)));
            player.sendSystemMessage(Component.literal("§a+1000 % ! Ton portefeuille déborde d'émeraudes."));
        } else if (tier == LuckTier.NEUTRAL) {
            int emeralds = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.EMERALD)) {
                    emeralds += stack.getCount();
                }
            }
            player.getInventory().add(new ItemStack(Items.EMERALD, Math.max(1, emeralds / 4)));
            player.sendSystemMessage(Component.literal("§eMarché stable… pour une fois. +25 % sur tes émeraudes."));
        } else {
            int lost = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.EMERALD) || stack.is(Items.GOLD_INGOT)) {
                    int half = stack.getCount() / 2;
                    stack.shrink(half);
                    lost += half;
                }
            }
            player.sendSystemMessage(Component.literal("§cKrach ! La moitié de tes émeraudes et lingots d'or s'évapore (" + lost + ")."));
        }
    }
}
