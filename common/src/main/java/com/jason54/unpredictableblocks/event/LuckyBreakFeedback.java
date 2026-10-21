package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.config.ModConfig;
import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Particules + sons selon le tier (HIGH / NEUTRAL / LOW). */
public final class LuckyBreakFeedback {
    private LuckyBreakFeedback() {}

    public static void play(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        double x = origin.getX() + 0.5;
        double y = origin.getY() + 0.65;
        double z = origin.getZ() + 0.5;
        switch (tier) {
            case HIGH -> {
                playTierSound(level, x, y, z, tier, SoundEvents.PLAYER_LEVELUP, 0.85f, 1.15f);
                burst(level, player, ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 24, 0.35, 0.08);
            }
            case NEUTRAL -> {
                playTierSound(level, x, y, z, tier, SoundEvents.NOTE_BLOCK_BELL.value(), 0.7f, 1.0f);
                burst(level, player, ParticleTypes.CLOUD, x, y, z, 16, 0.25, 0.02);
            }
            case LOW -> {
                playTierSound(level, x, y, z, tier, SoundEvents.VILLAGER_NO, 0.9f, 0.75f);
                burst(level, player, ParticleTypes.SMOKE, x, y, z, 20, 0.3, 0.04);
            }
        }
    }

    private static void playTierSound(
            ServerLevel level,
            double x,
            double y,
            double z,
            LuckTier tier,
            SoundEvent vanilla,
            float volume,
            float pitch
    ) {
        SoundEvent sound = vanilla;
        if (ModConfig.gameplay().useModTierSoundsOrDefault()) {
            sound = switch (tier) {
                case HIGH -> ModSounds.TIER_HIGH.get();
                case NEUTRAL -> ModSounds.TIER_NEUTRAL.get();
                case LOW -> ModSounds.TIER_LOW.get();
            };
        }
        level.playSound(null, x, y, z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void burst(
            ServerLevel level,
            ServerPlayer player,
            net.minecraft.core.particles.SimpleParticleType particle,
            double x,
            double y,
            double z,
            int count,
            double spread,
            double speed
    ) {
        level.sendParticles(
                player,
                particle,
                true,
                false,
                x,
                y,
                z,
                count,
                spread,
                spread * 0.6,
                spread,
                speed
        );
    }
}
