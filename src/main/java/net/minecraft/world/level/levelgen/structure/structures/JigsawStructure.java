package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class JigsawStructure extends Structure {
   public static final DimensionPadding DEFAULT_DIMENSION_PADDING = DimensionPadding.ZERO;
   public static final LiquidSettings DEFAULT_LIQUID_SETTINGS = LiquidSettings.APPLY_WATERLOGGING;
   public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
   public static final int MIN_DEPTH = 0;
   public static final int MAX_DEPTH = 20;
   public static final MapCodec<JigsawStructure> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               settingsCodec($$0),
               StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter($$0x -> $$0x.startPool),
               Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter($$0x -> $$0x.startJigsawName),
               Codec.intRange(0, 20).fieldOf("size").forGetter($$0x -> $$0x.maxDepth),
               HeightProvider.CODEC.fieldOf("start_height").forGetter($$0x -> $$0x.startHeight),
               Codec.BOOL.fieldOf("use_expansion_hack").forGetter($$0x -> $$0x.useExpansionHack),
               Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter($$0x -> $$0x.projectStartToHeightmap),
               JigsawStructure.MaxDistance.CODEC.fieldOf("max_distance_from_center").forGetter($$0x -> $$0x.maxDistanceFromCenter),
               Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter($$0x -> $$0x.poolAliases),
               DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DEFAULT_DIMENSION_PADDING).forGetter($$0x -> $$0x.dimensionPadding),
               LiquidSettings.CODEC.optionalFieldOf("liquid_settings", DEFAULT_LIQUID_SETTINGS).forGetter($$0x -> $$0x.liquidSettings)
            )
            .apply($$0, JigsawStructure::new)
      )
      .validate(JigsawStructure::verifyRange);
   private final Holder<StructureTemplatePool> startPool;
   private final Optional<Identifier> startJigsawName;
   private final int maxDepth;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final Optional<Heightmap.Types> projectStartToHeightmap;
   private final JigsawStructure.MaxDistance maxDistanceFromCenter;
   private final List<PoolAliasBinding> poolAliases;
   private final DimensionPadding dimensionPadding;
   private final LiquidSettings liquidSettings;

   private static DataResult<JigsawStructure> verifyRange(JigsawStructure $$0) {
      int $$1 = switch ($$0.terrainAdaptation()) {
         case NONE -> 0;
         case BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE -> 12;
      };
      return $$0.maxDistanceFromCenter.horizontal() + $$1 > 128
         ? DataResult.error(() -> "Horizontal structure size including terrain adaptation must not exceed 128")
         : DataResult.success($$0);
   }

   public JigsawStructure(
      Structure.StructureSettings $$0,
      Holder<StructureTemplatePool> $$1,
      Optional<Identifier> $$2,
      int $$3,
      HeightProvider $$4,
      boolean $$5,
      Optional<Heightmap.Types> $$6,
      JigsawStructure.MaxDistance $$7,
      List<PoolAliasBinding> $$8,
      DimensionPadding $$9,
      LiquidSettings $$10
   ) {
      super($$0);
      this.startPool = $$1;
      this.startJigsawName = $$2;
      this.maxDepth = $$3;
      this.startHeight = $$4;
      this.useExpansionHack = $$5;
      this.projectStartToHeightmap = $$6;
      this.maxDistanceFromCenter = $$7;
      this.poolAliases = $$8;
      this.dimensionPadding = $$9;
      this.liquidSettings = $$10;
   }

   public JigsawStructure(Structure.StructureSettings $$0, Holder<StructureTemplatePool> $$1, int $$2, HeightProvider $$3, boolean $$4, Heightmap.Types $$5) {
      this(
         $$0,
         $$1,
         Optional.empty(),
         $$2,
         $$3,
         $$4,
         Optional.of($$5),
         new JigsawStructure.MaxDistance(80),
         List.of(),
         DEFAULT_DIMENSION_PADDING,
         DEFAULT_LIQUID_SETTINGS
      );
   }

   public JigsawStructure(Structure.StructureSettings $$0, Holder<StructureTemplatePool> $$1, int $$2, HeightProvider $$3, boolean $$4) {
      this(
         $$0,
         $$1,
         Optional.empty(),
         $$2,
         $$3,
         $$4,
         Optional.empty(),
         new JigsawStructure.MaxDistance(80),
         List.of(),
         DEFAULT_DIMENSION_PADDING,
         DEFAULT_LIQUID_SETTINGS
      );
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      net.minecraft.world.level.ChunkPos $$1 = $$0.chunkPos();
      int $$2 = this.startHeight.sample($$0.random(), new WorldGenerationContext($$0.chunkGenerator(), $$0.heightAccessor()));
      BlockPos $$3 = new BlockPos($$1.getMinBlockX(), $$2, $$1.getMinBlockZ());
      return JigsawPlacement.addPieces(
         $$0,
         this.startPool,
         this.startJigsawName,
         this.maxDepth,
         $$3,
         this.useExpansionHack,
         this.projectStartToHeightmap,
         this.maxDistanceFromCenter,
         PoolAliasLookup.create(this.poolAliases, $$3, $$0.seed()),
         this.dimensionPadding,
         this.liquidSettings
      );
   }

   @Override
   public StructureType<?> type() {
      return StructureType.JIGSAW;
   }

   @VisibleForTesting
   public Holder<StructureTemplatePool> getStartPool() {
      return this.startPool;
   }

   @VisibleForTesting
   public List<PoolAliasBinding> getPoolAliases() {
      return this.poolAliases;
   }

   public record MaxDistance(int horizontal, int vertical) {
      private static final Codec<Integer> HORIZONTAL_VALUE_CODEC = Codec.intRange(1, 128);
      private static final Codec<JigsawStructure.MaxDistance> FULL_CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               HORIZONTAL_VALUE_CODEC.fieldOf("horizontal").forGetter(JigsawStructure.MaxDistance::horizontal),
               ExtraCodecs.intRange(1, DimensionType.Y_SIZE).optionalFieldOf("vertical", DimensionType.Y_SIZE).forGetter(JigsawStructure.MaxDistance::vertical)
            )
            .apply($$0, JigsawStructure.MaxDistance::new)
      );
      public static final Codec<JigsawStructure.MaxDistance> CODEC = Codec.either(FULL_CODEC, HORIZONTAL_VALUE_CODEC)
         .xmap(
            $$0 -> (JigsawStructure.MaxDistance)$$0.map(Function.identity(), JigsawStructure.MaxDistance::new),
            $$0 -> $$0.horizontal == $$0.vertical ? Either.right($$0.horizontal) : Either.left($$0)
         );

      public MaxDistance(int $$0) {
         this($$0, $$0);
      }
   }
}
