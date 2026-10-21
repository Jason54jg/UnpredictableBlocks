package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.util.ServerLevels;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Quêtes runtime (thèmes 1–32) et helpers associés. */
public final class QuestExtendedHandler {
    private static final long SPEEDRUN_LIMIT_TICKS = 20L * 60 * 3;
    private QuestExtendedHandler() {}

    public static void register() {
        BlockEvent.BREAK.register((level, pos, state, player) -> {
            if (level.isClientSide()) {
                return EventResult.pass();
            }
            if (state.is(Blocks.HONEYCOMB_BLOCK) || state.is(Blocks.WHITE_WOOL)) {
                int n = PlayerQuestData.incrementCounter(player, "bubble_pop");
                if (n >= 50) {
                    QuestRewards.complete(player, "quest/asmr_fidget", "bubble_pop_50");
                }
            }
            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register(QuestExtendedHandler::onLivingDeath);

        EntityEvent.MOUNT.register((entity, vehicle, isMounting) -> {
            if (!isMounting || entity.level().isClientSide()) {
                return EventResult.pass();
            }
            if (entity instanceof ServerPlayer sp && vehicle.getType() == EntityType.PIG) {
                QuestRewards.complete(sp, "quest/fast_food", "ride");
            }
            return EventResult.pass();
        });

        InteractionEvent.USE_ITEM_ON_BLOCK.register((level, player, hand, stack, state, hit) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return EventResult.pass();
            }
            if (!state.is(Blocks.JUKEBOX) || !stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
                return EventResult.pass();
            }
            QuestRewards.complete(sp, "quest/disco_80", "music");
            return EventResult.pass();
        });

        PlayerEvent.OPEN_MENU.register((player, menu) -> {
            if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
                return;
            }
            if (!PlayerQuestData.plomberieWindowActive(sp)) {
                return;
            }
            if (menu instanceof ChestMenu) {
                QuestRewards.complete(sp, "quest/plomberie", "chest");
                PlayerQuestData.clearPlomberieWindow(sp);
            }
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, from, to) -> {
            if (to == Level.END) {
                QuestRewards.complete(player, "quest/lag_bug", "end");
                PlayerQuestData.speedrunStartTick(player).ifPresent(start -> {
                    long elapsed = ServerLevels.of(player).getGameTime() - start;
                    if (elapsed <= SPEEDRUN_LIMIT_TICKS) {
                        QuestRewards.complete(player, "quest/speedrunner", "end_speedrun");
                    }
                    PlayerQuestData.clearSpeedrun(player);
                });
            }
        });

        TickEvent.PLAYER_POST.register(player -> {
            if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
                return;
            }
            QuestLegacyHandler.tickEffects(sp);
            if (!PlayerQuestData.multiversTracking(sp)) {
                return;
            }
            if (sp.tickCount % 15 != 0) {
                return;
            }
            var biomeKey = sp.level().getBiome(sp.blockPosition()).unwrapKey();
            if (biomeKey.isEmpty()) {
                return;
            }
            String id = biomeKey.get().identifier().toString();
            int count = PlayerQuestData.recordMultiversBiome(sp, id);
            if (count >= 3) {
                QuestRewards.complete(sp, "quest/multivers_sliders", "multivers_biomes");
            }
        });
    }

    private static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {
        if (entity.level().isClientSide()) {
            return EventResult.pass();
        }
        if (source.getEntity() instanceof ServerPlayer killer) {
            QuestLegacyHandler.onEntityKilled(killer, entity);
        }
        if (!(source.getEntity() instanceof ServerPlayer killer)) {
            return EventResult.pass();
        }
        var name = entity.getCustomName();
        if (name == null) {
            return EventResult.pass();
        }
        String label = name.getString();
        if (QuestNames.CHEF_ITALIEN.equals(label)) {
            if (!PlayerQuestData.isAnanasQuestFailed(killer)) {
                int n = PlayerQuestData.incrementCounter(killer, "chef_kill");
                if (n >= 10) {
                    QuestRewards.complete(killer, "quest/ananas_pizza", "survive_chefs");
                }
            }
            return EventResult.pass();
        }
        if (QuestNames.COMTE_DRACULA.equals(label) && !wearingMetalArmor(killer)) {
            QuestRewards.complete(killer, "quest/vampire_gothique", "dracula_no_metal");
        }
        return EventResult.pass();
    }

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

    /** Appelé à la consommation de la Tranche du Débat. */
    public static void onAnanasEaten(ServerPlayer player) {
        PlayerQuestData.setAnanasQuestFailed(player, true);
    }

    /** Début de quête ananas (chefs invoqués). */
    public static void onChefsSpawned(ServerPlayer player) {
        PlayerQuestData.resetAnanasQuest(player);
    }
}
