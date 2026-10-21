package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BONK = register("bonk");
    public static final RegistrySupplier<SoundEvent> BSOD_BOOT = register("bsod_boot");
    public static final RegistrySupplier<SoundEvent> JUMPSCARE = register("jumpscare");
    public static final RegistrySupplier<SoundEvent> TIER_HIGH = register("tier_high");
    public static final RegistrySupplier<SoundEvent> TIER_NEUTRAL = register("tier_neutral");
    public static final RegistrySupplier<SoundEvent> TIER_LOW = register("tier_low");

    private ModSounds() {}

    private static RegistrySupplier<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, id)));
    }

    public static void register() {
        SOUNDS.register();
    }
}
