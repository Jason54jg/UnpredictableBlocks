package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.compat.McRefs;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.registry.ModSounds;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ServerLevels;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Tous les déclencheurs de quêtes de thème, regroupés par événement. Les seuils vivent dans {@link Quests}.
 * Les déclencheurs liés à un objet (espresso, valise, disque dur) et au chat (fortune cookie) restent
 * dans leur objet / {@link FortuneCookieHandler} mais passent aussi par {@link Quests}.
 */
public final class QuestTriggers {
    private static final long SPEEDRUN_LIMIT_TICKS = 20L * 60 * 3;
    private static final int TICK_CHECK_INTERVAL = 10;

    private QuestTriggers() {}

    public static void register() {
        PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, hit) -> {
            if (player instanceof ServerPlayer sp && !level.isClientSide()) {
                onAttack(sp, target);
            }
            return EventResult.pass();
        });
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!entity.level().isClientSide() && source.getEntity() instanceof ServerPlayer killer) {
                onKill(killer, entity);
            }
            return EventResult.pass();
        });
        EntityEvent.MOUNT.register((entity, vehicle, isMounting) -> {
            if (isMounting && entity instanceof ServerPlayer sp) {
                if (vehicle.getType() == McRefs.PIG) {
                    Quests.complete(sp, LuckyTheme.FAST_FOOD, "ride");
                } else if (vehicle instanceof AbstractMinecart) {
                    Quests.complete(sp, LuckyTheme.GREVE_TRANSPORTS, "minecart");
                }
            }
            return EventResult.pass();
        });
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer sp && source.is(DamageTypes.LIGHTNING_BOLT)) {
                Quests.complete(sp, LuckyTheme.METEO_FOLLE, "struck");
            }
            return EventResult.pass();
        });
        EntityEvent.ANIMAL_TAME.register((animal, player) -> {
            if (player instanceof ServerPlayer sp && animal instanceof Cat) {
                Quests.increment(sp, LuckyTheme.CHAT_INTERNET, "tame_cats");
            }
            return EventResult.pass();
        });
        PlayerEvent.OPEN_MENU.register((player, menu) -> {
            if (player instanceof ServerPlayer sp && menu instanceof ChestMenu && PlayerQuestData.plomberieWindowActive(sp)) {
                Quests.complete(sp, LuckyTheme.PLOMBERIE, "chest");
                PlayerQuestData.clearPlomberieWindow(sp);
            }
        });
        PlayerEvent.CHANGE_DIMENSION.register((player, from, to) -> {
            if (to != Level.END) {
                return;
            }
            Quests.complete(player, LuckyTheme.LAG_BUG, "end");
            PlayerQuestData.speedrunStartTick(player).ifPresent(start -> {
                if (ServerLevels.of(player).getGameTime() - start <= SPEEDRUN_LIMIT_TICKS) {
                    Quests.complete(player, LuckyTheme.SPEEDRUNNER, "end_speedrun");
                }
                PlayerQuestData.clearSpeedrun(player);
            });
        });
        TickEvent.PLAYER_POST.register(player -> {
            if (!player.level().isClientSide() && player instanceof ServerPlayer sp && sp.tickCount % TICK_CHECK_INTERVAL == 0) {
                onPlayerTick(sp);
            }
        });
    }

    // ------------------------------------------------------------------ combat

    private static void onAttack(ServerPlayer player, Entity target) {
        if (!player.getMainHandItem().is(ModItems.MARTEAU_BONK.get())) {
            return;
        }
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.BONK.get(),
                SoundSource.PLAYERS, 1.0f, 0.9f + player.getRandom().nextFloat() * 0.2f);
        if (PlayerQuestData.incrementBonk(player) >= Quests.steps(LuckyTheme.INVASION_MEMES).getFirst().target()) {
            Quests.complete(player, LuckyTheme.INVASION_MEMES, "completed");
        }
    }

    private static void onKill(ServerPlayer killer, LivingEntity victim) {
        EntityType<?> type = victim.getType();
        if (type == McRefs.ZOMBIE) {
            Quests.increment(killer, LuckyTheme.PROPRETE, "kills");
            Quests.increment(killer, LuckyTheme.STREAMER, "streamer_zombies");
        } else if (type == McRefs.CHICKEN) {
            Quests.increment(killer, LuckyTheme.BOULANGERIE, "kill_poultry");
        } else if (type == McRefs.RAVAGER) {
            Quests.complete(killer, LuckyTheme.ANACHRONISME, "kill_ravager");
        } else if (type == McRefs.IRON_GOLEM) {
            Quests.complete(killer, LuckyTheme.VOLAILLE, "kill_golem");
        } else if (type == McRefs.POLAR_BEAR) {
            Quests.complete(killer, LuckyTheme.CAMPING, "bear");
        } else if (type == McRefs.WOLF) {
            Quests.complete(killer, LuckyTheme.MAGIE_COMPTOIR, "wolf");
        } else if (type == McRefs.ENDERMAN) {
            Quests.complete(killer, LuckyTheme.JDR_D20, "mimic");
        }

        var name = victim.getCustomName();
        if (name == null) {
            return;
        }
        String label = name.getString();
        if ("Toi du Futur".equals(label)) {
            Quests.complete(killer, LuckyTheme.ANACHRONISME, "kill_ravager");
        } else if (QuestNames.CHEF_ITALIEN.equals(label) && !PlayerQuestData.isAnanasQuestFailed(killer)) {
            Quests.increment(killer, LuckyTheme.ANANAS_PIZZA, "survive_chefs");
        } else if (QuestNames.COMTE_DRACULA.equals(label) && !wearingMetalArmor(killer)) {
            Quests.complete(killer, LuckyTheme.VAMPIRE_GOTHIQUE, "dracula_no_metal");
        }
    }

    // ------------------------------------------------------------------ état du joueur (toutes les 0.5 s)

    private static void onPlayerTick(ServerPlayer player) {
        if (player.hasEffect(MobEffects.BLINDNESS) && player.hasEffect(MobEffects.NAUSEA)) {
            Quests.complete(player, LuckyTheme.BSOD, "glitched");
        }
        if (player.hasEffect(MobEffects.SLOW_FALLING)) {
            Quests.complete(player, LuckyTheme.FILM_ACTION, "slow_fall");
        }
        if (player.hasEffect(MobEffects.LEVITATION)) {
            Quests.complete(player, LuckyTheme.GRAVITE_ZERO, "levitate");
        }
        if (player.hasEffect(MobEffects.STRENGTH)) {
            PlayerQuestData.markGymStrength(player, 20L * 30);
        }
        var slowness = player.getEffect(MobEffects.SLOWNESS);
        if (slowness != null && slowness.getAmplifier() >= 2 && PlayerQuestData.hadGymStrengthRecently(player)) {
            Quests.complete(player, LuckyTheme.GYM_BRO, "buff");
        }
        if (player.isSleeping()) {
            Quests.complete(player, LuckyTheme.CAUCHEMAR_MATIN, "sleep");
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Items.GOLDEN_HELMET)) {
            Quests.complete(player, LuckyTheme.COMPLOT, "hat");
        }
        completeIfReached(player, LuckyTheme.PENURIE_2020, "tp");
        completeIfReached(player, LuckyTheme.ART_MODERNE, "art");
        completeIfReached(player, LuckyTheme.BOULANGERIE, "have_bread");
        completeIfReached(player, LuckyTheme.CRYPTO_BULLE, "hodl");
        completeIfReached(player, LuckyTheme.DISCO_80, "music");
        completeIfReached(player, LuckyTheme.ASMR_FIDGET, "bubble_pop_50");
        trackImpotsEmeralds(player);
        trackMultiversBiomes(player);
    }

    /** Étapes à progression live (inventaire, statistiques) : valide quand la cible du registre est atteinte. */
    private static void completeIfReached(ServerPlayer player, LuckyTheme theme, String criterion) {
        for (Quests.Step step : Quests.steps(theme)) {
            if (step.criterion().equals(criterion) && step.current().applyAsInt(player) >= step.target()) {
                Quests.complete(player, theme, criterion);
            }
        }
    }

    private static void trackImpotsEmeralds(ServerPlayer player) {
        int emeralds = countItem(player, Items.EMERALD);
        int prev = PlayerQuestData.emeraldSnapshot(player);
        if (prev < 0) {
            PlayerQuestData.setEmeraldSnapshot(player, emeralds);
            return;
        }
        if (emeralds < prev && nearVillager(player)) {
            Quests.increment(player, LuckyTheme.IMPOTS, "pay", prev - emeralds);
        }
        if (emeralds != prev) {
            PlayerQuestData.setEmeraldSnapshot(player, emeralds);
        }
    }

    private static void trackMultiversBiomes(ServerPlayer player) {
        if (!PlayerQuestData.multiversTracking(player)) {
            return;
        }
        player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> {
            PlayerQuestData.recordMultiversBiome(player, key.identifier().toString());
            if (PlayerQuestData.multiversBiomeCount(player) >= Quests.steps(LuckyTheme.MULTIVERS_SLIDERS).getFirst().target()) {
                Quests.complete(player, LuckyTheme.MULTIVERS_SLIDERS, "multivers_biomes");
            }
        });
    }

    // ------------------------------------------------------------------ hooks appelés par les effets / objets

    /** Consommation de la Tranche du Débat : la quête ananas est ratée. */
    public static void onAnanasEaten(ServerPlayer player) {
        PlayerQuestData.setAnanasQuestFailed(player, true);
    }

    /** Chefs italiens invoqués : la quête ananas (re)commence. */
    public static void onChefsSpawned(ServerPlayer player) {
        PlayerQuestData.resetAnanasQuest(player);
    }

    // ------------------------------------------------------------------ utilitaires

    static boolean wearingMetalArmor(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();
            if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE
                    || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS
                    || item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE
                    || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS
                    || item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE
                    || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS
                    || item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE
                    || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS
                    || item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE
                    || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearVillager(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(8.0);
        return !player.level().getEntitiesOfClass(Villager.class, box).isEmpty();
    }

    /**
     * Disques joués et blocs « papier bulle » cassés : lus dans les statistiques vanilla du joueur.
     * Les événements Architectury équivalents (USE_ITEM_ON_BLOCK, BlockEvent.BREAK) changent de signature
     * entre la 20.x (MC 26.1.2) et la 21/22.x : les stats sont identiques sur toutes les versions.
     */
    static int recordsPlayed(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_RECORD));
    }

    static int bubblePops(ServerPlayer player) {
        return player.getStats().getValue(Stats.BLOCK_MINED.get(McRefs.WHITE_WOOL))
                + player.getStats().getValue(Stats.BLOCK_MINED.get(Blocks.HONEYCOMB_BLOCK));
    }

    static int countItem(ServerPlayer player, Item item) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
