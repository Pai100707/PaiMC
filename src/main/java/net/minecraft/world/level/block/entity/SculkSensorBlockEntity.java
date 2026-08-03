package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SculkSensorBlockEntity extends BlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
   private static final int DEFAULT_LAST_VIBRATION_FREQUENCY = 0;
   private VibrationSystem.Data vibrationData;
   private final VibrationSystem.Listener vibrationListener;
   private final VibrationSystem.User vibrationUser;
   private int lastVibrationFrequency = 0;

   protected SculkSensorBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      super($$0, $$1, $$2);
      this.vibrationUser = this.createVibrationUser();
      this.vibrationData = new VibrationSystem.Data();
      this.vibrationListener = new VibrationSystem.Listener(this);
   }

   public SculkSensorBlockEntity(BlockPos $$0, BlockState $$1) {
      this(BlockEntityType.SCULK_SENSOR, $$0, $$1);
   }

   public VibrationSystem.User createVibrationUser() {
      return new SculkSensorBlockEntity.VibrationUser(this.getBlockPos());
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.lastVibrationFrequency = $$0.getIntOr("last_vibration_frequency", 0);
      this.vibrationData = $$0.<VibrationSystem.Data>read("listener", VibrationSystem.Data.CODEC).orElseGet(VibrationSystem.Data::new);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.putInt("last_vibration_frequency", this.lastVibrationFrequency);
      $$0.store("listener", VibrationSystem.Data.CODEC, this.vibrationData);
   }

   @Override
   public VibrationSystem.Data getVibrationData() {
      return this.vibrationData;
   }

   @Override
   public VibrationSystem.User getVibrationUser() {
      return this.vibrationUser;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public void setLastVibrationFrequency(int $$0) {
      this.lastVibrationFrequency = $$0;
   }

   public VibrationSystem.Listener getListener() {
      return this.vibrationListener;
   }

   protected class VibrationUser implements VibrationSystem.User {
      public static final int LISTENER_RANGE = 8;
      protected final BlockPos blockPos;
      private final PositionSource positionSource;

      public VibrationUser(final BlockPos $$1) {
         this.blockPos = $$1;
         this.positionSource = new BlockPositionSource($$1);
      }

      @Override
      public int getListenerRadius() {
         return 8;
      }

      @Override
      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      @Override
      public boolean canTriggerAvoidVibration() {
         return true;
      }

      @Override
      public boolean canReceiveVibration(ServerLevel $$0, BlockPos $$1, Holder<GameEvent> $$2, @Nullable GameEvent.Context $$3) {
         if (!$$1.equals(this.blockPos) || !$$2.is(GameEvent.BLOCK_DESTROY) && !$$2.is(GameEvent.BLOCK_PLACE)) {
            return VibrationSystem.getGameEventFrequency($$2) == 0 ? false : SculkSensorBlock.canActivate(SculkSensorBlockEntity.this.getBlockState());
         } else {
            return false;
         }
      }

      @Override
      public void onReceiveVibration(ServerLevel $$0, BlockPos $$1, Holder<GameEvent> $$2, @Nullable Entity $$3, @Nullable Entity $$4, float $$5) {
         BlockState $$6 = SculkSensorBlockEntity.this.getBlockState();
         if (SculkSensorBlock.canActivate($$6)) {
            int $$7 = VibrationSystem.getGameEventFrequency($$2);
            SculkSensorBlockEntity.this.setLastVibrationFrequency($$7);
            int $$8 = VibrationSystem.getRedstoneStrengthForDistance($$5, this.getListenerRadius());
            if ($$6.getBlock() instanceof SculkSensorBlock $$9) {
               $$9.activate($$3, $$0, this.blockPos, $$6, $$8, $$7);
            }
         }
      }

      @Override
      public void onDataChanged() {
         SculkSensorBlockEntity.this.setChanged();
      }

      @Override
      public boolean requiresAdjacentChunksToBeTicking() {
         return true;
      }
   }
}
