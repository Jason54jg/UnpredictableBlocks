package com.jason54.unpredictableblocks.compat;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Block;

/** Cross-version Mojmap refs via registry IDs (26.1.2 / 26.2 / 26.3). */
public final class McRefs {
    private McRefs() {}

    public static final EntityType<Zombie> ZOMBIE = entity("zombie");
    public static final EntityType<Villager> VILLAGER = entity("villager");
    public static final EntityType<Pig> PIG = entity("pig");
    public static final EntityType<Chicken> CHICKEN = entity("chicken");
    public static final EntityType<Wolf> WOLF = entity("wolf");
    public static final EntityType<Ravager> RAVAGER = entity("ravager");
    public static final EntityType<IronGolem> IRON_GOLEM = entity("iron_golem");
    public static final EntityType<PolarBear> POLAR_BEAR = entity("polar_bear");
    public static final EntityType<?> ENDERMAN = entity("enderman");
    public static final EntityType<Skeleton> SKELETON = entity("skeleton");
    public static final EntityType<Rabbit> RABBIT = entity("rabbit");
    public static final EntityType<Silverfish> SILVERFISH = entity("silverfish");
    public static final EntityType<Cow> COW = entity("cow");
    public static final EntityType<Sheep> SHEEP = entity("sheep");
    public static final Block WHITE_WOOL = block("white_wool");
    public static final Block WHITE_CONCRETE = block("white_concrete");

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> entity(String path) {
        return (EntityType<T>) Objects.requireNonNull(
                BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(path)), path);
    }

    private static Block block(String path) {
        return Objects.requireNonNull(
                BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(path)), path);
    }
}