/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.AncientCityStructurePieces;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.data.worldgen.TrailRuinsStructurePools;
import net.minecraft.data.worldgen.TrialChambersStructurePools;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidStructure;
import net.minecraft.world.level.levelgen.structure.structures.EndCityStructure;
import net.minecraft.world.level.levelgen.structure.structures.IglooStructure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.structures.JungleTempleStructure;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinStructure;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public class Structures {
    public static void bootstrap(BootstrapContext<Structure> $$02) {
        HolderGetter<Biome> $$1 = $$02.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> $$2 = $$02.lookup(Registries.TEMPLATE_POOL);
        $$02.register(BuiltinStructures.PILLAGER_OUTPOST, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_PILLAGER_OUTPOST)).spawnOverrides(Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedList.of(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1))))).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), $$2.getOrThrow(PillagerOutpostPools.START), 7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.MINESHAFT, new MineshaftStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_MINESHAFT)).generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES).build(), MineshaftStructure.Type.NORMAL));
        $$02.register(BuiltinStructures.MINESHAFT_MESA, new MineshaftStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_MINESHAFT_MESA)).generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES).build(), MineshaftStructure.Type.MESA));
        $$02.register(BuiltinStructures.WOODLAND_MANSION, new WoodlandMansionStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_WOODLAND_MANSION))));
        $$02.register(BuiltinStructures.JUNGLE_TEMPLE, new JungleTempleStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_JUNGLE_TEMPLE))));
        $$02.register(BuiltinStructures.DESERT_PYRAMID, new DesertPyramidStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_DESERT_PYRAMID))));
        $$02.register(BuiltinStructures.IGLOO, new IglooStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_IGLOO))));
        $$02.register(BuiltinStructures.SHIPWRECK, new ShipwreckStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_SHIPWRECK)), false));
        $$02.register(BuiltinStructures.SHIPWRECK_BEACHED, new ShipwreckStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_SHIPWRECK_BEACHED)), true));
        $$02.register(BuiltinStructures.SWAMP_HUT, new SwampHutStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_SWAMP_HUT)).spawnOverrides(Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedList.of(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1))), MobCategory.CREATURE, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedList.of(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1))))).build()));
        $$02.register(BuiltinStructures.STRONGHOLD, new StrongholdStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_STRONGHOLD)).terrainAdapation(TerrainAdjustment.BURY).build()));
        $$02.register(BuiltinStructures.OCEAN_MONUMENT, new OceanMonumentStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_OCEAN_MONUMENT)).spawnOverrides(Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedList.of(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 2, 4))), MobCategory.UNDERGROUND_WATER_CREATURE, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST), MobCategory.AXOLOTLS, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST))).build()));
        $$02.register(BuiltinStructures.OCEAN_RUIN_COLD, new OceanRuinStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_COLD)), OceanRuinStructure.Type.COLD, 0.3f, 0.9f));
        $$02.register(BuiltinStructures.OCEAN_RUIN_WARM, new OceanRuinStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_WARM)), OceanRuinStructure.Type.WARM, 0.3f, 0.9f));
        $$02.register(BuiltinStructures.FORTRESS, new NetherFortressStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_NETHER_FORTRESS)).spawnOverrides(Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES))).generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION).build()));
        $$02.register(BuiltinStructures.NETHER_FOSSIL, new NetherFossilStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_NETHER_FOSSIL)).generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))));
        $$02.register(BuiltinStructures.END_CITY, new EndCityStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_END_CITY))));
        $$02.register(BuiltinStructures.BURIED_TREASURE, new BuriedTreasureStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_BURIED_TREASURE)).generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES).build()));
        $$02.register(BuiltinStructures.BASTION_REMNANT, new JigsawStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_BASTION_REMNANT)), $$2.getOrThrow(BastionPieces.START), 6, ConstantHeight.of(VerticalAnchor.absolute(33)), false));
        $$02.register(BuiltinStructures.VILLAGE_PLAINS, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_PLAINS)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), $$2.getOrThrow(PlainVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.VILLAGE_DESERT, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_DESERT)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), $$2.getOrThrow(DesertVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.VILLAGE_SAVANNA, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_SAVANNA)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), $$2.getOrThrow(SavannaVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.VILLAGE_SNOWY, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_SNOWY)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), $$2.getOrThrow(SnowyVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.VILLAGE_TAIGA, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_TAIGA)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(), $$2.getOrThrow(TaigaVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.RUINED_PORTAL_STANDARD, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_STANDARD)), List.of(new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.UNDERGROUND, 1.0f, 0.2f, false, false, true, false, 0.5f), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.2f, false, false, true, false, 0.5f))));
        $$02.register(BuiltinStructures.RUINED_PORTAL_DESERT, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_DESERT)), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED, 0.0f, 0.0f, false, false, false, false, 1.0f)));
        $$02.register(BuiltinStructures.RUINED_PORTAL_JUNGLE, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_JUNGLE)), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.8f, true, true, false, false, 1.0f)));
        $$02.register(BuiltinStructures.RUINED_PORTAL_SWAMP, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_SWAMP)), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0f, 0.5f, false, true, false, false, 1.0f)));
        $$02.register(BuiltinStructures.RUINED_PORTAL_MOUNTAIN, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)), List.of(new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN, 1.0f, 0.2f, false, false, true, false, 0.5f), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.2f, false, false, true, false, 0.5f))));
        $$02.register(BuiltinStructures.RUINED_PORTAL_OCEAN, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_OCEAN)), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0f, 0.8f, false, false, true, false, 1.0f)));
        $$02.register(BuiltinStructures.RUINED_PORTAL_NETHER, new RuinedPortalStructure(new Structure.StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_NETHER)), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_NETHER, 0.5f, 0.0f, false, false, false, true, 1.0f)));
        $$02.register(BuiltinStructures.ANCIENT_CITY, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_ANCIENT_CITY)).spawnOverrides(Arrays.stream(MobCategory.values()).collect(Collectors.toMap($$0 -> $$0, $$0 -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedList.of())))).generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION).terrainAdapation(TerrainAdjustment.BEARD_BOX).build(), $$2.getOrThrow(AncientCityStructurePieces.START), Optional.of(Identifier.withDefaultNamespace("city_anchor")), 7, ConstantHeight.of(VerticalAnchor.absolute(-27)), false, Optional.empty(), new JigsawStructure.MaxDistance(116), List.of(), JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS));
        $$02.register(BuiltinStructures.TRAIL_RUINS, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_TRAIL_RUINS)).generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES).terrainAdapation(TerrainAdjustment.BURY).build(), $$2.getOrThrow(TrailRuinsStructurePools.START), 7, ConstantHeight.of(VerticalAnchor.absolute(-15)), false, Heightmap.Types.WORLD_SURFACE_WG));
        $$02.register(BuiltinStructures.TRIAL_CHAMBERS, new JigsawStructure(new Structure.StructureSettings.Builder($$1.getOrThrow(BiomeTags.HAS_TRIAL_CHAMBERS)).generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES).terrainAdapation(TerrainAdjustment.ENCAPSULATE).spawnOverrides(Arrays.stream(MobCategory.values()).collect(Collectors.toMap($$0 -> $$0, $$0 -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedList.of())))).build(), $$2.getOrThrow(TrialChambersStructurePools.START), Optional.empty(), 20, UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)), false, Optional.empty(), new JigsawStructure.MaxDistance(116), TrialChambersStructurePools.ALIAS_BINDINGS, new DimensionPadding(10), LiquidSettings.IGNORE_WATERLOGGING));
    }
}

