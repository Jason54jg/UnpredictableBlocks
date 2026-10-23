package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.mojang.serialization.Codec;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelData;

/**
 * Mode {@link LuckyWorldMode#SKYBLOCK} : construit l'île de départ une fois, puis régénère un Lucky Block
 * sur le « générateur » (bloc d'or) de l'île toutes les {@link #REGEN_TICKS} ticks s'il a été cassé.
 * <p>
 * Construction différée (pas dans {@code SERVER_STARTED}) pour éviter le freeze NeoForge au boot.
 */
public final class LuckySkyblock {
    /** Coin nord-ouest du dessus de l'île (couche d'herbe). */
    private static final BlockPos ISLAND = new BlockPos(-3, 64, -3);
    private static final int SIZE = 7;
    private static final BlockPos SPAWN = ISLAND.offset(1, 1, 1);
    /** Lucky Block posé sur le bloc d'or, au centre de l'île. */
    private static final BlockPos GENERATOR = ISLAND.offset(SIZE / 2, 1, SIZE / 2);
    private static final int REGEN_TICKS = 600;
    /** NOTIFY_CLIENTS seulement — pas de maj voisins (évite getChunk en cascade). */
    private static final int PLACE_FLAGS = 2;

    private LuckySkyblock() {}

    public static void register() {
        // Tick différé : pas de setBlock pendant SERVER_STARTED (freeze NeoForge).
        TickEvent.SERVER_POST.register(LuckySkyblock::tick);
        PlayerEvent.PLAYER_JOIN.register(LuckySkyblock::rescueIfFalling);
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> rescueIfFalling(player));
    }

    private static void rescueIfFalling(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD
                || LuckyWorldMode.of(level) != LuckyWorldMode.SKYBLOCK) {
            return;
        }
        ensureIsland(level);
        BlockPos feet = player.blockPosition();
        for (int dy = 1; dy <= 16; dy++) {
            if (!level.getBlockState(feet.below(dy)).isAir()) {
                return;
            }
        }
        player.teleportTo(SPAWN.getX() + 0.5, SPAWN.getY(), SPAWN.getZ() + 0.5);
    }

    private static void tick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (LuckyWorldMode.of(overworld) == LuckyWorldMode.SKYBLOCK) {
            ensureIsland(overworld);
        }
        if (overworld.getGameTime() % REGEN_TICKS != 0 || LuckyWorldMode.of(overworld) != LuckyWorldMode.SKYBLOCK) {
            return;
        }
        if (!overworld.isLoaded(GENERATOR) || !overworld.getBlockState(GENERATOR).isAir()
                || !overworld.getBlockState(GENERATOR.below()).is(Blocks.GOLD_BLOCK)) {
            return;
        }
        if (LuckyBlockSpawnPlacement.placeRandom(overworld, GENERATOR, overworld.getRandom())) {
            overworld.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    GENERATOR.getX() + 0.5, GENERATOR.getY() + 0.5, GENERATOR.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.0);
            overworld.playSound(null, GENERATOR, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    private static void ensureIsland(ServerLevel overworld) {
        if (LuckyWorldMode.of(overworld) != LuckyWorldMode.SKYBLOCK) {
            return;
        }
        State state = State.get(overworld);
        if (state.built) {
            return;
        }
        buildIsland(overworld);
        overworld.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, SPAWN, 0.0F, 0.0F));
        state.built = true;
        state.setDirty();
        UnpredictableBlocks.LOGGER.info("Île Lucky Skyblock construite en {}", ISLAND);
    }

    private static void buildIsland(ServerLevel level) {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                // Coins arrondis
                if ((x == 0 || x == SIZE - 1) && (z == 0 || z == SIZE - 1)) {
                    continue;
                }
                level.setBlock(ISLAND.offset(x, -2, z), Blocks.DIRT.defaultBlockState(), PLACE_FLAGS);
                level.setBlock(ISLAND.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), PLACE_FLAGS);
                level.setBlock(ISLAND.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), PLACE_FLAGS);
            }
        }
        level.setBlock(ISLAND.offset(SIZE / 2, -2, SIZE / 2), Blocks.BEDROCK.defaultBlockState(), PLACE_FLAGS);
        level.setBlock(GENERATOR.below(), Blocks.GOLD_BLOCK.defaultBlockState(), PLACE_FLAGS);
        buildTree(level, ISLAND.offset(SIZE - 2, 1, SIZE - 2));

        BlockPos chestPos = ISLAND.offset(1, 1, SIZE - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), PLACE_FLAGS);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.LAVA_BUCKET));
            chest.setItem(1, new ItemStack(Items.ICE, 2));
            chest.setItem(2, new ItemStack(Items.OAK_SAPLING, 2));
            chest.setItem(3, new ItemStack(Items.BONE_MEAL, 8));
            chest.setItem(4, new ItemStack(Items.SUGAR_CANE));
            chest.setItem(5, new ItemStack(Items.MELON_SEEDS));
            chest.setItem(6, new ItemStack(Items.BREAD, 5));
        }
        LuckyBlockSpawnPlacement.placeRandom(level, GENERATOR, level.getRandom());
    }

    private static void buildTree(ServerLevel level, BlockPos base) {
        for (int y = 0; y < 5; y++) {
            level.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState(), PLACE_FLAGS);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 2; dy <= 5; dy++) {
                    int radius = dy >= 4 ? 1 : 2;
                    if (Math.abs(dx) > radius || Math.abs(dz) > radius || (dx == 0 && dz == 0 && dy < 5)) {
                        continue;
                    }
                    BlockPos leaf = base.offset(dx, dy, dz);
                    if (level.getBlockState(leaf).isAir()) {
                        level.setBlock(leaf, Blocks.OAK_LEAVES.defaultBlockState(), PLACE_FLAGS);
                    }
                }
            }
        }
    }

    /** « Île déjà construite » — par monde. */
    private static final class State extends SavedData {
        private static final Codec<State> CODEC = CompoundTag.CODEC.xmap(State::fromTag, State::toTag);
        private static final SavedDataType<State> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "skyblock"),
                State::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        private boolean built;

        private static State fromTag(CompoundTag tag) {
            State state = new State();
            state.built = tag.getBooleanOr("Built", false);
            return state;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Built", built);
            return tag;
        }

        static State get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(TYPE);
        }
    }
}
