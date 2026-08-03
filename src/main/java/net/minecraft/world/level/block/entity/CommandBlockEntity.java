package net.minecraft.world.level.block.entity;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandBlockEntity extends BlockEntity {
   private static final boolean DEFAULT_POWERED = false;
   private static final boolean DEFAULT_CONDITION_MET = false;
   private static final boolean DEFAULT_AUTOMATIC = false;
   private boolean powered = false;
   private boolean auto = false;
   private boolean conditionMet = false;
   private final net.minecraft.world.level.BaseCommandBlock commandBlock = new net.minecraft.world.level.BaseCommandBlock() {
      @Override
      public void setCommand(String $$0) {
         super.setCommand($$0);
         CommandBlockEntity.this.setChanged();
      }

      @Override
      public void onUpdated(ServerLevel $$0) {
         BlockState $$1 = $$0.getBlockState(CommandBlockEntity.this.worldPosition);
         $$0.sendBlockUpdated(CommandBlockEntity.this.worldPosition, $$1, $$1, 3);
      }

      @Override
      public CommandSourceStack createCommandSourceStack(ServerLevel $$0, CommandSource $$1) {
         Direction $$2 = CommandBlockEntity.this.getBlockState().getValue(CommandBlock.FACING);
         return new CommandSourceStack(
            $$1,
            Vec3.atCenterOf(CommandBlockEntity.this.worldPosition),
            new Vec2(0.0F, $$2.toYRot()),
            $$0,
            LevelBasedPermissionSet.GAMEMASTER,
            this.getName().getString(),
            this.getName(),
            $$0.getServer(),
            null
         );
      }

      @Override
      public boolean isValid() {
         return !CommandBlockEntity.this.isRemoved();
      }
   };

   public CommandBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.COMMAND_BLOCK, $$0, $$1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      this.commandBlock.save($$0);
      $$0.putBoolean("powered", this.isPowered());
      $$0.putBoolean("conditionMet", this.wasConditionMet());
      $$0.putBoolean("auto", this.isAutomatic());
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.commandBlock.load($$0);
      this.powered = $$0.getBooleanOr("powered", false);
      this.conditionMet = $$0.getBooleanOr("conditionMet", false);
      this.setAutomatic($$0.getBooleanOr("auto", false));
   }

   public net.minecraft.world.level.BaseCommandBlock getCommandBlock() {
      return this.commandBlock;
   }

   public void setPowered(boolean $$0) {
      this.powered = $$0;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public boolean isAutomatic() {
      return this.auto;
   }

   public void setAutomatic(boolean $$0) {
      boolean $$1 = this.auto;
      this.auto = $$0;
      if (!$$1 && $$0 && !this.powered && this.level != null && this.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
         this.scheduleTick();
      }
   }

   public void onModeSwitch() {
      CommandBlockEntity.Mode $$0 = this.getMode();
      if ($$0 == CommandBlockEntity.Mode.AUTO && (this.powered || this.auto) && this.level != null) {
         this.scheduleTick();
      }
   }

   private void scheduleTick() {
      Block $$0 = this.getBlockState().getBlock();
      if ($$0 instanceof CommandBlock) {
         this.markConditionMet();
         this.level.scheduleTick(this.worldPosition, $$0, 1);
      }
   }

   public boolean wasConditionMet() {
      return this.conditionMet;
   }

   public boolean markConditionMet() {
      this.conditionMet = true;
      if (this.isConditional()) {
         BlockPos $$0 = this.worldPosition.relative(((Direction)this.level.getBlockState(this.worldPosition).getValue(CommandBlock.FACING)).getOpposite());
         if (this.level.getBlockState($$0).getBlock() instanceof CommandBlock) {
            BlockEntity $$1 = this.level.getBlockEntity($$0);
            this.conditionMet = $$1 instanceof CommandBlockEntity && ((CommandBlockEntity)$$1).getCommandBlock().getSuccessCount() > 0;
         } else {
            this.conditionMet = false;
         }
      }

      return this.conditionMet;
   }

   public CommandBlockEntity.Mode getMode() {
      BlockState $$0 = this.getBlockState();
      if ($$0.is(Blocks.COMMAND_BLOCK)) {
         return CommandBlockEntity.Mode.REDSTONE;
      } else if ($$0.is(Blocks.REPEATING_COMMAND_BLOCK)) {
         return CommandBlockEntity.Mode.AUTO;
      } else {
         return $$0.is(Blocks.CHAIN_COMMAND_BLOCK) ? CommandBlockEntity.Mode.SEQUENCE : CommandBlockEntity.Mode.REDSTONE;
      }
   }

   public boolean isConditional() {
      BlockState $$0 = this.level.getBlockState(this.getBlockPos());
      return $$0.getBlock() instanceof CommandBlock ? $$0.getValue(CommandBlock.CONDITIONAL) : false;
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.commandBlock.setCustomName((Component)$$0.get(DataComponents.CUSTOM_NAME));
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.CUSTOM_NAME, this.commandBlock.getCustomName());
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      super.removeComponentsFromTag($$0);
      $$0.discard("CustomName");
      $$0.discard("conditionMet");
      $$0.discard("powered");
   }

   public static enum Mode {
      SEQUENCE,
      AUTO,
      REDSTONE;
   }
}
