package com.jason54.wtfluckyblocks.registry;

import com.jason54.wtfluckyblocks.WtfLuckyBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(WtfLuckyBlocks.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> BONK = register("bonk");
    public static final RegistrySupplier<SoundEvent> BSOD_BOOT = register("bsod_boot");
    public static final RegistrySupplier<SoundEvent> JUMPSCARE = register("jumpscare");

    private ModSounds() {}

    private static RegistrySupplier<SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(WtfLuckyBlocks.MOD_ID, id)));
    }

    public static void register() {
        SOUNDS.register();
    }
}
