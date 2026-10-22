package com.jason54.unpredictableblocks.compat;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Reflection bridges for Mojmap renames between 26.1.2 / 26.2 / 26.3. */
public final class McAccess {
    private McAccess() {}

    public static StructureTemplateManager structureTemplates(ServerLevel level) {
        try {
            Method m = ServerLevel.class.getMethod("getStructureTemplateManager");
            return (StructureTemplateManager) m.invoke(level);
        } catch (ReflectiveOperationException ignored) {
            try {
                Method m = ServerLevel.class.getMethod("getStructureManager");
                return (StructureTemplateManager) m.invoke(level);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No structure template manager API", e);
            }
        }
    }

    public static boolean updateCustomBlockEntityTag(
            BlockItem item, BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
        try {
            // 26.1 / 26.2 instance method
            Method m = BlockItem.class.getDeclaredMethod(
                    "updateCustomBlockEntityTag",
                    BlockPos.class, Level.class, Player.class, ItemStack.class, BlockState.class);
            m.setAccessible(true);
            return (boolean) m.invoke(item, pos, level, player, stack, state);
        } catch (ReflectiveOperationException ignored) {
            try {
                // 26.3 static method
                Method m = BlockItem.class.getMethod(
                        "updateCustomBlockEntityTag",
                        Level.class, Player.class, BlockPos.class, ItemStack.class);
                return (boolean) m.invoke(null, level, player, pos, stack);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No updateCustomBlockEntityTag API", e);
            }
        }
    }

    public static void giveOrDrop(Player player, ItemStack stack) {
        if (player.getInventory().add(stack)) {
            return;
        }
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            var item = new net.minecraft.world.entity.item.ItemEntity(
                    serverLevel, player.getX(), player.getY() + 0.5, player.getZ(), stack);
            serverLevel.addFreshEntity(item);
        }
    }

    /**
     * Fait lâcher l'objet en main au joueur (éjecté devant lui, ramassable après 2 s) et vide la main.
     * {@code Player.drop(ItemStack, boolean)} change de signature en 26.3 : on crée l'ItemEntity nous-mêmes.
     */
    public static void dropMainHand(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var item = new net.minecraft.world.entity.item.ItemEntity(
                serverLevel, player.getX(), player.getEyeY() - 0.3, player.getZ(), held.copy());
        item.setDeltaMovement(player.getLookAngle().scale(0.3));
        item.setPickUpDelay(40);
        serverLevel.addFreshEntity(item);
        held.setCount(0);
    }

    /** 26.1 / 26.2: {@code blocksMotion()}; 26.3: renommé {@code isSolid()}. */
    public static boolean blocksMotion(BlockState state) {
        try {
            Method m = BlockState.class.getMethod("blocksMotion");
            return (boolean) m.invoke(state);
        } catch (ReflectiveOperationException ignored) {
            try {
                Method m = BlockState.class.getMethod("isSolid");
                return (boolean) m.invoke(state);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No blocksMotion/isSolid API", e);
            }
        }
    }
}