package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.block.LuckyBlockEventDispatcher;
import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckStat;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) || !(context.getPlayer() instanceof ServerPlayer player)) {
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

        player.sendSystemMessage(ModMessages.msg("vip_block_place", theme.id()));

        consumeOne(context, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity entity,
            InteractionHand hand
    ) {
        if (player.level().isClientSide()
                || !(player instanceof ServerPlayer from)
                || !(entity instanceof ServerPlayer to)
                || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (from.getUUID().equals(to.getUUID())) {
            return InteractionResult.PASS;
        }
        if (!PlayerQuestData.hasMissedReroll(from)) {
            from.sendSystemMessage(ModMessages.msg("vip_no_pending"));
            return InteractionResult.FAIL;
        }
        if (!ItemCooldowns.tryConsume(from, ItemCooldowns.JETON, ItemCooldowns.JETON_TICKS)) {
            return InteractionResult.FAIL;
        }
        if (PlayerQuestData.transferMissedReroll(from, to)) {
            from.sendSystemMessage(ModMessages.msg("vip_transfer_from", to.getGameProfile().name()));
            to.sendSystemMessage(ModMessages.msg("vip_transfer_to", from.getGameProfile().name()));
            if (!from.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }
        if (!ItemCooldowns.tryConsume(serverPlayer, ItemCooldowns.JETON, ItemCooldowns.JETON_TICKS)) {
            return InteractionResult.FAIL;
        }

        Optional<LuckyTheme> theme = PlayerQuestData.consumeMissedReroll(serverPlayer);
        LuckyTheme chosen = theme.orElseGet(() -> LuckyTheme.values()[level.getRandom().nextInt(LuckyTheme.values().length)]);

        var forced = new LuckResolver.ResolvedOutcome(LuckTier.HIGH, "high");
        LuckyBlockEventDispatcher.dispatch(serverLevel, serverPlayer, serverPlayer.blockPosition(), chosen, forced);

        if (theme.isPresent()) {
            serverPlayer.sendSystemMessage(ModMessages.msg("vip_reroll", chosen.id()));
        } else {
            serverPlayer.sendSystemMessage(ModMessages.msg("vip_random_high", chosen.id()));
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
