package com.jason54.unpredictableblocks.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.jason54.unpredictableblocks.registry.ModBlockEntities;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.theme.LuckyVariant;
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

public class LuckyBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final String TAG_THEME = "Theme";
    private static final String TAG_LUCK = "Luck";
    private static final String TAG_VARIANT = "Variant";

    /** Bloc posé statique : l'envol se joue au cassage via {@code LuckyRocketEntity}. */
    private static final RawAnimation IDLE_ANIM =
            RawAnimation.begin().thenLoop("animation.lucky_block.idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private LuckyTheme theme;
    private int luck;
    private LuckyVariant variant = LuckyVariant.NORMAL;

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

    public LuckyVariant getVariant() {
        return variant;
    }

    /** Variante visible côté client : renvoie la mise à jour du bloc. */
    public void setVariant(LuckyVariant variant) {
        this.variant = variant;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("idle", state -> state.setAndContinue(IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putString(TAG_THEME, theme.id());
        output.putInt(TAG_LUCK, luck);
        output.putInt(TAG_VARIANT, variant.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        luck = input.getIntOr(TAG_LUCK, 0);
        variant = LuckyVariant.byOrdinal(input.getIntOr(TAG_VARIANT, 0));
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
