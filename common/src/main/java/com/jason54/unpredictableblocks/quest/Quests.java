package com.jason54.unpredictableblocks.quest;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import com.jason54.unpredictableblocks.util.ServerLevels;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Registre unique des quêtes de thème : une entrée par thème, une étape par critère d'advancement
 * ({@code data/unpredictableblocks/advancement/quest/<id>.json}).
 * <p>
 * Les déclencheurs ({@link QuestTriggers}, objets, chat…) n'écrivent jamais de chemin ni de seuil en dur :
 * ils appellent {@link #complete} ou {@link #increment} avec le thème et le critère. Le journal lit
 * {@link #progress} pour afficher « 3/10 ». Textes : {@code quest.unpredictableblocks.<thème>.<critère>}
 * (objectif) et {@code quest.unpredictableblocks.<thème>.hint} (indice).
 */
public final class Quests {
    /** Une étape : critère d'advancement + cible ; {@code current} = progression lue (null = étape oui/non). */
    public record Step(String criterion, int target, ToIntFunction<ServerPlayer> current, String counter) {
        boolean counted() {
            return counter != null;
        }
    }

    private static final Map<LuckyTheme, List<Step>> STEPS = new EnumMap<>(LuckyTheme.class);

    private static Step once(String criterion) {
        return new Step(criterion, 1, null, null);
    }

    /** Compteur persistant (PlayerQuestData) incrémenté par {@link #increment}. */
    private static Step counter(String criterion, String counter, int target) {
        return new Step(criterion, target, p -> PlayerQuestData.getCounter(p, counter), counter);
    }

    /** Progression calculée à la volée (inventaire, biomes…) ; l'étape est validée par {@link #complete}. */
    private static Step live(String criterion, int target, ToIntFunction<ServerPlayer> current) {
        return new Step(criterion, target, current, null);
    }

    private static Step hold(String criterion, Item item, int target) {
        return live(criterion, target, p -> QuestTriggers.countItem(p, item));
    }

    private static void def(LuckyTheme theme, Step... steps) {
        STEPS.put(theme, List.of(steps));
    }

    static {
        def(LuckyTheme.ANACHRONISME, once("kill_ravager"));
        def(LuckyTheme.BOULANGERIE, counter("kill_poultry", "kill_chicken_boulangerie", 10), hold("have_bread", Items.BREAD, 32));
        def(LuckyTheme.BSOD, once("glitched"));
        def(LuckyTheme.VOLAILLE, once("kill_golem"));
        def(LuckyTheme.IMPOTS, counter("pay", "impots_emerald", 16));
        def(LuckyTheme.STREAMER, counter("streamer_zombies", "kill_zombie_streamer", 15));
        def(LuckyTheme.PROPRETE, counter("kills", "kill_zombie_proprete", 5));
        def(LuckyTheme.GRAVITE_ZERO, once("levitate"));
        def(LuckyTheme.FAST_FOOD, once("ride"));
        def(LuckyTheme.CAUCHEMAR_MATIN, once("sleep"));
        def(LuckyTheme.FILM_ACTION, once("slow_fall"));
        def(LuckyTheme.JDR_D20, once("mimic"));
        def(LuckyTheme.PENURIE_2020, hold("tp", Items.PAPER, 64));
        def(LuckyTheme.COMPLOT, once("hat"));
        def(LuckyTheme.LAG_BUG, once("end"));
        def(LuckyTheme.DISCO_80, live("music", 1, QuestTriggers::recordsPlayed));
        def(LuckyTheme.PLOMBERIE, once("chest"));
        def(LuckyTheme.FORTUNE_COOKIE, once("fortune_code"));
        def(LuckyTheme.CAMPING, once("bear"));
        def(LuckyTheme.GYM_BRO, once("buff"));
        def(LuckyTheme.ART_MODERNE, hold("art", Items.PAINTING, 1));
        def(LuckyTheme.MAGIE_COMPTOIR, once("wolf"));
        def(LuckyTheme.INVASION_MEMES, live("completed", 50, PlayerQuestData::getBonkCount));
        def(LuckyTheme.PAUSE_CAFE, counter("five_espressos", "espresso", 5));
        def(LuckyTheme.RETARD_VOL, counter("three_valises", "valise", 3));
        def(LuckyTheme.ANANAS_PIZZA, counter("survive_chefs", "chef_kill", 10));
        def(LuckyTheme.SPEEDRUNNER, once("end_speedrun"));
        def(LuckyTheme.PIRATERIE_TORRENT, once("treasure_download"));
        def(LuckyTheme.ASMR_FIDGET, live("bubble_pop_50", 50, QuestTriggers::bubblePops));
        def(LuckyTheme.VAMPIRE_GOTHIQUE, once("dracula_no_metal"));
        def(LuckyTheme.MULTIVERS_SLIDERS, live("multivers_biomes", 3, PlayerQuestData::multiversBiomeCount));
        def(LuckyTheme.GREVE_TRANSPORTS, once("minecart"));
        def(LuckyTheme.METEO_FOLLE, once("struck"));
        def(LuckyTheme.CHAT_INTERNET, counter("tame_cats", "tame_cat", 3));
        def(LuckyTheme.CRYPTO_BULLE, hold("hodl", Items.EMERALD, 64));
    }

    private Quests() {}

    public static List<Step> steps(LuckyTheme theme) {
        return STEPS.getOrDefault(theme, List.of());
    }

    /** Valide l'étape {@code criterion} de la quête du thème. */
    public static void complete(ServerPlayer player, LuckyTheme theme, String criterion) {
        QuestRewards.complete(player, path(theme), criterion);
    }

    /** +{@code delta} sur une étape compteur ; la valide quand la cible du registre est atteinte. */
    public static void increment(ServerPlayer player, LuckyTheme theme, String criterion, int delta) {
        Step step = step(theme, criterion);
        if (step == null || !step.counted()) {
            UnpredictableBlocks.LOGGER.warn("Étape compteur inconnue : {}/{}", theme.id(), criterion);
            return;
        }
        int value = PlayerQuestData.addCounter(player, step.counter(), delta);
        if (value >= step.target()) {
            complete(player, theme, criterion);
        }
    }

    public static void increment(ServerPlayer player, LuckyTheme theme, String criterion) {
        increment(player, theme, criterion, 1);
    }

    /** Remet un compteur à zéro (quête ratée, ex. espresso le ventre vide). */
    public static void reset(ServerPlayer player, LuckyTheme theme, String criterion) {
        Step step = step(theme, criterion);
        if (step != null && step.counted()) {
            PlayerQuestData.resetCounter(player, step.counter());
        }
    }

    public static boolean isDone(ServerPlayer player, LuckyTheme theme) {
        return QuestRewards.isDone(player, path(theme));
    }

    public static boolean isStepDone(ServerPlayer player, LuckyTheme theme, Step step) {
        AdvancementProgress progress = advancementProgress(player, theme);
        if (progress == null) {
            return false;
        }
        CriterionProgress criterion = progress.getCriterion(step.criterion());
        return criterion != null && criterion.isDone();
    }

    /** Progression affichable d'une étape, bornée à sa cible. */
    public static int progress(ServerPlayer player, LuckyTheme theme, Step step) {
        if (isStepDone(player, theme, step)) {
            return step.target();
        }
        if (step.current() == null) {
            return 0;
        }
        return Math.clamp(step.current().applyAsInt(player), 0, step.target());
    }

    private static Step step(LuckyTheme theme, String criterion) {
        for (Step step : steps(theme)) {
            if (step.criterion().equals(criterion)) {
                return step;
            }
        }
        return null;
    }

    private static String path(LuckyTheme theme) {
        return "quest/" + theme.id();
    }

    private static AdvancementProgress advancementProgress(ServerPlayer player, LuckyTheme theme) {
        Identifier id = Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, path(theme));
        AdvancementHolder holder = ServerLevels.of(player).getServer().getAdvancements().get(id);
        return holder == null ? null : player.getAdvancements().getOrStartProgress(holder);
    }
}
