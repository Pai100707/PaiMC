package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TestBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEFAULT_MESSAGE = "";
   private static final boolean DEFAULT_POWERED = false;
   private TestBlockMode mode;
   private String message = "";
   private boolean powered = false;
   private boolean triggered;

   public TestBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.TEST_BLOCK, $$0, $$1);
      this.mode = $$1.getValue(TestBlock.MODE);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      $$0.store("mode", TestBlockMode.CODEC, this.mode);
      $$0.putString("message", this.message);
      $$0.putBoolean("powered", this.powered);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      this.mode = $$0.<TestBlockMode>read("mode", TestBlockMode.CODEC).orElse(TestBlockMode.FAIL);
      this.message = $$0.getStringOr("message", "");
      this.powered = $$0.getBooleanOr("powered", false);
   }

   private void updateBlockState() {
      if (this.level != null) {
         BlockPos $$0 = this.getBlockPos();
         BlockState $$1 = this.level.getBlockState($$0);
         if ($$1.is(Blocks.TEST_BLOCK)) {
            this.level.setBlock($$0, $$1.setValue(TestBlock.MODE, this.mode), 2);
         }
      }
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public boolean isPowered() {
      return this.powered;
   }

   public void setPowered(boolean $$0) {
      this.powered = $$0;
   }

   public TestBlockMode getMode() {
      return this.mode;
   }

   public void setMode(TestBlockMode $$0) {
      this.mode = $$0;
      this.updateBlockState();
   }

   private Block getBlockType() {
      return this.getBlockState().getBlock();
   }

   public void reset() {
      this.triggered = false;
      if (this.mode == TestBlockMode.START && this.level != null) {
         this.setPowered(false);
         this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockType());
      }
   }

   public void trigger() {
      if (this.mode == TestBlockMode.START && this.level != null) {
         this.setPowered(true);
         BlockPos $$0 = this.getBlockPos();
         this.level.updateNeighborsAt($$0, this.getBlockType());
         this.level.getBlockTicks().willTickThisTick($$0, this.getBlockType());
         this.log();
      } else {
         if (this.mode == TestBlockMode.LOG) {
            this.log();
         }

         this.triggered = true;
      }
   }

   public void log() {
      if (!this.message.isBlank()) {
         LOGGER.info("Test {} (at {}): {}", new Object[]{this.mode.getSerializedName(), this.getBlockPos(), this.message});
      }
   }

   public boolean hasTriggered() {
      return this.triggered;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String $$0) {
      this.message = $$0;
   }
}
