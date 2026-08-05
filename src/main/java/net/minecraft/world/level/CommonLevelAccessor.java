package net.minecraft.world.level;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor
   extends net.minecraft.world.level.EntityGetter,
   net.minecraft.world.level.LevelReader,
   net.minecraft.world.level.LevelSimulatedRW {
   @Override
   default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos $$0, BlockEntityType<T> $$1) {
      return net.minecraft.world.level.LevelReader.super.getBlockEntity($$0, $$1);
   }

   @Override
   default List<VoxelShape> getEntityCollisions(Entity $$0, AABB $$1) {
      return net.minecraft.world.level.EntityGetter.super.getEntityCollisions($$0, $$1);
   }

   @Override
   default boolean isUnobstructed(Entity $$0, VoxelShape $$1) {
      return net.minecraft.world.level.EntityGetter.super.isUnobstructed($$0, $$1);
   }

   @Override
   default BlockPos getHeightmapPos(Heightmap.Types $$0, BlockPos $$1) {
      return net.minecraft.world.level.LevelReader.super.getHeightmapPos($$0, $$1);
   }
}
