package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface SelectableSlotContainer {
   int getRows();

   int getColumns();

   default OptionalInt getHitSlot(BlockHitResult $$0, Direction $$1) {
      return getRelativeHitCoordinatesForBlockFace($$0, $$1).map($$0x -> {
         int $$1x = getSection(1.0F - $$0x.y, this.getRows());
         int $$2 = getSection($$0x.x, this.getColumns());
         return OptionalInt.of($$2 + $$1x * this.getColumns());
      }).orElseGet(OptionalInt::empty);
   }

   private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult $$0, Direction $$1) {
      Direction $$2 = $$0.getDirection();
      if ($$1 != $$2) {
         return Optional.empty();
      } else {
         BlockPos $$3 = $$0.getBlockPos().relative($$2);
         Vec3 $$4 = $$0.getLocation().subtract($$3.getX(), $$3.getY(), $$3.getZ());
         double $$5 = $$4.x();
         double $$6 = $$4.y();
         double $$7 = $$4.z();

         return switch ($$2) {
            case NORTH -> Optional.of(new Vec2((float)(1.0 - $$5), (float)$$6));
            case SOUTH -> Optional.of(new Vec2((float)$$5, (float)$$6));
            case WEST -> Optional.of(new Vec2((float)$$7, (float)$$6));
            case EAST -> Optional.of(new Vec2((float)(1.0 - $$7), (float)$$6));
            case DOWN, UP -> Optional.empty();
            default -> throw new MatchException(null, null);
         };
      }
   }

   private static int getSection(float $$0, int $$1) {
      float $$2 = $$0 * 16.0F;
      float $$3 = 16.0F / $$1;
      return Mth.clamp(Mth.floor($$2 / $$3), 0, $$1 - 1);
   }
}
