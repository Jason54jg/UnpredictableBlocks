package com.jason54.unpredictableblocks.entity;

import com.geckolib.animation.RawAnimation;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Style de décollage d'un Lucky Block cassé : trajectoire (ici), animation GeckoLib
 * {@code animation.lucky_rocket.<style>} (générée par tools/sync_geckolib_models.py) et particules.
 * <p>
 * La table {@link #LOOKS} est aussi lue par le générateur Python (regex sur {@code entry(...)}) pour que
 * le .bbmodel de chaque thème montre la même anim qu'en jeu : garder une entrée par ligne.
 */
public enum RocketStyle {
    /** Tremble puis monte tout droit en vrillant. */
    SPIN(true),
    /** Trois petits sauts puis décolle en faisant des saltos. */
    HOP(true),
    /** Monte en zigzag, comme ivre. */
    ZIGZAG(true),
    /** Monte en hélice. */
    SPIRAL(true),
    /** Part en tonneau, en dérivant. */
    TUMBLE(true),
    /** Monte par à-coups, se téléporte de quelques pixels (bug). */
    GLITCH(false),
    /** Lévite lentement, sans flamme. */
    FLOAT(false);

    private final boolean flame;
    private final RawAnimation animation;

    RocketStyle(boolean flame) {
        this.flame = flame;
        this.animation = RawAnimation.begin().thenPlayAndHold("animation.lucky_rocket." + name().toLowerCase(Locale.ROOT));
    }

    public RawAnimation animation() {
        return animation;
    }

    public boolean hasFlame() {
        return flame;
    }

    /** Décalage depuis le point de départ après {@code flying} ticks de vol (0 pendant le tremblement). */
    public Vec3 offset(int flying) {
        if (flying <= 0) {
            return Vec3.ZERO;
        }
        double ramp = Math.min(1.0, flying / 6.0);
        return switch (this) {
            case SPIN, HOP -> new Vec3(0, climb(flying), 0);
            case ZIGZAG -> new Vec3(0.6 * Math.sin(flying * 0.5) * ramp, climb(flying) * 0.9, 0);
            case SPIRAL -> {
                double radius = Math.min(1.5, 0.08 * flying);
                double angle = flying * 0.45;
                yield new Vec3(radius * Math.cos(angle) - radius, climb(flying), radius * Math.sin(angle));
            }
            case TUMBLE -> new Vec3(0.05 * flying, climb(flying), 0.025 * flying);
            case GLITCH -> {
                int step = flying / 3;
                double jitterX = ((step * 31) % 7 - 3) * 0.12;
                double jitterZ = ((step * 17) % 5 - 2) * 0.12;
                yield new Vec3(jitterX, climb(step * 3), jitterZ);
            }
            case FLOAT -> new Vec3(0.2 * Math.sin(flying * 0.2), 0.004 * flying * flying + 0.05 * flying, 0);
        };
    }

    /** Montée accélérée : +0.04 bloc/tick², plafonnée à 0.8 bloc/tick. */
    private static double climb(int ticks) {
        int accelerating = Math.min(ticks, 20);
        return 0.02 * accelerating * (accelerating + 1) + 0.8 * Math.max(0, ticks - 20);
    }

    /** Apparence d'un thème : style, couleur de la traînée, particule d'accent. */
    public record Look(RocketStyle style, ParticleOptions trail, ParticleOptions accent) {}

    private static final Map<LuckyTheme, Look> LOOKS = new EnumMap<>(LuckyTheme.class);

    private static void entry(LuckyTheme theme, RocketStyle style, int color, ParticleOptions accent) {
        LOOKS.put(theme, new Look(style, new DustParticleOptions(color, 1.6F), accent));
    }

    static {
        entry(LuckyTheme.ANACHRONISME, TUMBLE, 0xC9A86A, ParticleTypes.ENCHANT);
        entry(LuckyTheme.BOULANGERIE, SPIN, 0xF2D49B, ParticleTypes.WHITE_SMOKE);
        entry(LuckyTheme.BSOD, GLITCH, 0x1E5BD8, ParticleTypes.ELECTRIC_SPARK);
        entry(LuckyTheme.VOLAILLE, HOP, 0xFFFFFF, ParticleTypes.CLOUD);
        entry(LuckyTheme.IMPOTS, ZIGZAG, 0x4E7D3A, ParticleTypes.HAPPY_VILLAGER);
        entry(LuckyTheme.STREAMER, SPIRAL, 0x9146FF, ParticleTypes.WITCH);
        entry(LuckyTheme.PROPRETE, ZIGZAG, 0x9FE7FF, ParticleTypes.SPLASH);
        entry(LuckyTheme.GRAVITE_ZERO, FLOAT, 0x6A5ACD, ParticleTypes.END_ROD);
        entry(LuckyTheme.FAST_FOOD, SPIN, 0xE53935, ParticleTypes.SMALL_FLAME);
        entry(LuckyTheme.CAUCHEMAR_MATIN, ZIGZAG, 0x3B3355, ParticleTypes.SOUL);
        entry(LuckyTheme.FILM_ACTION, TUMBLE, 0xFF6F00, ParticleTypes.LAVA);
        entry(LuckyTheme.JDR_D20, HOP, 0xB71C1C, ParticleTypes.ENCHANTED_HIT);
        entry(LuckyTheme.PENURIE_2020, ZIGZAG, 0xEEEEEE, ParticleTypes.WHITE_SMOKE);
        entry(LuckyTheme.COMPLOT, GLITCH, 0x2E7D32, ParticleTypes.WARPED_SPORE);
        entry(LuckyTheme.LAG_BUG, GLITCH, 0xFF00FF, ParticleTypes.PORTAL);
        entry(LuckyTheme.DISCO_80, SPIRAL, 0xFF4081, ParticleTypes.NOTE);
        entry(LuckyTheme.PLOMBERIE, ZIGZAG, 0x1565C0, ParticleTypes.SPLASH);
        entry(LuckyTheme.FORTUNE_COOKIE, HOP, 0xE0B060, ParticleTypes.TOTEM_OF_UNDYING);
        entry(LuckyTheme.CAMPING, SPIN, 0x6D4C41, ParticleTypes.SMOKE);
        entry(LuckyTheme.GYM_BRO, HOP, 0xB0BEC5, ParticleTypes.CRIT);
        entry(LuckyTheme.ART_MODERNE, SPIRAL, 0xFFC107, ParticleTypes.CHERRY_LEAVES);
        entry(LuckyTheme.MAGIE_COMPTOIR, FLOAT, 0x7E57C2, ParticleTypes.WITCH);
        entry(LuckyTheme.INVASION_MEMES, SPIRAL, 0x76FF03, ParticleTypes.HAPPY_VILLAGER);
        entry(LuckyTheme.PAUSE_CAFE, SPIN, 0x6F4E37, ParticleTypes.CAMPFIRE_COSY_SMOKE);
        entry(LuckyTheme.RETARD_VOL, ZIGZAG, 0x90CAF9, ParticleTypes.CLOUD);
        entry(LuckyTheme.ANANAS_PIZZA, SPIN, 0xFFD54F, ParticleTypes.SMALL_FLAME);
        entry(LuckyTheme.SPEEDRUNNER, TUMBLE, 0x00E676, ParticleTypes.ELECTRIC_SPARK);
        entry(LuckyTheme.PIRATERIE_TORRENT, SPIRAL, 0x212121, ParticleTypes.SQUID_INK);
        entry(LuckyTheme.ASMR_FIDGET, FLOAT, 0xF48FB1, ParticleTypes.BUBBLE_POP);
        entry(LuckyTheme.VAMPIRE_GOTHIQUE, FLOAT, 0x8B0000, ParticleTypes.CRIMSON_SPORE);
        entry(LuckyTheme.MULTIVERS_SLIDERS, GLITCH, 0x00BCD4, ParticleTypes.REVERSE_PORTAL);
        entry(LuckyTheme.GREVE_TRANSPORTS, ZIGZAG, 0xE53935, ParticleTypes.SMOKE);
        entry(LuckyTheme.METEO_FOLLE, TUMBLE, 0x546E7A, ParticleTypes.ELECTRIC_SPARK);
        entry(LuckyTheme.CHAT_INTERNET, HOP, 0xFFA726, ParticleTypes.HEART);
        entry(LuckyTheme.CRYPTO_BULLE, GLITCH, 0xF7931A, ParticleTypes.HAPPY_VILLAGER);
    }

    public static Look look(LuckyTheme theme) {
        Look look = LOOKS.get(theme);
        return look != null ? look : new Look(SPIN, new DustParticleOptions(0xAAAAAA, 1.6F), ParticleTypes.SMOKE);
    }
}
