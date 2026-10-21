package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.block.LuckyBlockEventDispatcher;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckStat;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Relance un événement manqué (malus) ou pose un Lucky Block Chance +100. */
public class JetonVipItem extends LoreItem {
    public JetonVipItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (!ItemCooldowns.tryConsume(player, ItemCooldowns.JETON, ItemCooldowns.JETON_TICKS)) {
            return InteractionResult.FAIL;
        }

        LuckyTheme theme = PlayerQuestData.consumeMissedReroll(player)
                .orElse(LuckyTheme.CASINO);

        BlockState state = ModBlocks.get(theme).defaultBlockState();
        level.setBlock(pos, state, 3);
        if (level.getBlockEntity(pos) instanceof LuckyBlockEntity luckyBlockEntity) {
            luckyBlockEntity.setLuck(LuckStat.MAX);
        }

        player.sendSystemMessage(Component.literal("§eJeton VIP : Lucky Block " + theme.id() + " (Luck +100)"));

        consumeOne(context, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }
        if (!ItemCooldowns.tryConsume(serverPlayer, ItemCooldowns.JETON, ItemCooldowns.JETON_TICKS)) {
            return InteractionResult.FAIL;
        }

        Optional<LuckyTheme> theme = PlayerQuestData.consumeMissedReroll(serverPlayer);
        LuckyTheme chosen = theme.orElseGet(() -> LuckyTheme.values()[level.random.nextInt(LuckyTheme.values().length)]);

        var forced = new LuckResolver.ResolvedOutcome(LuckTier.HIGH, "high");
        LuckyBlockEventDispatcher.dispatch(serverLevel, serverPlayer, serverPlayer.blockPosition(), chosen, forced);

        if (theme.isPresent()) {
            serverPlayer.sendSystemMessage(Component.literal("§eRelance VIP (malus) : " + chosen.id() + " (HIGH)"));
        } else {
            serverPlayer.sendSystemMessage(Component.literal(
                    "§eJeton VIP : aucun malus en attente — " + chosen.id() + " (HIGH aléatoire)"
            ));
        }

        if (!serverPlayer.getAbilities().instabuild) {
            serverPlayer.getItemInHand(hand).shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static void consumeOne(UseOnContext context, ServerPlayer player) {
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
    }
}
