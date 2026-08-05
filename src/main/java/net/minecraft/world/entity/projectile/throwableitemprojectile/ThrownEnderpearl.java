package net.minecraft.world.entity.projectile.throwableitemprojectile;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderpearl extends ThrowableItemProjectile {
   private long ticketTimer = 0L;

   public ThrownEnderpearl(net.minecraft.world.entity.EntityType<? extends ThrownEnderpearl> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ThrownEnderpearl(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2) {
      super(net.minecraft.world.entity.EntityType.ENDER_PEARL, $$1, $$0, $$2);
   }

   @Override
   protected Item getDefaultItem() {
      return Items.ENDER_PEARL;
   }

   @Override
   protected void setOwner(net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.Entity> $$0) {
      this.deregisterFromCurrentOwner();
      super.setOwner($$0);
      this.registerToCurrentOwner();
   }

   private void deregisterFromCurrentOwner() {
      if (this.getOwner() instanceof ServerPlayer $$0) {
         $$0.deregisterEnderPearl(this);
      }
   }

   private void registerToCurrentOwner() {
      if (this.getOwner() instanceof ServerPlayer $$0) {
         $$0.registerEnderPearl(this);
      }
   }

   
   @Override
   public net.minecraft.world.entity.Entity getOwner() {
      return this.owner != null && this.level() instanceof ServerLevel $$0
         ? (net.minecraft.world.entity.Entity)this.owner.getEntity($$0, net.minecraft.world.entity.Entity.class)
         : super.getOwner();
   }

   
   private static net.minecraft.world.entity.Entity findOwnerIncludingDeadPlayer(ServerLevel $$0, UUID $$1) {
      net.minecraft.world.entity.Entity $$2 = $$0.getEntityInAnyDimension($$1);
      return (net.minecraft.world.entity.Entity)($$2 != null ? $$2 : $$0.getServer().getPlayerList().getPlayer($$1));
   }

   @Override
   protected void onHitEntity(EntityHitResult $$0) {
      super.onHitEntity($$0);
      $$0.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
   }

   @Override
   protected void onHit(HitResult $$0) {
      super.onHit($$0);

      for (int $$1 = 0; $$1 < 32; $$1++) {
         this.level()
            .addParticle(
               ParticleTypes.PORTAL,
               this.getX(),
               this.getY() + this.random.nextDouble() * 2.0,
               this.getZ(),
               this.random.nextGaussian(),
               0.0,
               this.random.nextGaussian()
            );
      }

      if (this.level() instanceof ServerLevel $$2 && !this.isRemoved()) {
         net.minecraft.world.entity.Entity $$4 = this.getOwner();
         if ($$4 != null && isAllowedToTeleportOwner($$4, $$2)) {
            Vec3 $$5 = this.oldPosition();
            if ($$4 instanceof ServerPlayer $$6) {
               if ($$6.connection.isAcceptingMessages()) {
                  if (this.random.nextFloat() < 0.05F && $$2.isSpawningMonsters()) {
                     Endermite $$7 = net.minecraft.world.entity.EntityType.ENDERMITE.create($$2, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                     if ($$7 != null) {
                        $$7.snapTo($$4.getX(), $$4.getY(), $$4.getZ(), $$4.getYRot(), $$4.getXRot());
                        $$2.addFreshEntity($$7);
                     }
                  }

                  if (this.isOnPortalCooldown()) {
                     $$4.setPortalCooldown();
                  }

                  ServerPlayer $$8 = $$6.teleport(
                     new TeleportTransition(
                        $$2,
                        $$5,
                        Vec3.ZERO,
                        0.0F,
                        0.0F,
                        net.minecraft.world.entity.Relative.union(net.minecraft.world.entity.Relative.ROTATION, net.minecraft.world.entity.Relative.DELTA),
                        TeleportTransition.DO_NOTHING
                     )
                  );
                  if ($$8 != null) {
                     $$8.resetFallDistance();
                     $$8.resetCurrentImpulseContext();
                     $$8.hurtServer($$6.level(), this.damageSources().enderPearl(), 5.0F);
                  }

                  this.playSound($$2, $$5);
               }
            } else {
               net.minecraft.world.entity.Entity $$9 = $$4.teleport(
                  new TeleportTransition($$2, $$5, $$4.getDeltaMovement(), $$4.getYRot(), $$4.getXRot(), TeleportTransition.DO_NOTHING)
               );
               if ($$9 != null) {
                  $$9.resetFallDistance();
               }

               this.playSound($$2, $$5);
            }

            this.discard();
         } else {
            this.discard();
         }
      }
   }

   private static boolean isAllowedToTeleportOwner(net.minecraft.world.entity.Entity $$0, Level $$1) {
      if ($$0.level().dimension() == $$1.dimension()) {
         return !($$0 instanceof net.minecraft.world.entity.LivingEntity $$2) ? $$0.isAlive() : $$2.isAlive() && !$$2.isSleeping();
      } else {
         return $$0.canUsePortal(true);
      }
   }

   @Override
   public void tick() {
      if (this.level() instanceof ServerLevel $$0) {
         int var7 = SectionPos.blockToSectionCoord(this.position().x());
         int $$3 = SectionPos.blockToSectionCoord(this.position().z());
         net.minecraft.world.entity.Entity $$4 = this.owner != null ? findOwnerIncludingDeadPlayer($$0, this.owner.getUUID()) : null;
         if ($$4 instanceof ServerPlayer $$5
            && !$$4.isAlive()
            && !$$5.wonGame
            && (Boolean)$$5.level().getGameRules().get(GameRules.ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
         } else {
            super.tick();
         }

         if (this.isAlive()) {
            BlockPos $$6 = BlockPos.containing(this.position());
            if ((--this.ticketTimer <= 0L || var7 != SectionPos.blockToSectionCoord($$6.getX()) || $$3 != SectionPos.blockToSectionCoord($$6.getZ()))
               && $$4 instanceof ServerPlayer $$7) {
               this.ticketTimer = $$7.registerAndUpdateEnderPearlTicket(this);
            }
         }
      } else {
         super.tick();
      }
   }

   private void playSound(Level $$0, Vec3 $$1) {
      $$0.playSound(null, $$1.x, $$1.y, $$1.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
   }

   
   @Override
   public net.minecraft.world.entity.Entity teleport(TeleportTransition $$0) {
      net.minecraft.world.entity.Entity $$1 = super.teleport($$0);
      if ($$1 != null) {
         $$1.placePortalTicket(BlockPos.containing($$1.position()));
      }

      return $$1;
   }

   @Override
   public boolean canTeleport(Level $$0, Level $$1) {
      return $$0.dimension() == Level.END && $$1.dimension() == Level.OVERWORLD && this.getOwner() instanceof ServerPlayer $$2
         ? super.canTeleport($$0, $$1) && $$2.seenCredits
         : super.canTeleport($$0, $$1);
   }

   @Override
   protected void onInsideBlock(BlockState $$0) {
      super.onInsideBlock($$0);
      if ($$0.is(Blocks.END_GATEWAY) && this.getOwner() instanceof ServerPlayer $$1) {
         $$1.onInsideBlock($$0);
      }
   }

   @Override
   public void onRemoval(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      if ($$0 != net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
         this.deregisterFromCurrentOwner();
      }

      super.onRemoval($$0);
   }

   @Override
   public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
      net.minecraft.world.entity.Entity.handleOnAboveBubbleColumn(this, $$0, $$1);
   }

   @Override
   public void onInsideBubbleColumn(boolean $$0) {
      net.minecraft.world.entity.Entity.handleOnInsideBubbleColumn(this, $$0);
   }
}
