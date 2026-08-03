package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;

public abstract class StructurePoolElement {
   public static final Codec<StructurePoolElement> CODEC = BuiltInRegistries.STRUCTURE_POOL_ELEMENT
      .byNameCodec()
      .dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
   private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));
   @Nullable
   private volatile StructureTemplatePool.Projection projection;

   protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
      return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
   }

   protected StructurePoolElement(StructureTemplatePool.Projection $$0) {
      this.projection = $$0;
   }

   public abstract Vec3i getSize(StructureTemplateManager var1, Rotation var2);

   public abstract List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(
      StructureTemplateManager var1, BlockPos var2, Rotation var3, RandomSource var4
   );

   public abstract BoundingBox getBoundingBox(StructureTemplateManager var1, BlockPos var2, Rotation var3);

   public abstract boolean place(
      StructureTemplateManager var1,
      net.minecraft.world.level.WorldGenLevel var2,
      net.minecraft.world.level.StructureManager var3,
      ChunkGenerator var4,
      BlockPos var5,
      BlockPos var6,
      Rotation var7,
      BoundingBox var8,
      RandomSource var9,
      LiquidSettings var10,
      boolean var11
   );

   public abstract StructurePoolElementType<?> getType();

   public void handleDataMarker(
      net.minecraft.world.level.LevelAccessor $$0, StructureTemplate.StructureBlockInfo $$1, BlockPos $$2, Rotation $$3, RandomSource $$4, BoundingBox $$5
   ) {
   }

   public StructurePoolElement setProjection(StructureTemplatePool.Projection $$0) {
      this.projection = $$0;
      return this;
   }

   public StructureTemplatePool.Projection getProjection() {
      StructureTemplatePool.Projection $$0 = this.projection;
      if ($$0 == null) {
         throw new IllegalStateException();
      } else {
         return $$0;
      }
   }

   public int getGroundLevelDelta() {
      return 1;
   }

   public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
      return $$0 -> EmptyPoolElement.INSTANCE;
   }

   public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String $$0) {
      return $$1 -> new LegacySinglePoolElement(Either.left(Identifier.parse($$0)), EMPTY, $$1, Optional.empty());
   }

   public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String $$0, Holder<StructureProcessorList> $$1) {
      return $$2 -> new LegacySinglePoolElement(Either.left(Identifier.parse($$0)), $$1, $$2, Optional.empty());
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String $$0) {
      return $$1 -> new SinglePoolElement(Either.left(Identifier.parse($$0)), EMPTY, $$1, Optional.empty());
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String $$0, Holder<StructureProcessorList> $$1) {
      return $$2 -> new SinglePoolElement(Either.left(Identifier.parse($$0)), $$1, $$2, Optional.empty());
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String $$0, LiquidSettings $$1) {
      return $$2 -> new SinglePoolElement(Either.left(Identifier.parse($$0)), EMPTY, $$2, Optional.of($$1));
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String $$0, Holder<StructureProcessorList> $$1, LiquidSettings $$2) {
      return $$3 -> new SinglePoolElement(Either.left(Identifier.parse($$0)), $$1, $$3, Optional.of($$2));
   }

   public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(Holder<PlacedFeature> $$0) {
      return $$1 -> new FeaturePoolElement($$0, $$1);
   }

   public static Function<StructureTemplatePool.Projection, ListPoolElement> list(
      List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> $$0
   ) {
      return $$1 -> new ListPoolElement($$0.stream().map($$1x -> (StructurePoolElement)$$1x.apply($$1)).collect(Collectors.toList()), $$1);
   }
}
