package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructurePlaceSettings {
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private BlockPos rotationPivot = BlockPos.ZERO;
   private boolean ignoreEntities;
   
   private BoundingBox boundingBox;
   private LiquidSettings liquidSettings = LiquidSettings.APPLY_WATERLOGGING;
   
   private RandomSource random;
   private int palette;
   private final List<StructureProcessor> processors = Lists.newArrayList();
   private boolean knownShape;
   private boolean finalizeEntities;

   public StructurePlaceSettings copy() {
      StructurePlaceSettings $$0 = new StructurePlaceSettings();
      $$0.mirror = this.mirror;
      $$0.rotation = this.rotation;
      $$0.rotationPivot = this.rotationPivot;
      $$0.ignoreEntities = this.ignoreEntities;
      $$0.boundingBox = this.boundingBox;
      $$0.liquidSettings = this.liquidSettings;
      $$0.random = this.random;
      $$0.palette = this.palette;
      $$0.processors.addAll(this.processors);
      $$0.knownShape = this.knownShape;
      $$0.finalizeEntities = this.finalizeEntities;
      return $$0;
   }

   public StructurePlaceSettings setMirror(Mirror $$0) {
      this.mirror = $$0;
      return this;
   }

   public StructurePlaceSettings setRotation(Rotation $$0) {
      this.rotation = $$0;
      return this;
   }

   public StructurePlaceSettings setRotationPivot(BlockPos $$0) {
      this.rotationPivot = $$0;
      return this;
   }

   public StructurePlaceSettings setIgnoreEntities(boolean $$0) {
      this.ignoreEntities = $$0;
      return this;
   }

   public StructurePlaceSettings setBoundingBox(BoundingBox $$0) {
      this.boundingBox = $$0;
      return this;
   }

   public StructurePlaceSettings setRandom(RandomSource $$0) {
      this.random = $$0;
      return this;
   }

   public StructurePlaceSettings setLiquidSettings(LiquidSettings $$0) {
      this.liquidSettings = $$0;
      return this;
   }

   public StructurePlaceSettings setKnownShape(boolean $$0) {
      this.knownShape = $$0;
      return this;
   }

   public StructurePlaceSettings clearProcessors() {
      this.processors.clear();
      return this;
   }

   public StructurePlaceSettings addProcessor(StructureProcessor $$0) {
      this.processors.add($$0);
      return this;
   }

   public StructurePlaceSettings popProcessor(StructureProcessor $$0) {
      this.processors.remove($$0);
      return this;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public BlockPos getRotationPivot() {
      return this.rotationPivot;
   }

   public RandomSource getRandom(BlockPos $$0) {
      if (this.random != null) {
         return this.random;
      } else {
         return $$0 == null ? RandomSource.create(Util.getMillis()) : RandomSource.create(Mth.getSeed($$0));
      }
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   
   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public boolean getKnownShape() {
      return this.knownShape;
   }

   public List<StructureProcessor> getProcessors() {
      return this.processors;
   }

   public boolean shouldApplyWaterlogging() {
      return this.liquidSettings == LiquidSettings.APPLY_WATERLOGGING;
   }

   public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> $$0, BlockPos $$1) {
      int $$2 = $$0.size();
      if ($$2 == 0) {
         throw new IllegalStateException("No palettes");
      } else {
         return $$0.get(this.getRandom($$1).nextInt($$2));
      }
   }

   public StructurePlaceSettings setFinalizeEntities(boolean $$0) {
      this.finalizeEntities = $$0;
      return this;
   }

   public boolean shouldFinalizeEntities() {
      return this.finalizeEntities;
   }
}
