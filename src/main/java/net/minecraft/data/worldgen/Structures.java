package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.Structure.StructureSettings;
import net.minecraft.world.level.levelgen.structure.Structure.StructureSettings.Builder;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType;
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
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.MaxDistance;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure.Type;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece.VerticalPlacement;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure.Setup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public class Structures {
   public static void bootstrap(BootstrapContext<Structure> $$0) {
      HolderGetter<Biome> $$1 = $$0.lookup(Registries.BIOME);
      HolderGetter<StructureTemplatePool> $$2 = $$0.lookup(Registries.TEMPLATE_POOL);
      $$0.register(
         BuiltinStructures.PILLAGER_OUTPOST,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_PILLAGER_OUTPOST))
               .spawnOverrides(
                  Map.of(
                     MobCategory.MONSTER, new StructureSpawnOverride(BoundingBoxType.STRUCTURE, WeightedList.of(new SpawnerData(EntityType.PILLAGER, 1, 1)))
                  )
               )
               .terrainAdapation(TerrainAdjustment.BEARD_THIN)
               .build(),
            $$2.getOrThrow(PillagerOutpostPools.START),
            7,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.MINESHAFT,
         new MineshaftStructure(new Builder($$1.getOrThrow(BiomeTags.HAS_MINESHAFT)).generationStep(Decoration.UNDERGROUND_STRUCTURES).build(), Type.NORMAL)
      );
      $$0.register(
         BuiltinStructures.MINESHAFT_MESA,
         new MineshaftStructure(new Builder($$1.getOrThrow(BiomeTags.HAS_MINESHAFT_MESA)).generationStep(Decoration.UNDERGROUND_STRUCTURES).build(), Type.MESA)
      );
      $$0.register(BuiltinStructures.WOODLAND_MANSION, new WoodlandMansionStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_WOODLAND_MANSION))));
      $$0.register(BuiltinStructures.JUNGLE_TEMPLE, new JungleTempleStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_JUNGLE_TEMPLE))));
      $$0.register(BuiltinStructures.DESERT_PYRAMID, new DesertPyramidStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_DESERT_PYRAMID))));
      $$0.register(BuiltinStructures.IGLOO, new IglooStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_IGLOO))));
      $$0.register(BuiltinStructures.SHIPWRECK, new ShipwreckStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_SHIPWRECK)), false));
      $$0.register(BuiltinStructures.SHIPWRECK_BEACHED, new ShipwreckStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_SHIPWRECK_BEACHED)), true));
      $$0.register(
         BuiltinStructures.SWAMP_HUT,
         new SwampHutStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_SWAMP_HUT))
               .spawnOverrides(
                  Map.of(
                     MobCategory.MONSTER,
                     new StructureSpawnOverride(BoundingBoxType.PIECE, WeightedList.of(new SpawnerData(EntityType.WITCH, 1, 1))),
                     MobCategory.CREATURE,
                     new StructureSpawnOverride(BoundingBoxType.PIECE, WeightedList.of(new SpawnerData(EntityType.CAT, 1, 1)))
                  )
               )
               .build()
         )
      );
      $$0.register(
         BuiltinStructures.STRONGHOLD,
         new StrongholdStructure(new Builder($$1.getOrThrow(BiomeTags.HAS_STRONGHOLD)).terrainAdapation(TerrainAdjustment.BURY).build())
      );
      $$0.register(
         BuiltinStructures.OCEAN_MONUMENT,
         new OceanMonumentStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_OCEAN_MONUMENT))
               .spawnOverrides(
                  Map.of(
                     MobCategory.MONSTER,
                     new StructureSpawnOverride(BoundingBoxType.STRUCTURE, WeightedList.of(new SpawnerData(EntityType.GUARDIAN, 2, 4))),
                     MobCategory.UNDERGROUND_WATER_CREATURE,
                     new StructureSpawnOverride(BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST),
                     MobCategory.AXOLOTLS,
                     new StructureSpawnOverride(BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST)
                  )
               )
               .build()
         )
      );
      $$0.register(
         BuiltinStructures.OCEAN_RUIN_COLD,
         new OceanRuinStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_COLD)),
            net.minecraft.world.level.levelgen.structure.structures.OceanRuinStructure.Type.COLD,
            0.3F,
            0.9F
         )
      );
      $$0.register(
         BuiltinStructures.OCEAN_RUIN_WARM,
         new OceanRuinStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_WARM)),
            net.minecraft.world.level.levelgen.structure.structures.OceanRuinStructure.Type.WARM,
            0.3F,
            0.9F
         )
      );
      $$0.register(
         BuiltinStructures.FORTRESS,
         new NetherFortressStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_NETHER_FORTRESS))
               .spawnOverrides(Map.of(MobCategory.MONSTER, new StructureSpawnOverride(BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES)))
               .generationStep(Decoration.UNDERGROUND_DECORATION)
               .build()
         )
      );
      $$0.register(
         BuiltinStructures.NETHER_FOSSIL,
         new NetherFossilStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_NETHER_FOSSIL))
               .generationStep(Decoration.UNDERGROUND_DECORATION)
               .terrainAdapation(TerrainAdjustment.BEARD_THIN)
               .build(),
            UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))
         )
      );
      $$0.register(BuiltinStructures.END_CITY, new EndCityStructure(new StructureSettings($$1.getOrThrow(BiomeTags.HAS_END_CITY))));
      $$0.register(
         BuiltinStructures.BURIED_TREASURE,
         new BuriedTreasureStructure(new Builder($$1.getOrThrow(BiomeTags.HAS_BURIED_TREASURE)).generationStep(Decoration.UNDERGROUND_STRUCTURES).build())
      );
      $$0.register(
         BuiltinStructures.BASTION_REMNANT,
         new JigsawStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_BASTION_REMNANT)),
            $$2.getOrThrow(BastionPieces.START),
            6,
            ConstantHeight.of(VerticalAnchor.absolute(33)),
            false
         )
      );
      $$0.register(
         BuiltinStructures.VILLAGE_PLAINS,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_PLAINS)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
            $$2.getOrThrow(PlainVillagePools.START),
            6,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.VILLAGE_DESERT,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_DESERT)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
            $$2.getOrThrow(DesertVillagePools.START),
            6,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.VILLAGE_SAVANNA,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_SAVANNA)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
            $$2.getOrThrow(SavannaVillagePools.START),
            6,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.VILLAGE_SNOWY,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_SNOWY)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
            $$2.getOrThrow(SnowyVillagePools.START),
            6,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.VILLAGE_TAIGA,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_VILLAGE_TAIGA)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
            $$2.getOrThrow(TaigaVillagePools.START),
            6,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_STANDARD,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_STANDARD)),
            List.of(
               new Setup(VerticalPlacement.UNDERGROUND, 1.0F, 0.2F, false, false, true, false, 0.5F),
               new Setup(VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
            )
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_DESERT,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_DESERT)),
            new Setup(VerticalPlacement.PARTLY_BURIED, 0.0F, 0.0F, false, false, false, false, 1.0F)
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_JUNGLE,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_JUNGLE)),
            new Setup(VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.8F, true, true, false, false, 1.0F)
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_SWAMP,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_SWAMP)),
            new Setup(VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.5F, false, true, false, false, 1.0F)
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_MOUNTAIN,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)),
            List.of(
               new Setup(VerticalPlacement.IN_MOUNTAIN, 1.0F, 0.2F, false, false, true, false, 0.5F),
               new Setup(VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
            )
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_OCEAN,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_OCEAN)),
            new Setup(VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.8F, false, false, true, false, 1.0F)
         )
      );
      $$0.register(
         BuiltinStructures.RUINED_PORTAL_NETHER,
         new RuinedPortalStructure(
            new StructureSettings($$1.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_NETHER)),
            new Setup(VerticalPlacement.IN_NETHER, 0.5F, 0.0F, false, false, false, true, 1.0F)
         )
      );
      $$0.register(
         BuiltinStructures.ANCIENT_CITY,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_ANCIENT_CITY))
               .spawnOverrides(
                  Arrays.stream(MobCategory.values())
                     .collect(Collectors.toMap($$0x -> $$0x, $$0x -> new StructureSpawnOverride(BoundingBoxType.STRUCTURE, WeightedList.of())))
               )
               .generationStep(Decoration.UNDERGROUND_DECORATION)
               .terrainAdapation(TerrainAdjustment.BEARD_BOX)
               .build(),
            $$2.getOrThrow(AncientCityStructurePieces.START),
            Optional.of(Identifier.withDefaultNamespace("city_anchor")),
            7,
            ConstantHeight.of(VerticalAnchor.absolute(-27)),
            false,
            Optional.empty(),
            new MaxDistance(116),
            List.of(),
            JigsawStructure.DEFAULT_DIMENSION_PADDING,
            JigsawStructure.DEFAULT_LIQUID_SETTINGS
         )
      );
      $$0.register(
         BuiltinStructures.TRAIL_RUINS,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_TRAIL_RUINS))
               .generationStep(Decoration.UNDERGROUND_STRUCTURES)
               .terrainAdapation(TerrainAdjustment.BURY)
               .build(),
            $$2.getOrThrow(TrailRuinsStructurePools.START),
            7,
            ConstantHeight.of(VerticalAnchor.absolute(-15)),
            false,
            Types.WORLD_SURFACE_WG
         )
      );
      $$0.register(
         BuiltinStructures.TRIAL_CHAMBERS,
         new JigsawStructure(
            new Builder($$1.getOrThrow(BiomeTags.HAS_TRIAL_CHAMBERS))
               .generationStep(Decoration.UNDERGROUND_STRUCTURES)
               .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
               .spawnOverrides(
                  Arrays.stream(MobCategory.values())
                     .collect(Collectors.toMap($$0x -> $$0x, $$0x -> new StructureSpawnOverride(BoundingBoxType.PIECE, WeightedList.of())))
               )
               .build(),
            $$2.getOrThrow(TrialChambersStructurePools.START),
            Optional.empty(),
            20,
            UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)),
            false,
            Optional.empty(),
            new MaxDistance(116),
            TrialChambersStructurePools.ALIAS_BINDINGS,
            new DimensionPadding(10),
            LiquidSettings.IGNORE_WATERLOGGING
         )
      );
   }
}
