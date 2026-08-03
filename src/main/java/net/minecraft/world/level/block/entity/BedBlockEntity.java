package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BedBlockEntity extends BlockEntity {
   private final DyeColor color;

   public BedBlockEntity(BlockPos $$0, BlockState $$1) {
      this($$0, $$1, ((BedBlock)$$1.getBlock()).getColor());
   }

   public BedBlockEntity(BlockPos $$0, BlockState $$1, DyeColor $$2) {
      super(BlockEntityType.BED, $$0, $$1);
      this.color = $$2;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public DyeColor getColor() {
      return this.color;
   }
}
