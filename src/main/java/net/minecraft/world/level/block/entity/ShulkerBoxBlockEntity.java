package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
   public static final int COLUMNS = 9;
   public static final int ROWS = 3;
   public static final int CONTAINER_SIZE = 27;
   public static final int EVENT_SET_OPEN_COUNT = 1;
   public static final int OPENING_TICK_LENGTH = 10;
   public static final float MAX_LID_HEIGHT = 0.5F;
   public static final float MAX_LID_ROTATION = 270.0F;
   private static final int[] SLOTS = IntStream.range(0, 27).toArray();
   private static final Component DEFAULT_NAME = Component.translatable("container.shulkerBox");
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
   private int openCount;
   private ShulkerBoxBlockEntity.AnimationStatus animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
   private float progress;
   private float progressOld;
   
   private final DyeColor color;

   public ShulkerBoxBlockEntity(DyeColor $$0, BlockPos $$1, BlockState $$2) {
      super(BlockEntityType.SHULKER_BOX, $$1, $$2);
      this.color = $$0;
   }

   public ShulkerBoxBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.SHULKER_BOX, $$0, $$1);
      this.color = $$1.getBlock() instanceof ShulkerBoxBlock $$2 ? $$2.getColor() : null;
   }

   public static void tick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, ShulkerBoxBlockEntity $$3) {
      $$3.updateAnimation($$0, $$1, $$2);
   }

   private void updateAnimation(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      this.progressOld = this.progress;
      switch (this.animationStatus) {
         case CLOSED:
            this.progress = 0.0F;
            break;
         case OPENING:
            this.progress += 0.1F;
            if (this.progressOld == 0.0F) {
               doNeighborUpdates($$0, $$1, $$2);
            }

            if (this.progress >= 1.0F) {
               this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENED;
               this.progress = 1.0F;
               doNeighborUpdates($$0, $$1, $$2);
            }

            this.moveCollidedEntities($$0, $$1, $$2);
            break;
         case OPENED:
            this.progress = 1.0F;
            break;
         case CLOSING:
            this.progress -= 0.1F;
            if (this.progressOld == 1.0F) {
               doNeighborUpdates($$0, $$1, $$2);
            }

            if (this.progress <= 0.0F) {
               this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
               this.progress = 0.0F;
               doNeighborUpdates($$0, $$1, $$2);
            }
      }
   }

   public ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus() {
      return this.animationStatus;
   }

   public AABB getBoundingBox(BlockState $$0) {
      Vec3 $$1 = new Vec3(0.5, 0.0, 0.5);
      return Shulker.getProgressAabb(1.0F, $$0.getValue(ShulkerBoxBlock.FACING), 0.5F * this.getProgress(1.0F), $$1);
   }

   private void moveCollidedEntities(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      if ($$2.getBlock() instanceof ShulkerBoxBlock) {
         Direction $$3 = $$2.getValue(ShulkerBoxBlock.FACING);
         AABB $$4 = Shulker.getProgressDeltaAabb(1.0F, $$3, this.progressOld, this.progress, $$1.getBottomCenter());
         List<Entity> $$5 = $$0.getEntities(null, $$4);
         if (!$$5.isEmpty()) {
            for (Entity $$6 : $$5) {
               if ($$6.getPistonPushReaction() != PushReaction.IGNORE) {
                  $$6.move(
                     MoverType.SHULKER_BOX,
                     new Vec3(($$4.getXsize() + 0.01) * $$3.getStepX(), ($$4.getYsize() + 0.01) * $$3.getStepY(), ($$4.getZsize() + 0.01) * $$3.getStepZ())
                  );
               }
            }
         }
      }
   }

   public int getContainerSize() {
      return this.itemStacks.size();
   }

   @Override
   public boolean triggerEvent(int $$0, int $$1) {
      if ($$0 == 1) {
         this.openCount = $$1;
         if ($$1 == 0) {
            this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSING;
         }

         if ($$1 == 1) {
            this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENING;
         }

         return true;
      } else {
         return super.triggerEvent($$0, $$1);
      }
   }

   private static void doNeighborUpdates(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      $$2.updateNeighbourShapes($$0, $$1, 3);
      $$0.updateNeighborsAt($$1, $$2.getBlock());
   }

   @Override
   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
   }

   public void startOpen(ContainerUser $$0) {
      if (!this.remove && !$$0.getLivingEntity().isSpectator()) {
         if (this.openCount < 0) {
            this.openCount = 0;
         }

         this.openCount++;
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
         if (this.openCount == 1) {
            this.level.gameEvent($$0.getLivingEntity(), GameEvent.CONTAINER_OPEN, this.worldPosition);
            this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }
      }
   }

   public void stopOpen(ContainerUser $$0) {
      if (!this.remove && !$$0.getLivingEntity().isSpectator()) {
         this.openCount--;
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
         if (this.openCount <= 0) {
            this.level.gameEvent($$0.getLivingEntity(), GameEvent.CONTAINER_CLOSE, this.worldPosition);
            this.level
               .playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }
      }
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.loadFromTag($$0);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.trySaveLootTable($$0)) {
         ContainerHelper.saveAllItems($$0, this.itemStacks, false);
      }
   }

   public void loadFromTag(ValueInput $$0) {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable($$0)) {
         ContainerHelper.loadAllItems($$0, this.itemStacks);
      }
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.itemStacks;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> $$0) {
      this.itemStacks = $$0;
   }

   public int[] getSlotsForFace(Direction $$0) {
      return SLOTS;
   }

   public boolean canPlaceItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
      return !(Block.byItem($$1.getItem()) instanceof ShulkerBoxBlock);
   }

   public boolean canTakeItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
      return true;
   }

   public float getProgress(float $$0) {
      return Mth.lerp($$0, this.progressOld, this.progress);
   }

   
   public DyeColor getColor() {
      return this.color;
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return new ShulkerBoxMenu($$0, $$1, this);
   }

   public boolean isClosed() {
      return this.animationStatus == ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
   }

   public static enum AnimationStatus {
      CLOSED,
      OPENING,
      OPENED,
      CLOSING;
   }
}
