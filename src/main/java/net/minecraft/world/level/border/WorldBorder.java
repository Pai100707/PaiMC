package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder extends SavedData {
   public static final double MAX_SIZE = 5.999997E7F;
   public static final double MAX_CENTER_COORDINATE = 2.9999984E7;
   public static final Codec<WorldBorder> CODEC = WorldBorder.Settings.CODEC.xmap(WorldBorder::new, WorldBorder.Settings::new);
   public static final SavedDataType<WorldBorder> TYPE = new SavedDataType<>("world_border", WorldBorder::new, CODEC, DataFixTypes.SAVED_DATA_WORLD_BORDER);
   private final WorldBorder.Settings settings;
   private boolean initialized;
   private final List<BorderChangeListener> listeners = Lists.newArrayList();
   double damagePerBlock = 0.2;
   double safeZone = 5.0;
   int warningTime = 15;
   int warningBlocks = 5;
   double centerX;
   double centerZ;
   int absoluteMaxSize = 29999984;
   WorldBorder.BorderExtent extent = new WorldBorder.StaticBorderExtent(5.999997E7F);

   public WorldBorder() {
      this(WorldBorder.Settings.DEFAULT);
   }

   public WorldBorder(WorldBorder.Settings $$0) {
      this.settings = $$0;
   }

   public boolean isWithinBounds(BlockPos $$0) {
      return this.isWithinBounds($$0.getX(), $$0.getZ());
   }

   public boolean isWithinBounds(Vec3 $$0) {
      return this.isWithinBounds($$0.x, $$0.z);
   }

   public boolean isWithinBounds(net.minecraft.world.level.ChunkPos $$0) {
      return this.isWithinBounds($$0.getMinBlockX(), $$0.getMinBlockZ()) && this.isWithinBounds($$0.getMaxBlockX(), $$0.getMaxBlockZ());
   }

   public boolean isWithinBounds(AABB $$0) {
      return this.isWithinBounds($$0.minX, $$0.minZ, $$0.maxX - 1.0E-5F, $$0.maxZ - 1.0E-5F);
   }

   private boolean isWithinBounds(double $$0, double $$1, double $$2, double $$3) {
      return this.isWithinBounds($$0, $$1) && this.isWithinBounds($$2, $$3);
   }

   public boolean isWithinBounds(double $$0, double $$1) {
      return this.isWithinBounds($$0, $$1, 0.0);
   }

   public boolean isWithinBounds(double $$0, double $$1, double $$2) {
      return $$0 >= this.getMinX() - $$2 && $$0 < this.getMaxX() + $$2 && $$1 >= this.getMinZ() - $$2 && $$1 < this.getMaxZ() + $$2;
   }

   public BlockPos clampToBounds(BlockPos $$0) {
      return this.clampToBounds($$0.getX(), $$0.getY(), $$0.getZ());
   }

   public BlockPos clampToBounds(Vec3 $$0) {
      return this.clampToBounds($$0.x(), $$0.y(), $$0.z());
   }

   public BlockPos clampToBounds(double $$0, double $$1, double $$2) {
      return BlockPos.containing(this.clampVec3ToBound($$0, $$1, $$2));
   }

   public Vec3 clampVec3ToBound(Vec3 $$0) {
      return this.clampVec3ToBound($$0.x, $$0.y, $$0.z);
   }

   public Vec3 clampVec3ToBound(double $$0, double $$1, double $$2) {
      return new Vec3(Mth.clamp($$0, this.getMinX(), this.getMaxX() - 1.0E-5F), $$1, Mth.clamp($$2, this.getMinZ(), this.getMaxZ() - 1.0E-5F));
   }

   public double getDistanceToBorder(Entity $$0) {
      return this.getDistanceToBorder($$0.getX(), $$0.getZ());
   }

   public VoxelShape getCollisionShape() {
      return this.extent.getCollisionShape();
   }

   public double getDistanceToBorder(double $$0, double $$1) {
      double $$2 = $$1 - this.getMinZ();
      double $$3 = this.getMaxZ() - $$1;
      double $$4 = $$0 - this.getMinX();
      double $$5 = this.getMaxX() - $$0;
      double $$6 = Math.min($$4, $$5);
      $$6 = Math.min($$6, $$2);
      return Math.min($$6, $$3);
   }

   public boolean isInsideCloseToBorder(Entity $$0, AABB $$1) {
      double $$2 = Math.max(Mth.absMax($$1.getXsize(), $$1.getZsize()), 1.0);
      return this.getDistanceToBorder($$0) < $$2 * 2.0 && this.isWithinBounds($$0.getX(), $$0.getZ(), $$2);
   }

   public BorderStatus getStatus() {
      return this.extent.getStatus();
   }

   public double getMinX() {
      return this.getMinX(0.0F);
   }

   public double getMinX(float $$0) {
      return this.extent.getMinX($$0);
   }

   public double getMinZ() {
      return this.getMinZ(0.0F);
   }

   public double getMinZ(float $$0) {
      return this.extent.getMinZ($$0);
   }

   public double getMaxX() {
      return this.getMaxX(0.0F);
   }

   public double getMaxX(float $$0) {
      return this.extent.getMaxX($$0);
   }

   public double getMaxZ() {
      return this.getMaxZ(0.0F);
   }

   public double getMaxZ(float $$0) {
      return this.extent.getMaxZ($$0);
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public void setCenter(double $$0, double $$1) {
      this.centerX = $$0;
      this.centerZ = $$1;
      this.extent.onCenterChange();
      this.setDirty();

      for (BorderChangeListener $$2 : this.getListeners()) {
         $$2.onSetCenter(this, $$0, $$1);
      }
   }

   public double getSize() {
      return this.extent.getSize();
   }

   public long getLerpTime() {
      return this.extent.getLerpTime();
   }

   public double getLerpTarget() {
      return this.extent.getLerpTarget();
   }

   public void setSize(double $$0) {
      this.extent = new WorldBorder.StaticBorderExtent($$0);
      this.setDirty();

      for (BorderChangeListener $$1 : this.getListeners()) {
         $$1.onSetSize(this, $$0);
      }
   }

   public void lerpSizeBetween(double $$0, double $$1, long $$2, long $$3) {
      this.extent = (WorldBorder.BorderExtent)($$0 == $$1 ? new WorldBorder.StaticBorderExtent($$1) : new WorldBorder.MovingBorderExtent($$0, $$1, $$2, $$3));
      this.setDirty();

      for (BorderChangeListener $$4 : this.getListeners()) {
         $$4.onLerpSize(this, $$0, $$1, $$2, $$3);
      }
   }

   protected List<BorderChangeListener> getListeners() {
      return Lists.newArrayList(this.listeners);
   }

   public void addListener(BorderChangeListener $$0) {
      this.listeners.add($$0);
   }

   public void removeListener(BorderChangeListener $$0) {
      this.listeners.remove($$0);
   }

   public void setAbsoluteMaxSize(int $$0) {
      this.absoluteMaxSize = $$0;
      this.extent.onAbsoluteMaxSizeChange();
   }

   public int getAbsoluteMaxSize() {
      return this.absoluteMaxSize;
   }

   public double getSafeZone() {
      return this.safeZone;
   }

   public void setSafeZone(double $$0) {
      this.safeZone = $$0;
      this.setDirty();

      for (BorderChangeListener $$1 : this.getListeners()) {
         $$1.onSetSafeZone(this, $$0);
      }
   }

   public double getDamagePerBlock() {
      return this.damagePerBlock;
   }

   public void setDamagePerBlock(double $$0) {
      this.damagePerBlock = $$0;
      this.setDirty();

      for (BorderChangeListener $$1 : this.getListeners()) {
         $$1.onSetDamagePerBlock(this, $$0);
      }
   }

   public double getLerpSpeed() {
      return this.extent.getLerpSpeed();
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public void setWarningTime(int $$0) {
      this.warningTime = $$0;
      this.setDirty();

      for (BorderChangeListener $$1 : this.getListeners()) {
         $$1.onSetWarningTime(this, $$0);
      }
   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }

   public void setWarningBlocks(int $$0) {
      this.warningBlocks = $$0;
      this.setDirty();

      for (BorderChangeListener $$1 : this.getListeners()) {
         $$1.onSetWarningBlocks(this, $$0);
      }
   }

   public void tick() {
      this.extent = this.extent.update();
   }

   public void applyInitialSettings(long $$0) {
      if (!this.initialized) {
         this.setCenter(this.settings.centerX(), this.settings.centerZ());
         this.setDamagePerBlock(this.settings.damagePerBlock());
         this.setSafeZone(this.settings.safeZone());
         this.setWarningBlocks(this.settings.warningBlocks());
         this.setWarningTime(this.settings.warningTime());
         if (this.settings.lerpTime() > 0L) {
            this.lerpSizeBetween(this.settings.size(), this.settings.lerpTarget(), this.settings.lerpTime(), $$0);
         } else {
            this.setSize(this.settings.size());
         }

         this.initialized = true;
      }
   }

   interface BorderExtent {
      double getMinX(float var1);

      double getMaxX(float var1);

      double getMinZ(float var1);

      double getMaxZ(float var1);

      double getSize();

      double getLerpSpeed();

      long getLerpTime();

      double getLerpTarget();

      BorderStatus getStatus();

      void onAbsoluteMaxSizeChange();

      void onCenterChange();

      WorldBorder.BorderExtent update();

      VoxelShape getCollisionShape();
   }

   class MovingBorderExtent implements WorldBorder.BorderExtent {
      private final double from;
      private final double to;
      private final long lerpEnd;
      private final long lerpBegin;
      private final double lerpDuration;
      private long lerpProgress;
      private double size;
      private double previousSize;

      MovingBorderExtent(final double $$0, final double $$1, final long $$2, final long $$3) {
         this.from = $$0;
         this.to = $$1;
         this.lerpDuration = $$2;
         this.lerpProgress = $$2;
         this.lerpBegin = $$3;
         this.lerpEnd = this.lerpBegin + $$2;
         double $$4 = this.calculateSize();
         this.size = $$4;
         this.previousSize = $$4;
      }

      @Override
      public double getMinX(float $$0) {
         return Mth.clamp(
            WorldBorder.this.getCenterX() - Mth.lerp($$0, this.getPreviousSize(), this.getSize()) / 2.0,
            -WorldBorder.this.absoluteMaxSize,
            WorldBorder.this.absoluteMaxSize
         );
      }

      @Override
      public double getMinZ(float $$0) {
         return Mth.clamp(
            WorldBorder.this.getCenterZ() - Mth.lerp($$0, this.getPreviousSize(), this.getSize()) / 2.0,
            -WorldBorder.this.absoluteMaxSize,
            WorldBorder.this.absoluteMaxSize
         );
      }

      @Override
      public double getMaxX(float $$0) {
         return Mth.clamp(
            WorldBorder.this.getCenterX() + Mth.lerp($$0, this.getPreviousSize(), this.getSize()) / 2.0,
            -WorldBorder.this.absoluteMaxSize,
            WorldBorder.this.absoluteMaxSize
         );
      }

      @Override
      public double getMaxZ(float $$0) {
         return Mth.clamp(
            WorldBorder.this.getCenterZ() + Mth.lerp($$0, this.getPreviousSize(), this.getSize()) / 2.0,
            -WorldBorder.this.absoluteMaxSize,
            WorldBorder.this.absoluteMaxSize
         );
      }

      @Override
      public double getSize() {
         return this.size;
      }

      public double getPreviousSize() {
         return this.previousSize;
      }

      private double calculateSize() {
         double $$0 = (this.lerpDuration - this.lerpProgress) / this.lerpDuration;
         return $$0 < 1.0 ? Mth.lerp($$0, this.from, this.to) : this.to;
      }

      @Override
      public double getLerpSpeed() {
         return Math.abs(this.from - this.to) / (this.lerpEnd - this.lerpBegin);
      }

      @Override
      public long getLerpTime() {
         return this.lerpProgress;
      }

      @Override
      public double getLerpTarget() {
         return this.to;
      }

      @Override
      public BorderStatus getStatus() {
         return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
      }

      @Override
      public void onCenterChange() {
      }

      @Override
      public void onAbsoluteMaxSizeChange() {
      }

      @Override
      public WorldBorder.BorderExtent update() {
         this.lerpProgress--;
         this.previousSize = this.size;
         this.size = this.calculateSize();
         if (this.lerpProgress <= 0L) {
            WorldBorder.this.setDirty();
            return WorldBorder.this.new StaticBorderExtent(this.to);
         } else {
            return this;
         }
      }

      @Override
      public VoxelShape getCollisionShape() {
         return Shapes.join(
            Shapes.INFINITY,
            Shapes.box(
               Math.floor(this.getMinX(0.0F)),
               Double.NEGATIVE_INFINITY,
               Math.floor(this.getMinZ(0.0F)),
               Math.ceil(this.getMaxX(0.0F)),
               Double.POSITIVE_INFINITY,
               Math.ceil(this.getMaxZ(0.0F))
            ),
            BooleanOp.ONLY_FIRST
         );
      }
   }

   public record Settings(
      double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long lerpTime, double lerpTarget
   ) {
      public static final WorldBorder.Settings DEFAULT = new WorldBorder.Settings(0.0, 0.0, 0.2, 5.0, 5, 300, 5.999997E7F, 0L, 0.0);
      public static final Codec<WorldBorder.Settings> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.doubleRange(-2.9999984E7, 2.9999984E7).fieldOf("center_x").forGetter(WorldBorder.Settings::centerX),
               Codec.doubleRange(-2.9999984E7, 2.9999984E7).fieldOf("center_z").forGetter(WorldBorder.Settings::centerZ),
               Codec.DOUBLE.fieldOf("damage_per_block").forGetter(WorldBorder.Settings::damagePerBlock),
               Codec.DOUBLE.fieldOf("safe_zone").forGetter(WorldBorder.Settings::safeZone),
               Codec.INT.fieldOf("warning_blocks").forGetter(WorldBorder.Settings::warningBlocks),
               Codec.INT.fieldOf("warning_time").forGetter(WorldBorder.Settings::warningTime),
               Codec.DOUBLE.fieldOf("size").forGetter(WorldBorder.Settings::size),
               Codec.LONG.fieldOf("lerp_time").forGetter(WorldBorder.Settings::lerpTime),
               Codec.DOUBLE.fieldOf("lerp_target").forGetter(WorldBorder.Settings::lerpTarget)
            )
            .apply($$0, WorldBorder.Settings::new)
      );

      public Settings(WorldBorder $$0) {
         this(
            $$0.centerX,
            $$0.centerZ,
            $$0.damagePerBlock,
            $$0.safeZone,
            $$0.warningBlocks,
            $$0.warningTime,
            $$0.extent.getSize(),
            $$0.extent.getLerpTime(),
            $$0.extent.getLerpTarget()
         );
      }
   }

   class StaticBorderExtent implements WorldBorder.BorderExtent {
      private final double size;
      private double minX;
      private double minZ;
      private double maxX;
      private double maxZ;
      private VoxelShape shape;

      public StaticBorderExtent(final double $$0) {
         this.size = $$0;
         this.updateBox();
      }

      @Override
      public double getMinX(float $$0) {
         return this.minX;
      }

      @Override
      public double getMaxX(float $$0) {
         return this.maxX;
      }

      @Override
      public double getMinZ(float $$0) {
         return this.minZ;
      }

      @Override
      public double getMaxZ(float $$0) {
         return this.maxZ;
      }

      @Override
      public double getSize() {
         return this.size;
      }

      @Override
      public BorderStatus getStatus() {
         return BorderStatus.STATIONARY;
      }

      @Override
      public double getLerpSpeed() {
         return 0.0;
      }

      @Override
      public long getLerpTime() {
         return 0L;
      }

      @Override
      public double getLerpTarget() {
         return this.size;
      }

      private void updateBox() {
         this.minX = Mth.clamp(WorldBorder.this.getCenterX() - this.size / 2.0, -WorldBorder.this.absoluteMaxSize, WorldBorder.this.absoluteMaxSize);
         this.minZ = Mth.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0, -WorldBorder.this.absoluteMaxSize, WorldBorder.this.absoluteMaxSize);
         this.maxX = Mth.clamp(WorldBorder.this.getCenterX() + this.size / 2.0, -WorldBorder.this.absoluteMaxSize, WorldBorder.this.absoluteMaxSize);
         this.maxZ = Mth.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0, -WorldBorder.this.absoluteMaxSize, WorldBorder.this.absoluteMaxSize);
         this.shape = Shapes.join(
            Shapes.INFINITY,
            Shapes.box(
               Math.floor(this.getMinX(0.0F)),
               Double.NEGATIVE_INFINITY,
               Math.floor(this.getMinZ(0.0F)),
               Math.ceil(this.getMaxX(0.0F)),
               Double.POSITIVE_INFINITY,
               Math.ceil(this.getMaxZ(0.0F))
            ),
            BooleanOp.ONLY_FIRST
         );
      }

      @Override
      public void onAbsoluteMaxSizeChange() {
         this.updateBox();
      }

      @Override
      public void onCenterChange() {
         this.updateBox();
      }

      @Override
      public WorldBorder.BorderExtent update() {
         return this;
      }

      @Override
      public VoxelShape getCollisionShape() {
         return this.shape;
      }
   }
}
