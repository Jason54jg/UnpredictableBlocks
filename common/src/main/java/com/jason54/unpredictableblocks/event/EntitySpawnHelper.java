package com.jason54.unpredictableblocks.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

public final class EntitySpawnHelper {
    private static final EntitySpawnReason REASON = EntitySpawnReason.MOB_SUMMONED;

    private EntitySpawnHelper() {}

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T create(EntityType<T> type, ServerLevel level) {
        return (T) type.create(level, REASON);
    }
}
