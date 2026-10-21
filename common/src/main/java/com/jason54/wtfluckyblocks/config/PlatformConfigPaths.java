package com.jason54.wtfluckyblocks.config;

import dev.architectury.platform.Platform;

import java.nio.file.Path;

public final class PlatformConfigPaths {
    private PlatformConfigPaths() {}

    public static Path configFile(String name) {
        return Platform.getConfigFolder().resolve(name);
    }
}
