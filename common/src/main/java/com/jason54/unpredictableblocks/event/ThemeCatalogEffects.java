package com.jason54.unpredictableblocks.event;

import com.jason54.unpredictableblocks.compat.McRefs;

import com.jason54.unpredictableblocks.luck.LuckTier;
import com.jason54.unpredictableblocks.quest.PlayerQuestData;
import com.jason54.unpredictableblocks.theme.LuckyTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Événements « catalogue v2 » : drops/thèmes existants (directive §4).
 */
public final class ThemeCatalogEffects {
    private ThemeCatalogEffects() {}

    /** Événement catalogue (directive §4) pour ce thème et ce palier — évite le double avec le legacy. */
    public static boolean applies(LuckyTheme theme, LuckTier tier) {
        return switch (theme) {
            case ANACHRONISME -> tier == LuckTier.HIGH;
            case BOULANGERIE -> tier == LuckTier.LOW || tier == LuckTier.NEUTRAL;
            case BSOD -> tier == LuckTier.NEUTRAL;
            case VOLAILLE -> tier == LuckTier.HIGH;
            case IMPOTS -> tier == LuckTier.LOW;
            case STREAMER -> tier == LuckTier.NEUTRAL;
            case PROPRETE -> tier == LuckTier.HIGH;
            case GRAVITE_ZERO -> tier == LuckTier.LOW;
            case FAST_FOOD -> tier == LuckTier.NEUTRAL;
            case CAUCHEMAR_MATIN -> tier == LuckTier.HIGH;
            case FILM_ACTION -> tier == LuckTier.LOW;
            case JDR_D20 -> tier == LuckTier.NEUTRAL;
            case PENURIE_2020 -> tier == LuckTier.HIGH;
            case COMPLOT -> tier == LuckTier.LOW;
            case LAG_BUG -> tier == LuckTier.NEUTRAL;
            case DISCO_80 -> tier == LuckTier.HIGH;
            case PLOMBERIE -> tier == LuckTier.LOW;
            case FORTUNE_COOKIE -> tier == LuckTier.NEUTRAL;
            case CAMPING -> tier == LuckTier.HIGH;
            case GYM_BRO -> tier == LuckTier.LOW;
            case ART_MODERNE -> tier == LuckTier.NEUTRAL;
            case MAGIE_COMPTOIR -> tier == LuckTier.HIGH;
            case INVASION_MEMES -> tier == LuckTier.NEUTRAL;
            default -> false;
        };
    }

    public static void apply(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            LuckyTheme theme,
            LuckTier tier
    ) {
        switch (theme) {
            case ANACHRONISME -> anachronisme(level, player, origin, tier);
            case BOULANGERIE -> boulangerie(level, player, origin, tier);
            case BSOD -> bsod(player, tier);
            case VOLAILLE -> volaille(level, player, origin, tier);
            case IMPOTS -> impots(player, tier);
            case STREAMER -> streamer(player, tier);
            case PROPRETE -> proprete(player, tier);
            case GRAVITE_ZERO -> gravite(player, tier);
            case FAST_FOOD -> fastFood(level, player, origin, tier);
            case CAUCHEMAR_MATIN -> cauchemar(level, player, origin, tier);
            case FILM_ACTION -> filmAction(level, player, tier);
            case JDR_D20 -> jdr(level, player, origin, tier);
            case PENURIE_2020 -> penurie(level, player, tier);
            case COMPLOT -> complot(player, tier);
            case LAG_BUG -> lagBug(level, player, origin, tier);
            case DISCO_80 -> disco(level, player, origin, tier);
            case PLOMBERIE -> plomberie(level, player, origin, tier);
            case FORTUNE_COOKIE -> fortuneCookie(player, tier);
            case CAMPING -> camping(player, tier);
            case GYM_BRO -> gymBro(level, player, origin, tier);
            case ART_MODERNE -> artModerne(level, player, origin, tier);
            case MAGIE_COMPTOIR -> magie(level, origin, tier);
            case INVASION_MEMES -> memes(player, tier);
            default -> {}
        }
    }

