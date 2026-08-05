package net.minecraft.world.entity.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LeashFenceKnotEntity extends BlockAttachedEntity {
   public static final double OFFSET_Y = 0.375;

   public LeashFenceKnotEntity(net.minecraft.world.entity.EntityType<? extends LeashFenceKnotEntity> $$0, Level $$1) {
      super($$0, $$1);
   }

   public LeashFenceKnotEntity(Level $$0, BlockPos $$1) {
      super(net.minecraft.world.entity.EntityType.LEASH_KNOT, $$0, $$1);
      this.setPos($$1.getX(), $$1.getY(), $$1.getZ());
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
   }

   @Override
   protected void recalculateBoundingBox() {
      this.setPosRaw(this.pos.getX() + 0.5, this.pos.getY() + 0.375, this.pos.getZ() + 0.5);
      double $$0 = this.getType().getWidth() / 2.0;
      double $$1 = this.getType().getHeight();
      this.setBoundingBox(new AABB(this.getX() - $$0, this.getY(), this.getZ() - $$0, this.getX() + $$0, this.getY() + $$1, this.getZ() + $$0));
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      return $$0 < 1024.0;
   }

   @Override
   public void dropItem(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      this.playSound(SoundEvents.LEAD_UNTIED, 1.0F, 1.0F);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      if (this.level().isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         if ($$0.getItemInHand($$1).is(Items.SHEARS)) {
            InteractionResult $$2 = super.interact($$0, $$1);
            if ($$2 instanceof Success $$3 && $$3.wasItemInteraction()) {
               return $$2;
            }
         }

         boolean $$4 = false;

         for (net.minecraft.world.entity.Leashable $$6 : net.minecraft.world.entity.Leashable.leashableLeashedTo($$0)) {
            if ($$6.canHaveALeashAttachedTo(this)) {
               $$6.setLeashedTo(this, true);
               $$4 = true;
            }
         }

         boolean $$7 = false;
         if (!$$4 && !$$0.isSecondaryUseActive()) {
            for (net.minecraft.world.entity.Leashable $$9 : net.minecraft.world.entity.Leashable.leashableLeashedTo(this)) {
               if ($$9.canHaveALeashAttachedTo($$0)) {
                  $$9.setLeashedTo($$0, true);
                  $$7 = true;
               }
            }
         }

         if (!$$4 && !$$7) {
            return super.interact($$0, $$1);
         } else {
            this.gameEvent(GameEvent.BLOCK_ATTACH, $$0);
            this.playSound(SoundEvents.LEAD_TIED);
            return InteractionResult.SUCCESS;
         }
      }
   }

   @Override
   public void notifyLeasheeRemoved(net.minecraft.world.entity.Leashable $$0) {
      if (net.minecraft.world.entity.Leashable.leashableLeashedTo(this).isEmpty()) {
         this.discard();
      }
   }

   @Override
   public boolean survives() {
      return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
   }

   public static LeashFenceKnotEntity getOrCreateKnot(Level $$0, BlockPos $$1) {
      int $$2 = $$1.getX();
      int $$3 = $$1.getY();
      int $$4 = $$1.getZ();

      for (LeashFenceKnotEntity $$6 : $$0.getEntitiesOfClass(
         LeashFenceKnotEntity.class, new AABB($$2 - 1.0, $$3 - 1.0, $$4 - 1.0, $$2 + 1.0, $$3 + 1.0, $$4 + 1.0)
      )) {
         if ($$6.getPos().equals($$1)) {
            return $$6;
         }
      }

      LeashFenceKnotEntity $$7 = new LeashFenceKnotEntity($$0, $$1);
      $$0.addFreshEntity($$7);
      return $$7;
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.LEAD_TIED, 1.0F, 1.0F);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      return new ClientboundAddEntityPacket(this, 0, this.getPos());
   }

   @Override
   public Vec3 getRopeHoldPosition(float $$0) {
      return this.getPosition($$0).add(0.0, 0.2, 0.0);
   }

   @Override
   public ItemStack getPickResult() {
      return new ItemStack(Items.LEAD);
   }
}
