package com.jason54.unpredictableblocks.block;

import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckStat;
import com.jason54.unpredictableblocks.luck.PlayerLuckStorage;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.world.ChunkLuckAura;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Un bloc = un thème fixe ({@link LuckyTheme}) : loot, structure, quête et effets catalogue/legacy
 * de ce thème uniquement. Seul le tier HIGH/NEUTRAL/LOW est aléatoire (luck).
 */
public class LuckyBlock extends BaseEntityBlock {
    public static final MapCodec<LuckyBlock> CODEC = simpleCodec(
            properties -> new LuckyBlock(LuckyTheme.BOULANGERIE, properties)
    );
    private final LuckyTheme theme;

    public LuckyBlock(LuckyTheme theme, Properties properties) {
        super(properties);
        this.theme = theme;
    }

    public LuckyTheme theme() {
        return theme;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LuckyBlockEntity(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LuckyBlockEntity luckyBe) {
                LuckStat blockLuck = new LuckStat(luckyBe.getLuck());
                LuckStat playerLuck = PlayerLuckStorage.getModifier(serverPlayer);
                int aura = ChunkLuckAura.luckModifier(serverLevel, pos);
                blockLuck = blockLuck.add(aura);
                int bet = consumeLuckBet(serverPlayer);
                if (bet != 0) {
                    blockLuck = blockLuck.add(bet);
                    serverPlayer.sendSystemMessage(Component.literal("§ePari Luck : §b+" + bet + " §esur ce cassage !"));
                }
                LuckResolver.ResolvedOutcome outcome = LuckResolver.resolve(blockLuck, playerLuck, serverLevel.getRandom());
                LuckyTheme blockTheme = ((LuckyBlock) state.getBlock()).theme();
                LuckyBlockEventDispatcher.dispatch(serverLevel, serverPlayer, pos, blockTheme, outcome);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Sneak + poussière d'émeraude en main secondaire : +25 luck effectif (1 consommée). */
    private static int consumeLuckBet(ServerPlayer player) {
        if (!player.isShiftKeyDown()) {
            return 0;
        }
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!off.is(ModItems.LUCK_EMERALD_DUST.get())) {
            return 0;
        }
        if (!player.getAbilities().instabuild) {
            off.shrink(1);
        }
        return 25;
    }
}