    private static void anachronisme(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        var ally = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.TOI_DU_FUTUR.get(), level);
        if (ally == null) {
            return;
        }
        ally.setAllyOwner(player);
        ally.setPos(Vec3.atBottomCenterOf(origin));
        level.addFreshEntity(ally);
        player.sendSystemMessage(Component.literal("§bUn « Toi du Futur » chromé au jetpack vient prêter main-forte."));
    }

    private static void boulangerie(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier == LuckTier.NEUTRAL) {
            for (int i = 0; i < 6; i++) {
                var chicken = EntitySpawnHelper.create(McRefs.CHICKEN, level);
                if (chicken != null) {
                    chicken.setPos(Vec3.atBottomCenterOf(origin).add(i * 0.3, 0, 0));
                    level.addFreshEntity(chicken);
                }
            }
            player.sendSystemMessage(Component.literal("§eLa basse-cour s'échappe de la boutique !"));
            return;
        }
        if (tier != LuckTier.LOW) {
            return;
        }
        var random = level.getRandom();
        for (int i = 0; i < 40; i++) {
            BlockPos p = origin.offset(random.nextInt(7) - 3, random.nextInt(4) + 2, random.nextInt(7) - 3);
            level.setBlockAndUpdate(p, Blocks.HAY_BLOCK.defaultBlockState());
        }
        player.sendSystemMessage(Component.literal("§cPluie de pain rassis vengeur !"));
    }

    private static void bsod(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 15, 4));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 0));
        player.sendSystemMessage(Component.literal("§1Alt+Tab — champ de vision réduit 15s."));
    }

    private static void volaille(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        for (int i = 0; i < 6; i++) {
            var chicken = EntitySpawnHelper.create(McRefs.CHICKEN, level);
            if (chicken != null) {
                chicken.setPos(Vec3.atBottomCenterOf(origin).add(i * 0.3, 0, 0));
                level.addFreshEntity(chicken);
            }
        }
        player.sendSystemMessage(Component.literal("§6Œuf d'or incubateur : poulets dorés."));
    }

    private static void impots(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.LOW) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 45, 3));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 45, 1));
        player.sendSystemMessage(Component.literal("§4Grève générale : outils inutilisables 45s."));
    }

    private static void streamer(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 30, 0));
        player.setCustomName(Component.literal("Joueur_***"));
        player.setCustomNameVisible(true);
        player.sendSystemMessage(Component.literal("§9Sponsorisé par NordVPN — pseudo masqué + invisibilité."));
    }

    private static void proprete(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 300, 0));
        player.sendSystemMessage(Component.literal("§fDouche désinfectante — immunité poison 5 min."));
    }

    private static void gravite(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.LOW) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 15, 0));
        player.hurt(player.level().damageSources().drown(), 4);
        player.sendSystemMessage(Component.literal("§7Pression atmosphérique nulle — asphyxie 15s."));
    }

    private static void fastFood(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        var mascotte = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.MASCOTTE_HAMBURGER.get(), level);
        if (mascotte != null) {
            mascotte.setPos(Vec3.atBottomCenterOf(origin).add(2, 0, 0));
            level.addFreshEntity(mascotte);
        }
        player.sendSystemMessage(Component.literal("§eMascotte encombrante devant la caméra…"));
    }

    private static void cauchemar(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 30, 1));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 30, 0));
        var reveil = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.REVEIL_CAUCHEMAR.get(), level);
        if (reveil != null) {
            reveil.setPos(Vec3.atBottomCenterOf(origin));
            level.addFreshEntity(reveil);
        }
        player.sendSystemMessage(Component.literal("§dRêve lucide… et un Réveil Cauchemardesque qui sonne !"));
    }

    private static void filmAction(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.LOW) {
            return;
        }
        var sniper = EntitySpawnHelper.create(McRefs.SKELETON, level);
        if (sniper != null) {
            sniper.setPos(player.getX() + 12, player.getY() + 2, player.getZ());
            sniper.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            level.addFreshEntity(sniper);
        }
        level.explode(player, player.getX(), player.getY(), player.getZ(), 2.0f, Level.ExplosionInteraction.TNT);
        player.sendSystemMessage(Component.literal("§cTir du méchant — sniper squelette !"));
    }

    private static void jdr(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        var mimic = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.MIMIQUE_D20.get(), level);
        if (mimic != null) {
            mimic.setPos(Vec3.atBottomCenterOf(origin));
            level.addFreshEntity(mimic);
        }
        player.sendSystemMessage(Component.literal("§5Un coffre-dé suspect apparaît… Perception : 12 ?"));
    }

    private static void penurie(ServerLevel level, ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        var dealer = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.GROSSISTE_CLANDESTIN.get(), level);
        if (dealer != null) {
            dealer.setPos(player.position());
            level.addFreshEntity(dealer);
        }
        player.sendSystemMessage(Component.literal("§7Grossiste clandestin : objet rare contre 8 émeraudes (clic droit)."));
    }

    private static void complot(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.LOW) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 600, 0));
        player.sendSystemMessage(Component.literal("§5Puce 5G — Glow permanent, phantoms attirés."));
    }

    private static void lagBug(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        shuffleHotbar(player);
        var hacker = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.HACKER_FANTOME.get(), level);
        if (hacker != null) {
            hacker.setPos(Vec3.atBottomCenterOf(origin).add(0, 1.0, 0));
            level.addFreshEntity(hacker);
        }
        player.sendSystemMessage(Component.literal("§cDesync inventaire — un Hacker Fantôme apparaît !"));
    }

    private static void disco(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        level.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(16), e -> e instanceof net.minecraft.world.entity.monster.Monster)
                .forEach(m -> m.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 10, 0)));
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlockAndUpdate(origin.offset(dx, 1, dz), Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        var dancer = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.DANSEUR_DISCO.get(), level);
        if (dancer != null) {
            dancer.setPos(Vec3.atBottomCenterOf(origin));
            level.addFreshEntity(dancer);
        }
        player.sendSystemMessage(Component.literal("§dPiste ouverte — le Danseur Disco prend le micro !"));
    }

    private static void plomberie(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.LOW) {
            return;
        }
        player.setDeltaMovement(0, 1.8, 0);
        level.setBlockAndUpdate(origin, Blocks.MUD.defaultBlockState());
        player.sendSystemMessage(Component.literal("§6Geyser de boue — tuyauterie bouchée !"));
    }

    private static void fortuneCookie(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        int code = player.getRandom().nextInt(9000) + 1000;
        PlayerQuestData.setFortuneCode(player, code, 5L * 20);
        player.sendSystemMessage(Component.literal("§eCode fortune : §l" + code + " §e(tapez /say " + code + " sous 5s pour +500 XP)."));
    }

    private static void camping(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        player.getInventory().add(new ItemStack(Items.SHULKER_BOX));
        player.sendSystemMessage(Component.literal("§2Sac à dos suréquipé — Shulker Box de survie."));
    }

    private static void gymBro(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.LOW) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 10, 255));
        var bro = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.BODYBUILDER.get(), level);
        if (bro != null) {
            bro.setPos(Vec3.atBottomCenterOf(origin));
            level.addFreshEntity(bro);
        }
        player.sendSystemMessage(Component.literal("§cCrampe inattendue — et un Bodybuilder veut te « coacher » !"));
    }

    private static void artModerne(ServerLevel level, ServerPlayer player, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        var statue = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.STATUE_VIVANTE.get(), level);
        if (statue != null) {
            statue.setPos(Vec3.atBottomCenterOf(origin).add(0, 0.5, 0));
            level.addFreshEntity(statue);
        }
        player.sendSystemMessage(Component.literal("§7Une sculpture géométrique… ne la quitte pas des yeux."));
    }

    private static void magie(ServerLevel level, BlockPos origin, LuckTier tier) {
        if (tier != LuckTier.HIGH) {
            return;
        }
        var rabbit = EntitySpawnHelper.create(com.jason54.unpredictableblocks.registry.ModEntities.LAPIN_EXTIRPATEUR.get(), level);
        if (rabbit != null) {
            rabbit.setPos(Vec3.atBottomCenterOf(origin).add(0, 2, 0));
            level.addFreshEntity(rabbit);
        }
    }

    private static void memes(ServerPlayer player, LuckTier tier) {
        if (tier != LuckTier.NEUTRAL) {
            return;
        }
        player.setDeltaMovement(player.getDeltaMovement().add(0, 0.8, 0));
        player.sendSystemMessage(Component.literal("§cEmotional Damage !"));
    }

    private static void shuffleHotbar(ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < 8; i++) {
            int j = player.getRandom().nextInt(9);
            ItemStack a = inv.getItem(i);
            ItemStack b = inv.getItem(j);
            inv.setItem(i, b);
            inv.setItem(j, a);
        }
    }

}
