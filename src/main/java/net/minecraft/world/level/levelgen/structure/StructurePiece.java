package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public abstract class StructurePiece {
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected BoundingBox boundingBox;
   @Nullable
   private Direction orientation;
   private Mirror mirror;
   private Rotation rotation;
   protected int genDepth;
   private final StructurePieceType type;
   private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.builder()
      .add(Blocks.NETHER_BRICK_FENCE)
      .add(Blocks.TORCH)
      .add(Blocks.WALL_TORCH)
      .add(Blocks.OAK_FENCE)
      .add(Blocks.SPRUCE_FENCE)
      .add(Blocks.DARK_OAK_FENCE)
      .add(Blocks.PALE_OAK_FENCE)
      .add(Blocks.ACACIA_FENCE)
      .add(Blocks.BIRCH_FENCE)
      .add(Blocks.JUNGLE_FENCE)
      .add(Blocks.LADDER)
      .add(Blocks.IRON_BARS)
      .build();

   protected StructurePiece(StructurePieceType $$0, int $$1, BoundingBox $$2) {
      this.type = $$0;
      this.genDepth = $$1;
      this.boundingBox = $$2;
   }

   public StructurePiece(StructurePieceType $$0, CompoundTag $$1) {
      this($$0, $$1.getIntOr("GD", 0), (BoundingBox)$$1.read("BB", BoundingBox.CODEC).orElseThrow());
      int $$2 = $$1.getIntOr("O", 0);
      this.setOrientation($$2 == -1 ? null : Direction.from2DDataValue($$2));
   }

   protected static BoundingBox makeBoundingBox(int $$0, int $$1, int $$2, Direction $$3, int $$4, int $$5, int $$6) {
      return $$3.getAxis() == Axis.Z
         ? new BoundingBox($$0, $$1, $$2, $$0 + $$4 - 1, $$1 + $$5 - 1, $$2 + $$6 - 1)
         : new BoundingBox($$0, $$1, $$2, $$0 + $$6 - 1, $$1 + $$5 - 1, $$2 + $$4 - 1);
   }

   protected static Direction getRandomHorizontalDirection(RandomSource $$0) {
      return Plane.HORIZONTAL.getRandomDirection($$0);
   }

   public final CompoundTag createTag(StructurePieceSerializationContext $$0) {
      CompoundTag $$1 = new CompoundTag();
      $$1.putString("id", BuiltInRegistries.STRUCTURE_PIECE.getKey(this.getType()).toString());
      $$1.store("BB", BoundingBox.CODEC, this.boundingBox);
      Direction $$2 = this.getOrientation();
      $$1.putInt("O", $$2 == null ? -1 : $$2.get2DDataValue());
      $$1.putInt("GD", this.genDepth);
      this.addAdditionalSaveData($$0, $$1);
      return $$1;
   }

   protected abstract void addAdditionalSaveData(StructurePieceSerializationContext var1, CompoundTag var2);

   public void addChildren(StructurePiece $$0, StructurePieceAccessor $$1, RandomSource $$2) {
   }

   public abstract void postProcess(
      net.minecraft.world.level.WorldGenLevel var1,
      net.minecraft.world.level.StructureManager var2,
      ChunkGenerator var3,
      RandomSource var4,
      BoundingBox var5,
      net.minecraft.world.level.ChunkPos var6,
      BlockPos var7
   );

   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getGenDepth() {
      return this.genDepth;
   }

   public void setGenDepth(int $$0) {
      this.genDepth = $$0;
   }

   public boolean isCloseToChunk(net.minecraft.world.level.ChunkPos $$0, int $$1) {
      int $$2 = $$0.getMinBlockX();
      int $$3 = $$0.getMinBlockZ();
      return this.boundingBox.intersects($$2 - $$1, $$3 - $$1, $$2 + 15 + $$1, $$3 + 15 + $$1);
   }

   public BlockPos getLocatorPosition() {
      return new BlockPos(this.boundingBox.getCenter());
   }

   protected MutableBlockPos getWorldPos(int $$0, int $$1, int $$2) {
      return new MutableBlockPos(this.getWorldX($$0, $$2), this.getWorldY($$1), this.getWorldZ($$0, $$2));
   }

   protected int getWorldX(int $$0, int $$1) {
      Direction $$2 = this.getOrientation();
      if ($$2 == null) {
         return $$0;
      } else {
         switch ($$2) {
            case NORTH:
            case SOUTH:
               return this.boundingBox.minX() + $$0;
            case WEST:
               return this.boundingBox.maxX() - $$1;
            case EAST:
               return this.boundingBox.minX() + $$1;
            default:
               return $$0;
         }
      }
   }

   protected int getWorldY(int $$0) {
      return this.getOrientation() == null ? $$0 : $$0 + this.boundingBox.minY();
   }

   protected int getWorldZ(int $$0, int $$1) {
      Direction $$2 = this.getOrientation();
      if ($$2 == null) {
         return $$1;
      } else {
         switch ($$2) {
            case NORTH:
               return this.boundingBox.maxZ() - $$1;
            case SOUTH:
               return this.boundingBox.minZ() + $$1;
            case WEST:
            case EAST:
               return this.boundingBox.minZ() + $$0;
            default:
               return $$1;
         }
      }
   }

   protected void placeBlock(net.minecraft.world.level.WorldGenLevel $$0, BlockState $$1, int $$2, int $$3, int $$4, BoundingBox $$5) {
      BlockPos $$6 = this.getWorldPos($$2, $$3, $$4);
      if ($$5.isInside($$6)) {
         if (this.canBeReplaced($$0, $$2, $$3, $$4, $$5)) {
            if (this.mirror != Mirror.NONE) {
               $$1 = $$1.mirror(this.mirror);
            }

            if (this.rotation != Rotation.NONE) {
               $$1 = $$1.rotate(this.rotation);
            }

            $$0.setBlock($$6, $$1, 2);
            FluidState $$7 = $$0.getFluidState($$6);
            if (!$$7.isEmpty()) {
               $$0.scheduleTick($$6, $$7.getType(), 0);
            }

            if (SHAPE_CHECK_BLOCKS.contains($$1.getBlock())) {
               $$0.getChunk($$6).markPosForPostprocessing($$6);
            }
         }
      }
   }

   protected boolean canBeReplaced(net.minecraft.world.level.LevelReader $$0, int $$1, int $$2, int $$3, BoundingBox $$4) {
      return true;
   }

   protected BlockState getBlock(net.minecraft.world.level.BlockGetter $$0, int $$1, int $$2, int $$3, BoundingBox $$4) {
      BlockPos $$5 = this.getWorldPos($$1, $$2, $$3);
      return !$$4.isInside($$5) ? Blocks.AIR.defaultBlockState() : $$0.getBlockState($$5);
   }

   protected boolean isInterior(net.minecraft.world.level.LevelReader $$0, int $$1, int $$2, int $$3, BoundingBox $$4) {
      BlockPos $$5 = this.getWorldPos($$1, $$2 + 1, $$3);
      return !$$4.isInside($$5) ? false : $$5.getY() < $$0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, $$5.getX(), $$5.getZ());
   }

   protected void generateAirBox(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7) {
      for (int $$8 = $$3; $$8 <= $$6; $$8++) {
         for (int $$9 = $$2; $$9 <= $$5; $$9++) {
            for (int $$10 = $$4; $$10 <= $$7; $$10++) {
               this.placeBlock($$0, Blocks.AIR.defaultBlockState(), $$9, $$8, $$10, $$1);
            }
         }
      }
   }

   protected void generateBox(
      net.minecraft.world.level.WorldGenLevel $$0,
      BoundingBox $$1,
      int $$2,
      int $$3,
      int $$4,
      int $$5,
      int $$6,
      int $$7,
      BlockState $$8,
      BlockState $$9,
      boolean $$10
   ) {
      for (int $$11 = $$3; $$11 <= $$6; $$11++) {
         for (int $$12 = $$2; $$12 <= $$5; $$12++) {
            for (int $$13 = $$4; $$13 <= $$7; $$13++) {
               if (!$$10 || !this.getBlock($$0, $$12, $$11, $$13, $$1).isAir()) {
                  if ($$11 != $$3 && $$11 != $$6 && $$12 != $$2 && $$12 != $$5 && $$13 != $$4 && $$13 != $$7) {
                     this.placeBlock($$0, $$9, $$12, $$11, $$13, $$1);
                  } else {
                     this.placeBlock($$0, $$8, $$12, $$11, $$13, $$1);
                  }
               }
            }
         }
      }
   }

   protected void generateBox(net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, BoundingBox $$2, BlockState $$3, BlockState $$4, boolean $$5) {
      this.generateBox($$0, $$1, $$2.minX(), $$2.minY(), $$2.minZ(), $$2.maxX(), $$2.maxY(), $$2.maxZ(), $$3, $$4, $$5);
   }

   protected void generateBox(
      net.minecraft.world.level.WorldGenLevel $$0,
      BoundingBox $$1,
      int $$2,
      int $$3,
      int $$4,
      int $$5,
      int $$6,
      int $$7,
      boolean $$8,
      RandomSource $$9,
      StructurePiece.BlockSelector $$10
   ) {
      for (int $$11 = $$3; $$11 <= $$6; $$11++) {
         for (int $$12 = $$2; $$12 <= $$5; $$12++) {
            for (int $$13 = $$4; $$13 <= $$7; $$13++) {
               if (!$$8 || !this.getBlock($$0, $$12, $$11, $$13, $$1).isAir()) {
                  $$10.next($$9, $$12, $$11, $$13, $$11 == $$3 || $$11 == $$6 || $$12 == $$2 || $$12 == $$5 || $$13 == $$4 || $$13 == $$7);
                  this.placeBlock($$0, $$10.getNext(), $$12, $$11, $$13, $$1);
               }
            }
         }
      }
   }

   protected void generateBox(
      net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, BoundingBox $$2, boolean $$3, RandomSource $$4, StructurePiece.BlockSelector $$5
   ) {
      this.generateBox($$0, $$1, $$2.minX(), $$2.minY(), $$2.minZ(), $$2.maxX(), $$2.maxY(), $$2.maxZ(), $$3, $$4, $$5);
   }

   protected void generateMaybeBox(
      net.minecraft.world.level.WorldGenLevel $$0,
      BoundingBox $$1,
      RandomSource $$2,
      float $$3,
      int $$4,
      int $$5,
      int $$6,
      int $$7,
      int $$8,
      int $$9,
      BlockState $$10,
      BlockState $$11,
      boolean $$12,
      boolean $$13
   ) {
      for (int $$14 = $$5; $$14 <= $$8; $$14++) {
         for (int $$15 = $$4; $$15 <= $$7; $$15++) {
            for (int $$16 = $$6; $$16 <= $$9; $$16++) {
               if (!($$2.nextFloat() > $$3)
                  && (!$$12 || !this.getBlock($$0, $$15, $$14, $$16, $$1).isAir())
                  && (!$$13 || this.isInterior($$0, $$15, $$14, $$16, $$1))) {
                  if ($$14 != $$5 && $$14 != $$8 && $$15 != $$4 && $$15 != $$7 && $$16 != $$6 && $$16 != $$9) {
                     this.placeBlock($$0, $$11, $$15, $$14, $$16, $$1);
                  } else {
                     this.placeBlock($$0, $$10, $$15, $$14, $$16, $$1);
                  }
               }
            }
         }
      }
   }

   protected void maybeGenerateBlock(
      net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, RandomSource $$2, float $$3, int $$4, int $$5, int $$6, BlockState $$7
   ) {
      if ($$2.nextFloat() < $$3) {
         this.placeBlock($$0, $$7, $$4, $$5, $$6, $$1);
      }
   }

   protected void generateUpperHalfSphere(
      net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7, BlockState $$8, boolean $$9
   ) {
      float $$10 = $$5 - $$2 + 1;
      float $$11 = $$6 - $$3 + 1;
      float $$12 = $$7 - $$4 + 1;
      float $$13 = $$2 + $$10 / 2.0F;
      float $$14 = $$4 + $$12 / 2.0F;

      for (int $$15 = $$3; $$15 <= $$6; $$15++) {
         float $$16 = ($$15 - $$3) / $$11;

         for (int $$17 = $$2; $$17 <= $$5; $$17++) {
            float $$18 = ($$17 - $$13) / ($$10 * 0.5F);

            for (int $$19 = $$4; $$19 <= $$7; $$19++) {
               float $$20 = ($$19 - $$14) / ($$12 * 0.5F);
               if (!$$9 || !this.getBlock($$0, $$17, $$15, $$19, $$1).isAir()) {
                  float $$21 = $$18 * $$18 + $$16 * $$16 + $$20 * $$20;
                  if ($$21 <= 1.05F) {
                     this.placeBlock($$0, $$8, $$17, $$15, $$19, $$1);
                  }
               }
            }
         }
      }
   }

   protected void fillColumnDown(net.minecraft.world.level.WorldGenLevel $$0, BlockState $$1, int $$2, int $$3, int $$4, BoundingBox $$5) {
      MutableBlockPos $$6 = this.getWorldPos($$2, $$3, $$4);
      if ($$5.isInside($$6)) {
         while (this.isReplaceableByStructures($$0.getBlockState($$6)) && $$6.getY() > $$0.getMinY() + 1) {
            $$0.setBlock($$6, $$1, 2);
            $$6.move(Direction.DOWN);
         }
      }
   }

   protected boolean isReplaceableByStructures(BlockState $$0) {
      return $$0.isAir() || $$0.liquid() || $$0.is(Blocks.GLOW_LICHEN) || $$0.is(Blocks.SEAGRASS) || $$0.is(Blocks.TALL_SEAGRASS);
   }

   protected boolean createChest(
      net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, RandomSource $$2, int $$3, int $$4, int $$5, ResourceKey<LootTable> $$6
   ) {
      return this.createChest($$0, $$1, $$2, this.getWorldPos($$3, $$4, $$5), $$6, null);
   }

   public static BlockState reorient(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      Direction $$3 = null;

      for (Direction $$4 : Plane.HORIZONTAL) {
         BlockPos $$5 = $$1.relative($$4);
         BlockState $$6 = $$0.getBlockState($$5);
         if ($$6.is(Blocks.CHEST)) {
            return $$2;
         }

         if ($$6.isSolidRender()) {
            if ($$3 != null) {
               $$3 = null;
               break;
            }

            $$3 = $$4;
         }
      }

      if ($$3 != null) {
         return $$2.setValue(HorizontalDirectionalBlock.FACING, $$3.getOpposite());
      } else {
         Direction $$7 = $$2.getValue(HorizontalDirectionalBlock.FACING);
         BlockPos $$8 = $$1.relative($$7);
         if ($$0.getBlockState($$8).isSolidRender()) {
            $$7 = $$7.getOpposite();
            $$8 = $$1.relative($$7);
         }

         if ($$0.getBlockState($$8).isSolidRender()) {
            $$7 = $$7.getClockWise();
            $$8 = $$1.relative($$7);
         }

         if ($$0.getBlockState($$8).isSolidRender()) {
            $$7 = $$7.getOpposite();
            $$8 = $$1.relative($$7);
         }

         return $$2.setValue(HorizontalDirectionalBlock.FACING, $$7);
      }
   }

   protected boolean createChest(
      net.minecraft.world.level.ServerLevelAccessor $$0, BoundingBox $$1, RandomSource $$2, BlockPos $$3, ResourceKey<LootTable> $$4, @Nullable BlockState $$5
   ) {
      if ($$1.isInside($$3) && !$$0.getBlockState($$3).is(Blocks.CHEST)) {
         if ($$5 == null) {
            $$5 = reorient($$0, $$3, Blocks.CHEST.defaultBlockState());
         }

         $$0.setBlock($$3, $$5, 2);
         BlockEntity $$6 = $$0.getBlockEntity($$3);
         if ($$6 instanceof ChestBlockEntity) {
            ((ChestBlockEntity)$$6).setLootTable($$4, $$2.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean createDispenser(
      net.minecraft.world.level.WorldGenLevel $$0, BoundingBox $$1, RandomSource $$2, int $$3, int $$4, int $$5, Direction $$6, ResourceKey<LootTable> $$7
   ) {
      BlockPos $$8 = this.getWorldPos($$3, $$4, $$5);
      if ($$1.isInside($$8) && !$$0.getBlockState($$8).is(Blocks.DISPENSER)) {
         this.placeBlock($$0, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, $$6), $$3, $$4, $$5, $$1);
         BlockEntity $$9 = $$0.getBlockEntity($$8);
         if ($$9 instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)$$9).setLootTable($$7, $$2.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   public void move(int $$0, int $$1, int $$2) {
      this.boundingBox.move($$0, $$1, $$2);
   }

   public static BoundingBox createBoundingBox(Stream<StructurePiece> $$0) {
      return BoundingBox.encapsulatingBoxes($$0.map(StructurePiece::getBoundingBox)::iterator)
         .orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox without pieces"));
   }

   @Nullable
   public static StructurePiece findCollisionPiece(List<StructurePiece> $$0, BoundingBox $$1) {
      for (StructurePiece $$2 : $$0) {
         if ($$2.getBoundingBox().intersects($$1)) {
            return $$2;
         }
      }

      return null;
   }

   @Nullable
   public Direction getOrientation() {
      return this.orientation;
   }

   public void setOrientation(@Nullable Direction $$0) {
      this.orientation = $$0;
      if ($$0 == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch ($$0) {
            case SOUTH:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.NONE;
               break;
            case WEST:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            case EAST:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            default:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.NONE;
         }
      }
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public StructurePieceType getType() {
      return this.type;
   }

   public abstract static class BlockSelector {
      protected BlockState next = Blocks.AIR.defaultBlockState();

      public abstract void next(RandomSource var1, int var2, int var3, int var4, boolean var5);

      public BlockState getNext() {
         return this.next;
      }
   }
}
