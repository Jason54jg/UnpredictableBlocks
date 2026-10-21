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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(TAG_THEME, theme.id());
        tag.putInt(TAG_LUCK, luck);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TAG_THEME)) {
            String id = tag.getString(TAG_THEME);
            for (LuckyTheme candidate : LuckyTheme.values()) {
                if (candidate.id().equals(id)) {
                    theme = candidate;
                    break;
                }
            }
        }
        luck = tag.getInt(TAG_LUCK);
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
