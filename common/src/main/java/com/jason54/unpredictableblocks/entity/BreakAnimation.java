package com.jason54.unpredictableblocks.entity;

import com.geckolib.animation.RawAnimation;
import java.util.Locale;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Ce que fait le Lucky Block quand on le casse. {@link #ROCKET} = envol selon le {@link RocketStyle} du thème ;
 * les autres sont communs à tous les thèmes. Animations GeckoLib {@code animation.lucky_rocket.<nom>}
 * (sauf ROCKET : celle du style), générées par tools/sync_geckolib_models.py.
 */
public enum BreakAnimation {
    /** Décolle comme une fusée (trajectoire et anim du style du thème). */
    ROCKET(40, 40),
    /** Gonfle en tremblant puis éclate en nuage. */
    POP(22, 15),
    /** S'enfonce dans le sol en tremblant. */
    SINK(32, 15),
    /** Tourne de plus en plus vite et rétrécit jusqu'à disparaître. */
    VANISH(24, 15),
    /** Rebondit trois fois en s'éloignant, puis disparaît. */
    BOUNCE(40, 15);

    private final int lifetime;
    /** Poids du tirage au cassage (sur 100). */
    private final int weight;
    private final RawAnimation animation;

    BreakAnimation(int lifetime, int weight) {
        this.lifetime = lifetime;
        this.weight = weight;
        this.animation = RawAnimation.begin().thenPlayAndHold("animation.lucky_rocket." + name().toLowerCase(Locale.ROOT));
    }

    public int lifetime() {
        return lifetime;
    }

    /** Anim GeckoLib propre au type (ROCKET utilise celle du style du thème). */
    public RawAnimation animation() {
        return animation;
    }

    public static BreakAnimation byOrdinal(int ordinal) {
        BreakAnimation[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ROCKET;
    }

    /** Tirage au cassage : fusée 40 %, chacune des autres 15 %. */
    public static BreakAnimation roll(RandomSource random) {
        int roll = random.nextInt(100);
        for (BreakAnimation anim : values()) {
            roll -= anim.weight;
            if (roll < 0) {
                return anim;
            }
        }
        return ROCKET;
    }

    /**
     * Décalage depuis le point de départ après {@code tick} ticks (hors ROCKET).
     * {@code seed} fixe la direction de BOUNCE, identique côté serveur et client (id de l'entité).
     */
    public Vec3 offset(int tick, int seed) {
        return switch (this) {
            case ROCKET, POP, VANISH -> Vec3.ZERO;
            case SINK -> new Vec3(0, -Math.min(1.1, tick / 26.0 * 1.1), 0);
            case BOUNCE -> {
                double angle = (seed * 137.508) % 360 * Math.PI / 180;
                double distance = tick * 0.12;
                double hop = Math.abs(Math.sin(tick * Math.PI / 13.0)) * Math.max(0.2, 1.2 - tick * 0.025);
                yield new Vec3(Math.cos(angle) * distance, hop, Math.sin(angle) * distance);
            }
        };
    }
}
