package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.compat.McAccess;
import com.jason54.unpredictableblocks.compat.McRefs;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.registry.ModBlocks;
import com.jason54.unpredictableblocks.registry.ModItems;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ModMessages;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Événements bonus tirés au hasard en plus de l'effet du thème : un même Lucky Block ne donne
 * plus toujours le même résultat. Un pool par tier ; texte {@code message.unpredictableblocks.variation.<id>}.
 */
public final class LuckyVariations {
    /** Chance qu'un cassage normal déclenche une variation (les blocs dorés/maudits/géants en tirent plus). */
    public static final float BASE_CHANCE = 0.5F;

    private interface Effect {
        void apply(ServerLevel level, ServerPlayer player, BlockPos origin, LuckyTheme theme, RandomSource random);
    }

    private record Variation(String id, Effect effect) {}

    private static final List<Variation> HIGH = List.of(
            new Variation("xp_rain", (level, player, origin, theme, random) -> {
                for (int i = 0; i < 12; i++) {
                    ExperienceOrb.award(level, scatter(origin, random, 2.0), 5 + random.nextInt(6));
                }
            }),
            new Variation("gold_rain", (level, player, origin, theme, random) -> {
                for (int i = 0; i < 6; i++) {
                    McAccess.giveOrDrop(player, new ItemStack(random.nextInt(4) == 0 ? Items.GOLD_INGOT : Items.GOLD_NUGGET, 1 + random.nextInt(4)));
                }
            }),
            new Variation("lucky_aura", (level, player, origin, theme, random) -> {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1));
            }),
            new Variation("gift_block", (level, player, origin, theme, random) -> {
                LuckyTheme gift = sameRarity(theme, random);
                McAccess.giveOrDrop(player, new ItemStack(ModBlocks.get(gift)));
            }),
            new Variation("diamond", (level, player, origin, theme, random) ->
                    McAccess.giveOrDrop(player, new ItemStack(random.nextInt(5) == 0 ? Items.DIAMOND : Items.EMERALD, 1 + random.nextInt(2)))),
            new Variation("emerald_dust", (level, player, origin, theme, random) ->
                    McAccess.giveOrDrop(player, new ItemStack(ModItems.LUCK_EMERALD_DUST.get(), 2 + random.nextInt(3))))
    );

    private static final List<Variation> NEUTRAL = List.of(
            new Variation("zoo", (level, player, origin, theme, random) -> {
                List<EntityType<? extends Mob>> animals = List.of(McRefs.PIG, McRefs.CHICKEN, McRefs.COW, McRefs.SHEEP);
                for (int i = 0; i < 3; i++) {
                    spawn(level, animals.get(random.nextInt(animals.size())), scatter(origin, random, 2.0));
                }
            }),
            new Variation("flowers", (level, player, origin, theme, random) -> {
                Block[] flowers = {Blocks.POPPY, Blocks.DANDELION, Blocks.CORNFLOWER, Blocks.ALLIUM, Blocks.OXEYE_DAISY};
                for (int i = 0; i < 10; i++) {
                    BlockPos column = origin.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
                    BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
                    if (level.getBlockState(top).isAir() && level.getBlockState(top.below()).is(Blocks.GRASS_BLOCK)) {
                        level.setBlock(top, flowers[random.nextInt(flowers.length)].defaultBlockState(), 3);
                    }
                }
            }),
            new Variation("blink", (level, player, origin, theme, random) -> {
                BlockPos target = origin.offset(random.nextInt(17) - 8, 0, random.nextInt(17) - 8);
                BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target);
                level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 1, 0.5, 0.2);
                player.teleportTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5);
                level.playSound(null, top, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }),
            new Variation("concert", (level, player, origin, theme, random) -> {
                for (int i = 0; i < 8; i++) {
                    level.playSound(null, origin, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 1.0F, 0.5F + random.nextFloat());
                }
                level.sendParticles(ParticleTypes.NOTE, origin.getX() + 0.5, origin.getY() + 1.2, origin.getZ() + 0.5, 20, 1.5, 0.5, 1.5, 1.0);
            }),
            new Variation("snack", (level, player, origin, theme, random) ->
                    McAccess.giveOrDrop(player, new ItemStack(random.nextBoolean() ? Items.COOKIE : Items.PUMPKIN_PIE, 3))),
            new Variation("bouncy", (level, player, origin, theme, random) ->
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 60, 2)))
    );

    private static final List<Variation> LOW = List.of(
            new Variation("anvil", (level, player, origin, theme, random) -> {
                BlockPos above = player.blockPosition().above(8);
                if (level.getBlockState(above).isAir()) {
                    FallingBlockEntity anvil = FallingBlockEntity.fall(level, above, Blocks.ANVIL.defaultBlockState());
                    anvil.setHurtsEntities(1.0F, 8);
                }
            }),
            new Variation("cobwebs", (level, player, origin, theme, random) -> {
                BlockPos feet = player.blockPosition();
                for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-1, 0, -1), feet.offset(1, 1, 1))) {
                    if (level.getBlockState(pos).isAir() && random.nextInt(3) != 0) {
                        level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
                    }
                }
            }),
            new Variation("silverfish", (level, player, origin, theme, random) -> {
                for (int i = 0; i < 4; i++) {
                    spawn(level, McRefs.SILVERFISH, scatter(origin, random, 1.5));
                }
            }),
            new Variation("hungry", (level, player, origin, theme, random) ->
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 2))),
            new Variation("hiccup", (level, player, origin, theme, random) ->
                    player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 3, 1))),
            new Variation("butterfingers", (level, player, origin, theme, random) -> McAccess.dropMainHand(player))
    );

    private LuckyVariations() {}

    /** Tire {@code rolls} variations du pool du tier (chacune avec la probabilité {@code chance}). */
    public static void roll(ServerLevel level, ServerPlayer player, BlockPos origin, LuckyTheme theme,
                            LuckTier tier, int rolls, float chance) {
        RandomSource random = level.getRandom();
        List<Variation> pool = switch (tier) {
            case HIGH -> HIGH;
            case NEUTRAL -> NEUTRAL;
            case LOW -> LOW;
        };
        for (int i = 0; i < rolls; i++) {
            if (random.nextFloat() >= chance) {
                continue;
            }
            Variation variation = pool.get(random.nextInt(pool.size()));
            variation.effect().apply(level, player, origin, theme, random);
            player.sendOverlayMessage(ModMessages.msg("variation." + variation.id()));
        }
    }

    private static Vec3 scatter(BlockPos origin, RandomSource random, double radius) {
        return Vec3.atCenterOf(origin).add((random.nextDouble() - 0.5) * 2 * radius, 0.5, (random.nextDouble() - 0.5) * 2 * radius);
    }

    private static void spawn(ServerLevel level, EntityType<? extends Mob> type, Vec3 pos) {
        Mob mob = EntitySpawnHelper.create(type, level);
        if (mob != null) {
            mob.setPos(pos);
            level.addFreshEntity(mob);
        }
    }

    private static LuckyTheme sameRarity(LuckyTheme theme, RandomSource random) {
        LuckyTheme[] candidates = java.util.Arrays.stream(LuckyTheme.values())
                .filter(t -> t.rarity() == theme.rarity())
                .toArray(LuckyTheme[]::new);
        return candidates[random.nextInt(candidates.length)];
    }
}
