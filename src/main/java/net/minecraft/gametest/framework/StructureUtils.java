package net.minecraft.gametest.framework;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity.Data;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity.Status;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StructureUtils {
   public static final int DEFAULT_Y_SEARCH_RADIUS = 10;
   public static final String DEFAULT_TEST_STRUCTURES_DIR = "Minecraft.Server/src/test/convertables/data";
   public static Path testStructuresDir = Paths.get("Minecraft.Server/src/test/convertables/data");

   public static Rotation getRotationForRotationSteps(int $$0) {
      switch ($$0) {
         case 0:
            return Rotation.NONE;
         case 1:
            return Rotation.CLOCKWISE_90;
         case 2:
            return Rotation.CLOCKWISE_180;
         case 3:
            return Rotation.COUNTERCLOCKWISE_90;
         default:
            throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + $$0);
      }
   }

   public static int getRotationStepsForRotation(Rotation $$0) {
      switch ($$0) {
         case NONE:
            return 0;
         case CLOCKWISE_90:
            return 1;
         case CLOCKWISE_180:
            return 2;
         case COUNTERCLOCKWISE_90:
            return 3;
         default:
            throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + $$0);
      }
   }

   public static TestInstanceBlockEntity createNewEmptyTest(Identifier $$0, BlockPos $$1, Vec3i $$2, Rotation $$3, ServerLevel $$4) {
      BoundingBox $$5 = getStructureBoundingBox(TestInstanceBlockEntity.getStructurePos($$1), $$2, $$3);
      clearSpaceForStructure($$5, $$4);
      $$4.setBlockAndUpdate($$1, Blocks.TEST_INSTANCE_BLOCK.defaultBlockState());
      TestInstanceBlockEntity $$6 = (TestInstanceBlockEntity)$$4.getBlockEntity($$1);
      ResourceKey<GameTestInstance> $$7 = ResourceKey.create(Registries.TEST_INSTANCE, $$0);
      $$6.set(new Data(Optional.of($$7), $$2, $$3, false, Status.CLEARED, Optional.empty()));
      return $$6;
   }

   public static void clearSpaceForStructure(BoundingBox $$0, ServerLevel $$1) {
      int $$2 = $$0.minY() - 1;
      BlockPos.betweenClosedStream($$0).forEach($$2x -> clearBlock($$2, $$2x, $$1));
      $$1.getBlockTicks().clearArea($$0);
      $$1.clearBlockEvents($$0);
      AABB $$3 = AABB.of($$0);
      List<Entity> $$4 = $$1.getEntitiesOfClass(Entity.class, $$3, $$0x -> !($$0x instanceof Player));
      $$4.forEach(Entity::discard);
   }

   public static BlockPos getTransformedFarCorner(BlockPos $$0, Vec3i $$1, Rotation $$2) {
      BlockPos $$3 = $$0.offset($$1).offset(-1, -1, -1);
      return StructureTemplate.transform($$3, Mirror.NONE, $$2, $$0);
   }

   public static BoundingBox getStructureBoundingBox(BlockPos $$0, Vec3i $$1, Rotation $$2) {
      BlockPos $$3 = getTransformedFarCorner($$0, $$1, $$2);
      BoundingBox $$4 = BoundingBox.fromCorners($$0, $$3);
      int $$5 = Math.min($$4.minX(), $$4.maxX());
      int $$6 = Math.min($$4.minZ(), $$4.maxZ());
      return $$4.move($$0.getX() - $$5, 0, $$0.getZ() - $$6);
   }

   public static Optional<BlockPos> findTestContainingPos(BlockPos $$0, int $$1, ServerLevel $$2) {
      return findTestBlocks($$0, $$1, $$2).filter($$2x -> doesStructureContain($$2x, $$0, $$2)).findFirst();
   }

   public static Optional<BlockPos> findNearestTest(BlockPos $$0, int $$1, ServerLevel $$2) {
      Comparator<BlockPos> $$3 = Comparator.comparingInt($$1x -> $$1x.distManhattan($$0));
      return findTestBlocks($$0, $$1, $$2).min($$3);
   }

   public static Stream<BlockPos> findTestBlocks(BlockPos $$0, int $$1, ServerLevel $$2) {
      return $$2.getPoiManager().findAll($$0x -> $$0x.is(PoiTypes.TEST_INSTANCE), $$0x -> true, $$0, $$1, Occupancy.ANY).map(BlockPos::immutable);
   }

   public static Stream<BlockPos> lookedAtTestPos(BlockPos $$0, Entity $$1, ServerLevel $$2) {
      int $$3 = 250;
      Vec3 $$4 = $$1.getEyePosition();
      Vec3 $$5 = $$4.add($$1.getLookAngle().scale(250.0));
      return findTestBlocks($$0, 250, $$2)
         .map($$1x -> $$2.getBlockEntity($$1x, BlockEntityType.TEST_INSTANCE_BLOCK))
         .flatMap(Optional::stream)
         .filter($$2x -> $$2x.getStructureBounds().clip($$4, $$5).isPresent())
         .<BlockPos>map(BlockEntity::getBlockPos)
         .sorted(Comparator.comparing($$0::distSqr))
         .limit(1L);
   }

   private static void clearBlock(int $$0, BlockPos $$1, ServerLevel $$2) {
      BlockState $$3;
      if ($$1.getY() < $$0) {
         $$3 = Blocks.STONE.defaultBlockState();
      } else {
         $$3 = Blocks.AIR.defaultBlockState();
      }

      BlockInput $$5 = new BlockInput($$3, Collections.emptySet(), null);
      $$5.place($$2, $$1, 818);
      $$2.updateNeighborsAt($$1, $$3.getBlock());
   }

   private static boolean doesStructureContain(BlockPos $$0, BlockPos $$1, ServerLevel $$2) {
      return $$2.getBlockEntity($$0) instanceof TestInstanceBlockEntity $$3 ? $$3.getStructureBoundingBox().isInside($$1) : false;
   }
}
