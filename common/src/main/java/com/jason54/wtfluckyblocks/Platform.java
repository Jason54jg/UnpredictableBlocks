package com.jason54.wtfluckyblocks;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class Platform {
    private Platform() {}

    @ExpectPlatform
    public static void onInitialize() {
        throw new AssertionError();
    }
}
