package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.block.LuckyBlockEventDispatcher;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/** Relance un événement manqué (malus) en version bénéfique. */
public class JetonVipItem extends LoreItem {
    public JetonVipItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        Optional<LuckyTheme> theme = PlayerQuestData.consumeMissedReroll(serverPlayer);
        LuckyTheme chosen = theme.orElseGet(() -> LuckyTheme.values()[level.random.nextInt(LuckyTheme.values().length)]);

        var forced = new LuckResolver.ResolvedOutcome(LuckTier.HIGH, "high");
        LuckyBlockEventDispatcher.dispatch(serverLevel, serverPlayer, serverPlayer.blockPosition(), chosen, forced);

        serverPlayer.sendSystemMessage(Component.literal("§eRelance VIP : " + chosen.id() + " (HIGH)"));

        if (!serverPlayer.getAbilities().instabuild) {
            serverPlayer.getItemInHand(hand).shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
