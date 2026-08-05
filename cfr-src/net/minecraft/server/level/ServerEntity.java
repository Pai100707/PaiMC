/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
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
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
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
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private static final double TOLERANCE_LEVEL_POSITION = 7.62939453125E-6;
    public static final int FORCED_POS_UPDATE_PERIOD = 60;
    private static final int FORCED_TELEPORT_PERIOD = 400;
    private final ServerLevel level;
    private final Entity entity;
    private final int updateInterval;
    private final boolean trackDelta;
    private final Synchronizer synchronizer;
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
    private @Nullable List<SynchedEntityData.DataValue<?>> trackedDataValues;

    public ServerEntity(ServerLevel $$0, Entity $$1, int $$2, boolean $$3, Synchronizer $$4) {
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
        Entity entity;
        this.entity.updateDataBeforeSync();
        List<Entity> $$0 = this.entity.getPassengers();
        if (!$$0.equals(this.lastPassengers)) {
            this.synchronizer.sendToTrackingPlayersFiltered(new ClientboundSetPassengersPacket(this.entity), $$1 -> $$0.contains($$1) == this.lastPassengers.contains($$1));
            this.lastPassengers = $$0;
        }
        if ((entity = this.entity) instanceof ItemFrame) {
            ItemFrame $$12 = (ItemFrame)entity;
            if (this.tickCount % 10 == 0) {
                MapId $$3;
                MapItemSavedData $$4;
                ItemStack $$2 = $$12.getItem();
                if ($$2.getItem() instanceof MapItem && ($$4 = MapItem.getSavedData($$3 = $$2.get(DataComponents.MAP_ID), (Level)this.level)) != null) {
                    for (ServerPlayer serverPlayer : this.level.players()) {
                        $$4.tickCarriedBy(serverPlayer, $$2);
                        Packet<?> $$6 = $$4.getUpdatePacket($$3, serverPlayer);
                        if ($$6 == null) continue;
                        serverPlayer.connection.send($$6);
                    }
                }
                this.sendDirtyEntityData();
            }
        }
        if (this.tickCount % this.updateInterval == 0 || this.entity.needsSync || this.entity.getEntityData().isDirty()) {
            boolean $$9;
            byte $$7 = Mth.packDegrees(this.entity.getYRot());
            byte $$8 = Mth.packDegrees(this.entity.getXRot());
            boolean bl = $$9 = Math.abs($$7 - this.lastSentYRot) >= 1 || Math.abs($$8 - this.lastSentXRot) >= 1;
            if (this.entity.isPassenger()) {
                if ($$9) {
                    this.synchronizer.sendToTrackingPlayers(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), $$7, $$8, this.entity.onGround()));
                    this.lastSentYRot = $$7;
                    this.lastSentXRot = $$8;
                }
                this.positionCodec.setBase(this.entity.trackingPosition());
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                AbstractMinecart $$10;
                MinecartBehavior minecartBehavior;
                Entity entity2 = this.entity;
                if (entity2 instanceof AbstractMinecart && (minecartBehavior = ($$10 = (AbstractMinecart)entity2).getBehavior()) instanceof NewMinecartBehavior) {
                    NewMinecartBehavior $$11 = (NewMinecartBehavior)minecartBehavior;
                    this.handleMinecartPosRot($$11, $$7, $$8, $$9);
                } else {
                    Vec3 $$22;
                    double $$23;
                    boolean $$21;
                    ++this.teleportDelay;
                    Vec3 vec3 = this.entity.trackingPosition();
                    boolean $$13 = this.positionCodec.delta(vec3).lengthSqr() >= 7.62939453125E-6;
                    Packet<ClientGamePacketListener> $$14 = null;
                    boolean $$15 = $$13 || this.tickCount % 60 == 0;
                    boolean $$16 = false;
                    boolean $$17 = false;
                    long $$18 = this.positionCodec.encodeX(vec3);
                    long $$19 = this.positionCodec.encodeY(vec3);
                    long $$20 = this.positionCodec.encodeZ(vec3);
                    boolean bl2 = $$21 = $$18 < -32768L || $$18 > 32767L || $$19 < -32768L || $$19 > 32767L || $$20 < -32768L || $$20 > 32767L;
                    if (this.entity.getRequiresPrecisePosition() || $$21 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
                        this.wasOnGround = this.entity.onGround();
                        this.teleportDelay = 0;
                        $$14 = ClientboundEntityPositionSyncPacket.of(this.entity);
                        $$16 = true;
                        $$17 = true;
                    } else if ($$15 && $$9 || this.entity instanceof AbstractArrow) {
                        $$14 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)$$18, (short)$$19, (short)$$20, $$7, $$8, this.entity.onGround());
                        $$16 = true;
                        $$17 = true;
                    } else if ($$15) {
                        $$14 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)$$18, (short)$$19, (short)$$20, this.entity.onGround());
                        $$16 = true;
                    } else if ($$9) {
                        $$14 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), $$7, $$8, this.entity.onGround());
                        $$17 = true;
                    }
                    if ((this.entity.needsSync || this.trackDelta || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && (($$23 = ($$22 = this.entity.getDeltaMovement()).distanceToSqr(this.lastSentMovement)) > 1.0E-7 || $$23 > 0.0 && $$22.lengthSqr() == 0.0)) {
                        this.lastSentMovement = $$22;
                        Entity entity3 = this.entity;
                        if (entity3 instanceof AbstractHurtingProjectile) {
                            AbstractHurtingProjectile $$24 = (AbstractHurtingProjectile)entity3;
                            this.synchronizer.sendToTrackingPlayers(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)List.of(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement), new ClientboundProjectilePowerPacket($$24.getId(), $$24.accelerationPower))));
                        } else {
                            this.synchronizer.sendToTrackingPlayers(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
                        }
                    }
                    if ($$14 != null) {
                        this.synchronizer.sendToTrackingPlayers($$14);
                    }
                    this.sendDirtyEntityData();
                    if ($$16) {
                        this.positionCodec.setBase(vec3);
                    }
                    if ($$17) {
                        this.lastSentYRot = $$7;
                        this.lastSentXRot = $$8;
                    }
                    this.wasRiding = false;
                }
            }
            byte $$25 = Mth.packDegrees(this.entity.getYHeadRot());
            if (Math.abs($$25 - this.lastSentYHeadRot) >= 1) {
                this.synchronizer.sendToTrackingPlayers(new ClientboundRotateHeadPacket(this.entity, $$25));
                this.lastSentYHeadRot = $$25;
            }
            this.entity.needsSync = false;
        }
        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.entity.hurtMarked = false;
            this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundSetEntityMotionPacket(this.entity));
        }
    }

    private void handleMinecartPosRot(NewMinecartBehavior $$0, byte $$1, byte $$2, boolean $$3) {
        this.sendDirtyEntityData();
        if ($$0.lerpSteps.isEmpty()) {
            boolean $$8;
            Vec3 $$4 = this.entity.getDeltaMovement();
            double $$5 = $$4.distanceToSqr(this.lastSentMovement);
            Vec3 $$6 = this.entity.trackingPosition();
            boolean $$7 = this.positionCodec.delta($$6).lengthSqr() >= 7.62939453125E-6;
            boolean bl = $$8 = $$7 || this.tickCount % 60 == 0;
            if ($$8 || $$3 || $$5 > 1.0E-7) {
                this.synchronizer.sendToTrackingPlayers(new ClientboundMoveMinecartPacket(this.entity.getId(), List.of(new NewMinecartBehavior.MinecartStep(this.entity.position(), this.entity.getDeltaMovement(), this.entity.getYRot(), this.entity.getXRot(), 1.0f))));
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
        $$0.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void addPairing(ServerPlayer $$0) {
        ArrayList<Packet<? super ClientGamePacketListener>> $$1 = new ArrayList<Packet<? super ClientGamePacketListener>>();
        this.sendPairingData($$0, $$1::add);
        $$0.connection.send(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)$$1));
        this.entity.startSeenByPlayer($$0);
    }

    public void sendPairingData(ServerPlayer $$0, Consumer<Packet<ClientGamePacketListener>> $$1) {
        Leashable $$9;
        LivingEntity $$3;
        Object $$4;
        Entity entity;
        this.entity.updateDataBeforeSync();
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<ClientGamePacketListener> $$2 = this.entity.getAddEntityPacket(this);
        $$1.accept($$2);
        if (this.trackedDataValues != null) {
            $$1.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
        }
        if ((entity = this.entity) instanceof LivingEntity && !($$4 = ($$3 = (LivingEntity)entity).getAttributes().getSyncableAttributes()).isEmpty()) {
            $$1.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), (Collection<AttributeInstance>)$$4));
        }
        if (($$4 = this.entity) instanceof LivingEntity) {
            LivingEntity $$5 = (LivingEntity)$$4;
            ArrayList $$6 = Lists.newArrayList();
            for (EquipmentSlot $$7 : EquipmentSlot.VALUES) {
                ItemStack $$8 = $$5.getItemBySlot($$7);
                if ($$8.isEmpty()) continue;
                $$6.add(Pair.of((Object)$$7, (Object)$$8.copy()));
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
        if ((entity = this.entity) instanceof Leashable && ($$9 = (Leashable)((Object)entity)).isLeashed()) {
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
        List<SynchedEntityData.DataValue<?>> $$1 = $$0.packDirty();
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

    public static interface Synchronizer {
        public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> var1);

        public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> var1);

        public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> var1, Predicate<ServerPlayer> var2);
    }
}

