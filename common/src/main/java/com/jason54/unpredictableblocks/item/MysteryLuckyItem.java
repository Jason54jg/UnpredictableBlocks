package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.block.LuckyBlockEventDispatcher;
import com.jason54.unpredictableblocks.luck.LuckResolver;
import com.jason54.unpredictableblocks.luck.LuckStat;
import com.jason54.unpredictableblocks.luck.PlayerLuckStorage;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Clic droit : événement Lucky aléatoire parmi les 32 thèmes. */
public class MysteryLuckyItem extends LoreItem {
    public MysteryLuckyItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!ItemCooldowns.tryConsume(sp, "lucky_mystere", 20 * 30)) {
            return InteractionResult.FAIL;
        }
        LuckyTheme[] themes = LuckyTheme.values();
        LuckyTheme theme = themes[serverLevel.getRandom().nextInt(themes.length)];
        LuckStat playerLuck = PlayerLuckStorage.getModifier(sp);
        var outcome = LuckResolver.resolve(new LuckStat(0), playerLuck, serverLevel.getRandom());
        LuckyBlockEventDispatcher.dispatch(serverLevel, sp, sp.blockPosition(), theme, outcome);
        sp.sendSystemMessage(Component.literal("§dLucky mystère → §f" + theme.id()));
        if (!sp.getAbilities().instabuild) {
            sp.getItemInHand(hand).shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
