package com.jason54.unpredictableblocks.entity.mob;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import java.util.function.BooleanSupplier;

/**
 * Animations d'un mob du mod, générées par tools/generate_mobs.py ({@code animation.<id>.<nom>}).
 * <p>
 * Deux contrôleurs : {@code move} (repos / marche / assis, en boucle) et {@code action}
 * (attaque et capacité spéciale, déclenchées côté serveur par {@code triggerAnim("action", ...)}).
 */
public record MobAnimations(RawAnimation idle, RawAnimation walk, RawAnimation sit, RawAnimation attack, RawAnimation special) {
    public static final String ACTION = "action";
    public static final String ATTACK = "attack";
    public static final String SPECIAL = "special";

    public static MobAnimations of(String mobId) {
        String prefix = "animation." + mobId + ".";
        return new MobAnimations(
                RawAnimation.begin().thenLoop(prefix + "idle"),
                RawAnimation.begin().thenLoop(prefix + "walk"),
                RawAnimation.begin().thenLoop(prefix + "sit"),
                RawAnimation.begin().thenPlay(prefix + "attack"),
                RawAnimation.begin().thenPlay(prefix + "special"));
    }

    /** Contrôleurs standards ; {@code sitting} = null si le mob ne s'assoit jamais. */
    public <T extends GeoAnimatable> void register(AnimatableManager.ControllerRegistrar controllers, BooleanSupplier sitting) {
        controllers.add(new AnimationController<T>("move", 4, state -> {
            if (sitting != null && sitting.getAsBoolean()) {
                return state.setAndContinue(sit);
            }
            return state.setAndContinue(state.isMoving() ? walk : idle);
        }));
        controllers.add(new AnimationController<T>(ACTION, 2, state -> com.geckolib.animation.object.PlayState.STOP)
                .triggerableAnim(ATTACK, attack)
                .triggerableAnim(SPECIAL, special));
    }
}
