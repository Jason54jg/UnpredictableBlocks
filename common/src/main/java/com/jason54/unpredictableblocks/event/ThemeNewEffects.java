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
            case PAUSE_CAFE -> pauseCafe(level, player, origin, tier);
            case RETARD_VOL -> retardVol(level, player, origin, tier);
            case ANANAS_PIZZA -> ananasPizza(level, player, origin, tier);
            case SPEEDRUNNER -> speedrunner(level, player, origin, tier);
            case PIRATERIE_TORRENT -> piraterie(level, player, origin, tier);
            case ASMR_FIDGET -> asmr(level, player, origin, tier);
            case VAMPIRE_GOTHIQUE -> vampire(player, tier);
            case MULTIVERS_SLIDERS -> multivers(level, player, origin, tier);
            case GREVE_TRANSPORTS -> greve(level, player, origin, tier);
            case METEO_FOLLE -> meteo(level, player, origin, tier);
            case CHAT_INTERNET -> chats(level, player, origin, tier);
            case CRYPTO_BULLE -> crypto(level, player, origin, tier);
            default -> {}
        }
    }

    private static void pauseCafe(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 120, 4));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 120, 2));
            var barista = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.BARISTA.get(), level);
            if (barista != null) {
                barista.setPos(Vec3.atBottomCenterOf(origin));
                level.addFreshEntity(barista);
            }
            player.sendSystemMessage(Component.literal("§6Overdose caféinée — un Barista ouvre le comptoir (clic droit)."));
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
            var douanier = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.DOUANIER.get(), level);
            if (douanier != null) {
                douanier.setPos(Vec3.atBottomCenterOf(origin));
                level.addFreshEntity(douanier);
            }
            player.sendSystemMessage(Component.literal("§bVol annulé — contrôle douanier + périmètre verre."));
        }
    }

    private static void ananasPizza(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            QuestTriggers.onChefsSpawned(player);
            for (int i = 0; i < 4; i++) {
                var chef = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.CHEF_ITALIEN.get(), level);
                if (chef != null) {
                    chef.setPos(Vec3.atBottomCenterOf(origin).add(i % 2 * 3 - 1.5, 0, i / 2 * 3 - 1.5));
                    level.addFreshEntity(chef);
                }
            }
            player.sendSystemMessage(Component.literal("§cLe débat enflammé — chefs à la baguette !"));
        }
    }

    private static void speedrunner(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.teleportTo(player.getX() + 64, player.getY(), player.getZ() + 64);
            var bot = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.BOT_SPEEDRUNNER.get(), level);
            if (bot != null) {
                bot.setPos(player.position().add(2, 0, 2));
                level.addFreshEntity(bot);
            }
            player.sendSystemMessage(Component.literal("§eSkip de donjon — un Bot Speedrunner te talonne !"));
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
            var pirate = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.PIRATE_TORRENT.get(), level);
            if (pirate != null) {
                pirate.setPos(Vec3.atBottomCenterOf(origin));
                level.addFreshEntity(pirate);
            }
            player.sendSystemMessage(Component.literal("§4Ransomware + Pirate du Torrent en zone !"));
        }
    }

    private static void asmr(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 25, 0));
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.setBlockAndUpdate(player.blockPosition().offset(dx, 2, dz), Blocks.GLASS.defaultBlockState());
                }
            }
            var popit = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.POP_IT_GEANT.get(), level);
            if (popit != null) {
                popit.setPos(Vec3.atBottomCenterOf(origin));
                level.addFreshEntity(popit);
            }
            player.sendSystemMessage(Component.literal("§fBulle de savon + Pop-it Géant ASMR !"));
        }
    }

    private static void vampire(ServerPlayer player, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 90, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 90, 2));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 90, 1));
            player.sendSystemMessage(Component.literal("§5Pacte des ténèbres — forme d'ombre."));
        } else if (tier == LuckTier.NEUTRAL && player.level() instanceof ServerLevel level) {
            var dracula = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.COMTE_DRACULA.get(), level);
            if (dracula != null) {
                dracula.setPos(player.position().add(3, 0, 0));
                level.addFreshEntity(dracula);
                player.sendSystemMessage(Component.literal("§5Le Comte Dracula exige un duel sans armure métallique."));
            }
        }
    }

    private static void multivers(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        // Fenêtre quête sur tout tier (avant : LOW seulement → Temporal Link jamais progressait).
        PlayerQuestData.startMultiversTrack(player, 20L * 120);
        if (tier == LuckTier.LOW) {
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
            var slider = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.SLIDER_MULTIVERS.get(), level);
            if (slider != null) {
                slider.setPos(Vec3.atBottomCenterOf(origin).add(0, 1, 0));
                level.addFreshEntity(slider);
            }
            player.sendSystemMessage(Component.literal("§bZapper + Slider Multivers — traversez 3 biomes en 2 min !"));
        } else {
            player.sendSystemMessage(Component.literal("§dPortail multivers — traversez 3 biomes en 2 min !"));
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
                var sign = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.MANIFESTANT.get(), level);
                if (sign != null) {
                    sign.setPos(Vec3.atBottomCenterOf(origin).add(level.getRandom().nextDouble() * 4 - 2, 0.5, level.getRandom().nextDouble() * 4 - 2));
                    level.addFreshEntity(sign);
                }
            }
            player.sendSystemMessage(Component.literal("§eManifestation : pancartes vivantes partout !"));
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
            var cloud = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.NUAGE_ORAGE.get(), level);
            if (cloud != null) {
                cloud.setPos(Vec3.atBottomCenterOf(origin).add(0, 2, 0));
                level.addFreshEntity(cloud);
            }
            player.sendSystemMessage(Component.literal("§7Orage soudain — un Nuage Orageux arrive !"));
        } else {
            // Frapper le joueur (pas un offset aléatoire) pour valider la quête « Human Lightning Rod ».
            var bolt = EntitySpawnHelper.create(McRefs.LIGHTNING_BOLT, level);
            if (bolt != null) {
                bolt.setPos(player.position());
                level.addFreshEntity(bolt);
            }
            player.sendSystemMessage(Component.literal("§eLa foudre te frappe !"));
        }
    }

    private static void chats(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.HIGH) {
            var cat = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.CHAT_INTERNET.get(), level);
            if (cat != null) {
                cat.setPos(Vec3.atBottomCenterOf(origin));
                cat.tame(player);
                level.addFreshEntity(cat);
            }
            player.getInventory().add(new ItemStack(Items.COD, 8));
            player.sendSystemMessage(Component.literal("§aUn chat t'a adopté. Il est à toi (ou l'inverse)."));
        } else if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 4; i++) {
                var cat = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.CHAT_INTERNET.get(), level);
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

    private static void crypto(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
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
            var bubble = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.CRYPTO_BULLE.get(), level);
            if (bubble != null) {
                bubble.setPos(Vec3.atBottomCenterOf(origin).add(0, 1, 0));
                level.addFreshEntity(bubble);
            }
            player.sendSystemMessage(Component.literal("§eMarché stable… une Crypto Bulle flotte près de toi."));
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
