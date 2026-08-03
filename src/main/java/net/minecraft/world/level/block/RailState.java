package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jspecify.annotations.Nullable;

public class RailState {
   private final net.minecraft.world.level.Level level;
   private final BlockPos pos;
   private final BaseRailBlock block;
   private BlockState state;
   private final boolean isStraight;
   private final List<BlockPos> connections = Lists.newArrayList();

   public RailState(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      this.level = $$0;
      this.pos = $$1;
      this.state = $$2;
      this.block = (BaseRailBlock)$$2.getBlock();
      RailShape $$3 = $$2.getValue(this.block.getShapeProperty());
      this.isStraight = this.block.isStraight();
      this.updateConnections($$3);
   }

   public List<BlockPos> getConnections() {
      return this.connections;
   }

   private void updateConnections(RailShape $$0) {
      this.connections.clear();
      switch ($$0) {
         case NORTH_SOUTH:
            this.connections.add(this.pos.north());
            this.connections.add(this.pos.south());
            break;
         case EAST_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.east());
            break;
         case ASCENDING_EAST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.east().above());
            break;
         case ASCENDING_WEST:
            this.connections.add(this.pos.west().above());
            this.connections.add(this.pos.east());
            break;
         case ASCENDING_NORTH:
            this.connections.add(this.pos.north().above());
            this.connections.add(this.pos.south());
            break;
         case ASCENDING_SOUTH:
            this.connections.add(this.pos.north());
            this.connections.add(this.pos.south().above());
            break;
         case SOUTH_EAST:
            this.connections.add(this.pos.east());
            this.connections.add(this.pos.south());
            break;
         case SOUTH_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.south());
            break;
         case NORTH_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.north());
            break;
         case NORTH_EAST:
            this.connections.add(this.pos.east());
            this.connections.add(this.pos.north());
      }
   }

   private void removeSoftConnections() {
      for (int $$0 = 0; $$0 < this.connections.size(); $$0++) {
         RailState $$1 = this.getRail(this.connections.get($$0));
         if ($$1 != null && $$1.connectsTo(this)) {
            this.connections.set($$0, $$1.pos);
         } else {
            this.connections.remove($$0--);
         }
      }
   }

   private boolean hasRail(BlockPos $$0) {
      return BaseRailBlock.isRail(this.level, $$0) || BaseRailBlock.isRail(this.level, $$0.above()) || BaseRailBlock.isRail(this.level, $$0.below());
   }

   @Nullable
   private RailState getRail(BlockPos $$0) {
      BlockState $$2 = this.level.getBlockState($$0);
      if (BaseRailBlock.isRail($$2)) {
         return new RailState(this.level, $$0, $$2);
      } else {
         BlockPos $$1 = $$0.above();
         $$2 = this.level.getBlockState($$1);
         if (BaseRailBlock.isRail($$2)) {
            return new RailState(this.level, $$1, $$2);
         } else {
            $$1 = $$0.below();
            $$2 = this.level.getBlockState($$1);
            return BaseRailBlock.isRail($$2) ? new RailState(this.level, $$1, $$2) : null;
         }
      }
   }

   private boolean connectsTo(RailState $$0) {
      return this.hasConnection($$0.pos);
   }

   private boolean hasConnection(BlockPos $$0) {
      for (int $$1 = 0; $$1 < this.connections.size(); $$1++) {
         BlockPos $$2 = this.connections.get($$1);
         if ($$2.getX() == $$0.getX() && $$2.getZ() == $$0.getZ()) {
            return true;
         }
      }

      return false;
   }

   protected int countPotentialConnections() {
      int $$0 = 0;

      for (Direction $$1 : Plane.HORIZONTAL) {
         if (this.hasRail(this.pos.relative($$1))) {
            $$0++;
         }
      }

      return $$0;
   }

   private boolean canConnectTo(RailState $$0) {
      return this.connectsTo($$0) || this.connections.size() != 2;
   }

   private void connectTo(RailState $$0) {
      this.connections.add($$0.pos);
      BlockPos $$1 = this.pos.north();
      BlockPos $$2 = this.pos.south();
      BlockPos $$3 = this.pos.west();
      BlockPos $$4 = this.pos.east();
      boolean $$5 = this.hasConnection($$1);
      boolean $$6 = this.hasConnection($$2);
      boolean $$7 = this.hasConnection($$3);
      boolean $$8 = this.hasConnection($$4);
      RailShape $$9 = null;
      if ($$5 || $$6) {
         $$9 = RailShape.NORTH_SOUTH;
      }

      if ($$7 || $$8) {
         $$9 = RailShape.EAST_WEST;
      }

      if (!this.isStraight) {
         if ($$6 && $$8 && !$$5 && !$$7) {
            $$9 = RailShape.SOUTH_EAST;
         }

         if ($$6 && $$7 && !$$5 && !$$8) {
            $$9 = RailShape.SOUTH_WEST;
         }

         if ($$5 && $$7 && !$$6 && !$$8) {
            $$9 = RailShape.NORTH_WEST;
         }

         if ($$5 && $$8 && !$$6 && !$$7) {
            $$9 = RailShape.NORTH_EAST;
         }
      }

      if ($$9 == RailShape.NORTH_SOUTH) {
         if (BaseRailBlock.isRail(this.level, $$1.above())) {
            $$9 = RailShape.ASCENDING_NORTH;
         }

         if (BaseRailBlock.isRail(this.level, $$2.above())) {
            $$9 = RailShape.ASCENDING_SOUTH;
         }
      }

      if ($$9 == RailShape.EAST_WEST) {
         if (BaseRailBlock.isRail(this.level, $$4.above())) {
            $$9 = RailShape.ASCENDING_EAST;
         }

         if (BaseRailBlock.isRail(this.level, $$3.above())) {
            $$9 = RailShape.ASCENDING_WEST;
         }
      }

      if ($$9 == null) {
         $$9 = RailShape.NORTH_SOUTH;
      }

      this.state = this.state.setValue(this.block.getShapeProperty(), $$9);
      this.level.setBlock(this.pos, this.state, 3);
   }

   private boolean hasNeighborRail(BlockPos $$0) {
      RailState $$1 = this.getRail($$0);
      if ($$1 == null) {
         return false;
      } else {
         $$1.removeSoftConnections();
         return $$1.canConnectTo(this);
      }
   }

   public RailState place(boolean $$0, boolean $$1, RailShape $$2) {
      BlockPos $$3 = this.pos.north();
      BlockPos $$4 = this.pos.south();
      BlockPos $$5 = this.pos.west();
      BlockPos $$6 = this.pos.east();
      boolean $$7 = this.hasNeighborRail($$3);
      boolean $$8 = this.hasNeighborRail($$4);
      boolean $$9 = this.hasNeighborRail($$5);
      boolean $$10 = this.hasNeighborRail($$6);
      RailShape $$11 = null;
      boolean $$12 = $$7 || $$8;
      boolean $$13 = $$9 || $$10;
      if ($$12 && !$$13) {
         $$11 = RailShape.NORTH_SOUTH;
      }

      if ($$13 && !$$12) {
         $$11 = RailShape.EAST_WEST;
      }

      boolean $$14 = $$8 && $$10;
      boolean $$15 = $$8 && $$9;
      boolean $$16 = $$7 && $$10;
      boolean $$17 = $$7 && $$9;
      if (!this.isStraight) {
         if ($$14 && !$$7 && !$$9) {
            $$11 = RailShape.SOUTH_EAST;
         }

         if ($$15 && !$$7 && !$$10) {
            $$11 = RailShape.SOUTH_WEST;
         }

         if ($$17 && !$$8 && !$$10) {
            $$11 = RailShape.NORTH_WEST;
         }

         if ($$16 && !$$8 && !$$9) {
            $$11 = RailShape.NORTH_EAST;
         }
      }

      if ($$11 == null) {
         if ($$12 && $$13) {
            $$11 = $$2;
         } else if ($$12) {
            $$11 = RailShape.NORTH_SOUTH;
         } else if ($$13) {
            $$11 = RailShape.EAST_WEST;
         }

         if (!this.isStraight) {
            if ($$0) {
               if ($$14) {
                  $$11 = RailShape.SOUTH_EAST;
               }

               if ($$15) {
                  $$11 = RailShape.SOUTH_WEST;
               }

               if ($$16) {
                  $$11 = RailShape.NORTH_EAST;
               }

               if ($$17) {
                  $$11 = RailShape.NORTH_WEST;
               }
            } else {
               if ($$17) {
                  $$11 = RailShape.NORTH_WEST;
               }

               if ($$16) {
                  $$11 = RailShape.NORTH_EAST;
               }

               if ($$15) {
                  $$11 = RailShape.SOUTH_WEST;
               }

               if ($$14) {
                  $$11 = RailShape.SOUTH_EAST;
               }
            }
         }
      }

      if ($$11 == RailShape.NORTH_SOUTH) {
         if (BaseRailBlock.isRail(this.level, $$3.above())) {
            $$11 = RailShape.ASCENDING_NORTH;
         }

         if (BaseRailBlock.isRail(this.level, $$4.above())) {
            $$11 = RailShape.ASCENDING_SOUTH;
         }
      }

      if ($$11 == RailShape.EAST_WEST) {
         if (BaseRailBlock.isRail(this.level, $$6.above())) {
            $$11 = RailShape.ASCENDING_EAST;
         }

         if (BaseRailBlock.isRail(this.level, $$5.above())) {
            $$11 = RailShape.ASCENDING_WEST;
         }
      }

      if ($$11 == null) {
         $$11 = $$2;
      }

      this.updateConnections($$11);
      this.state = this.state.setValue(this.block.getShapeProperty(), $$11);
      if ($$1 || this.level.getBlockState(this.pos) != this.state) {
         this.level.setBlock(this.pos, this.state, 3);

         for (int $$18 = 0; $$18 < this.connections.size(); $$18++) {
            RailState $$19 = this.getRail(this.connections.get($$18));
            if ($$19 != null) {
               $$19.removeSoftConnections();
               if ($$19.canConnectTo(this)) {
                  $$19.connectTo(this);
               }
            }
         }
      }

      return this;
   }

   public BlockState getState() {
      return this.state;
   }
}
