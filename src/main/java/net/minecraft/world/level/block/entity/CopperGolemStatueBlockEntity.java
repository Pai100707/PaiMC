package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CopperGolemStatueBlockEntity extends BlockEntity {
   public CopperGolemStatueBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.COPPER_GOLEM_STATUE, $$0, $$1);
   }

   public void createStatue(CopperGolem $$0) {
      this.setComponents(DataComponentMap.builder().addAll(this.components()).set(DataComponents.CUSTOM_NAME, $$0.getCustomName()).build());
      super.setChanged();
   }

   @Nullable
   public CopperGolem removeStatue(BlockState $$0) {
      CopperGolem $$1 = (CopperGolem)EntityType.COPPER_GOLEM.create(this.level, EntitySpawnReason.TRIGGERED);
      if ($$1 != null) {
         $$1.setCustomName((Component)this.components().get(DataComponents.CUSTOM_NAME));
         return this.initCopperGolem($$0, $$1);
      } else {
         return null;
      }
   }

   private CopperGolem initCopperGolem(BlockState $$0, CopperGolem $$1) {
      BlockPos $$2 = this.getBlockPos();
      $$1.snapTo($$2.getCenter().x, $$2.getY(), $$2.getCenter().z, ((Direction)$$0.getValue(CopperGolemStatueBlock.FACING)).toYRot(), 0.0F);
      $$1.yHeadRot = $$1.getYRot();
      $$1.yBodyRot = $$1.getYRot();
      $$1.playSpawnSound();
      return $$1;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public ItemStack getItem(ItemStack $$0, CopperGolemStatueBlock.Pose $$1) {
      $$0.applyComponents(this.collectComponents());
      $$0.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CopperGolemStatueBlock.POSE, $$1));
      return $$0;
   }
}
