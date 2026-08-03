package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SinglePoolElement extends StructurePoolElement {
   private static final Comparator<StructureTemplate.JigsawBlockInfo> HIGHEST_SELECTION_PRIORITY_FIRST = Comparator.comparingInt(
         StructureTemplate.JigsawBlockInfo::selectionPriority
      )
      .reversed();
   private static final Codec<Either<Identifier, StructureTemplate>> TEMPLATE_CODEC = Codec.of(
      SinglePoolElement::encodeTemplate, Identifier.CODEC.map(Either::left)
   );
   public static final MapCodec<SinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec()).apply($$0, SinglePoolElement::new)
   );
   protected final Either<Identifier, StructureTemplate> template;
   protected final Holder<StructureProcessorList> processors;
   protected final Optional<LiquidSettings> overrideLiquidSettings;

   private static <T> DataResult<T> encodeTemplate(Either<Identifier, StructureTemplate> $$0, DynamicOps<T> $$1, T $$2) {
      Optional<Identifier> $$3 = $$0.left();
      return $$3.isEmpty() ? DataResult.error(() -> "Can not serialize a runtime pool element") : Identifier.CODEC.encode($$3.get(), $$1, $$2);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Holder<StructureProcessorList>> processorsCodec() {
      return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter($$0 -> $$0.processors);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Optional<LiquidSettings>> overrideLiquidSettingsCodec() {
      return LiquidSettings.CODEC.optionalFieldOf("override_liquid_settings").forGetter($$0 -> $$0.overrideLiquidSettings);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<Identifier, StructureTemplate>> templateCodec() {
      return TEMPLATE_CODEC.fieldOf("location").forGetter($$0 -> $$0.template);
   }

   protected SinglePoolElement(
      Either<Identifier, StructureTemplate> $$0, Holder<StructureProcessorList> $$1, StructureTemplatePool.Projection $$2, Optional<LiquidSettings> $$3
   ) {
      super($$2);
      this.template = $$0;
      this.processors = $$1;
      this.overrideLiquidSettings = $$3;
   }

   @Override
   public Vec3i getSize(StructureTemplateManager $$0, Rotation $$1) {
      StructureTemplate $$2 = this.getTemplate($$0);
      return $$2.getSize($$1);
   }

   private StructureTemplate getTemplate(StructureTemplateManager $$0) {
      return (StructureTemplate)this.template.map($$0::getOrCreate, Function.identity());
   }

   public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, boolean $$3) {
      StructureTemplate $$4 = this.getTemplate($$0);
      List<StructureTemplate.StructureBlockInfo> $$5 = $$4.filterBlocks($$1, new StructurePlaceSettings().setRotation($$2), Blocks.STRUCTURE_BLOCK, $$3);
      List<StructureTemplate.StructureBlockInfo> $$6 = Lists.newArrayList();

      for (StructureTemplate.StructureBlockInfo $$7 : $$5) {
         CompoundTag $$8 = $$7.nbt();
         if ($$8 != null) {
            StructureMode $$9 = (StructureMode)$$8.read("mode", StructureMode.LEGACY_CODEC).orElseThrow();
            if ($$9 == StructureMode.DATA) {
               $$6.add($$7);
            }
         }
      }

      return $$6;
   }

   @Override
   public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, RandomSource $$3) {
      List<StructureTemplate.JigsawBlockInfo> $$4 = this.getTemplate($$0).getJigsaws($$1, $$2);
      Util.shuffle($$4, $$3);
      sortBySelectionPriority($$4);
      return $$4;
   }

   @VisibleForTesting
   static void sortBySelectionPriority(List<StructureTemplate.JigsawBlockInfo> $$0) {
      $$0.sort(HIGHEST_SELECTION_PRIORITY_FIRST);
   }

   @Override
   public BoundingBox getBoundingBox(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2) {
      StructureTemplate $$3 = this.getTemplate($$0);
      return $$3.getBoundingBox(new StructurePlaceSettings().setRotation($$2), $$1);
   }

   @Override
   public boolean place(
      StructureTemplateManager $$0,
      net.minecraft.world.level.WorldGenLevel $$1,
      net.minecraft.world.level.StructureManager $$2,
      ChunkGenerator $$3,
      BlockPos $$4,
      BlockPos $$5,
      Rotation $$6,
      BoundingBox $$7,
      RandomSource $$8,
      LiquidSettings $$9,
      boolean $$10
   ) {
      StructureTemplate $$11 = this.getTemplate($$0);
      StructurePlaceSettings $$12 = this.getSettings($$6, $$7, $$9, $$10);
      if (!$$11.placeInWorld($$1, $$4, $$5, $$12, $$8, 18)) {
         return false;
      } else {
         for (StructureTemplate.StructureBlockInfo $$14 : StructureTemplate.processBlockInfos($$1, $$4, $$5, $$12, this.getDataMarkers($$0, $$4, $$6, false))) {
            this.handleDataMarker($$1, $$14, $$4, $$6, $$8, $$7);
         }

         return true;
      }
   }

   protected StructurePlaceSettings getSettings(Rotation $$0, BoundingBox $$1, LiquidSettings $$2, boolean $$3) {
      StructurePlaceSettings $$4 = new StructurePlaceSettings();
      $$4.setBoundingBox($$1);
      $$4.setRotation($$0);
      $$4.setKnownShape(true);
      $$4.setIgnoreEntities(false);
      $$4.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      $$4.setFinalizeEntities(true);
      $$4.setLiquidSettings(this.overrideLiquidSettings.orElse($$2));
      if (!$$3) {
         $$4.addProcessor(JigsawReplacementProcessor.INSTANCE);
      }

      ((StructureProcessorList)this.processors.value()).list().forEach($$4::addProcessor);
      this.getProjection().getProcessors().forEach($$4::addProcessor);
      return $$4;
   }

   @Override
   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.SINGLE;
   }

   @Override
   public String toString() {
      return "Single[" + this.template + "]";
   }

   @VisibleForTesting
   public Identifier getTemplateLocation() {
      return (Identifier)this.template.orThrow();
   }
}
