package com.jason54.unpredictableblocks.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Casse des Lucky Blocks devant la caméra et prend des captures pendant l'animation.
 * Lancer : ./gradlew :fabric:runClientGameTest — captures dans fabric/run/clientGameTest/screenshots.
 */
public final class LuckyBlockClientGameTest implements FabricClientGameTest {
    private static final String[] THEMES = {"boulangerie", "disco_80", "bsod", "gravite_zero", "chat_internet"};
    private static final int[] SHOT_TICKS = {4, 10, 16, 24, 34};

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule advance_time false");
            server.runCommand("gamerule unpredictableblocks:lucky_density 300");
            server.runCommand("gamerule unpredictableblocks:lucky_shrines false");
            server.runCommand("fill -6 99 -6 6 99 6 minecraft:stone");
            server.runCommand("fill -6 100 -6 6 110 6 minecraft:air");

            for (int i = 0; i < THEMES.length; i++) {
                server.runCommand("effect clear @a"); // cécité / nausée laissées par le bloc précédent
                server.runCommand("setblock 0 100 0 unpredictableblocks:lucky_" + THEMES[i]);
                // 6 blocs devant, regard au-dessus du bloc pour cadrer l'envol
                server.runCommand("tp @a 0.5 100 -5.5 facing 0.5 102 0.5");
                singleplayer.getConnection().waitForChunksRender();
                context.waitTicks(10);
                context.takeScreenshot("lucky_" + i + "_" + THEMES[i] + "_before");

                // Cassé « par le joueur » côté serveur (même chemin que le clic : playerWillDestroy)
                server.runOnServer(mc -> mc.getPlayerList().getPlayers().getFirst().gameMode.destroyBlock(new BlockPos(0, 100, 0)));
                int waited = 0;
                for (int shot : SHOT_TICKS) {
                    context.waitTicks(shot - waited);
                    waited = shot;
                    context.takeScreenshot("lucky_" + i + "_" + THEMES[i] + "_t" + shot);
                }
                context.waitTicks(30);
                server.runCommand("kill @e[type=!player]");
                server.runCommand("fill -6 100 -6 6 110 6 minecraft:air");
            }
        }
    }
}
