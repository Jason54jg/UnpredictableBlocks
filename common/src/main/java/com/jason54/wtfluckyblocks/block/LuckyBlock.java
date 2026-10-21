package com.jason54.wtfluckyblocks.block;

import com.jason54.wtfluckyblocks.block.entity.LuckyBlockEntity;
import com.jason54.wtfluckyblocks.luck.LuckResolver;
import com.jason54.wtfluckyblocks.luck.LuckStat;
import com.jason54.wtfluckyblocks.luck.PlayerLuckStorage;
import com.jason54.wtfluckyblocks.theme.LuckyTheme;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LuckyBlock extends BaseEntityBlock {
    public static final MapCodec<LuckyBlock> CODEC = simpleCodec(LuckyBlock::new);
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
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LuckyBlockEntity luckyBe) {
                LuckStat blockLuck = new LuckStat(luckyBe.getLuck());
                LuckStat playerLuck = PlayerLuckStorage.getModifier(serverPlayer);
                LuckResolver.ResolvedOutcome outcome = LuckResolver.resolve(blockLuck, playerLuck, serverLevel.getRandom());
                LuckyBlockEventDispatcher.dispatch(serverLevel, serverPlayer, pos, theme, outcome);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
