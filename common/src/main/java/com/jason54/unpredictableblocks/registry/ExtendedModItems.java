package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.item.LoreItem;
import com.jason54.unpredictableblocks.item.ThemedGearHelper;
import com.jason54.unpredictableblocks.quest.QuestTriggers;
import com.jason54.unpredictableblocks.quest.Quests;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Items des thèmes 25–32. */
public final class ExtendedModItems {
    private ExtendedModItems() {}

    public static Item espressoUltraDose(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "L'Espresso Ultra-Dosé");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    if (sp.getFoodData().getFoodLevel() <= 0) {
                        sp.sendSystemMessage(Component.literal("§cTrop affamé pour un espresso — quête annulée."));
                        Quests.reset(sp, LuckyTheme.PAUSE_CAFE, "five_espressos");
                    } else {
                        sp.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 45, 2));
                        sp.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 45, 2));
                        sp.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 30, 0));
                        Quests.increment(sp, LuckyTheme.PAUSE_CAFE, "five_espressos");
                    }
                    consumeOne(sp, hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item valisePerdue(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "La Valise Perdue");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.getInventory().add(new ItemStack(Items.SHULKER_BOX));
                    sp.getInventory().add(new ItemStack(Items.LEATHER_CHESTPLATE));
                    Quests.increment(sp, LuckyTheme.RETARD_VOL, "three_valises");
                    consumeOne(sp, hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item trancheDebat(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "La Tranche du Débat");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    QuestTriggers.onAnanasEaten(sp);
                    sp.getFoodData().eat(8, 0.8f);
                    level.getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal("§e" + sp.getName().getString() + " tranche : l'ananas sur la pizza, oui ou non ?"),
                            false
                    );
                    consumeOne(sp, hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item chronoWorldRecord(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "Le Chrono du World Record");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    Vec3 look = sp.getLookAngle().scale(15);
                    sp.teleportTo(sp.getX() + look.x, sp.getY() + look.y, sp.getZ() + look.z);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item disqueDurCapitaine(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "Disque Dur du Capitaine");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.getInventory().add(new ItemStack(Items.FILLED_MAP));
                    sp.getInventory().add(new ItemStack(Items.WRITABLE_BOOK));
                    sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60, 0));
                    Quests.complete(sp, LuckyTheme.PIRATERIE_TORRENT, "treasure_download");
                    consumeOne(sp, hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item popItSupreme(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "Le Pop-It Suprême");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.removeEffect(MobEffects.POISON);
                    sp.removeEffect(MobEffects.NAUSEA);
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 0));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item fioleSangNoble(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "Fiole de Sang Noble");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.heal(6);
                    sp.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 30, 0));
                    if (level.canSeeSky(sp.blockPosition())) {
                        sp.setRemainingFireTicks(4 * 20);
                    }
                    consumeOne(sp, hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    public static Item zapperDimensions(Item.Properties properties) {
        return new LoreItem(properties, stack -> {
            ThemedGearHelper.name(stack, "Zapper de Dimensions");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.level().getEntities(sp, sp.getBoundingBox().inflate(8)).stream().findFirst()
                            .ifPresent(e -> e.teleportTo(e.getX(), e.getY() + 12, e.getZ()));
                    consumeOne(sp, hand);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }
        };
    }

    private static void consumeOne(ServerPlayer player, InteractionHand hand) {
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
    }
}
