package net.minecraft.util.datafix;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFixerBuilder.Result;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.fixes.AbstractArrowPickupFix;
import net.minecraft.util.datafix.fixes.AddFieldFix;
import net.minecraft.util.datafix.fixes.AddFlagIfNotPresentFix;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.AdvancementsFix;
import net.minecraft.util.datafix.fixes.AdvancementsRenameFix;
import net.minecraft.util.datafix.fixes.AreaEffectCloudDurationScaleFix;
import net.minecraft.util.datafix.fixes.AreaEffectCloudPotionFix;
import net.minecraft.util.datafix.fixes.AttributeIdPrefixFix;
import net.minecraft.util.datafix.fixes.AttributeModifierIdFix;
import net.minecraft.util.datafix.fixes.AttributesRenameLegacy;
import net.minecraft.util.datafix.fixes.BannerEntityCustomNameToOverrideComponentFix;
import net.minecraft.util.datafix.fixes.BannerPatternFormatFix;
import net.minecraft.util.datafix.fixes.BedItemColorFix;
import net.minecraft.util.datafix.fixes.BeehiveFieldRenameFix;
import net.minecraft.util.datafix.fixes.BiomeFix;
import net.minecraft.util.datafix.fixes.BitStorageAlignFix;
import net.minecraft.util.datafix.fixes.BlendingDataFix;
import net.minecraft.util.datafix.fixes.BlendingDataRemoveFromNetherEndFix;
import net.minecraft.util.datafix.fixes.BlockEntityBannerColorFix;
import net.minecraft.util.datafix.fixes.BlockEntityBlockStateFix;
import net.minecraft.util.datafix.fixes.BlockEntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.BlockEntityFurnaceBurnTimeFix;
import net.minecraft.util.datafix.fixes.BlockEntityIdFix;
import net.minecraft.util.datafix.fixes.BlockEntityJukeboxFix;
import net.minecraft.util.datafix.fixes.BlockEntityKeepPacked;
import net.minecraft.util.datafix.fixes.BlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.BlockEntityShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.BlockEntitySignDoubleSidedEditableTextFix;
import net.minecraft.util.datafix.fixes.BlockEntityUUIDFix;
import net.minecraft.util.datafix.fixes.BlockNameFlatteningFix;
import net.minecraft.util.datafix.fixes.BlockPosFormatAndRenamesFix;
import net.minecraft.util.datafix.fixes.BlockPropertyRenameAndFix;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.BlockStateStructureTemplateFix;
import net.minecraft.util.datafix.fixes.BoatSplitFix;
import net.minecraft.util.datafix.fixes.CarvingStepRemoveFix;
import net.minecraft.util.datafix.fixes.CatTypeFix;
import net.minecraft.util.datafix.fixes.CauldronRenameFix;
import net.minecraft.util.datafix.fixes.CavesAndCliffsRenames;
import net.minecraft.util.datafix.fixes.ChestedHorsesInventoryZeroIndexingFix;
import net.minecraft.util.datafix.fixes.ChunkBedBlockEntityInjecterFix;
import net.minecraft.util.datafix.fixes.ChunkBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkDeleteIgnoredLightDataFix;
import net.minecraft.util.datafix.fixes.ChunkDeleteLightFix;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkLightRemoveFix;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.util.datafix.fixes.ChunkProtoTickListFix;
import net.minecraft.util.datafix.fixes.ChunkRenamesFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix2;
import net.minecraft.util.datafix.fixes.ChunkStructuresTemplateRenameFix;
import net.minecraft.util.datafix.fixes.ChunkTicketUnpackPosFix;
import net.minecraft.util.datafix.fixes.ChunkToProtochunkFix;
import net.minecraft.util.datafix.fixes.ColorlessShulkerEntityFix;
import net.minecraft.util.datafix.fixes.ContainerBlockEntityLockPredicateFix;
import net.minecraft.util.datafix.fixes.CopperGolemWeatherStateFix;
import net.minecraft.util.datafix.fixes.CriteriaRenameFix;
import net.minecraft.util.datafix.fixes.CustomModelDataExpandFix;
import net.minecraft.util.datafix.fixes.DebugProfileOverlayReferenceFix;
import net.minecraft.util.datafix.fixes.DecoratedPotFieldRenameFix;
import net.minecraft.util.datafix.fixes.DropChancesFormatFix;
import net.minecraft.util.datafix.fixes.DropInvalidSignDataFix;
import net.minecraft.util.datafix.fixes.DyeItemRenameFix;
import net.minecraft.util.datafix.fixes.EffectDurationFix;
import net.minecraft.util.datafix.fixes.EmptyItemInHotbarFix;
import net.minecraft.util.datafix.fixes.EmptyItemInVillagerTradeFix;
import net.minecraft.util.datafix.fixes.EntityArmorStandSilentFix;
import net.minecraft.util.datafix.fixes.EntityAttributeBaseValueFix;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.EntityBrushableBlockFieldsRenameFix;
import net.minecraft.util.datafix.fixes.EntityCatSplitFix;
import net.minecraft.util.datafix.fixes.EntityCodSalmonFix;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.EntityElderGuardianSplitFix;
import net.minecraft.util.datafix.fixes.EntityEquipmentToArmorAndHandFix;
import net.minecraft.util.datafix.fixes.EntityFallDistanceFloatToDoubleFix;
import net.minecraft.util.datafix.fixes.EntityFieldsRenameFix;
import net.minecraft.util.datafix.fixes.EntityGoatMissingStateFix;
import net.minecraft.util.datafix.fixes.EntityHealthFix;
import net.minecraft.util.datafix.fixes.EntityHorseSaddleFix;
import net.minecraft.util.datafix.fixes.EntityHorseSplitFix;
import net.minecraft.util.datafix.fixes.EntityIdFix;
import net.minecraft.util.datafix.fixes.EntityItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityMinecartIdentifiersFix;
import net.minecraft.util.datafix.fixes.EntityPaintingItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityPaintingMotiveFix;
import net.minecraft.util.datafix.fixes.EntityProjectileOwnerFix;
import net.minecraft.util.datafix.fixes.EntityPufferfishRenameFix;
import net.minecraft.util.datafix.fixes.EntityRavagerRenameFix;
import net.minecraft.util.datafix.fixes.EntityRedundantChanceTagsFix;
import net.minecraft.util.datafix.fixes.EntityRidingToPassengersFix;
import net.minecraft.util.datafix.fixes.EntitySalmonSizeFix;
import net.minecraft.util.datafix.fixes.EntityShulkerColorFix;
import net.minecraft.util.datafix.fixes.EntityShulkerRotationFix;
import net.minecraft.util.datafix.fixes.EntitySkeletonSplitFix;
import net.minecraft.util.datafix.fixes.EntitySpawnerItemVariantComponentFix;
import net.minecraft.util.datafix.fixes.EntityStringUuidFix;
import net.minecraft.util.datafix.fixes.EntityTheRenameningFix;
import net.minecraft.util.datafix.fixes.EntityTippedArrowFix;
import net.minecraft.util.datafix.fixes.EntityUUIDFix;
import net.minecraft.util.datafix.fixes.EntityVariantFix;
import net.minecraft.util.datafix.fixes.EntityWolfColorFix;
import net.minecraft.util.datafix.fixes.EntityZombieSplitFix;
import net.minecraft.util.datafix.fixes.EntityZombieVillagerTypeFix;
import net.minecraft.util.datafix.fixes.EntityZombifiedPiglinRenameFix;
import net.minecraft.util.datafix.fixes.EquipmentFormatFix;
import net.minecraft.util.datafix.fixes.EquippableAssetRenameFix;
import net.minecraft.util.datafix.fixes.FeatureFlagRemoveFix;
import net.minecraft.util.datafix.fixes.FilteredBooksFix;
import net.minecraft.util.datafix.fixes.FilteredSignsFix;
import net.minecraft.util.datafix.fixes.FireResistantToDamageResistantComponentFix;
import net.minecraft.util.datafix.fixes.FixProjectileStoredItem;
import net.minecraft.util.datafix.fixes.FixWolfHealth;
import net.minecraft.util.datafix.fixes.FoodToConsumableFix;
import net.minecraft.util.datafix.fixes.ForcePoiRebuild;
import net.minecraft.util.datafix.fixes.ForcedChunkToTicketFix;
import net.minecraft.util.datafix.fixes.FurnaceRecipeFix;
import net.minecraft.util.datafix.fixes.GameRuleRegistryFix;
import net.minecraft.util.datafix.fixes.GoatHornIdFix;
import net.minecraft.util.datafix.fixes.GossipUUIDFix;
import net.minecraft.util.datafix.fixes.HeightmapRenamingFix;
import net.minecraft.util.datafix.fixes.HorseBodyArmorItemFix;
import net.minecraft.util.datafix.fixes.IglooMetadataRemovalFix;
import net.minecraft.util.datafix.fixes.InlineBlockPosFormatFix;
import net.minecraft.util.datafix.fixes.InvalidBlockEntityLockFix;
import net.minecraft.util.datafix.fixes.InvalidLockComponentFix;
import net.minecraft.util.datafix.fixes.ItemBannerColorFix;
import net.minecraft.util.datafix.fixes.ItemCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemLoreFix;
import net.minecraft.util.datafix.fixes.ItemPotionFix;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.ItemShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.ItemSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.ItemStackCustomNameToOverrideComponentFix;
import net.minecraft.util.datafix.fixes.ItemStackEnchantmentNamesFix;
import net.minecraft.util.datafix.fixes.ItemStackMapIdFix;
import net.minecraft.util.datafix.fixes.ItemStackSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.util.datafix.fixes.ItemStackUUIDFix;
import net.minecraft.util.datafix.fixes.ItemWaterPotionFix;
import net.minecraft.util.datafix.fixes.JigsawPropertiesFix;
import net.minecraft.util.datafix.fixes.JigsawRotationFix;
import net.minecraft.util.datafix.fixes.JukeboxTicksSinceSongStartedFix;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.util.datafix.fixes.LegacyDimensionIdFix;
import net.minecraft.util.datafix.fixes.LegacyDragonFightFix;
import net.minecraft.util.datafix.fixes.LegacyHoverEventFix;
import net.minecraft.util.datafix.fixes.LegacyWorldBorderFix;
import net.minecraft.util.datafix.fixes.LevelDataGeneratorOptionsFix;
import net.minecraft.util.datafix.fixes.LevelFlatGeneratorInfoFix;
import net.minecraft.util.datafix.fixes.LevelLegacyWorldGenSettingsFix;
import net.minecraft.util.datafix.fixes.LevelUUIDFix;
import net.minecraft.util.datafix.fixes.LockComponentPredicateFix;
import net.minecraft.util.datafix.fixes.LodestoneCompassComponentFix;
import net.minecraft.util.datafix.fixes.MapBannerBlockPosFormatFix;
import net.minecraft.util.datafix.fixes.MapIdFix;
import net.minecraft.util.datafix.fixes.MemoryExpiryDataFix;
import net.minecraft.util.datafix.fixes.MissingDimensionFix;
import net.minecraft.util.datafix.fixes.MobEffectIdFix;
import net.minecraft.util.datafix.fixes.MobSpawnerEntityIdentifiersFix;
import net.minecraft.util.datafix.fixes.NamedEntityConvertUncheckedFix;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.NamespacedTypeRenameFix;
import net.minecraft.util.datafix.fixes.NewVillageFix;
import net.minecraft.util.datafix.fixes.ObjectiveRenderTypeFix;
import net.minecraft.util.datafix.fixes.OminousBannerBlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.OminousBannerRarityFix;
import net.minecraft.util.datafix.fixes.OminousBannerRenameFix;
import net.minecraft.util.datafix.fixes.OptionsAccessibilityOnboardFix;
import net.minecraft.util.datafix.fixes.OptionsAddTextBackgroundFix;
import net.minecraft.util.datafix.fixes.OptionsAmbientOcclusionFix;
import net.minecraft.util.datafix.fixes.OptionsFancyGraphicsToGraphicsModeFix;
import net.minecraft.util.datafix.fixes.OptionsForceVBOFix;
import net.minecraft.util.datafix.fixes.OptionsGraphicsModeSplitFix;
import net.minecraft.util.datafix.fixes.OptionsKeyLwjgl3Fix;
import net.minecraft.util.datafix.fixes.OptionsKeyTranslationFix;
import net.minecraft.util.datafix.fixes.OptionsLowerCaseLanguageFix;
import net.minecraft.util.datafix.fixes.OptionsMenuBlurrinessFix;
import net.minecraft.util.datafix.fixes.OptionsMusicToastFix;
import net.minecraft.util.datafix.fixes.OptionsProgrammerArtFix;
import net.minecraft.util.datafix.fixes.OptionsRenameFieldFix;
import net.minecraft.util.datafix.fixes.OptionsSetGraphicsPresetToCustomFix;
import net.minecraft.util.datafix.fixes.OverreachingTickFix;
import net.minecraft.util.datafix.fixes.ParticleUnflatteningFix;
import net.minecraft.util.datafix.fixes.PlayerEquipmentFix;
import net.minecraft.util.datafix.fixes.PlayerHeadBlockProfileFix;
import net.minecraft.util.datafix.fixes.PlayerRespawnDataFix;
import net.minecraft.util.datafix.fixes.PlayerUUIDFix;
import net.minecraft.util.datafix.fixes.PoiTypeRemoveFix;
import net.minecraft.util.datafix.fixes.PoiTypeRenameFix;
import net.minecraft.util.datafix.fixes.PrimedTntBlockStateFixer;
import net.minecraft.util.datafix.fixes.ProjectileStoredWeaponFix;
import net.minecraft.util.datafix.fixes.RaidRenamesDataFix;
import net.minecraft.util.datafix.fixes.RandomSequenceSettingsFix;
import net.minecraft.util.datafix.fixes.RecipesFix;
import net.minecraft.util.datafix.fixes.RecipesRenameningFix;
import net.minecraft.util.datafix.fixes.RedstoneWireConnectionsFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.RemapChunkStatusFix;
import net.minecraft.util.datafix.fixes.RemoveBlockEntityTagFix;
import net.minecraft.util.datafix.fixes.RemoveEmptyItemInBrushableBlockFix;
import net.minecraft.util.datafix.fixes.RemoveGolemGossipFix;
import net.minecraft.util.datafix.fixes.RenameEnchantmentsFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFansFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFix;
import net.minecraft.util.datafix.fixes.ReorganizePoi;
import net.minecraft.util.datafix.fixes.SaddleEquipmentSlotFix;
import net.minecraft.util.datafix.fixes.SavedDataFeaturePoolElementFix;
import net.minecraft.util.datafix.fixes.SavedDataUUIDFix;
import net.minecraft.util.datafix.fixes.ScoreboardDisplayNameFix;
import net.minecraft.util.datafix.fixes.ScoreboardDisplaySlotFix;
import net.minecraft.util.datafix.fixes.SignTextStrictJsonFix;
import net.minecraft.util.datafix.fixes.SpawnerDataFix;
import net.minecraft.util.datafix.fixes.StatsCounterFix;
import net.minecraft.util.datafix.fixes.StatsRenameFix;
import net.minecraft.util.datafix.fixes.StriderGravityFix;
import net.minecraft.util.datafix.fixes.StructureReferenceCountFix;
import net.minecraft.util.datafix.fixes.StructureSettingsFlattenFix;
import net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix;
import net.minecraft.util.datafix.fixes.TextComponentHoverAndClickEventFix;
import net.minecraft.util.datafix.fixes.TextComponentStringifiedFlagsFix;
import net.minecraft.util.datafix.fixes.ThrownPotionSplitFix;
import net.minecraft.util.datafix.fixes.TippedArrowPotionToItemFix;
import net.minecraft.util.datafix.fixes.TooltipDisplayComponentFix;
import net.minecraft.util.datafix.fixes.TrappedChestBlockEntityFix;
import net.minecraft.util.datafix.fixes.TrialSpawnerConfigFix;
import net.minecraft.util.datafix.fixes.TrialSpawnerConfigInRegistryFix;
import net.minecraft.util.datafix.fixes.TridentAnimationFix;
import net.minecraft.util.datafix.fixes.UnflattenTextComponentFix;
import net.minecraft.util.datafix.fixes.VariantRenameFix;
import net.minecraft.util.datafix.fixes.VillagerDataFix;
import net.minecraft.util.datafix.fixes.VillagerFollowRangeFix;
import net.minecraft.util.datafix.fixes.VillagerRebuildLevelAndXpFix;
import net.minecraft.util.datafix.fixes.VillagerSetCanPickUpLootFix;
import net.minecraft.util.datafix.fixes.VillagerTradeFix;
import net.minecraft.util.datafix.fixes.WallPropertyFix;
import net.minecraft.util.datafix.fixes.WeaponSmithChestLootTableFix;
import net.minecraft.util.datafix.fixes.WorldBorderWarningTimeFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsDisallowOldCustomWorldsFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.WorldSpawnDataFix;
import net.minecraft.util.datafix.fixes.WriteAndReadFix;
import net.minecraft.util.datafix.fixes.WrittenBookPagesStrictJsonFix;
import net.minecraft.util.datafix.fixes.ZombieVillagerRebuildXpFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;
import net.minecraft.util.datafix.schemas.V102;
import net.minecraft.util.datafix.schemas.V1022;
import net.minecraft.util.datafix.schemas.V106;
import net.minecraft.util.datafix.schemas.V107;
import net.minecraft.util.datafix.schemas.V1125;
import net.minecraft.util.datafix.schemas.V135;
import net.minecraft.util.datafix.schemas.V143;
import net.minecraft.util.datafix.schemas.V1451;
import net.minecraft.util.datafix.schemas.V1451_1;
import net.minecraft.util.datafix.schemas.V1451_2;
import net.minecraft.util.datafix.schemas.V1451_3;
import net.minecraft.util.datafix.schemas.V1451_4;
import net.minecraft.util.datafix.schemas.V1451_5;
import net.minecraft.util.datafix.schemas.V1451_6;
import net.minecraft.util.datafix.schemas.V1458;
import net.minecraft.util.datafix.schemas.V1460;
import net.minecraft.util.datafix.schemas.V1466;
import net.minecraft.util.datafix.schemas.V1470;
import net.minecraft.util.datafix.schemas.V1481;
import net.minecraft.util.datafix.schemas.V1483;
import net.minecraft.util.datafix.schemas.V1486;
import net.minecraft.util.datafix.schemas.V1488;
import net.minecraft.util.datafix.schemas.V1510;
import net.minecraft.util.datafix.schemas.V1800;
import net.minecraft.util.datafix.schemas.V1801;
import net.minecraft.util.datafix.schemas.V1904;
import net.minecraft.util.datafix.schemas.V1906;
import net.minecraft.util.datafix.schemas.V1909;
import net.minecraft.util.datafix.schemas.V1920;
import net.minecraft.util.datafix.schemas.V1928;
import net.minecraft.util.datafix.schemas.V1929;
import net.minecraft.util.datafix.schemas.V1931;
import net.minecraft.util.datafix.schemas.V2100;
import net.minecraft.util.datafix.schemas.V2501;
import net.minecraft.util.datafix.schemas.V2502;
import net.minecraft.util.datafix.schemas.V2505;
import net.minecraft.util.datafix.schemas.V2509;
import net.minecraft.util.datafix.schemas.V2511_1;
import net.minecraft.util.datafix.schemas.V2519;
import net.minecraft.util.datafix.schemas.V2522;
import net.minecraft.util.datafix.schemas.V2551;
import net.minecraft.util.datafix.schemas.V2568;
import net.minecraft.util.datafix.schemas.V2571;
import net.minecraft.util.datafix.schemas.V2684;
import net.minecraft.util.datafix.schemas.V2686;
import net.minecraft.util.datafix.schemas.V2688;
import net.minecraft.util.datafix.schemas.V2704;
import net.minecraft.util.datafix.schemas.V2707;
import net.minecraft.util.datafix.schemas.V2831;
import net.minecraft.util.datafix.schemas.V2832;
import net.minecraft.util.datafix.schemas.V2842;
import net.minecraft.util.datafix.schemas.V3076;
import net.minecraft.util.datafix.schemas.V3078;
import net.minecraft.util.datafix.schemas.V3081;
import net.minecraft.util.datafix.schemas.V3082;
import net.minecraft.util.datafix.schemas.V3083;
import net.minecraft.util.datafix.schemas.V3202;
import net.minecraft.util.datafix.schemas.V3203;
import net.minecraft.util.datafix.schemas.V3204;
import net.minecraft.util.datafix.schemas.V3325;
import net.minecraft.util.datafix.schemas.V3326;
import net.minecraft.util.datafix.schemas.V3327;
import net.minecraft.util.datafix.schemas.V3328;
import net.minecraft.util.datafix.schemas.V3438;
import net.minecraft.util.datafix.schemas.V3439;
import net.minecraft.util.datafix.schemas.V3439_1;
import net.minecraft.util.datafix.schemas.V3448;
import net.minecraft.util.datafix.schemas.V3682;
import net.minecraft.util.datafix.schemas.V3683;
import net.minecraft.util.datafix.schemas.V3685;
import net.minecraft.util.datafix.schemas.V3689;
import net.minecraft.util.datafix.schemas.V3799;
import net.minecraft.util.datafix.schemas.V3807;
import net.minecraft.util.datafix.schemas.V3808;
import net.minecraft.util.datafix.schemas.V3808_1;
import net.minecraft.util.datafix.schemas.V3808_2;
import net.minecraft.util.datafix.schemas.V3813;
import net.minecraft.util.datafix.schemas.V3816;
import net.minecraft.util.datafix.schemas.V3818;
import net.minecraft.util.datafix.schemas.V3818_3;
import net.minecraft.util.datafix.schemas.V3818_4;
import net.minecraft.util.datafix.schemas.V3818_5;
import net.minecraft.util.datafix.schemas.V3825;
import net.minecraft.util.datafix.schemas.V3938;
import net.minecraft.util.datafix.schemas.V4059;
import net.minecraft.util.datafix.schemas.V4067;
import net.minecraft.util.datafix.schemas.V4070;
import net.minecraft.util.datafix.schemas.V4071;
import net.minecraft.util.datafix.schemas.V4290;
import net.minecraft.util.datafix.schemas.V4292;
import net.minecraft.util.datafix.schemas.V4300;
import net.minecraft.util.datafix.schemas.V4301;
import net.minecraft.util.datafix.schemas.V4302;
import net.minecraft.util.datafix.schemas.V4306;
import net.minecraft.util.datafix.schemas.V4307;
import net.minecraft.util.datafix.schemas.V4312;
import net.minecraft.util.datafix.schemas.V4420;
import net.minecraft.util.datafix.schemas.V4421;
import net.minecraft.util.datafix.schemas.V4531;
import net.minecraft.util.datafix.schemas.V4532;
import net.minecraft.util.datafix.schemas.V4533;
import net.minecraft.util.datafix.schemas.V4543;
import net.minecraft.util.datafix.schemas.V4648;
import net.minecraft.util.datafix.schemas.V4656;
import net.minecraft.util.datafix.schemas.V501;
import net.minecraft.util.datafix.schemas.V700;
import net.minecraft.util.datafix.schemas.V701;
import net.minecraft.util.datafix.schemas.V702;
import net.minecraft.util.datafix.schemas.V703;
import net.minecraft.util.datafix.schemas.V704;
import net.minecraft.util.datafix.schemas.V705;
import net.minecraft.util.datafix.schemas.V808;
import net.minecraft.util.datafix.schemas.V99;

