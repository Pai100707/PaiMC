package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BlockPosTracker implements PositionTracker {
   private final BlockPos blockPos;
   private final Vec3 centerPosition;

   public BlockPosTracker(BlockPos $$0) {
      this.blockPos = $$0.immutable();
      this.centerPosition = Vec3.atCenterOf($$0);
   }

   public BlockPosTracker(Vec3 $$0) {
      this.blockPos = BlockPos.containing($$0);
      this.centerPosition = $$0;
   }

   @Override
   public Vec3 currentPosition() {
      return this.centerPosition;
   }

   @Override
   public BlockPos currentBlockPosition() {
      return this.blockPos;
   }

   @Override
   public boolean isVisibleBy(net.minecraft.world.entity.LivingEntity $$0) {
      return true;
   }

   @Override
   public String toString() {
      return "BlockPosTracker{blockPos=" + this.blockPos + ", centerPosition=" + this.centerPosition + "}";
   }
}
