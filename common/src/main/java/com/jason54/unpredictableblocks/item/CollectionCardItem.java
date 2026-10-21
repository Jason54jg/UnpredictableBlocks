package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.util.ModMessages;
import com.jason54.unpredictableblocks.lucky.PlayerLuckyBreakStats;
import com.jason54.unpredictableblocks.quest.QuestRewards;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** Carte à collectionner ; tag NBT {@code Theme} = id thème. */
public class CollectionCardItem extends Item {
    private static final String TAG_THEME = "Theme";

    public CollectionCardItem(Properties properties) {
        super(properties);
    }

    public static ItemStack forTheme(LuckyTheme theme) {
        ItemStack stack = new ItemStack(com.jason54.unpredictableblocks.registry.ModItems.CARTE_COLLECTION.get());
        applyTheme(stack, theme);
        return stack;
    }

    public static void applyTheme(ItemStack stack, LuckyTheme theme) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_THEME, theme.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Carte — " + theme.id()));
    }

    public static LuckyTheme readTheme(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        String id = data.copyTag().getStringOr(TAG_THEME, "");
        for (LuckyTheme theme : LuckyTheme.values()) {
            if (theme.id().equals(id)) {
                return theme;
            }
        }
        return null;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) {
            return InteractionResult.CONSUME;
        }
        ItemStack stack = player.getItemInHand(hand);
        LuckyTheme theme = readTheme(stack);
        if (theme == null) {
            sp.sendSystemMessage(ModMessages.msg("card_empty"));
            return InteractionResult.SUCCESS;
        }
        var spawn = ModConfig.themeSpawn(theme);
        String biomes = String.join(", ", spawn.preferredBiomes());
        boolean quest = QuestRewards.isDone(sp, "quest/" + theme.id());
        boolean master = QuestRewards.isDone(sp, "theme_master/" + theme.id());
        sp.sendSystemMessage(ModMessages.msg("card_title", theme.id()));
        sp.sendSystemMessage(ModMessages.msg(
                "card_stats",
                PlayerLuckyBreakStats.breaks(sp, theme),
                PlayerLuckyBreakStats.highs(sp, theme),
                PlayerLuckyBreakStats.lows(sp, theme)
        ));
        sp.sendSystemMessage(ModMessages.msg(
                quest ? "card_quest_done" : "card_quest_pending"
        ));
        sp.sendSystemMessage(ModMessages.msg(
                master ? "card_master_done" : "card_master_pending"
        ));
        sp.sendSystemMessage(ModMessages.msg("card_biomes", biomes));
        return InteractionResult.SUCCESS;
    }
}
