package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Pos;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.PosRot;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Rot;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior.MinecartStep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int TOLERANCE_LEVEL_ROTATION = 1;
   private static final double TOLERANCE_LEVEL_POSITION = 7.6293945E-6F;
   public static final int FORCED_POS_UPDATE_PERIOD = 60;
   private static final int FORCED_TELEPORT_PERIOD = 400;
   private final ServerLevel level;
   private final Entity entity;
   private final int updateInterval;
   private final boolean trackDelta;
   private final ServerEntity.Synchronizer synchronizer;
   private final VecDeltaCodec positionCodec = new VecDeltaCodec();
   private byte lastSentYRot;
   private byte lastSentXRot;
   private byte lastSentYHeadRot;
   private Vec3 lastSentMovement;
   private int tickCount;
   private int teleportDelay;
   private List<Entity> lastPassengers = Collections.emptyList();
   private boolean wasRiding;
   private boolean wasOnGround;
   
   private List<DataValue<?>> trackedDataValues;

   public ServerEntity(ServerLevel $$0, Entity $$1, int $$2, boolean $$3, ServerEntity.Synchronizer $$4) {
      this.level = $$0;
      this.synchronizer = $$4;
      this.entity = $$1;
      this.updateInterval = $$2;
      this.trackDelta = $$3;
      this.positionCodec.setBase($$1.trackingPosition());
      this.lastSentMovement = $$1.getDeltaMovement();
      this.lastSentYRot = Mth.packDegrees($$1.getYRot());
      this.lastSentXRot = Mth.packDegrees($$1.getXRot());
      this.lastSentYHeadRot = Mth.packDegrees($$1.getYHeadRot());
      this.wasOnGround = $$1.onGround();
      this.trackedDataValues = $$1.getEntityData().getNonDefaultValues();
   }

   public void sendChanges() {
      this.entity.updateDataBeforeSync();
      List<Entity> $$0 = this.entity.getPassengers();
      if (!$$0.equals(this.lastPassengers)) {
         this.synchronizer
            .sendToTrackingPlayersFiltered(new ClientboundSetPassengersPacket(this.entity), $$1 -> $$0.contains($$1) == this.lastPassengers.contains($$1));
         this.lastPassengers = $$0;
      }

      if (this.entity instanceof ItemFrame $$1 && this.tickCount % 10 == 0) {
         ItemStack $$2 = $$1.getItem();
         if ($$2.getItem() instanceof MapItem) {
            MapId $$3 = (MapId)$$2.get(DataComponents.MAP_ID);
            MapItemSavedData $$4 = MapItem.getSavedData($$3, this.level);
            if ($$4 != null) {
               for (ServerPlayer $$5 : this.level.players()) {
                  $$4.tickCarriedBy($$5, $$2);
                  Packet<?> $$6 = $$4.getUpdatePacket($$3, $$5);
                  if ($$6 != null) {
                     $$5.connection.send($$6);
                  }
               }
            }
         }

         this.sendDirtyEntityData();
      }

      if (this.tickCount % this.updateInterval == 0 || this.entity.needsSync || this.entity.getEntityData().isDirty()) {
         byte $$7 = Mth.packDegrees(this.entity.getYRot());
         byte $$8 = Mth.packDegrees(this.entity.getXRot());
         boolean $$9 = Math.abs($$7 - this.lastSentYRot) >= 1 || Math.abs($$8 - this.lastSentXRot) >= 1;
         if (this.entity.isPassenger()) {
            if ($$9) {
               this.synchronizer.sendToTrackingPlayers(new Rot(this.entity.getId(), $$7, $$8, this.entity.onGround()));
               this.lastSentYRot = $$7;
               this.lastSentXRot = $$8;
            }

            this.positionCodec.setBase(this.entity.trackingPosition());
            this.sendDirtyEntityData();
            this.wasRiding = true;
         } else if (this.entity instanceof AbstractMinecart $$10 && $$10.getBehavior() instanceof NewMinecartBehavior $$11) {
            this.handleMinecartPosRot($$11, $$7, $$8, $$9);
         } else {
            this.teleportDelay++;
            Vec3 $$12 = this.entity.trackingPosition();
            boolean $$13 = this.positionCodec.delta($$12).lengthSqr() >= 7.6293945E-6F;
            Packet<ClientGamePacketListener> $$14 = null;
            boolean $$15 = $$13 || this.tickCount % 60 == 0;
            boolean $$16 = false;
            boolean $$17 = false;
            long $$18 = this.positionCodec.encodeX($$12);
            long $$19 = this.positionCodec.encodeY($$12);
            long $$20 = this.positionCodec.encodeZ($$12);
            boolean $$21 = $$18 < -32768L || $$18 > 32767L || $$19 < -32768L || $$19 > 32767L || $$20 < -32768L || $$20 > 32767L;
            if (this.entity.getRequiresPrecisePosition() || $$21 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
               this.wasOnGround = this.entity.onGround();
               this.teleportDelay = 0;
               $$14 = ClientboundEntityPositionSyncPacket.of(this.entity);
               $$16 = true;
               $$17 = true;
            } else if ((!$$15 || !$$9) && !(this.entity instanceof AbstractArrow)) {
               if ($$15) {
                  $$14 = new Pos(this.entity.getId(), (short)$$18, (short)$$19, (short)$$20, this.entity.onGround());
                  $$16 = true;
               } else if ($$9) {
                  $$14 = new Rot(this.entity.getId(), $$7, $$8, this.entity.onGround());
                  $$17 = true;
               }
            } else {
               $$14 = new PosRot(this.entity.getId(), (short)$$18, (short)$$19, (short)$$20, $$7, $$8, this.entity.onGround());
               $$16 = true;
               $$17 = true;
            }

            if (this.entity.needsSync || this.trackDelta || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) {
               Vec3 $$22 = this.entity.getDeltaMovement();
               double $$23 = $$22.distanceToSqr(this.lastSentMovement);
               if ($$23 > 1.0E-7 || $$23 > 0.0 && $$22.lengthSqr() == 0.0) {
                  this.lastSentMovement = $$22;
                  if (this.entity instanceof AbstractHurtingProjectile $$24) {
                     this.synchronizer
                        .sendToTrackingPlayers(
                           new ClientboundBundlePacket(
                              List.of(
                                 new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement),
                                 new ClientboundProjectilePowerPacket($$24.getId(), $$24.accelerationPower)
                              )
                           )
                        );
                  } else {
                     this.synchronizer.sendToTrackingPlayers(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
                  }
               }
            }

            if ($$14 != null) {
               this.synchronizer.sendToTrackingPlayers($$14);
            }

            this.sendDirtyEntityData();
            if ($$16) {
               this.positionCodec.setBase($$12);
            }

            if ($$17) {
               this.lastSentYRot = $$7;
               this.lastSentXRot = $$8;
            }

            this.wasRiding = false;
         }

         byte $$25 = Mth.packDegrees(this.entity.getYHeadRot());
         if (Math.abs($$25 - this.lastSentYHeadRot) >= 1) {
            this.synchronizer.sendToTrackingPlayers(new ClientboundRotateHeadPacket(this.entity, $$25));
            this.lastSentYHeadRot = $$25;
         }

         this.entity.needsSync = false;
      }

      this.tickCount++;
      if (this.entity.hurtMarked) {
         this.entity.hurtMarked = false;
         this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundSetEntityMotionPacket(this.entity));
      }
   }

   private void handleMinecartPosRot(NewMinecartBehavior $$0, byte $$1, byte $$2, boolean $$3) {
      this.sendDirtyEntityData();
      if ($$0.lerpSteps.isEmpty()) {
         Vec3 $$4 = this.entity.getDeltaMovement();
         double $$5 = $$4.distanceToSqr(this.lastSentMovement);
         Vec3 $$6 = this.entity.trackingPosition();
         boolean $$7 = this.positionCodec.delta($$6).lengthSqr() >= 7.6293945E-6F;
         boolean $$8 = $$7 || this.tickCount % 60 == 0;
         if ($$8 || $$3 || $$5 > 1.0E-7) {
            this.synchronizer
               .sendToTrackingPlayers(
                  new ClientboundMoveMinecartPacket(
                     this.entity.getId(),
                     List.of(new MinecartStep(this.entity.position(), this.entity.getDeltaMovement(), this.entity.getYRot(), this.entity.getXRot(), 1.0F))
                  )
               );
         }
      } else {
         this.synchronizer.sendToTrackingPlayers(new ClientboundMoveMinecartPacket(this.entity.getId(), List.copyOf($$0.lerpSteps)));
         $$0.lerpSteps.clear();
      }

      this.lastSentYRot = $$1;
      this.lastSentXRot = $$2;
      this.positionCodec.setBase(this.entity.position());
   }

   public void removePairing(ServerPlayer $$0) {
      this.entity.stopSeenByPlayer($$0);
      $$0.connection.send(new ClientboundRemoveEntitiesPacket(new int[]{this.entity.getId()}));
   }

   public void addPairing(ServerPlayer $$0) {
      List<Packet<? super ClientGamePacketListener>> $$1 = new ArrayList<>();
      this.sendPairingData($$0, $$1::add);
      $$0.connection.send(new ClientboundBundlePacket($$1));
      this.entity.startSeenByPlayer($$0);
   }

   public void sendPairingData(ServerPlayer $$0, Consumer<Packet<ClientGamePacketListener>> $$1) {
      this.entity.updateDataBeforeSync();
      if (this.entity.isRemoved()) {
         LOGGER.warn("Fetching packet for removed entity {}", this.entity);
      }

      Packet<ClientGamePacketListener> $$2 = this.entity.getAddEntityPacket(this);
      $$1.accept($$2);
      if (this.trackedDataValues != null) {
         $$1.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
      }

      if (this.entity instanceof LivingEntity $$3) {
         Collection<AttributeInstance> $$4 = $$3.getAttributes().getSyncableAttributes();
         if (!$$4.isEmpty()) {
            $$1.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), $$4));
         }
      }

      if (this.entity instanceof LivingEntity $$5) {
         List<Pair<EquipmentSlot, ItemStack>> $$6 = Lists.newArrayList();

         for (EquipmentSlot $$7 : EquipmentSlot.VALUES) {
            ItemStack $$8 = $$5.getItemBySlot($$7);
            if (!$$8.isEmpty()) {
               $$6.add(Pair.of($$7, $$8.copy()));
            }
         }

         if (!$$6.isEmpty()) {
            $$1.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), $$6));
         }
      }

      if (!this.entity.getPassengers().isEmpty()) {
         $$1.accept(new ClientboundSetPassengersPacket(this.entity));
      }

      if (this.entity.isPassenger()) {
         $$1.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
      }

      if (this.entity instanceof Leashable $$9 && $$9.isLeashed()) {
         $$1.accept(new ClientboundSetEntityLinkPacket(this.entity, $$9.getLeashHolder()));
      }
   }

   public Vec3 getPositionBase() {
      return this.positionCodec.getBase();
   }

   public Vec3 getLastSentMovement() {
      return this.lastSentMovement;
   }

   public float getLastSentXRot() {
      return Mth.unpackDegrees(this.lastSentXRot);
   }

   public float getLastSentYRot() {
      return Mth.unpackDegrees(this.lastSentYRot);
   }

   public float getLastSentYHeadRot() {
      return Mth.unpackDegrees(this.lastSentYHeadRot);
   }

   private void sendDirtyEntityData() {
      SynchedEntityData $$0 = this.entity.getEntityData();
      List<DataValue<?>> $$1 = $$0.packDirty();
      if ($$1 != null) {
         this.trackedDataValues = $$0.getNonDefaultValues();
         this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundSetEntityDataPacket(this.entity.getId(), $$1));
      }

      if (this.entity instanceof LivingEntity) {
         Set<AttributeInstance> $$2 = ((LivingEntity)this.entity).getAttributes().getAttributesToSync();
         if (!$$2.isEmpty()) {
            this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundUpdateAttributesPacket(this.entity.getId(), $$2));
         }

         $$2.clear();
      }
   }

   public interface Synchronizer {
      void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> var1);

      void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> var1);

      void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> var1, Predicate<ServerPlayer> var2);
   }
}
