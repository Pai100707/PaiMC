package net.minecraft.world.level.block;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SideChainPart;

public interface SideChainPartBlock {
   SideChainPart getSideChainPart(BlockState var1);

   BlockState setSideChainPart(BlockState var1, SideChainPart var2);

   Direction getFacing(BlockState var1);

   boolean isConnectable(BlockState var1);

   int getMaxChainLength();

   default List<BlockPos> getAllBlocksConnectedTo(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      if (!this.isConnectable($$2)) {
         return List.of();
      } else {
         SideChainPartBlock.Neighbors $$3 = this.getNeighbors($$0, $$1, this.getFacing($$2));
         List<BlockPos> $$4 = new LinkedList<>();
         $$4.add($$1);
         this.addBlocksConnectingTowards($$3::left, SideChainPart.LEFT, $$4::addFirst);
         this.addBlocksConnectingTowards($$3::right, SideChainPart.RIGHT, $$4::addLast);
         return $$4;
      }
   }

   private void addBlocksConnectingTowards(IntFunction<SideChainPartBlock.Neighbor> $$0, SideChainPart $$1, Consumer<BlockPos> $$2) {
      for (int $$3 = 1; $$3 < this.getMaxChainLength(); $$3++) {
         SideChainPartBlock.Neighbor $$4 = $$0.apply($$3);
         if ($$4.connectsTowards($$1)) {
            $$2.accept($$4.pos());
         }

         if ($$4.isUnconnectableOrChainEnd()) {
            break;
         }
      }
   }

   default void updateNeighborsAfterPoweringDown(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2) {
      SideChainPartBlock.Neighbors $$3 = this.getNeighbors($$0, $$1, this.getFacing($$2));
      $$3.left().disconnectFromRight();
      $$3.right().disconnectFromLeft();
   }

   default void updateSelfAndNeighborsOnPoweringUp(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, BlockState $$3) {
      if (this.isConnectable($$2)) {
         if (!this.isBeingUpdatedByNeighbor($$2, $$3)) {
            SideChainPartBlock.Neighbors $$4 = this.getNeighbors($$0, $$1, this.getFacing($$2));
            SideChainPart $$5 = SideChainPart.UNCONNECTED;
            int $$6 = $$4.left().isConnectable() ? this.getAllBlocksConnectedTo($$0, $$4.left().pos()).size() : 0;
            int $$7 = $$4.right().isConnectable() ? this.getAllBlocksConnectedTo($$0, $$4.right().pos()).size() : 0;
            int $$8 = 1;
            if (this.canConnect($$6, $$8)) {
               $$5 = $$5.whenConnectedToTheLeft();
               $$4.left().connectToTheRight();
               $$8 += $$6;
            }

            if (this.canConnect($$7, $$8)) {
               $$5 = $$5.whenConnectedToTheRight();
               $$4.right().connectToTheLeft();
            }

            this.setPart($$0, $$1, $$5);
         }
      }
   }

   private boolean canConnect(int $$0, int $$1) {
      return $$0 > 0 && $$1 + $$0 <= this.getMaxChainLength();
   }

   private boolean isBeingUpdatedByNeighbor(BlockState $$0, BlockState $$1) {
      boolean $$2 = this.getSideChainPart($$0).isConnected();
      boolean $$3 = this.isConnectable($$1) && this.getSideChainPart($$1).isConnected();
      return $$2 || $$3;
   }

   private SideChainPartBlock.Neighbors getNeighbors(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, Direction $$2) {
      return new SideChainPartBlock.Neighbors(this, $$0, $$2, $$1, new HashMap<>());
   }

   default void setPart(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, SideChainPart $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      if (this.getSideChainPart($$3) != $$2) {
         $$0.setBlock($$1, this.setSideChainPart($$3, $$2), 3);
      }
   }

   public record EmptyNeighbor(BlockPos pos) implements SideChainPartBlock.Neighbor {
      @Override
      public boolean isConnectable() {
         return false;
      }

      @Override
      public boolean isUnconnectableOrChainEnd() {
         return true;
      }

      @Override
      public boolean connectsTowards(SideChainPart $$0) {
         return false;
      }
   }

   public sealed interface Neighbor permits SideChainPartBlock.EmptyNeighbor, SideChainPartBlock.SideChainNeighbor {
      BlockPos pos();

      boolean isConnectable();

      boolean isUnconnectableOrChainEnd();

      boolean connectsTowards(SideChainPart var1);

      default void connectToTheRight() {
      }

      default void connectToTheLeft() {
      }

      default void disconnectFromRight() {
      }

      default void disconnectFromLeft() {
      }
   }

   public record Neighbors(
      SideChainPartBlock block,
      net.minecraft.world.level.LevelAccessor level,
      Direction facing,
      BlockPos center,
      Map<BlockPos, SideChainPartBlock.Neighbor> cache
   ) {
      private boolean isConnectableToThisBlock(BlockState $$0) {
         return this.block.isConnectable($$0) && this.block.getFacing($$0) == this.facing;
      }

      private SideChainPartBlock.Neighbor createNewNeighbor(BlockPos $$0) {
         BlockState $$1 = this.level.getBlockState($$0);
         SideChainPart $$2 = this.isConnectableToThisBlock($$1) ? this.block.getSideChainPart($$1) : null;
         return (SideChainPartBlock.Neighbor)($$2 == null
            ? new SideChainPartBlock.EmptyNeighbor($$0)
            : new SideChainPartBlock.SideChainNeighbor(this.level, this.block, $$0, $$2));
      }

      private SideChainPartBlock.Neighbor getOrCreateNeighbor(Direction $$0, Integer $$1) {
         return this.cache.computeIfAbsent(this.center.relative($$0, $$1), this::createNewNeighbor);
      }

      public SideChainPartBlock.Neighbor left(int $$0) {
         return this.getOrCreateNeighbor(this.facing.getClockWise(), $$0);
      }

      public SideChainPartBlock.Neighbor right(int $$0) {
         return this.getOrCreateNeighbor(this.facing.getCounterClockWise(), $$0);
      }

      public SideChainPartBlock.Neighbor left() {
         return this.left(1);
      }

      public SideChainPartBlock.Neighbor right() {
         return this.right(1);
      }
   }

   public record SideChainNeighbor(net.minecraft.world.level.LevelAccessor level, SideChainPartBlock block, BlockPos pos, SideChainPart part)
      implements SideChainPartBlock.Neighbor {
      @Override
      public boolean isConnectable() {
         return true;
      }

      @Override
      public boolean isUnconnectableOrChainEnd() {
         return this.part.isChainEnd();
      }

      @Override
      public boolean connectsTowards(SideChainPart $$0) {
         return this.part.isConnectionTowards($$0);
      }

      @Override
      public void connectToTheRight() {
         this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheRight());
      }

      @Override
      public void connectToTheLeft() {
         this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheLeft());
      }

      @Override
      public void disconnectFromRight() {
         this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheRight());
      }

      @Override
      public void disconnectFromLeft() {
         this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheLeft());
      }
   }
}
