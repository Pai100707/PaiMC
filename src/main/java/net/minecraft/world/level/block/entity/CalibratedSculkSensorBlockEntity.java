package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jspecify.annotations.Nullable;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
   public CalibratedSculkSensorBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.CALIBRATED_SCULK_SENSOR, $$0, $$1);
   }

   @Override
   public VibrationSystem.User createVibrationUser() {
      return new CalibratedSculkSensorBlockEntity.VibrationUser(this.getBlockPos());
   }

   protected class VibrationUser extends SculkSensorBlockEntity.VibrationUser {
      public VibrationUser(final BlockPos $$1) {
         super($$1);
      }

      @Override
      public int getListenerRadius() {
         return 16;
      }

      @Override
      public boolean canReceiveVibration(ServerLevel $$0, BlockPos $$1, Holder<GameEvent> $$2, @Nullable GameEvent.Context $$3) {
         int $$4 = this.getBackSignal($$0, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());
         return $$4 != 0 && VibrationSystem.getGameEventFrequency($$2) != $$4 ? false : super.canReceiveVibration($$0, $$1, $$2, $$3);
      }

      private int getBackSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         Direction $$3 = ((Direction)$$2.getValue(CalibratedSculkSensorBlock.FACING)).getOpposite();
         return $$0.getSignal($$1.relative($$3), $$3);
      }
   }
}
