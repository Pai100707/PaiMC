package net.minecraft.world.item.context;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class UseOnContext {
   
   private final Player player;
   private final InteractionHand hand;
   private final BlockHitResult hitResult;
   private final Level level;
   private final net.minecraft.world.item.ItemStack itemStack;

   public UseOnContext(Player $$0, InteractionHand $$1, BlockHitResult $$2) {
      this($$0.level(), $$0, $$1, $$0.getItemInHand($$1), $$2);
   }

   protected UseOnContext(Level $$0, Player $$1, InteractionHand $$2, net.minecraft.world.item.ItemStack $$3, BlockHitResult $$4) {
      this.player = $$1;
      this.hand = $$2;
      this.hitResult = $$4;
      this.itemStack = $$3;
      this.level = $$0;
   }

   protected final BlockHitResult getHitResult() {
      return this.hitResult;
   }

   public BlockPos getClickedPos() {
      return this.hitResult.getBlockPos();
   }

   public Direction getClickedFace() {
      return this.hitResult.getDirection();
   }

   public Vec3 getClickLocation() {
      return this.hitResult.getLocation();
   }

   public boolean isInside() {
      return this.hitResult.isInside();
   }

   public net.minecraft.world.item.ItemStack getItemInHand() {
      return this.itemStack;
   }

   
   public Player getPlayer() {
      return this.player;
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public Level getLevel() {
      return this.level;
   }

   public Direction getHorizontalDirection() {
      return this.player == null ? Direction.NORTH : this.player.getDirection();
   }

   public boolean isSecondaryUseActive() {
      return this.player != null && this.player.isSecondaryUseActive();
   }

   public float getRotation() {
      return this.player == null ? 0.0F : this.player.getYRot();
   }
}
