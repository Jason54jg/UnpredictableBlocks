package com.jason54.unpredictableblocks.item;

import com.jason54.unpredictableblocks.quest.QuestJournal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QuestJournalItem extends LoreItem {
    public QuestJournalItem(Properties properties, java.util.function.Consumer<ItemStack> setup) {
        super(properties, setup);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) {
            return InteractionResult.SUCCESS;
        }
        if (!ItemCooldowns.tryConsume(sp, "journal_quetes", 20 * 3)) {
            return InteractionResult.FAIL;
        }
        QuestJournal.open(sp);
        return InteractionResult.SUCCESS;
    }
}
