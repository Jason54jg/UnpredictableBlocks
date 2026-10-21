package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.block.entity.LuckyBlockEntity;
import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.util.ServerLevels;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Particules discrètes sur les Lucky Blocks posés (luck NBT). */
public final class LuckyBlockAmbientHandler {
    private LuckyBlockAmbientHandler() {}

    public static void register() {
        TickEvent.PLAYER_POST.register(player -> {
            if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
                return;
            }
            int interval = ModConfig.gameplay().ambientParticleIntervalTicksOrDefault();
            if (interval <= 0 || sp.tickCount % interval != 0) {
                return;
            }
            ServerLevel level = ServerLevels.of(sp);
            double radius = ModConfig.gameplay().ambientParticleRadiusOrDefault();
            AABB box = sp.getBoundingBox().inflate(radius);
            BlockPos.betweenClosedStream(
                    BlockPos.containing(box.minX, box.minY, box.minZ),
                    BlockPos.containing(box.maxX, box.maxY, box.maxZ)
            ).forEach(pos -> {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof LuckyBlockEntity lucky) {
                    spawnHint(level, pos, lucky.getLuck());
                }
            });
        });
    }

    private static void spawnHint(ServerLevel level, BlockPos pos, int luck) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.85;
        double z = pos.getZ() + 0.5;
        var particle = luck > 10 ? ParticleTypes.HAPPY_VILLAGER
                : luck < -10 ? ParticleTypes.ANGRY_VILLAGER
                : ParticleTypes.END_ROD;
        level.sendParticles(particle, x, y, z, 1, 0.1, 0.05, 0.1, 0.0);
    }
}
