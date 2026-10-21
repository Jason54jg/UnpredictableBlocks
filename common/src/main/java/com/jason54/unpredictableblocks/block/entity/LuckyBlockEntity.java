package com.jason54.unpredictableblocks.block.entity;

import com.jason54.unpredictableblocks.registry.ModBlockEntities;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LuckyBlockEntity extends BlockEntity {
    private static final String TAG_THEME = "Theme";
    private static final String TAG_LUCK = "Luck";

    private LuckyTheme theme;
    private int luck;

    public LuckyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUCKY_BLOCK.get(), pos, state);
        if (state.getBlock() instanceof com.jason54.unpredictableblocks.block.LuckyBlock luckyBlock) {
            this.theme = luckyBlock.theme();
        } else {
            this.theme = LuckyTheme.BOULANGERIE;
        }
        this.luck = 0;
    }

    public LuckyTheme getTheme() {
        return theme;
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putString(TAG_THEME, theme.id());
        output.putInt(TAG_LUCK, luck);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        luck = input.getIntOr(TAG_LUCK, 0);
        // Le thème est imposé par le bloc posé (lucky_<id>), pas par un tirage aléatoire.
        if (getBlockState().getBlock() instanceof com.jason54.unpredictableblocks.block.LuckyBlock luckyBlock) {
            theme = luckyBlock.theme();
            return;
        }
        String id = input.getStringOr(TAG_THEME, "");
        if (!id.isEmpty()) {
            for (LuckyTheme candidate : LuckyTheme.values()) {
                if (candidate.id().equals(id)) {
                    theme = candidate;
                    break;
                }
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
