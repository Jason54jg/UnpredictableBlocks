package com.jason54.unpredictableblocks.registry;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.jason54.unpredictableblocks.entity.LuckyRocketEntity;
import com.jason54.unpredictableblocks.entity.mob.BaristaEntity;
import com.jason54.unpredictableblocks.entity.mob.BodybuilderEntity;
import com.jason54.unpredictableblocks.entity.mob.BotSpeedrunnerEntity;
import com.jason54.unpredictableblocks.entity.mob.ChatInternetEntity;
import com.jason54.unpredictableblocks.entity.mob.CryptoBulleEntity;
import com.jason54.unpredictableblocks.entity.mob.ChefItalienEntity;
import com.jason54.unpredictableblocks.entity.mob.ComteDraculaEntity;
import com.jason54.unpredictableblocks.entity.mob.CoqGauloisEntity;
import com.jason54.unpredictableblocks.entity.mob.DanseurDiscoEntity;
import com.jason54.unpredictableblocks.entity.mob.DouanierEntity;
import com.jason54.unpredictableblocks.entity.mob.EcranBleuEntity;
import com.jason54.unpredictableblocks.entity.mob.GrossisteClandestinEntity;
import com.jason54.unpredictableblocks.entity.mob.HackerFantomeEntity;
import com.jason54.unpredictableblocks.entity.mob.LapinExtirpateurEntity;
import com.jason54.unpredictableblocks.entity.mob.ManifestantEntity;
import com.jason54.unpredictableblocks.entity.mob.MascotteHamburgerEntity;
import com.jason54.unpredictableblocks.entity.mob.MimiqueD20Entity;
import com.jason54.unpredictableblocks.entity.mob.NuageOrageEntity;
import com.jason54.unpredictableblocks.entity.mob.PirateTorrentEntity;
import com.jason54.unpredictableblocks.entity.mob.PopItGeantEntity;
import com.jason54.unpredictableblocks.entity.mob.ReveilCauchemarEntity;
import com.jason54.unpredictableblocks.entity.mob.BaguetteVivanteEntity;
import com.jason54.unpredictableblocks.entity.mob.DossierFiscalEntity;
import com.jason54.unpredictableblocks.entity.mob.AlerteDonEntity;
import com.jason54.unpredictableblocks.entity.mob.EpongeSavonEntity;
import com.jason54.unpredictableblocks.entity.mob.CasqueOrbitalEntity;
import com.jason54.unpredictableblocks.entity.mob.ClapCinemaEntity;
import com.jason54.unpredictableblocks.entity.mob.PyramideOeilEntity;
import com.jason54.unpredictableblocks.entity.mob.CleTuyauEntity;
import com.jason54.unpredictableblocks.entity.mob.BiscuitFortuneEntity;
import com.jason54.unpredictableblocks.entity.mob.TenteCampEntity;
import com.jason54.unpredictableblocks.entity.mob.CubeMemeEntity;
import com.jason54.unpredictableblocks.entity.mob.SliderMultiversEntity;
import com.jason54.unpredictableblocks.entity.mob.StatueVivanteEntity;
import com.jason54.unpredictableblocks.entity.mob.ToiDuFuturEntity;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(UnpredictableBlocks.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<LuckyRocketEntity>> LUCKY_ROCKET =
            ENTITIES.register("lucky_rocket", () -> EntityType.Builder
                    .<LuckyRocketEntity>of(LuckyRocketEntity::new, MobCategory.MISC)
                    .noSave()
                    .noLootTable()
                    .fireImmune()
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "lucky_rocket"))));

    // Lot 1 (silhouettes objets / creatures)
    public static final RegistrySupplier<EntityType<ChefItalienEntity>> CHEF_ITALIEN =
            mob(ChefItalienEntity.ID, ChefItalienEntity::new, MobCategory.MONSTER, 1.0F, 1.4F);
    public static final RegistrySupplier<EntityType<ComteDraculaEntity>> COMTE_DRACULA =
            mob(ComteDraculaEntity.ID, ComteDraculaEntity::new, MobCategory.MONSTER, 1.0F, 1.6F);
    public static final RegistrySupplier<EntityType<MascotteHamburgerEntity>> MASCOTTE_HAMBURGER =
            mob(MascotteHamburgerEntity.ID, MascotteHamburgerEntity::new, MobCategory.MONSTER, 1.0F, 1.4F);
    public static final RegistrySupplier<EntityType<ChatInternetEntity>> CHAT_INTERNET =
            mob(ChatInternetEntity.ID, ChatInternetEntity::new, MobCategory.CREATURE, 0.6F, 0.7F);
    public static final RegistrySupplier<EntityType<CoqGauloisEntity>> COQ_GAULOIS =
            mob(CoqGauloisEntity.ID, CoqGauloisEntity::new, MobCategory.CREATURE, 0.9F, 1.9F);
    public static final RegistrySupplier<EntityType<EcranBleuEntity>> ECRAN_BLEU =
            mob(EcranBleuEntity.ID, EcranBleuEntity::new, MobCategory.MONSTER, 1.1F, 0.9F);

    // Lot 2
    public static final RegistrySupplier<EntityType<ToiDuFuturEntity>> TOI_DU_FUTUR =
            mob(ToiDuFuturEntity.ID, ToiDuFuturEntity::new, MobCategory.CREATURE, 0.8F, 1.4F);
    public static final RegistrySupplier<EntityType<DouanierEntity>> DOUANIER =
            mob(DouanierEntity.ID, DouanierEntity::new, MobCategory.MISC, 1.4F, 1.5F);
    public static final RegistrySupplier<EntityType<LapinExtirpateurEntity>> LAPIN_EXTIRPATEUR =
            mob(LapinExtirpateurEntity.ID, LapinExtirpateurEntity::new, MobCategory.CREATURE, 0.6F, 0.9F);
    public static final RegistrySupplier<EntityType<GrossisteClandestinEntity>> GROSSISTE_CLANDESTIN =
            mob(GrossisteClandestinEntity.ID, GrossisteClandestinEntity::new, MobCategory.MISC, 0.9F, 1.5F);
    public static final RegistrySupplier<EntityType<StatueVivanteEntity>> STATUE_VIVANTE =
            mob(StatueVivanteEntity.ID, StatueVivanteEntity::new, MobCategory.MONSTER, 0.9F, 1.8F);
    public static final RegistrySupplier<EntityType<MimiqueD20Entity>> MIMIQUE_D20 =
            mob(MimiqueD20Entity.ID, MimiqueD20Entity::new, MobCategory.MONSTER, 0.9F, 0.9F);

    // Lot 3
    public static final RegistrySupplier<EntityType<BodybuilderEntity>> BODYBUILDER =
            mob(BodybuilderEntity.ID, BodybuilderEntity::new, MobCategory.MONSTER, 1.6F, 1.0F);
    public static final RegistrySupplier<EntityType<DanseurDiscoEntity>> DANSEUR_DISCO =
            mob(DanseurDiscoEntity.ID, DanseurDiscoEntity::new, MobCategory.MONSTER, 0.9F, 1.6F);
    public static final RegistrySupplier<EntityType<HackerFantomeEntity>> HACKER_FANTOME =
            mob(HackerFantomeEntity.ID, HackerFantomeEntity::new, MobCategory.MONSTER, 1.0F, 1.2F);
    public static final RegistrySupplier<EntityType<BotSpeedrunnerEntity>> BOT_SPEEDRUNNER =
            mob(BotSpeedrunnerEntity.ID, BotSpeedrunnerEntity::new, MobCategory.MONSTER, 0.9F, 1.2F);
    public static final RegistrySupplier<EntityType<BaristaEntity>> BARISTA =
            mob(BaristaEntity.ID, BaristaEntity::new, MobCategory.MISC, 0.9F, 1.3F);
    public static final RegistrySupplier<EntityType<PirateTorrentEntity>> PIRATE_TORRENT =
            mob(PirateTorrentEntity.ID, PirateTorrentEntity::new, MobCategory.MONSTER, 1.0F, 1.4F);

    // Lot 4 — silhouettes objets / créatures (pas de forme Steve)
    public static final RegistrySupplier<EntityType<ManifestantEntity>> MANIFESTANT =
            mob(ManifestantEntity.ID, ManifestantEntity::new, MobCategory.MONSTER, 0.8F, 1.6F);
    public static final RegistrySupplier<EntityType<CryptoBulleEntity>> CRYPTO_BULLE =
            mob(CryptoBulleEntity.ID, CryptoBulleEntity::new, MobCategory.MONSTER, 1.0F, 1.2F);
    public static final RegistrySupplier<EntityType<NuageOrageEntity>> NUAGE_ORAGE =
            mob(NuageOrageEntity.ID, NuageOrageEntity::new, MobCategory.MONSTER, 1.4F, 1.2F);
    public static final RegistrySupplier<EntityType<SliderMultiversEntity>> SLIDER_MULTIVERS =
            mob(SliderMultiversEntity.ID, SliderMultiversEntity::new, MobCategory.MONSTER, 1.6F, 1.0F);
    public static final RegistrySupplier<EntityType<PopItGeantEntity>> POP_IT_GEANT =
            mob(PopItGeantEntity.ID, PopItGeantEntity::new, MobCategory.MONSTER, 1.4F, 0.6F);
    public static final RegistrySupplier<EntityType<ReveilCauchemarEntity>> REVEIL_CAUCHEMAR =
            mob(ReveilCauchemarEntity.ID, ReveilCauchemarEntity::new, MobCategory.MONSTER, 0.9F, 1.1F);

    // Lot 5 — themes restants (objets)
    public static final RegistrySupplier<EntityType<BaguetteVivanteEntity>> BAGUETTE_VIVANTE =
            mob(BaguetteVivanteEntity.ID, BaguetteVivanteEntity::new, MobCategory.MONSTER, 1.6F, 0.8F);
    public static final RegistrySupplier<EntityType<DossierFiscalEntity>> DOSSIER_FISCAL =
            mob(DossierFiscalEntity.ID, DossierFiscalEntity::new, MobCategory.MONSTER, 1.0F, 1.4F);
    public static final RegistrySupplier<EntityType<AlerteDonEntity>> ALERTE_DON =
            mob(AlerteDonEntity.ID, AlerteDonEntity::new, MobCategory.MONSTER, 1.0F, 1.3F);
    public static final RegistrySupplier<EntityType<EpongeSavonEntity>> EPONGE_SAVON =
            mob(EpongeSavonEntity.ID, EpongeSavonEntity::new, MobCategory.MONSTER, 1.2F, 1.0F);
    public static final RegistrySupplier<EntityType<CasqueOrbitalEntity>> CASQUE_ORBITAL =
            mob(CasqueOrbitalEntity.ID, CasqueOrbitalEntity::new, MobCategory.MONSTER, 1.0F, 1.4F);
    public static final RegistrySupplier<EntityType<ClapCinemaEntity>> CLAP_CINEMA =
            mob(ClapCinemaEntity.ID, ClapCinemaEntity::new, MobCategory.MONSTER, 1.2F, 1.2F);

    // Lot 6 — derniers themes
    public static final RegistrySupplier<EntityType<PyramideOeilEntity>> PYRAMIDE_OEIL =
            mob(PyramideOeilEntity.ID, PyramideOeilEntity::new, MobCategory.MONSTER, 1.2F, 1.4F);
    public static final RegistrySupplier<EntityType<CleTuyauEntity>> CLE_TUYAU =
            mob(CleTuyauEntity.ID, CleTuyauEntity::new, MobCategory.MONSTER, 1.4F, 1.0F);
    public static final RegistrySupplier<EntityType<BiscuitFortuneEntity>> BISCUIT_FORTUNE =
            mob(BiscuitFortuneEntity.ID, BiscuitFortuneEntity::new, MobCategory.MONSTER, 0.9F, 0.8F);
    public static final RegistrySupplier<EntityType<TenteCampEntity>> TENTE_CAMP =
            mob(TenteCampEntity.ID, TenteCampEntity::new, MobCategory.MONSTER, 1.4F, 1.4F);
    public static final RegistrySupplier<EntityType<CubeMemeEntity>> CUBE_MEME =
            mob(CubeMemeEntity.ID, CubeMemeEntity::new, MobCategory.MONSTER, 1.1F, 1.1F);

    private ModEntities() {}

    private static <T extends Entity> RegistrySupplier<EntityType<T>> mob(
            String id, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
        return ENTITIES.register(id, () -> EntityType.Builder.of(factory, category)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, id))));
    }

    public static void register() {
        ENTITIES.register();
        EntityAttributeRegistry.register(CHEF_ITALIEN, ChefItalienEntity::createAttributes);
        EntityAttributeRegistry.register(COMTE_DRACULA, ComteDraculaEntity::createAttributes);
        EntityAttributeRegistry.register(MASCOTTE_HAMBURGER, MascotteHamburgerEntity::createAttributes);
        EntityAttributeRegistry.register(CHAT_INTERNET, ChatInternetEntity::createAttributes);
        EntityAttributeRegistry.register(COQ_GAULOIS, CoqGauloisEntity::createAttributes);
        EntityAttributeRegistry.register(ECRAN_BLEU, EcranBleuEntity::createAttributes);
        EntityAttributeRegistry.register(TOI_DU_FUTUR, ToiDuFuturEntity::createAttributes);
        EntityAttributeRegistry.register(DOUANIER, DouanierEntity::createAttributes);
        EntityAttributeRegistry.register(LAPIN_EXTIRPATEUR, LapinExtirpateurEntity::createAttributes);
        EntityAttributeRegistry.register(GROSSISTE_CLANDESTIN, GrossisteClandestinEntity::createAttributes);
        EntityAttributeRegistry.register(STATUE_VIVANTE, StatueVivanteEntity::createAttributes);
        EntityAttributeRegistry.register(MIMIQUE_D20, MimiqueD20Entity::createAttributes);
        EntityAttributeRegistry.register(BODYBUILDER, BodybuilderEntity::createAttributes);
        EntityAttributeRegistry.register(DANSEUR_DISCO, DanseurDiscoEntity::createAttributes);
        EntityAttributeRegistry.register(HACKER_FANTOME, HackerFantomeEntity::createAttributes);
        EntityAttributeRegistry.register(BOT_SPEEDRUNNER, BotSpeedrunnerEntity::createAttributes);
        EntityAttributeRegistry.register(BARISTA, BaristaEntity::createAttributes);
        EntityAttributeRegistry.register(PIRATE_TORRENT, PirateTorrentEntity::createAttributes);
        EntityAttributeRegistry.register(MANIFESTANT, ManifestantEntity::createAttributes);
        EntityAttributeRegistry.register(CRYPTO_BULLE, CryptoBulleEntity::createAttributes);
        EntityAttributeRegistry.register(NUAGE_ORAGE, NuageOrageEntity::createAttributes);
        EntityAttributeRegistry.register(SLIDER_MULTIVERS, SliderMultiversEntity::createAttributes);
        EntityAttributeRegistry.register(POP_IT_GEANT, PopItGeantEntity::createAttributes);
        EntityAttributeRegistry.register(REVEIL_CAUCHEMAR, ReveilCauchemarEntity::createAttributes);
        EntityAttributeRegistry.register(BAGUETTE_VIVANTE, BaguetteVivanteEntity::createAttributes);
        EntityAttributeRegistry.register(DOSSIER_FISCAL, DossierFiscalEntity::createAttributes);
        EntityAttributeRegistry.register(ALERTE_DON, AlerteDonEntity::createAttributes);
        EntityAttributeRegistry.register(EPONGE_SAVON, EpongeSavonEntity::createAttributes);
        EntityAttributeRegistry.register(CASQUE_ORBITAL, CasqueOrbitalEntity::createAttributes);
        EntityAttributeRegistry.register(CLAP_CINEMA, ClapCinemaEntity::createAttributes);
        EntityAttributeRegistry.register(PYRAMIDE_OEIL, PyramideOeilEntity::createAttributes);
        EntityAttributeRegistry.register(CLE_TUYAU, CleTuyauEntity::createAttributes);
        EntityAttributeRegistry.register(BISCUIT_FORTUNE, BiscuitFortuneEntity::createAttributes);
        EntityAttributeRegistry.register(TENTE_CAMP, TenteCampEntity::createAttributes);
        EntityAttributeRegistry.register(CUBE_MEME, CubeMemeEntity::createAttributes);
    }
}