public class DataFixers {
   private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
   private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;
   private static final Result DATA_FIXER = createFixerUpper();
   public static final int BLENDING_VERSION = 4295;

   private DataFixers() {
   }

   public static DataFixer getDataFixer() {
      return DATA_FIXER.fixer();
   }

   private static Result createFixerUpper() {
      DataFixerBuilder $$0 = new DataFixerBuilder(SharedConstants.getCurrentVersion().dataVersion().version());
      addFixers($$0);
      return $$0.build();
   }

   public static CompletableFuture<?> optimize(Set<TypeReference> $$0) {
      if ($$0.isEmpty()) {
         return CompletableFuture.completedFuture(null);
      } else {
         Executor $$1 = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("Datafixer Bootstrap").setDaemon(true).setPriority(1).build()
         );
         return DATA_FIXER.optimize($$0, $$1);
      }
   }

   private static void addFixers(DataFixerBuilder $$0) {
      $$0.addSchema(99, V99::new);
      Schema $$1 = $$0.addSchema(100, V100::new);
      $$0.addFixer(new EntityEquipmentToArmorAndHandFix($$1));
      Schema $$2 = $$0.addSchema(101, SAME);
      $$0.addFixer(new VillagerSetCanPickUpLootFix($$2));
      Schema $$3 = $$0.addSchema(102, V102::new);
      $$0.addFixer(new ItemIdFix($$3, true));
      $$0.addFixer(new ItemPotionFix($$3, false));
      Schema $$4 = $$0.addSchema(105, SAME);
      $$0.addFixer(new ItemSpawnEggFix($$4, true));
      Schema $$5 = $$0.addSchema(106, V106::new);
      $$0.addFixer(new MobSpawnerEntityIdentifiersFix($$5, true));
      Schema $$6 = $$0.addSchema(107, V107::new);
      $$0.addFixer(new EntityMinecartIdentifiersFix($$6));
      Schema $$7 = $$0.addSchema(108, SAME);
      $$0.addFixer(new EntityStringUuidFix($$7, true));
      Schema $$8 = $$0.addSchema(109, SAME);
      $$0.addFixer(new EntityHealthFix($$8, true));
      Schema $$9 = $$0.addSchema(110, SAME);
      $$0.addFixer(new EntityHorseSaddleFix($$9, true));
      Schema $$10 = $$0.addSchema(111, SAME);
      $$0.addFixer(new EntityPaintingItemFrameDirectionFix($$10, true));
      Schema $$11 = $$0.addSchema(113, SAME);
      $$0.addFixer(new EntityRedundantChanceTagsFix($$11, true));
      Schema $$12 = $$0.addSchema(135, V135::new);
      $$0.addFixer(new EntityRidingToPassengersFix($$12, true));
      Schema $$13 = $$0.addSchema(143, V143::new);
      $$0.addFixer(new EntityTippedArrowFix($$13, true));
      Schema $$14 = $$0.addSchema(147, SAME);
      $$0.addFixer(new EntityArmorStandSilentFix($$14, true));
      Schema $$15 = $$0.addSchema(165, SAME);
      $$0.addFixer(new SignTextStrictJsonFix($$15));
      $$0.addFixer(new WrittenBookPagesStrictJsonFix($$15));
      Schema $$16 = $$0.addSchema(501, V501::new);
      $$0.addFixer(new AddNewChoices($$16, "Add 1.10 entities fix", References.ENTITY));
      Schema $$17 = $$0.addSchema(502, SAME);
      $$0.addFixer(
         ItemRenameFix.create(
            $$17,
            "cooked_fished item renamer",
            $$0x -> Objects.equals(NamespacedSchema.ensureNamespaced($$0x), "minecraft:cooked_fished") ? "minecraft:cooked_fish" : $$0x
         )
      );
      $$0.addFixer(new EntityZombieVillagerTypeFix($$17, false));
      Schema $$18 = $$0.addSchema(505, SAME);
      $$0.addFixer(new OptionsForceVBOFix($$18, false));
      Schema $$19 = $$0.addSchema(700, V700::new);
      $$0.addFixer(new EntityElderGuardianSplitFix($$19, true));
      Schema $$20 = $$0.addSchema(701, V701::new);
      $$0.addFixer(new EntitySkeletonSplitFix($$20, true));
      Schema $$21 = $$0.addSchema(702, V702::new);
      $$0.addFixer(new EntityZombieSplitFix($$21));
      Schema $$22 = $$0.addSchema(703, V703::new);
      $$0.addFixer(new EntityHorseSplitFix($$22, true));
      Schema $$23 = $$0.addSchema(704, V704::new);
      $$0.addFixer(new BlockEntityIdFix($$23, true));
      Schema $$24 = $$0.addSchema(705, V705::new);
      $$0.addFixer(new EntityIdFix($$24, true));
      Schema $$25 = $$0.addSchema(804, SAME_NAMESPACED);
      $$0.addFixer(new ItemBannerColorFix($$25, true));
      Schema $$26 = $$0.addSchema(806, SAME_NAMESPACED);
      $$0.addFixer(new ItemWaterPotionFix($$26, false));
      Schema $$27 = $$0.addSchema(808, V808::new);
      $$0.addFixer(new AddNewChoices($$27, "added shulker box", References.BLOCK_ENTITY));
      Schema $$28 = $$0.addSchema(808, 1, SAME_NAMESPACED);
      $$0.addFixer(new EntityShulkerColorFix($$28, false));
      Schema $$29 = $$0.addSchema(813, SAME_NAMESPACED);
      $$0.addFixer(new ItemShulkerBoxColorFix($$29, false));
      $$0.addFixer(new BlockEntityShulkerBoxColorFix($$29, false));
      Schema $$30 = $$0.addSchema(816, SAME_NAMESPACED);
      $$0.addFixer(new OptionsLowerCaseLanguageFix($$30, false));
      Schema $$31 = $$0.addSchema(820, SAME_NAMESPACED);
      $$0.addFixer(ItemRenameFix.create($$31, "totem item renamer", createRenamer("minecraft:totem", "minecraft:totem_of_undying")));
      Schema $$32 = $$0.addSchema(1022, V1022::new);
      $$0.addFixer(new WriteAndReadFix($$32, "added shoulder entities to players", References.PLAYER));
      Schema $$33 = $$0.addSchema(1125, V1125::new);
      $$0.addFixer(new ChunkBedBlockEntityInjecterFix($$33, true));
      $$0.addFixer(new BedItemColorFix($$33, false));
      Schema $$34 = $$0.addSchema(1344, SAME_NAMESPACED);
      $$0.addFixer(new OptionsKeyLwjgl3Fix($$34, false));
      Schema $$35 = $$0.addSchema(1446, SAME_NAMESPACED);
      $$0.addFixer(new OptionsKeyTranslationFix($$35, false));
      Schema $$36 = $$0.addSchema(1450, SAME_NAMESPACED);
      $$0.addFixer(new BlockStateStructureTemplateFix($$36, false));
      Schema $$37 = $$0.addSchema(1451, V1451::new);
      $$0.addFixer(new AddNewChoices($$37, "AddTrappedChestFix", References.BLOCK_ENTITY));
      Schema $$38 = $$0.addSchema(1451, 1, V1451_1::new);
      $$0.addFixer(new ChunkPalettedStorageFix($$38, true));
      Schema $$39 = $$0.addSchema(1451, 2, V1451_2::new);
      $$0.addFixer(new BlockEntityBlockStateFix($$39, true));
      Schema $$40 = $$0.addSchema(1451, 3, V1451_3::new);
      $$0.addFixer(new EntityBlockStateFix($$40, true));
      $$0.addFixer(new ItemStackMapIdFix($$40, false));
      Schema $$41 = $$0.addSchema(1451, 4, V1451_4::new);
      $$0.addFixer(new BlockNameFlatteningFix($$41, true));
      $$0.addFixer(new ItemStackTheFlatteningFix($$41, false));
      Schema $$42 = $$0.addSchema(1451, 5, V1451_5::new);
      $$0.addFixer(new RemoveBlockEntityTagFix($$42, Set.of("minecraft:noteblock", "minecraft:flower_pot")));
      $$0.addFixer(new ItemStackSpawnEggFix($$42, false, "minecraft:spawn_egg"));
      $$0.addFixer(new EntityWolfColorFix($$42, false));
      $$0.addFixer(new BlockEntityBannerColorFix($$42, false));
      $$0.addFixer(new LevelFlatGeneratorInfoFix($$42, false));
      Schema $$43 = $$0.addSchema(1451, 6, V1451_6::new);
      $$0.addFixer(new StatsCounterFix($$43, true));
      $$0.addFixer(new BlockEntityJukeboxFix($$43, false));
      Schema $$44 = $$0.addSchema(1451, 7, SAME_NAMESPACED);
      $$0.addFixer(new VillagerTradeFix($$44));
      Schema $$45 = $$0.addSchema(1456, SAME_NAMESPACED);
      $$0.addFixer(new EntityItemFrameDirectionFix($$45, false));
      Schema $$46 = $$0.addSchema(1458, V1458::new);
      $$0.addFixer(new EntityCustomNameToComponentFix($$46));
      $$0.addFixer(new ItemCustomNameToComponentFix($$46));
      $$0.addFixer(new BlockEntityCustomNameToComponentFix($$46));
      Schema $$47 = $$0.addSchema(1460, V1460::new);
      $$0.addFixer(new EntityPaintingMotiveFix($$47, false));
      Schema $$48 = $$0.addSchema(1466, V1466::new);
      $$0.addFixer(new AddNewChoices($$48, "Add DUMMY block entity", References.BLOCK_ENTITY));
      $$0.addFixer(new ChunkToProtochunkFix($$48, true));
      Schema $$49 = $$0.addSchema(1470, V1470::new);
      $$0.addFixer(new AddNewChoices($$49, "Add 1.13 entities fix", References.ENTITY));
      Schema $$50 = $$0.addSchema(1474, SAME_NAMESPACED);
      $$0.addFixer(new ColorlessShulkerEntityFix($$50, false));
      $$0.addFixer(
         BlockRenameFix.create(
            $$50,
            "Colorless shulker block fixer",
            $$0x -> Objects.equals(NamespacedSchema.ensureNamespaced($$0x), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : $$0x
         )
      );
      $$0.addFixer(
         ItemRenameFix.create(
            $$50,
            "Colorless shulker item fixer",
            $$0x -> Objects.equals(NamespacedSchema.ensureNamespaced($$0x), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : $$0x
         )
      );
      Schema $$51 = $$0.addSchema(1475, SAME_NAMESPACED);
      $$0.addFixer(
         BlockRenameFix.create(
            $$51, "Flowing fixer", createRenamer(ImmutableMap.of("minecraft:flowing_water", "minecraft:water", "minecraft:flowing_lava", "minecraft:lava"))
         )
      );
      Schema $$52 = $$0.addSchema(1480, SAME_NAMESPACED);
      $$0.addFixer(BlockRenameFix.create($$52, "Rename coral blocks", createRenamer(RenamedCoralFix.RENAMED_IDS)));
      $$0.addFixer(ItemRenameFix.create($$52, "Rename coral items", createRenamer(RenamedCoralFix.RENAMED_IDS)));
      Schema $$53 = $$0.addSchema(1481, V1481::new);
      $$0.addFixer(new AddNewChoices($$53, "Add conduit", References.BLOCK_ENTITY));
      Schema $$54 = $$0.addSchema(1483, V1483::new);
      $$0.addFixer(new EntityPufferfishRenameFix($$54, true));
      $$0.addFixer(ItemRenameFix.create($$54, "Rename pufferfish egg item", createRenamer(EntityPufferfishRenameFix.RENAMED_IDS)));
      Schema $$55 = $$0.addSchema(1484, SAME_NAMESPACED);
      $$0.addFixer(
         ItemRenameFix.create(
            $$55,
            "Rename seagrass items",
            createRenamer(ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass"))
         )
      );
      $$0.addFixer(
         BlockRenameFix.create(
            $$55,
            "Rename seagrass blocks",
            createRenamer(ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass"))
         )
      );
      $$0.addFixer(new HeightmapRenamingFix($$55, false));
      Schema $$56 = $$0.addSchema(1486, V1486::new);
      $$0.addFixer(new EntityCodSalmonFix($$56, true));
      $$0.addFixer(ItemRenameFix.create($$56, "Rename cod/salmon egg items", createRenamer(EntityCodSalmonFix.RENAMED_EGG_IDS)));
      Schema $$57 = $$0.addSchema(1487, SAME_NAMESPACED);
      $$0.addFixer(
         ItemRenameFix.create(
            $$57,
            "Rename prismarine_brick(s)_* blocks",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:prismarine_bricks_slab",
                  "minecraft:prismarine_brick_slab",
                  "minecraft:prismarine_bricks_stairs",
                  "minecraft:prismarine_brick_stairs"
               )
            )
         )
      );
      $$0.addFixer(
         BlockRenameFix.create(
            $$57,
            "Rename prismarine_brick(s)_* items",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:prismarine_bricks_slab",
                  "minecraft:prismarine_brick_slab",
                  "minecraft:prismarine_bricks_stairs",
                  "minecraft:prismarine_brick_stairs"
               )
            )
         )
      );
      Schema $$58 = $$0.addSchema(1488, V1488::new);
      $$0.addFixer(
         BlockRenameFix.create(
            $$58, "Rename kelp/kelptop", createRenamer(ImmutableMap.of("minecraft:kelp_top", "minecraft:kelp", "minecraft:kelp", "minecraft:kelp_plant"))
         )
      );
      $$0.addFixer(ItemRenameFix.create($$58, "Rename kelptop", createRenamer("minecraft:kelp_top", "minecraft:kelp")));
      $$0.addFixer(new NamedEntityWriteReadFix($$58, true, "Command block block entity custom name fix", References.BLOCK_ENTITY, "minecraft:command_block") {
         @Override
         protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
            return BlockEntityCustomNameToComponentFix.fixTagCustomName($$0);
         }
      });
      $$0.addFixer(
         new DataFix($$58, false) {
            protected TypeRewriteRule makeRule() {
               Type<?> $$0x = this.getInputSchema().getType(References.ENTITY);
               OpticFinder<String> $$1x = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
               OpticFinder<?> $$2x = $$0x.findField("CustomName");
               OpticFinder<Pair<String, String>> $$3x = DSL.typeFinder(this.getInputSchema().getType(References.TEXT_COMPONENT));
               return this.fixTypeEverywhereTyped(
                  "Command block minecart custom name fix",
                  $$0x,
                  $$3xx -> {
                     String $$4x = $$3xx.getOptional($$1).orElse("");
                     return !"minecraft:commandblock_minecart".equals($$4x)
                        ? $$3xx
                        : $$3xx.updateTyped($$2, $$1xx -> $$1xx.update($$3, $$0xxx -> $$0xxx.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson)));
                  }
               );
            }
         }
      );
      $$0.addFixer(new IglooMetadataRemovalFix($$58, false));
      Schema $$59 = $$0.addSchema(1490, SAME_NAMESPACED);
      $$0.addFixer(BlockRenameFix.create($$59, "Rename melon_block", createRenamer("minecraft:melon_block", "minecraft:melon")));
      $$0.addFixer(
         ItemRenameFix.create(
            $$59,
            "Rename melon_block/melon/speckled_melon",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:melon_block",
                  "minecraft:melon",
                  "minecraft:melon",
                  "minecraft:melon_slice",
                  "minecraft:speckled_melon",
                  "minecraft:glistering_melon_slice"
               )
            )
         )
      );
      Schema $$60 = $$0.addSchema(1492, SAME_NAMESPACED);
      $$0.addFixer(new ChunkStructuresTemplateRenameFix($$60, false));
      Schema $$61 = $$0.addSchema(1494, SAME_NAMESPACED);
      $$0.addFixer(new ItemStackEnchantmentNamesFix($$61, false));
      Schema $$62 = $$0.addSchema(1496, SAME_NAMESPACED);
      $$0.addFixer(new LeavesFix($$62, false));
      Schema $$63 = $$0.addSchema(1500, SAME_NAMESPACED);
      $$0.addFixer(new BlockEntityKeepPacked($$63, false));
      Schema $$64 = $$0.addSchema(1501, SAME_NAMESPACED);
      $$0.addFixer(new AdvancementsFix($$64, false));
      Schema $$65 = $$0.addSchema(1502, SAME_NAMESPACED);
      $$0.addFixer(new NamespacedTypeRenameFix($$65, "Recipes fix", References.RECIPE, createRenamer(RecipesFix.RECIPES)));
      Schema $$66 = $$0.addSchema(1506, SAME_NAMESPACED);
      $$0.addFixer(new LevelDataGeneratorOptionsFix($$66, false));
      Schema $$67 = $$0.addSchema(1510, V1510::new);
      $$0.addFixer(BlockRenameFix.create($$67, "Block renamening fix", createRenamer(EntityTheRenameningFix.RENAMED_BLOCKS)));
      $$0.addFixer(ItemRenameFix.create($$67, "Item renamening fix", createRenamer(EntityTheRenameningFix.RENAMED_ITEMS)));
      $$0.addFixer(new NamespacedTypeRenameFix($$67, "Recipes renamening fix", References.RECIPE, createRenamer(RecipesRenameningFix.RECIPES)));
      $$0.addFixer(new EntityTheRenameningFix($$67, true));
      $$0.addFixer(
         new StatsRenameFix(
            $$67,
            "SwimStatsRenameFix",
            ImmutableMap.of("minecraft:swim_one_cm", "minecraft:walk_on_water_one_cm", "minecraft:dive_one_cm", "minecraft:walk_under_water_one_cm")
         )
      );
      Schema $$68 = $$0.addSchema(1514, SAME_NAMESPACED);
      $$0.addFixer(new ScoreboardDisplayNameFix($$68, "ObjectiveDisplayNameFix", References.OBJECTIVE));
      $$0.addFixer(new ScoreboardDisplayNameFix($$68, "TeamDisplayNameFix", References.TEAM));
      $$0.addFixer(new ObjectiveRenderTypeFix($$68));
      Schema $$69 = $$0.addSchema(1515, SAME_NAMESPACED);
      $$0.addFixer(BlockRenameFix.create($$69, "Rename coral fan blocks", createRenamer(RenamedCoralFansFix.RENAMED_IDS)));
      Schema $$70 = $$0.addSchema(1624, SAME_NAMESPACED);
      $$0.addFixer(new TrappedChestBlockEntityFix($$70, false));
      Schema $$71 = $$0.addSchema(1800, V1800::new);
      $$0.addFixer(new AddNewChoices($$71, "Added 1.14 mobs fix", References.ENTITY));
      $$0.addFixer(ItemRenameFix.create($$71, "Rename dye items", createRenamer(DyeItemRenameFix.RENAMED_IDS)));
      Schema $$72 = $$0.addSchema(1801, V1801::new);
      $$0.addFixer(new AddNewChoices($$72, "Added Illager Beast", References.ENTITY));
      Schema $$73 = $$0.addSchema(1802, SAME_NAMESPACED);
      $$0.addFixer(
         BlockRenameFix.create(
            $$73,
            "Rename sign blocks & stone slabs",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:stone_slab",
                  "minecraft:smooth_stone_slab",
                  "minecraft:sign",
                  "minecraft:oak_sign",
                  "minecraft:wall_sign",
                  "minecraft:oak_wall_sign"
               )
            )
         )
      );
      $$0.addFixer(
         ItemRenameFix.create(
            $$73,
            "Rename sign item & stone slabs",
            createRenamer(ImmutableMap.of("minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign"))
         )
      );
      Schema $$74 = $$0.addSchema(1803, SAME_NAMESPACED);
      $$0.addFixer(new ItemLoreFix($$74));
      Schema $$75 = $$0.addSchema(1904, V1904::new);
      $$0.addFixer(new AddNewChoices($$75, "Added Cats", References.ENTITY));
      $$0.addFixer(new EntityCatSplitFix($$75, false));
      Schema $$76 = $$0.addSchema(1905, SAME_NAMESPACED);
      $$0.addFixer(new ChunkStatusFix($$76, false));
      Schema $$77 = $$0.addSchema(1906, V1906::new);
      $$0.addFixer(new AddNewChoices($$77, "Add POI Blocks", References.BLOCK_ENTITY));
      Schema $$78 = $$0.addSchema(1909, V1909::new);
      $$0.addFixer(new AddNewChoices($$78, "Add jigsaw", References.BLOCK_ENTITY));
      Schema $$79 = $$0.addSchema(1911, SAME_NAMESPACED);
      $$0.addFixer(new ChunkStatusFix2($$79, false));
      Schema $$80 = $$0.addSchema(1914, SAME_NAMESPACED);
      $$0.addFixer(new WeaponSmithChestLootTableFix($$80, false));
      Schema $$81 = $$0.addSchema(1917, SAME_NAMESPACED);
      $$0.addFixer(new CatTypeFix($$81, false));
      Schema $$82 = $$0.addSchema(1918, SAME_NAMESPACED);
      $$0.addFixer(new VillagerDataFix($$82, "minecraft:villager"));
      $$0.addFixer(new VillagerDataFix($$82, "minecraft:zombie_villager"));
      Schema $$83 = $$0.addSchema(1920, V1920::new);
      $$0.addFixer(new NewVillageFix($$83, false));
      $$0.addFixer(new AddNewChoices($$83, "Add campfire", References.BLOCK_ENTITY));
      Schema $$84 = $$0.addSchema(1925, SAME_NAMESPACED);
      $$0.addFixer(new MapIdFix($$84));
      Schema $$85 = $$0.addSchema(1928, V1928::new);
      $$0.addFixer(new EntityRavagerRenameFix($$85, true));
      $$0.addFixer(ItemRenameFix.create($$85, "Rename ravager egg item", createRenamer(EntityRavagerRenameFix.RENAMED_IDS)));
      Schema $$86 = $$0.addSchema(1929, V1929::new);
      $$0.addFixer(new AddNewChoices($$86, "Add Wandering Trader and Trader Llama", References.ENTITY));
      Schema $$87 = $$0.addSchema(1931, V1931::new);
      $$0.addFixer(new AddNewChoices($$87, "Added Fox", References.ENTITY));
      Schema $$88 = $$0.addSchema(1936, SAME_NAMESPACED);
      $$0.addFixer(new OptionsAddTextBackgroundFix($$88, false));
      Schema $$89 = $$0.addSchema(1946, SAME_NAMESPACED);
      $$0.addFixer(new ReorganizePoi($$89, false));
      Schema $$90 = $$0.addSchema(1948, SAME_NAMESPACED);
      $$0.addFixer(new OminousBannerRenameFix($$90));
      Schema $$91 = $$0.addSchema(1953, SAME_NAMESPACED);
      $$0.addFixer(new OminousBannerBlockEntityRenameFix($$91, false));
      Schema $$92 = $$0.addSchema(1955, SAME_NAMESPACED);
      $$0.addFixer(new VillagerRebuildLevelAndXpFix($$92, false));
      $$0.addFixer(new ZombieVillagerRebuildXpFix($$92, false));
      Schema $$93 = $$0.addSchema(1961, SAME_NAMESPACED);
      $$0.addFixer(new ChunkLightRemoveFix($$93, false));
      Schema $$94 = $$0.addSchema(1963, SAME_NAMESPACED);
      $$0.addFixer(new RemoveGolemGossipFix($$94, false));
      Schema $$95 = $$0.addSchema(2100, V2100::new);
      $$0.addFixer(new AddNewChoices($$95, "Added Bee and Bee Stinger", References.ENTITY));
      $$0.addFixer(new AddNewChoices($$95, "Add beehive", References.BLOCK_ENTITY));
      $$0.addFixer(
         new NamespacedTypeRenameFix($$95, "Rename sugar recipe", References.RECIPE, createRenamer("minecraft:sugar", "minecraft:sugar_from_sugar_cane"))
      );
      $$0.addFixer(
         new AdvancementsRenameFix(
            $$95, false, "Rename sugar recipe advancement", createRenamer("minecraft:recipes/misc/sugar", "minecraft:recipes/misc/sugar_from_sugar_cane")
         )
      );
      Schema $$96 = $$0.addSchema(2202, SAME_NAMESPACED);
      $$0.addFixer(new ChunkBiomeFix($$96, false));
      Schema $$97 = $$0.addSchema(2209, SAME_NAMESPACED);
      UnaryOperator<String> $$98 = createRenamer("minecraft:bee_hive", "minecraft:beehive");
      $$0.addFixer(ItemRenameFix.create($$97, "Rename bee_hive item to beehive", $$98));
      $$0.addFixer(new PoiTypeRenameFix($$97, "Rename bee_hive poi to beehive", $$98));
      $$0.addFixer(BlockRenameFix.create($$97, "Rename bee_hive block to beehive", $$98));
      Schema $$99 = $$0.addSchema(2211, SAME_NAMESPACED);
      $$0.addFixer(new StructureReferenceCountFix($$99, false));
      Schema $$100 = $$0.addSchema(2218, SAME_NAMESPACED);
      $$0.addFixer(new ForcePoiRebuild($$100, false));
      Schema $$101 = $$0.addSchema(2501, V2501::new);
      $$0.addFixer(new FurnaceRecipeFix($$101, true));
      Schema $$102 = $$0.addSchema(2502, V2502::new);
      $$0.addFixer(new AddNewChoices($$102, "Added Hoglin", References.ENTITY));
      Schema $$103 = $$0.addSchema(2503, SAME_NAMESPACED);
      $$0.addFixer(new WallPropertyFix($$103, false));
      $$0.addFixer(
         new AdvancementsRenameFix(
            $$103, false, "Composter category change", createRenamer("minecraft:recipes/misc/composter", "minecraft:recipes/decorations/composter")
         )
      );
      Schema $$104 = $$0.addSchema(2505, V2505::new);
      $$0.addFixer(new AddNewChoices($$104, "Added Piglin", References.ENTITY));
      $$0.addFixer(new MemoryExpiryDataFix($$104, "minecraft:villager"));
      Schema $$105 = $$0.addSchema(2508, SAME_NAMESPACED);
      $$0.addFixer(
         ItemRenameFix.create(
            $$105,
            "Renamed fungi items to fungus",
            createRenamer(ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus"))
         )
      );
      $$0.addFixer(
         BlockRenameFix.create(
            $$105,
            "Renamed fungi blocks to fungus",
            createRenamer(ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus"))
         )
      );
      Schema $$106 = $$0.addSchema(2509, V2509::new);
      $$0.addFixer(new EntityZombifiedPiglinRenameFix($$106));
      $$0.addFixer(ItemRenameFix.create($$106, "Rename zombie pigman egg item", createRenamer(EntityZombifiedPiglinRenameFix.RENAMED_IDS)));
      Schema $$107 = $$0.addSchema(2511, SAME_NAMESPACED);
      $$0.addFixer(new EntityProjectileOwnerFix($$107));
      Schema $$108 = $$0.addSchema(2511, 1, V2511_1::new);
      $$0.addFixer(new NamedEntityConvertUncheckedFix($$108, "SplashPotionItemFieldRenameFix", References.ENTITY, "minecraft:potion"));
      Schema $$109 = $$0.addSchema(2514, SAME_NAMESPACED);
      $$0.addFixer(new EntityUUIDFix($$109));
      $$0.addFixer(new BlockEntityUUIDFix($$109));
      $$0.addFixer(new PlayerUUIDFix($$109));
      $$0.addFixer(new LevelUUIDFix($$109));
      $$0.addFixer(new SavedDataUUIDFix($$109));
      $$0.addFixer(new ItemStackUUIDFix($$109));
      Schema $$110 = $$0.addSchema(2516, SAME_NAMESPACED);
      $$0.addFixer(new GossipUUIDFix($$110, "minecraft:villager"));
      $$0.addFixer(new GossipUUIDFix($$110, "minecraft:zombie_villager"));
      Schema $$111 = $$0.addSchema(2518, SAME_NAMESPACED);
      $$0.addFixer(new JigsawPropertiesFix($$111, false));
      $$0.addFixer(new JigsawRotationFix($$111));
      Schema $$112 = $$0.addSchema(2519, V2519::new);
      $$0.addFixer(new AddNewChoices($$112, "Added Strider", References.ENTITY));
      Schema $$113 = $$0.addSchema(2522, V2522::new);
      $$0.addFixer(new AddNewChoices($$113, "Added Zoglin", References.ENTITY));
      Schema $$114 = $$0.addSchema(2523, SAME_NAMESPACED);
      $$0.addFixer(
         new AttributesRenameLegacy(
            $$114,
            "Attribute renames",
            createRenamerNoNamespace(
               ImmutableMap.builder()
                  .put("generic.maxHealth", "minecraft:generic.max_health")
                  .put("Max Health", "minecraft:generic.max_health")
                  .put("zombie.spawnReinforcements", "minecraft:zombie.spawn_reinforcements")
                  .put("Spawn Reinforcements Chance", "minecraft:zombie.spawn_reinforcements")
                  .put("horse.jumpStrength", "minecraft:horse.jump_strength")
                  .put("Jump Strength", "minecraft:horse.jump_strength")
                  .put("generic.followRange", "minecraft:generic.follow_range")
                  .put("Follow Range", "minecraft:generic.follow_range")
                  .put("generic.knockbackResistance", "minecraft:generic.knockback_resistance")
                  .put("Knockback Resistance", "minecraft:generic.knockback_resistance")
                  .put("generic.movementSpeed", "minecraft:generic.movement_speed")
                  .put("Movement Speed", "minecraft:generic.movement_speed")
                  .put("generic.flyingSpeed", "minecraft:generic.flying_speed")
                  .put("Flying Speed", "minecraft:generic.flying_speed")
                  .put("generic.attackDamage", "minecraft:generic.attack_damage")
                  .put("generic.attackKnockback", "minecraft:generic.attack_knockback")
                  .put("generic.attackSpeed", "minecraft:generic.attack_speed")
                  .put("generic.armorToughness", "minecraft:generic.armor_toughness")
                  .build()
            )
         )
      );
      Schema $$115 = $$0.addSchema(2527, SAME_NAMESPACED);
      $$0.addFixer(new BitStorageAlignFix($$115));
      Schema $$116 = $$0.addSchema(2528, SAME_NAMESPACED);
      $$0.addFixer(
         ItemRenameFix.create(
            $$116,
            "Rename soul fire torch and soul fire lantern",
            createRenamer(ImmutableMap.of("minecraft:soul_fire_torch", "minecraft:soul_torch", "minecraft:soul_fire_lantern", "minecraft:soul_lantern"))
         )
      );
      $$0.addFixer(
         BlockRenameFix.create(
            $$116,
            "Rename soul fire torch and soul fire lantern",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:soul_fire_torch",
                  "minecraft:soul_torch",
                  "minecraft:soul_fire_wall_torch",
                  "minecraft:soul_wall_torch",
                  "minecraft:soul_fire_lantern",
                  "minecraft:soul_lantern"
               )
            )
         )
      );
      Schema $$117 = $$0.addSchema(2529, SAME_NAMESPACED);
      $$0.addFixer(new StriderGravityFix($$117, false));
      Schema $$118 = $$0.addSchema(2531, SAME_NAMESPACED);
      $$0.addFixer(new RedstoneWireConnectionsFix($$118));
      Schema $$119 = $$0.addSchema(2533, SAME_NAMESPACED);
      $$0.addFixer(new VillagerFollowRangeFix($$119));
      Schema $$120 = $$0.addSchema(2535, SAME_NAMESPACED);
      $$0.addFixer(new EntityShulkerRotationFix($$120));
      Schema $$121 = $$0.addSchema(2537, SAME_NAMESPACED);
      $$0.addFixer(new LegacyDimensionIdFix($$121));
      Schema $$122 = $$0.addSchema(2538, SAME_NAMESPACED);
      $$0.addFixer(new LevelLegacyWorldGenSettingsFix($$122));
      Schema $$123 = $$0.addSchema(2550, SAME_NAMESPACED);
      $$0.addFixer(new WorldGenSettingsFix($$123));
      Schema $$124 = $$0.addSchema(2551, V2551::new);
      $$0.addFixer(new WriteAndReadFix($$124, "add types to WorldGenData", References.WORLD_GEN_SETTINGS));
      Schema $$125 = $$0.addSchema(2552, SAME_NAMESPACED);
      $$0.addFixer(new NamespacedTypeRenameFix($$125, "Nether biome rename", References.BIOME, createRenamer("minecraft:nether", "minecraft:nether_wastes")));
      Schema $$126 = $$0.addSchema(2553, SAME_NAMESPACED);
      $$0.addFixer(new NamespacedTypeRenameFix($$126, "Biomes fix", References.BIOME, createRenamer(BiomeFix.BIOMES)));
      Schema $$127 = $$0.addSchema(2556, SAME_NAMESPACED);
      $$0.addFixer(new OptionsFancyGraphicsToGraphicsModeFix($$127));
      Schema $$128 = $$0.addSchema(2558, SAME_NAMESPACED);
      $$0.addFixer(new MissingDimensionFix($$128, false));
      $$0.addFixer(new OptionsRenameFieldFix($$128, false, "Rename swapHands setting", "key_key.swapHands", "key_key.swapOffhand"));
      Schema $$129 = $$0.addSchema(2568, V2568::new);
      $$0.addFixer(new AddNewChoices($$129, "Added Piglin Brute", References.ENTITY));
      Schema $$130 = $$0.addSchema(2571, V2571::new);
      $$0.addFixer(new AddNewChoices($$130, "Added Goat", References.ENTITY));
      Schema $$131 = $$0.addSchema(2679, SAME_NAMESPACED);
      $$0.addFixer(new CauldronRenameFix($$131, false));
      Schema $$132 = $$0.addSchema(2680, SAME_NAMESPACED);
      $$0.addFixer(ItemRenameFix.create($$132, "Renamed grass path item to dirt path", createRenamer("minecraft:grass_path", "minecraft:dirt_path")));
      $$0.addFixer(BlockRenameFix.create($$132, "Renamed grass path block to dirt path", createRenamer("minecraft:grass_path", "minecraft:dirt_path")));
      Schema $$133 = $$0.addSchema(2684, V2684::new);
      $$0.addFixer(new AddNewChoices($$133, "Added Sculk Sensor", References.BLOCK_ENTITY));
      Schema $$134 = $$0.addSchema(2686, V2686::new);
      $$0.addFixer(new AddNewChoices($$134, "Added Axolotl", References.ENTITY));
      Schema $$135 = $$0.addSchema(2688, V2688::new);
      $$0.addFixer(new AddNewChoices($$135, "Added Glow Squid", References.ENTITY));
      $$0.addFixer(new AddNewChoices($$135, "Added Glow Item Frame", References.ENTITY));
      Schema $$136 = $$0.addSchema(2690, SAME_NAMESPACED);
      ImmutableMap<String, String> $$137 = ImmutableMap.builder()
         .put("minecraft:weathered_copper_block", "minecraft:oxidized_copper_block")
         .put("minecraft:semi_weathered_copper_block", "minecraft:weathered_copper_block")
         .put("minecraft:lightly_weathered_copper_block", "minecraft:exposed_copper_block")
         .put("minecraft:weathered_cut_copper", "minecraft:oxidized_cut_copper")
         .put("minecraft:semi_weathered_cut_copper", "minecraft:weathered_cut_copper")
         .put("minecraft:lightly_weathered_cut_copper", "minecraft:exposed_cut_copper")
         .put("minecraft:weathered_cut_copper_stairs", "minecraft:oxidized_cut_copper_stairs")
         .put("minecraft:semi_weathered_cut_copper_stairs", "minecraft:weathered_cut_copper_stairs")
         .put("minecraft:lightly_weathered_cut_copper_stairs", "minecraft:exposed_cut_copper_stairs")
         .put("minecraft:weathered_cut_copper_slab", "minecraft:oxidized_cut_copper_slab")
         .put("minecraft:semi_weathered_cut_copper_slab", "minecraft:weathered_cut_copper_slab")
         .put("minecraft:lightly_weathered_cut_copper_slab", "minecraft:exposed_cut_copper_slab")
         .put("minecraft:waxed_semi_weathered_copper", "minecraft:waxed_weathered_copper")
         .put("minecraft:waxed_lightly_weathered_copper", "minecraft:waxed_exposed_copper")
         .put("minecraft:waxed_semi_weathered_cut_copper", "minecraft:waxed_weathered_cut_copper")
         .put("minecraft:waxed_lightly_weathered_cut_copper", "minecraft:waxed_exposed_cut_copper")
         .put("minecraft:waxed_semi_weathered_cut_copper_stairs", "minecraft:waxed_weathered_cut_copper_stairs")
         .put("minecraft:waxed_lightly_weathered_cut_copper_stairs", "minecraft:waxed_exposed_cut_copper_stairs")
         .put("minecraft:waxed_semi_weathered_cut_copper_slab", "minecraft:waxed_weathered_cut_copper_slab")
         .put("minecraft:waxed_lightly_weathered_cut_copper_slab", "minecraft:waxed_exposed_cut_copper_slab")
         .build();
      $$0.addFixer(ItemRenameFix.create($$136, "Renamed copper block items to new oxidized terms", createRenamer($$137)));
      $$0.addFixer(BlockRenameFix.create($$136, "Renamed copper blocks to new oxidized terms", createRenamer($$137)));
      Schema $$138 = $$0.addSchema(2691, SAME_NAMESPACED);
      ImmutableMap<String, String> $$139 = ImmutableMap.builder()
         .put("minecraft:waxed_copper", "minecraft:waxed_copper_block")
         .put("minecraft:oxidized_copper_block", "minecraft:oxidized_copper")
         .put("minecraft:weathered_copper_block", "minecraft:weathered_copper")
         .put("minecraft:exposed_copper_block", "minecraft:exposed_copper")
         .build();
      $$0.addFixer(ItemRenameFix.create($$138, "Rename copper item suffixes", createRenamer($$139)));
      $$0.addFixer(BlockRenameFix.create($$138, "Rename copper blocks suffixes", createRenamer($$139)));
      Schema $$140 = $$0.addSchema(2693, SAME_NAMESPACED);
      $$0.addFixer(new AddFlagIfNotPresentFix($$140, References.WORLD_GEN_SETTINGS, "has_increased_height_already", false));
      Schema $$141 = $$0.addSchema(2696, SAME_NAMESPACED);
      ImmutableMap<String, String> $$142 = ImmutableMap.builder()
         .put("minecraft:grimstone", "minecraft:deepslate")
         .put("minecraft:grimstone_slab", "minecraft:cobbled_deepslate_slab")
         .put("minecraft:grimstone_stairs", "minecraft:cobbled_deepslate_stairs")
         .put("minecraft:grimstone_wall", "minecraft:cobbled_deepslate_wall")
         .put("minecraft:polished_grimstone", "minecraft:polished_deepslate")
         .put("minecraft:polished_grimstone_slab", "minecraft:polished_deepslate_slab")
         .put("minecraft:polished_grimstone_stairs", "minecraft:polished_deepslate_stairs")
         .put("minecraft:polished_grimstone_wall", "minecraft:polished_deepslate_wall")
         .put("minecraft:grimstone_tiles", "minecraft:deepslate_tiles")
         .put("minecraft:grimstone_tile_slab", "minecraft:deepslate_tile_slab")
         .put("minecraft:grimstone_tile_stairs", "minecraft:deepslate_tile_stairs")
         .put("minecraft:grimstone_tile_wall", "minecraft:deepslate_tile_wall")
         .put("minecraft:grimstone_bricks", "minecraft:deepslate_bricks")
         .put("minecraft:grimstone_brick_slab", "minecraft:deepslate_brick_slab")
         .put("minecraft:grimstone_brick_stairs", "minecraft:deepslate_brick_stairs")
         .put("minecraft:grimstone_brick_wall", "minecraft:deepslate_brick_wall")
         .put("minecraft:chiseled_grimstone", "minecraft:chiseled_deepslate")
         .build();
      $$0.addFixer(ItemRenameFix.create($$141, "Renamed grimstone block items to deepslate", createRenamer($$142)));
      $$0.addFixer(BlockRenameFix.create($$141, "Renamed grimstone blocks to deepslate", createRenamer($$142)));
      Schema $$143 = $$0.addSchema(2700, SAME_NAMESPACED);
      $$0.addFixer(
         BlockRenameFix.create(
            $$143,
            "Renamed cave vines blocks",
            createRenamer(ImmutableMap.of("minecraft:cave_vines_head", "minecraft:cave_vines", "minecraft:cave_vines_body", "minecraft:cave_vines_plant"))
         )
      );
      Schema $$144 = $$0.addSchema(2701, SAME_NAMESPACED);
      $$0.addFixer(new SavedDataFeaturePoolElementFix($$144));
      Schema $$145 = $$0.addSchema(2702, SAME_NAMESPACED);
      $$0.addFixer(new AbstractArrowPickupFix($$145));
      Schema $$146 = $$0.addSchema(2704, V2704::new);
      $$0.addFixer(new AddNewChoices($$146, "Added Goat", References.ENTITY));
      Schema $$147 = $$0.addSchema(2707, V2707::new);
      $$0.addFixer(new AddNewChoices($$147, "Added Marker", References.ENTITY));
      $$0.addFixer(new AddFlagIfNotPresentFix($$147, References.WORLD_GEN_SETTINGS, "has_increased_height_already", true));
      Schema $$148 = $$0.addSchema(2710, SAME_NAMESPACED);
      $$0.addFixer(new StatsRenameFix($$148, "Renamed play_one_minute stat to play_time", ImmutableMap.of("minecraft:play_one_minute", "minecraft:play_time")));
      Schema $$149 = $$0.addSchema(2717, SAME_NAMESPACED);
      $$0.addFixer(
         ItemRenameFix.create(
            $$149, "Rename azalea_leaves_flowers", createRenamer(ImmutableMap.of("minecraft:azalea_leaves_flowers", "minecraft:flowering_azalea_leaves"))
         )
      );
      $$0.addFixer(
         BlockRenameFix.create(
            $$149, "Rename azalea_leaves_flowers items", createRenamer(ImmutableMap.of("minecraft:azalea_leaves_flowers", "minecraft:flowering_azalea_leaves"))
         )
      );
      Schema $$150 = $$0.addSchema(2825, SAME_NAMESPACED);
      $$0.addFixer(new AddFlagIfNotPresentFix($$150, References.WORLD_GEN_SETTINGS, "has_increased_height_already", false));
      Schema $$151 = $$0.addSchema(2831, V2831::new);
      $$0.addFixer(new SpawnerDataFix($$151));
      Schema $$152 = $$0.addSchema(2832, V2832::new);
      $$0.addFixer(new WorldGenSettingsHeightAndBiomeFix($$152));
      $$0.addFixer(new ChunkHeightAndBiomeFix($$152));
      Schema $$153 = $$0.addSchema(2833, SAME_NAMESPACED);
      $$0.addFixer(new WorldGenSettingsDisallowOldCustomWorldsFix($$153));
      Schema $$154 = $$0.addSchema(2838, SAME_NAMESPACED);
      $$0.addFixer(new NamespacedTypeRenameFix($$154, "Caves and Cliffs biome renames", References.BIOME, createRenamer(CavesAndCliffsRenames.RENAMES)));
      Schema $$155 = $$0.addSchema(2841, SAME_NAMESPACED);
      $$0.addFixer(new ChunkProtoTickListFix($$155));
      Schema $$156 = $$0.addSchema(2842, V2842::new);
      $$0.addFixer(new ChunkRenamesFix($$156));
      Schema $$157 = $$0.addSchema(2843, SAME_NAMESPACED);
      $$0.addFixer(new OverreachingTickFix($$157));
      $$0.addFixer(
         new NamespacedTypeRenameFix($$157, "Remove Deep Warm Ocean", References.BIOME, createRenamer("minecraft:deep_warm_ocean", "minecraft:warm_ocean"))
      );
      Schema $$158 = $$0.addSchema(2846, SAME_NAMESPACED);
      $$0.addFixer(
         new AdvancementsRenameFix(
            $$158,
            false,
            "Rename some C&C part 2 advancements",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:husbandry/play_jukebox_in_meadows",
                  "minecraft:adventure/play_jukebox_in_meadows",
                  "minecraft:adventure/caves_and_cliff",
                  "minecraft:adventure/fall_from_world_height",
                  "minecraft:adventure/ride_strider_in_overworld_lava",
                  "minecraft:nether/ride_strider_in_overworld_lava"
               )
            )
         )
      );
      Schema $$159 = $$0.addSchema(2852, SAME_NAMESPACED);
      $$0.addFixer(new WorldGenSettingsDisallowOldCustomWorldsFix($$159));
      Schema $$160 = $$0.addSchema(2967, SAME_NAMESPACED);
      $$0.addFixer(new StructureSettingsFlattenFix($$160));
      Schema $$161 = $$0.addSchema(2970, SAME_NAMESPACED);
      $$0.addFixer(new StructuresBecomeConfiguredFix($$161));
      Schema $$162 = $$0.addSchema(3076, V3076::new);
      $$0.addFixer(new AddNewChoices($$162, "Added Sculk Catalyst", References.BLOCK_ENTITY));
      Schema $$163 = $$0.addSchema(3077, SAME_NAMESPACED);
      $$0.addFixer(new ChunkDeleteIgnoredLightDataFix($$163));
      Schema $$164 = $$0.addSchema(3078, V3078::new);
      $$0.addFixer(new AddNewChoices($$164, "Added Frog", References.ENTITY));
      $$0.addFixer(new AddNewChoices($$164, "Added Tadpole", References.ENTITY));
      $$0.addFixer(new AddNewChoices($$164, "Added Sculk Shrieker", References.BLOCK_ENTITY));
      Schema $$165 = $$0.addSchema(3081, V3081::new);
      $$0.addFixer(new AddNewChoices($$165, "Added Warden", References.ENTITY));
      Schema $$166 = $$0.addSchema(3082, V3082::new);
      $$0.addFixer(new AddNewChoices($$166, "Added Chest Boat", References.ENTITY));
      Schema $$167 = $$0.addSchema(3083, V3083::new);
      $$0.addFixer(new AddNewChoices($$167, "Added Allay", References.ENTITY));
      Schema $$168 = $$0.addSchema(3084, SAME_NAMESPACED);
      $$0.addFixer(
         new NamespacedTypeRenameFix(
            $$168,
            "game_event_renames_3084",
            References.GAME_EVENT_NAME,
            createRenamer(
               ImmutableMap.builder()
                  .put("minecraft:block_press", "minecraft:block_activate")
                  .put("minecraft:block_switch", "minecraft:block_activate")
                  .put("minecraft:block_unpress", "minecraft:block_deactivate")
                  .put("minecraft:block_unswitch", "minecraft:block_deactivate")
                  .put("minecraft:drinking_finish", "minecraft:drink")
                  .put("minecraft:elytra_free_fall", "minecraft:elytra_glide")
                  .put("minecraft:entity_damaged", "minecraft:entity_damage")
                  .put("minecraft:entity_dying", "minecraft:entity_die")
                  .put("minecraft:entity_killed", "minecraft:entity_die")
                  .put("minecraft:mob_interact", "minecraft:entity_interact")
                  .put("minecraft:ravager_roar", "minecraft:entity_roar")
                  .put("minecraft:ring_bell", "minecraft:block_change")
                  .put("minecraft:shulker_close", "minecraft:container_close")
                  .put("minecraft:shulker_open", "minecraft:container_open")
                  .put("minecraft:wolf_shaking", "minecraft:entity_shake")
                  .build()
            )
         )
      );
      Schema $$169 = $$0.addSchema(3086, SAME_NAMESPACED);
      $$0.addFixer(
         new EntityVariantFix(
            $$169,
            "Change cat variant type",
            References.ENTITY,
            "minecraft:cat",
            "CatType",
            net.minecraft.util.Util.make(new Int2ObjectOpenHashMap(), $$0x -> {
               $$0x.defaultReturnValue("minecraft:tabby");
               $$0x.put(0, "minecraft:tabby");
               $$0x.put(1, "minecraft:black");
               $$0x.put(2, "minecraft:red");
               $$0x.put(3, "minecraft:siamese");
               $$0x.put(4, "minecraft:british");
               $$0x.put(5, "minecraft:calico");
               $$0x.put(6, "minecraft:persian");
               $$0x.put(7, "minecraft:ragdoll");
               $$0x.put(8, "minecraft:white");
               $$0x.put(9, "minecraft:jellie");
               $$0x.put(10, "minecraft:all_black");
            })::get
         )
      );
      ImmutableMap<String, String> $$170 = ImmutableMap.builder()
         .put("textures/entity/cat/tabby.png", "minecraft:tabby")
         .put("textures/entity/cat/black.png", "minecraft:black")
         .put("textures/entity/cat/red.png", "minecraft:red")
         .put("textures/entity/cat/siamese.png", "minecraft:siamese")
         .put("textures/entity/cat/british_shorthair.png", "minecraft:british")
         .put("textures/entity/cat/calico.png", "minecraft:calico")
         .put("textures/entity/cat/persian.png", "minecraft:persian")
         .put("textures/entity/cat/ragdoll.png", "minecraft:ragdoll")
         .put("textures/entity/cat/white.png", "minecraft:white")
         .put("textures/entity/cat/jellie.png", "minecraft:jellie")
         .put("textures/entity/cat/all_black.png", "minecraft:all_black")
         .build();
      $$0.addFixer(
         new CriteriaRenameFix(
            $$169, "Migrate cat variant advancement", "minecraft:husbandry/complete_catalogue", $$1x -> (String)$$170.getOrDefault($$1x, $$1x)
         )
      );
      Schema $$171 = $$0.addSchema(3087, SAME_NAMESPACED);
      $$0.addFixer(
         new EntityVariantFix(
            $$171,
            "Change frog variant type",
            References.ENTITY,
            "minecraft:frog",
            "Variant",
            net.minecraft.util.Util.make(new Int2ObjectOpenHashMap(), $$0x -> {
               $$0x.put(0, "minecraft:temperate");
               $$0x.put(1, "minecraft:warm");
               $$0x.put(2, "minecraft:cold");
            })::get
         )
      );
      Schema $$172 = $$0.addSchema(3090, SAME_NAMESPACED);
      $$0.addFixer(new EntityFieldsRenameFix($$172, "EntityPaintingFieldsRenameFix", "minecraft:painting", Map.of("Motive", "variant", "Facing", "facing")));
      Schema $$173 = $$0.addSchema(3093, SAME_NAMESPACED);
      $$0.addFixer(new EntityGoatMissingStateFix($$173));
      Schema $$174 = $$0.addSchema(3094, SAME_NAMESPACED);
      $$0.addFixer(new GoatHornIdFix($$174));
      Schema $$175 = $$0.addSchema(3097, SAME_NAMESPACED);
      $$0.addFixer(new FilteredBooksFix($$175));
      $$0.addFixer(new FilteredSignsFix($$175));
      Map<String, String> $$176 = Map.of("minecraft:british", "minecraft:british_shorthair");
      $$0.addFixer(new VariantRenameFix($$175, "Rename british shorthair", References.ENTITY, "minecraft:cat", $$176));
      $$0.addFixer(
         new CriteriaRenameFix(
            $$175, "Migrate cat variant advancement for british shorthair", "minecraft:husbandry/complete_catalogue", $$1x -> $$176.getOrDefault($$1x, $$1x)
         )
      );
      $$0.addFixer(new PoiTypeRemoveFix($$175, "Remove unpopulated villager PoI types", Set.of("minecraft:unemployed", "minecraft:nitwit")::contains));
      Schema $$177 = $$0.addSchema(3108, SAME_NAMESPACED);
      $$0.addFixer(new BlendingDataRemoveFromNetherEndFix($$177));
      Schema $$178 = $$0.addSchema(3201, SAME_NAMESPACED);
      $$0.addFixer(new OptionsProgrammerArtFix($$178));
      Schema $$179 = $$0.addSchema(3202, V3202::new);
      $$0.addFixer(new AddNewChoices($$179, "Added Hanging Sign", References.BLOCK_ENTITY));
      Schema $$180 = $$0.addSchema(3203, V3203::new);
      $$0.addFixer(new AddNewChoices($$180, "Added Camel", References.ENTITY));
      Schema $$181 = $$0.addSchema(3204, V3204::new);
      $$0.addFixer(new AddNewChoices($$181, "Added Chiseled Bookshelf", References.BLOCK_ENTITY));
      Schema $$182 = $$0.addSchema(3209, SAME_NAMESPACED);
      $$0.addFixer(new ItemStackSpawnEggFix($$182, false, "minecraft:pig_spawn_egg"));
      Schema $$183 = $$0.addSchema(3214, SAME_NAMESPACED);
      $$0.addFixer(new OptionsAmbientOcclusionFix($$183));
      Schema $$184 = $$0.addSchema(3319, SAME_NAMESPACED);
      $$0.addFixer(new OptionsAccessibilityOnboardFix($$184));
      Schema $$185 = $$0.addSchema(3322, SAME_NAMESPACED);
      $$0.addFixer(new EffectDurationFix($$185));
      Schema $$186 = $$0.addSchema(3325, V3325::new);
      $$0.addFixer(new AddNewChoices($$186, "Added displays", References.ENTITY));
      Schema $$187 = $$0.addSchema(3326, V3326::new);
      $$0.addFixer(new AddNewChoices($$187, "Added Sniffer", References.ENTITY));
      Schema $$188 = $$0.addSchema(3327, V3327::new);
      $$0.addFixer(new AddNewChoices($$188, "Archaeology", References.BLOCK_ENTITY));
      Schema $$189 = $$0.addSchema(3328, V3328::new);
      $$0.addFixer(new AddNewChoices($$189, "Added interaction", References.ENTITY));
      Schema $$190 = $$0.addSchema(3438, V3438::new);
      $$0.addFixer(
         BlockEntityRenameFix.create(
            $$190, "Rename Suspicious Sand to Brushable Block", createRenamer("minecraft:suspicious_sand", "minecraft:brushable_block")
         )
      );
      $$0.addFixer(new EntityBrushableBlockFieldsRenameFix($$190));
      $$0.addFixer(
         ItemRenameFix.create(
            $$190,
            "Pottery shard renaming",
            createRenamer(
               ImmutableMap.of(
                  "minecraft:pottery_shard_archer",
                  "minecraft:archer_pottery_shard",
                  "minecraft:pottery_shard_prize",
                  "minecraft:prize_pottery_shard",
                  "minecraft:pottery_shard_arms_up",
                  "minecraft:arms_up_pottery_shard",
                  "minecraft:pottery_shard_skull",
                  "minecraft:skull_pottery_shard"
               )
            )
         )
      );
      $$0.addFixer(new AddNewChoices($$190, "Added calibrated sculk sensor", References.BLOCK_ENTITY));
      Schema $$191 = $$0.addSchema(3439, V3439::new);
      $$0.addFixer(new BlockEntitySignDoubleSidedEditableTextFix($$191, "Updated sign text format for Signs", "minecraft:sign"));
      Schema $$192 = $$0.addSchema(3439, 1, V3439_1::new);
      $$0.addFixer(new BlockEntitySignDoubleSidedEditableTextFix($$192, "Updated sign text format for Hanging Signs", "minecraft:hanging_sign"));
      Schema $$193 = $$0.addSchema(3440, SAME_NAMESPACED);
      $$0.addFixer(
         new NamespacedTypeRenameFix(
            $$193,
            "Replace experimental 1.20 overworld",
            References.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
            createRenamer("minecraft:overworld_update_1_20", "minecraft:overworld")
         )
      );
      $$0.addFixer(new FeatureFlagRemoveFix($$193, "Remove 1.20 feature toggle", Set.of("minecraft:update_1_20")));
      Schema $$194 = $$0.addSchema(3447, SAME_NAMESPACED);
      $$0.addFixer(
         ItemRenameFix.create(
            $$194,
            "Pottery shard item renaming to Pottery sherd",
            createRenamer(
               Stream.of(
                     "minecraft:angler_pottery_shard",
                     "minecraft:archer_pottery_shard",
                     "minecraft:arms_up_pottery_shard",
                     "minecraft:blade_pottery_shard",
                     "minecraft:brewer_pottery_shard",
                     "minecraft:burn_pottery_shard",
                     "minecraft:danger_pottery_shard",
                     "minecraft:explorer_pottery_shard",
                     "minecraft:friend_pottery_shard",
                     "minecraft:heart_pottery_shard",
                     "minecraft:heartbreak_pottery_shard",
                     "minecraft:howl_pottery_shard",
                     "minecraft:miner_pottery_shard",
                     "minecraft:mourner_pottery_shard",
                     "minecraft:plenty_pottery_shard",
                     "minecraft:prize_pottery_shard",
                     "minecraft:sheaf_pottery_shard",
                     "minecraft:shelter_pottery_shard",
                     "minecraft:skull_pottery_shard",
                     "minecraft:snort_pottery_shard"
                  )
                  .collect(Collectors.toMap(Function.identity(), $$0x -> $$0x.replace("_pottery_shard", "_pottery_sherd")))
            )
         )
      );
      Schema $$195 = $$0.addSchema(3448, V3448::new);
      $$0.addFixer(new DecoratedPotFieldRenameFix($$195));
      Schema $$196 = $$0.addSchema(3450, SAME_NAMESPACED);
      $$0.addFixer(
         new RemapChunkStatusFix(
            $$196,
            "Remove liquid_carvers and heightmap chunk statuses",
            createRenamer(Map.of("minecraft:liquid_carvers", "minecraft:carvers", "minecraft:heightmaps", "minecraft:spawn"))
         )
      );
      Schema $$197 = $$0.addSchema(3451, SAME_NAMESPACED);
      $$0.addFixer(new ChunkDeleteLightFix($$197));
      Schema $$198 = $$0.addSchema(3459, SAME_NAMESPACED);
      $$0.addFixer(new LegacyDragonFightFix($$198));
      Schema $$199 = $$0.addSchema(3564, SAME_NAMESPACED);
      $$0.addFixer(new DropInvalidSignDataFix($$199, "minecraft:sign"));
      Schema $$200 = $$0.addSchema(3564, 1, SAME_NAMESPACED);
      $$0.addFixer(new DropInvalidSignDataFix($$200, "minecraft:hanging_sign"));
      Schema $$201 = $$0.addSchema(3565, SAME_NAMESPACED);
      $$0.addFixer(new RandomSequenceSettingsFix($$201));
      Schema $$202 = $$0.addSchema(3566, SAME_NAMESPACED);
      $$0.addFixer(new ScoreboardDisplaySlotFix($$202));
      Schema $$203 = $$0.addSchema(3568, SAME_NAMESPACED);
      $$0.addFixer(new MobEffectIdFix($$203));
      Schema $$204 = $$0.addSchema(3682, V3682::new);
      $$0.addFixer(new AddNewChoices($$204, "Added Crafter", References.BLOCK_ENTITY));
      Schema $$205 = $$0.addSchema(3683, V3683::new);
      $$0.addFixer(new PrimedTntBlockStateFixer($$205));
      Schema $$206 = $$0.addSchema(3685, V3685::new);
      $$0.addFixer(new FixProjectileStoredItem($$206));
      Schema $$207 = $$0.addSchema(3689, V3689::new);
      $$0.addFixer(new AddNewChoices($$207, "Added Breeze", References.ENTITY));
      $$0.addFixer(new AddNewChoices($$207, "Added Trial Spawner", References.BLOCK_ENTITY));
      Schema $$208 = $$0.addSchema(3692, SAME_NAMESPACED);
      UnaryOperator<String> $$209 = createRenamer(Map.of("minecraft:grass", "minecraft:short_grass"));
      $$0.addFixer(BlockRenameFix.create($$208, "Rename grass block to short_grass", $$209));
      $$0.addFixer(ItemRenameFix.create($$208, "Rename grass item to short_grass", $$209));
      Schema $$210 = $$0.addSchema(3799, V3799::new);
      $$0.addFixer(new AddNewChoices($$210, "Added Armadillo", References.ENTITY));
      Schema $$211 = $$0.addSchema(3800, SAME_NAMESPACED);
      UnaryOperator<String> $$212 = createRenamer(Map.of("minecraft:scute", "minecraft:turtle_scute"));
      $$0.addFixer(ItemRenameFix.create($$211, "Rename scute item to turtle_scute", $$212));
      Schema $$213 = $$0.addSchema(3803, SAME_NAMESPACED);
      $$0.addFixer(new RenameEnchantmentsFix($$213, "Rename sweeping enchant to sweeping_edge", Map.of("minecraft:sweeping", "minecraft:sweeping_edge")));
      Schema $$214 = $$0.addSchema(3807, V3807::new);
      $$0.addFixer(new AddNewChoices($$214, "Added Vault", References.BLOCK_ENTITY));
      Schema $$215 = $$0.addSchema(3807, 1, SAME_NAMESPACED);
      $$0.addFixer(new MapBannerBlockPosFormatFix($$215));
      Schema $$216 = $$0.addSchema(3808, V3808::new);
      $$0.addFixer(new HorseBodyArmorItemFix($$216, "minecraft:horse", "ArmorItem", true));
      Schema $$217 = $$0.addSchema(3808, 1, V3808_1::new);
      $$0.addFixer(new HorseBodyArmorItemFix($$217, "minecraft:llama", "DecorItem", false));
      Schema $$218 = $$0.addSchema(3808, 2, V3808_2::new);
      $$0.addFixer(new HorseBodyArmorItemFix($$218, "minecraft:trader_llama", "DecorItem", false));
      Schema $$219 = $$0.addSchema(3809, SAME_NAMESPACED);
      $$0.addFixer(new ChestedHorsesInventoryZeroIndexingFix($$219));
      Schema $$220 = $$0.addSchema(3812, SAME_NAMESPACED);
      $$0.addFixer(new FixWolfHealth($$220));
      Schema $$221 = $$0.addSchema(3813, V3813::new);
      $$0.addFixer(new BlockPosFormatAndRenamesFix($$221));
      Schema $$222 = $$0.addSchema(3814, SAME_NAMESPACED);
      $$0.addFixer(
         new AttributesRenameLegacy($$222, "Rename jump strength attribute", createRenamer("minecraft:horse.jump_strength", "minecraft:generic.jump_strength"))
      );
      Schema $$223 = $$0.addSchema(3816, V3816::new);
      $$0.addFixer(new AddNewChoices($$223, "Added Bogged", References.ENTITY));
      Schema $$224 = $$0.addSchema(3818, V3818::new);
      $$0.addFixer(new BeehiveFieldRenameFix($$224));
      $$0.addFixer(new EmptyItemInHotbarFix($$224));
      Schema $$225 = $$0.addSchema(3818, 1, SAME_NAMESPACED);
      $$0.addFixer(new BannerPatternFormatFix($$225));
      Schema $$226 = $$0.addSchema(3818, 2, SAME_NAMESPACED);
      $$0.addFixer(new TippedArrowPotionToItemFix($$226));
      Schema $$227 = $$0.addSchema(3818, 3, V3818_3::new);
      $$0.addFixer(new WriteAndReadFix($$227, "Inject data component types", References.DATA_COMPONENTS));
      Schema $$228 = $$0.addSchema(3818, 4, V3818_4::new);
      $$0.addFixer(new ParticleUnflatteningFix($$228));
      Schema $$229 = $$0.addSchema(3818, 5, V3818_5::new);
      $$0.addFixer(new ItemStackComponentizationFix($$229));
      Schema $$230 = $$0.addSchema(3818, 6, SAME_NAMESPACED);
      $$0.addFixer(new AreaEffectCloudPotionFix($$230));
      Schema $$231 = $$0.addSchema(3820, SAME_NAMESPACED);
      $$0.addFixer(new PlayerHeadBlockProfileFix($$231));
      $$0.addFixer(new LodestoneCompassComponentFix($$231));
      Schema $$232 = $$0.addSchema(3825, V3825::new);
      $$0.addFixer(new ItemStackCustomNameToOverrideComponentFix($$232));
      $$0.addFixer(new BannerEntityCustomNameToOverrideComponentFix($$232));
      $$0.addFixer(new TrialSpawnerConfigFix($$232));
      $$0.addFixer(new AddNewChoices($$232, "Added Ominous Item Spawner", References.ENTITY));
      Schema $$233 = $$0.addSchema(3828, SAME_NAMESPACED);
      $$0.addFixer(new EmptyItemInVillagerTradeFix($$233));
      Schema $$234 = $$0.addSchema(3833, SAME_NAMESPACED);
      $$0.addFixer(new RemoveEmptyItemInBrushableBlockFix($$234));
      Schema $$235 = $$0.addSchema(3938, V3938::new);
      $$0.addFixer(new ProjectileStoredWeaponFix($$235));
      Schema $$236 = $$0.addSchema(3939, SAME_NAMESPACED);
      $$0.addFixer(new FeatureFlagRemoveFix($$236, "Remove 1.21 feature toggle", Set.of("minecraft:update_1_21")));
      Schema $$237 = $$0.addSchema(3943, SAME_NAMESPACED);
      $$0.addFixer(new OptionsMenuBlurrinessFix($$237));
      Schema $$238 = $$0.addSchema(3945, SAME_NAMESPACED);
      $$0.addFixer(new AttributeModifierIdFix($$238));
      $$0.addFixer(new JukeboxTicksSinceSongStartedFix($$238));
      Schema $$239 = $$0.addSchema(4054, SAME_NAMESPACED);
      $$0.addFixer(new OminousBannerRarityFix($$239));
      Schema $$240 = $$0.addSchema(4055, SAME_NAMESPACED);
      $$0.addFixer(new AttributeIdPrefixFix($$240));
      Schema $$241 = $$0.addSchema(4057, SAME_NAMESPACED);
      $$0.addFixer(new CarvingStepRemoveFix($$241));
      Schema $$242 = $$0.addSchema(4059, V4059::new);
      $$0.addFixer(new FoodToConsumableFix($$242));
      Schema $$243 = $$0.addSchema(4061, SAME_NAMESPACED);
      $$0.addFixer(new TrialSpawnerConfigInRegistryFix($$243));
      Schema $$244 = $$0.addSchema(4064, SAME_NAMESPACED);
      $$0.addFixer(new FireResistantToDamageResistantComponentFix($$244));
      Schema $$245 = $$0.addSchema(4067, V4067::new);
      $$0.addFixer(new BoatSplitFix($$245));
      $$0.addFixer(new FeatureFlagRemoveFix($$245, "Remove Bundle experimental feature flag", Set.of("minecraft:bundle")));
      Schema $$246 = $$0.addSchema(4068, SAME_NAMESPACED);
      $$0.addFixer(new LockComponentPredicateFix($$246));
      $$0.addFixer(new ContainerBlockEntityLockPredicateFix($$246));
      Schema $$247 = $$0.addSchema(4070, V4070::new);
      $$0.addFixer(new AddNewChoices($$247, "Added Pale Oak Boat and Pale Oak Chest Boat", References.ENTITY));
      Schema $$248 = $$0.addSchema(4071, V4071::new);
      $$0.addFixer(new AddNewChoices($$248, "Added Creaking", References.ENTITY));
      $$0.addFixer(new AddNewChoices($$248, "Added Creaking Heart", References.BLOCK_ENTITY));
      Schema $$249 = $$0.addSchema(4081, SAME_NAMESPACED);
      $$0.addFixer(new EntitySalmonSizeFix($$249));
      Schema $$250 = $$0.addSchema(4173, SAME_NAMESPACED);
      $$0.addFixer(new EntityFieldsRenameFix($$250, "Rename TNT Minecart fuse", "minecraft:tnt_minecart", Map.of("TNTFuse", "fuse")));
      Schema $$251 = $$0.addSchema(4175, SAME_NAMESPACED);
      $$0.addFixer(new EquippableAssetRenameFix($$251));
      $$0.addFixer(new CustomModelDataExpandFix($$251));
      Schema $$252 = $$0.addSchema(4176, SAME_NAMESPACED);
      $$0.addFixer(new InvalidBlockEntityLockFix($$252));
      $$0.addFixer(new InvalidLockComponentFix($$252));
      Schema $$253 = $$0.addSchema(4180, SAME_NAMESPACED);
      $$0.addFixer(new FeatureFlagRemoveFix($$253, "Remove Winter Drop toggle", Set.of("minecraft:winter_drop")));
      Schema $$254 = $$0.addSchema(4181, SAME_NAMESPACED);
      $$0.addFixer(new BlockEntityFurnaceBurnTimeFix($$254, "minecraft:furnace"));
      $$0.addFixer(new BlockEntityFurnaceBurnTimeFix($$254, "minecraft:smoker"));
      $$0.addFixer(new BlockEntityFurnaceBurnTimeFix($$254, "minecraft:blast_furnace"));
      Schema $$255 = $$0.addSchema(4187, SAME_NAMESPACED);
      $$0.addFixer(
         new EntityAttributeBaseValueFix(
            $$255, "Villager follow range fix undo", "minecraft:villager", "minecraft:follow_range", $$0x -> $$0x == 48.0 ? 16.0 : $$0x
         )
      );
      $$0.addFixer(
         new EntityAttributeBaseValueFix($$255, "Bee follow range fix", "minecraft:bee", "minecraft:follow_range", $$0x -> $$0x == 48.0 ? 16.0 : $$0x)
      );
      $$0.addFixer(
         new EntityAttributeBaseValueFix($$255, "Allay follow range fix", "minecraft:allay", "minecraft:follow_range", $$0x -> $$0x == 48.0 ? 16.0 : $$0x)
      );
      $$0.addFixer(
         new EntityAttributeBaseValueFix($$255, "Llama follow range fix", "minecraft:llama", "minecraft:follow_range", $$0x -> $$0x == 40.0 ? 16.0 : $$0x)
      );
      $$0.addFixer(
         new EntityAttributeBaseValueFix(
            $$255, "Piglin Brute follow range fix", "minecraft:piglin_brute", "minecraft:follow_range", $$0x -> $$0x == 16.0 ? 12.0 : $$0x
         )
      );
      $$0.addFixer(
         new EntityAttributeBaseValueFix($$255, "Warden follow range fix", "minecraft:warden", "minecraft:follow_range", $$0x -> $$0x == 16.0 ? 24.0 : $$0x)
      );
      Schema $$256 = $$0.addSchema(4290, V4290::new);
      $$0.addFixer(new UnflattenTextComponentFix($$256));
      Schema $$257 = $$0.addSchema(4291, SAME_NAMESPACED);
      $$0.addFixer(new LegacyHoverEventFix($$257));
      $$0.addFixer(new TextComponentStringifiedFlagsFix($$257));
      Schema $$258 = $$0.addSchema(4292, V4292::new);
      $$0.addFixer(new TextComponentHoverAndClickEventFix($$258));
      Schema $$259 = $$0.addSchema(4293, SAME_NAMESPACED);
      $$0.addFixer(new DropChancesFormatFix($$259));
      Schema $$260 = $$0.addSchema(4294, SAME_NAMESPACED);
      $$0.addFixer(
         new BlockPropertyRenameAndFix(
            $$260,
            "CreakingHeartBlockStateFix",
            "minecraft:creaking_heart",
            "active",
            "creaking_heart_state",
            $$0x -> $$0x.equals("true") ? "awake" : "uprooted"
         )
      );
      Schema $$261 = $$0.addSchema(4295, SAME_NAMESPACED);
      $$0.addFixer(new BlendingDataFix($$261));
      Schema $$262 = $$0.addSchema(4296, SAME_NAMESPACED);
      $$0.addFixer(new AreaEffectCloudDurationScaleFix($$262));
      Schema $$263 = $$0.addSchema(4297, SAME_NAMESPACED);
      $$0.addFixer(new ForcedChunkToTicketFix($$263));
      Schema $$264 = $$0.addSchema(4299, SAME_NAMESPACED);
      $$0.addFixer(new EntitySpawnerItemVariantComponentFix($$264));
      Schema $$265 = $$0.addSchema(4300, V4300::new);
      $$0.addFixer(new SaddleEquipmentSlotFix($$265));
      Schema $$266 = $$0.addSchema(4301, V4301::new);
      $$0.addFixer(new EquipmentFormatFix($$266));
      Schema $$267 = $$0.addSchema(4302, V4302::new);
      $$0.addFixer(new AddNewChoices($$267, "Added Test and Test Instance Block Entities", References.BLOCK_ENTITY));
      Schema $$268 = $$0.addSchema(4303, SAME_NAMESPACED);
      $$0.addFixer(new EntityFallDistanceFloatToDoubleFix($$268, References.ENTITY));
      $$0.addFixer(new EntityFallDistanceFloatToDoubleFix($$268, References.PLAYER));
      Schema $$269 = $$0.addSchema(4305, SAME_NAMESPACED);
      $$0.addFixer(new BlockPropertyRenameAndFix($$269, "rename test block mode", "minecraft:test_block", "test_block_mode", "mode", $$0x -> $$0x));
      Schema $$270 = $$0.addSchema(4306, V4306::new);
      $$0.addFixer(new ThrownPotionSplitFix($$270));
      Schema $$271 = $$0.addSchema(4307, V4307::new);
      $$0.addFixer(new TooltipDisplayComponentFix($$271));
      Schema $$272 = $$0.addSchema(4309, SAME_NAMESPACED);
      $$0.addFixer(new RaidRenamesDataFix($$272));
      $$0.addFixer(new ChunkTicketUnpackPosFix($$272));
      Schema $$273 = $$0.addSchema(4311, SAME_NAMESPACED);
      $$0.addFixer(
         new AdvancementsRenameFix(
            $$273, false, "Use lodestone category change", createRenamer("minecraft:nether/use_lodestone", "minecraft:adventure/use_lodestone")
         )
      );
      Schema $$274 = $$0.addSchema(4312, V4312::new);
      $$0.addFixer(new PlayerEquipmentFix($$274));
      Schema $$275 = $$0.addSchema(4314, SAME_NAMESPACED);
      $$0.addFixer(new InlineBlockPosFormatFix($$275));
      Schema $$276 = $$0.addSchema(4420, V4420::new);
      $$0.addFixer(new NamedEntityConvertUncheckedFix($$276, "AreaEffectCloudCustomParticleFix", References.ENTITY, "minecraft:area_effect_cloud"));
      Schema $$277 = $$0.addSchema(4421, V4421::new);
      $$0.addFixer(new AddNewChoices($$277, "Added Happy Ghast", References.ENTITY));
      Schema $$278 = $$0.addSchema(4424, SAME_NAMESPACED);
      $$0.addFixer(new FeatureFlagRemoveFix($$278, "Remove Locator Bar experimental feature flag", Set.of("minecraft:locator_bar")));
      $$0.addFixer(new AddFieldFix($$278, References.PLAYER, "style", $$0x -> $$0x.createString("minecraft:default"), "locator_bar_icon"));
      $$0.addFixer(new AddFieldFix($$278, References.ENTITY, "style", $$0x -> $$0x.createString("minecraft:default"), "locator_bar_icon"));
      Schema $$279 = $$0.addSchema(4531, V4531::new);
      $$0.addFixer(new AddNewChoices($$279, "Added Copper Golem", References.ENTITY));
      Schema $$280 = $$0.addSchema(4532, V4532::new);
      $$0.addFixer(new AddNewChoices($$280, "Added Copper Golem Statue Block Entity", References.BLOCK_ENTITY));
      Schema $$281 = $$0.addSchema(4533, V4533::new);
      $$0.addFixer(new AddNewChoices($$281, "Added Shelf", References.BLOCK_ENTITY));
      Schema $$282 = $$0.addSchema(4535, SAME_NAMESPACED);
      $$0.addFixer(new CopperGolemWeatherStateFix($$282));
      Schema $$283 = $$0.addSchema(4537, SAME_NAMESPACED);
      $$0.addFixer(new ChunkDeleteLightFix($$283));
      Schema $$284 = $$0.addSchema(4541, SAME_NAMESPACED);
      $$0.addFixer(BlockRenameFix.create($$284, "Rename chain to iron_chain", createRenamer("minecraft:chain", "minecraft:iron_chain")));
      $$0.addFixer(ItemRenameFix.create($$284, "Rename chain to iron_chain", createRenamer("minecraft:chain", "minecraft:iron_chain")));
      Schema $$285 = $$0.addSchema(4543, V4543::new);
      $$0.addFixer(new AddNewChoices($$285, "Added Mannequin", References.ENTITY));
      Schema $$286 = $$0.addSchema(4544, SAME_NAMESPACED);
      $$0.addFixer(new LegacyWorldBorderFix($$286));
      Schema $$287 = $$0.addSchema(4548, SAME_NAMESPACED);
      $$0.addFixer(new WorldSpawnDataFix($$287));
      $$0.addFixer(new PlayerRespawnDataFix($$287));
      Schema $$288 = $$0.addSchema(4648, V4648::new);
      $$0.addFixer(new AddNewChoices($$288, "Added Nautilus and Zombie Nautilus", References.ENTITY));
      Schema $$289 = $$0.addSchema(4649, SAME_NAMESPACED);
      $$0.addFixer(new TridentAnimationFix($$289));
      Schema $$290 = $$0.addSchema(4650, SAME_NAMESPACED);
      $$0.addFixer(new DebugProfileOverlayReferenceFix($$290));
      Schema $$291 = $$0.addSchema(4651, SAME_NAMESPACED);
      $$0.addFixer(new OptionsGraphicsModeSplitFix($$291, "cutoutLeaves", "false", "true", "true"));
      $$0.addFixer(new OptionsGraphicsModeSplitFix($$291, "weatherRadius", "5", "10", "10"));
      $$0.addFixer(new OptionsGraphicsModeSplitFix($$291, "vignette", "false", "true", "true"));
      $$0.addFixer(new OptionsGraphicsModeSplitFix($$291, "improvedTransparency", "false", "false", "true"));
      $$0.addFixer(new OptionsSetGraphicsPresetToCustomFix($$291));
      Schema $$292 = $$0.addSchema(4656, V4656::new);
      $$0.addFixer(new AddNewChoices($$292, "Added Parched and Camel Husk", References.ENTITY));
      Schema $$293 = $$0.addSchema(4657, SAME_NAMESPACED);
      $$0.addFixer(new WorldBorderWarningTimeFix($$293));
      Schema $$294 = $$0.addSchema(4658, SAME_NAMESPACED);
      $$0.addFixer(new GameRuleRegistryFix($$294));
      Schema $$295 = $$0.addSchema(4661, SAME_NAMESPACED);
      $$0.addFixer(new OptionsMusicToastFix($$295, false));
   }

   private static UnaryOperator<String> createRenamerNoNamespace(Map<String, String> $$0) {
      return $$1 -> $$0.getOrDefault($$1, $$1);
   }

   private static UnaryOperator<String> createRenamer(Map<String, String> $$0) {
      return $$1 -> $$0.getOrDefault(NamespacedSchema.ensureNamespaced($$1), $$1);
   }

   private static UnaryOperator<String> createRenamer(String $$0, String $$1) {
      return $$2 -> Objects.equals(NamespacedSchema.ensureNamespaced($$2), $$0) ? $$1 : $$2;
   }
}
