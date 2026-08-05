/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Marker
extends Entity {
    public Marker(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
        throw new IllegalStateException("Markers should never be sent");
    }

    @Override
    protected boolean canAddPassenger(Entity $$0) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity $$0) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        return false;
    }
}

