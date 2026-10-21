package com.jason54.unpredictableblocks.quest;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

/** Quêtes runtime thèmes 1–24 (compteurs & effets catalogue). */
public final class QuestLegacyHandler {
    private QuestLegacyHandler() {}

    public static void onEntityKilled(ServerPlayer killer, LivingEntity victim) {
        EntityType<?> type = victim.getType();
        if (type == EntityType.ZOMBIE) {
            int proprete = PlayerQuestData.incrementCounter(killer, "kill_zombie_proprete");
            if (proprete >= 5) {
                QuestRewards.complete(killer, "quest/proprete", "kills");
            }
            int streamer = PlayerQuestData.incrementCounter(killer, "kill_zombie_streamer");
            if (streamer >= 15) {
                QuestRewards.complete(killer, "quest/streamer", "streamer_zombies");
            }
        }
        if (type == EntityType.CHICKEN) {
            int n = PlayerQuestData.incrementCounter(killer, "kill_chicken_boulangerie");
            if (n >= 10) {
                QuestRewards.complete(killer, "quest/boulangerie", "kill_poultry");
            }
        }
        if (type == EntityType.RAVAGER) {
            QuestRewards.complete(killer, "quest/anachronisme", "kill_ravager");
        }
        if (type == EntityType.IRON_GOLEM) {
            QuestRewards.complete(killer, "quest/volaille", "kill_golem");
        }
        if (type == EntityType.POLAR_BEAR) {
            QuestRewards.complete(killer, "quest/camping", "bear");
        }
        if (type == EntityType.WOLF) {
            QuestRewards.complete(killer, "quest/magie_comptoir", "wolf");
        }
        if (type == EntityType.ENDERMAN) {
            QuestRewards.complete(killer, "quest/jdr_d20", "mimic");
        }
        var name = victim.getCustomName();
        if (name != null && "Toi du Futur".equals(name.getString())) {
            QuestRewards.complete(killer, "quest/anachronisme", "kill_ravager");
        }
    }

    public static void tickEffects(ServerPlayer player) {
        if (player.tickCount % 10 != 0) {
            return;
        }
        if (player.hasEffect(MobEffects.BLINDNESS) && player.hasEffect(MobEffects.NAUSEA)) {
            QuestRewards.complete(player, "quest/bsod", "glitched");
        }
        if (player.hasEffect(MobEffects.SLOW_FALLING)) {
            QuestRewards.complete(player, "quest/film_action", "slow_fall");
        }
        if (player.hasEffect(MobEffects.LEVITATION)) {
            QuestRewards.complete(player, "quest/gravite_zero", "levitate");
        }
        if (player.hasEffect(MobEffects.STRENGTH)) {
            PlayerQuestData.markGymStrength(player, 20L * 30);
        }
        var slowness = player.getEffect(MobEffects.SLOWNESS);
        if (slowness != null
                && slowness.getAmplifier() >= 2
                && PlayerQuestData.hadGymStrengthRecently(player)) {
            QuestRewards.complete(player, "quest/gym_bro", "buff");
        }
        int paper = countItem(player, Items.PAPER);
        if (paper >= 64) {
            QuestRewards.complete(player, "quest/penurie_2020", "tp");
        }
        if (player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.GOLDEN_HELMET)) {
            QuestRewards.complete(player, "quest/complot", "hat");
        }
        if (countItem(player, Items.PAINTING) >= 1) {
            QuestRewards.complete(player, "quest/art_moderne", "art");
        }
    }

    private static int countItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
