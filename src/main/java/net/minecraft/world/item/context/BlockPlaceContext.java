package net.minecraft.world.item.context;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BlockPlaceContext extends UseOnContext {
   private final BlockPos relativePos;
   protected boolean replaceClicked = true;

   public BlockPlaceContext(Player $$0, InteractionHand $$1, net.minecraft.world.item.ItemStack $$2, BlockHitResult $$3) {
      this($$0.level(), $$0, $$1, $$2, $$3);
   }

   public BlockPlaceContext(UseOnContext $$0) {
      this($$0.getLevel(), $$0.getPlayer(), $$0.getHand(), $$0.getItemInHand(), $$0.getHitResult());
   }

   protected BlockPlaceContext(Level $$0, @Nullable Player $$1, InteractionHand $$2, net.minecraft.world.item.ItemStack $$3, BlockHitResult $$4) {
      super($$0, $$1, $$2, $$3, $$4);
      this.relativePos = $$4.getBlockPos().relative($$4.getDirection());
      this.replaceClicked = $$0.getBlockState($$4.getBlockPos()).canBeReplaced(this);
   }

   public static BlockPlaceContext at(BlockPlaceContext $$0, BlockPos $$1, Direction $$2) {
      return new BlockPlaceContext(
         $$0.getLevel(),
         $$0.getPlayer(),
         $$0.getHand(),
         $$0.getItemInHand(),
         new BlockHitResult(
            new Vec3($$1.getX() + 0.5 + $$2.getStepX() * 0.5, $$1.getY() + 0.5 + $$2.getStepY() * 0.5, $$1.getZ() + 0.5 + $$2.getStepZ() * 0.5),
            $$2,
            $$1,
            false
         )
      );
   }

   @Override
   public BlockPos getClickedPos() {
      return this.replaceClicked ? super.getClickedPos() : this.relativePos;
   }

   public boolean canPlace() {
      return this.replaceClicked || this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
   }

   public boolean replacingClickedOnBlock() {
      return this.replaceClicked;
   }

   public Direction getNearestLookingDirection() {
      return Direction.orderedByNearest(this.getPlayer())[0];
   }

   public Direction getNearestLookingVerticalDirection() {
      return Direction.getFacingAxis(this.getPlayer(), Axis.Y);
   }

   public Direction[] getNearestLookingDirections() {
      Direction[] $$0 = Direction.orderedByNearest(this.getPlayer());
      if (this.replaceClicked) {
         return $$0;
      } else {
         Direction $$1 = this.getClickedFace();
         int $$2 = 0;

         while ($$2 < $$0.length && $$0[$$2] != $$1.getOpposite()) {
            $$2++;
         }

         if ($$2 > 0) {
            System.arraycopy($$0, 0, $$0, 1, $$2);
            $$0[0] = $$1.getOpposite();
         }

         return $$0;
      }
   }
}
