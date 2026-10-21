package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.item.LoreItem;
import com.jason54.unpredictableblocks.item.ThemedGearHelper;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.quest.QuestExtendedHandler;
import com.jason54.unpredictableblocks.quest.QuestRewards;
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
            ThemedGearHelper.lore(stack, "Hâte III, Vitesse III, Nausée I.");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    if (sp.getFoodData().getFoodLevel() <= 0) {
                        sp.sendSystemMessage(Component.literal("§cTrop affamé pour un espresso — quête annulée."));
                        PlayerQuestData.resetCounter(sp, "espresso");
                    } else {
                        sp.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 45, 2));
                        sp.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 45, 2));
                        sp.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 30, 0));
                        int n = PlayerQuestData.incrementCounter(sp, "espresso");
                        if (n >= 5 && sp.getFoodData().getFoodLevel() > 0) {
                            QuestRewards.complete(sp, "quest/pause_cafe", "five_espressos");
                        }
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
            ThemedGearHelper.lore(stack, "Shulker d'armures cuir multicolores.");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.getInventory().add(new ItemStack(Items.SHULKER_BOX));
                    sp.getInventory().add(new ItemStack(Items.LEATHER_CHESTPLATE));
                    int n = PlayerQuestData.incrementCounter(sp, "valise");
                    if (n >= 3) {
                        QuestRewards.complete(sp, "quest/retard_vol", "three_valises");
                    }
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
            ThemedGearHelper.lore(stack, "Ananas ? Sur la pizza ?");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    QuestExtendedHandler.onAnanasEaten(sp);
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
            ThemedGearHelper.lore(stack, "Téléportation 15 blocs avant.");
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
            ThemedGearHelper.lore(stack, "Livre de malédictions numériques.");
        }) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    sp.getInventory().add(new ItemStack(Items.FILLED_MAP));
                    sp.getInventory().add(new ItemStack(Items.WRITABLE_BOOK));
                    sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60, 0));
                    QuestRewards.complete(sp, "quest/piraterie_torrent", "treasure_download");
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
            ThemedGearHelper.lore(stack, "Anti-nausée, sons relaxants.");
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
            ThemedGearHelper.lore(stack, "Vol de vie, brûlure au soleil.");
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
            ThemedGearHelper.lore(stack, "Projette une entité visée.");
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
