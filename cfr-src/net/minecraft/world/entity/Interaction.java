/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Interaction
extends Entity
implements Attackable,
Targeting {
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.BOOLEAN);
    private static final String TAG_WIDTH = "width";
    private static final String TAG_HEIGHT = "height";
    private static final String TAG_ATTACK = "attack";
    private static final String TAG_INTERACTION = "interaction";
    private static final String TAG_RESPONSE = "response";
    private static final float DEFAULT_WIDTH = 1.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;
    private static final boolean DEFAULT_RESPONSE = false;
    private @Nullable PlayerAction attack;
    private @Nullable PlayerAction interaction;

    public Interaction(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_WIDTH_ID, Float.valueOf(1.0f));
        $$0.define(DATA_HEIGHT_ID, Float.valueOf(1.0f));
        $$0.define(DATA_RESPONSE_ID, false);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setWidth($$0.getFloatOr(TAG_WIDTH, 1.0f));
        this.setHeight($$0.getFloatOr(TAG_HEIGHT, 1.0f));
        this.attack = $$0.read(TAG_ATTACK, PlayerAction.CODEC).orElse(null);
        this.interaction = $$0.read(TAG_INTERACTION, PlayerAction.CODEC).orElse(null);
        this.setResponse($$0.getBooleanOr(TAG_RESPONSE, false));
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.putFloat(TAG_WIDTH, this.getWidth());
        $$0.putFloat(TAG_HEIGHT, this.getHeight());
        $$0.storeNullable(TAG_ATTACK, PlayerAction.CODEC, this.attack);
        $$0.storeNullable(TAG_INTERACTION, PlayerAction.CODEC, this.interaction);
        $$0.putBoolean(TAG_RESPONSE, this.getResponse());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (DATA_HEIGHT_ID.equals($$0) || DATA_WIDTH_ID.equals($$0)) {
            this.refreshDimensions();
        }
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
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
    public boolean skipAttackInteraction(Entity $$0) {
        if ($$0 instanceof Player) {
            Player $$1 = (Player)$$0;
            this.attack = new PlayerAction($$1.getUUID(), this.level().getGameTime());
            if ($$1 instanceof ServerPlayer) {
                ServerPlayer $$2 = (ServerPlayer)$$1;
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger($$2, this, $$1.damageSources().generic(), 1.0f, 1.0f, false);
            }
            return !this.getResponse();
        }
        return false;
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        return false;
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        if (this.level().isClientSide()) {
            return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        this.interaction = new PlayerAction($$0.getUUID(), this.level().getGameTime());
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
    }

    @Override
    public @Nullable LivingEntity getLastAttacker() {
        if (this.attack != null) {
            return this.level().getPlayerByUUID(this.attack.player());
        }
        return null;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        if (this.interaction != null) {
            return this.level().getPlayerByUUID(this.interaction.player());
        }
        return null;
    }

    private void setWidth(float $$0) {
        this.entityData.set(DATA_WIDTH_ID, Float.valueOf($$0));
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID).floatValue();
    }

    private void setHeight(float $$0) {
        this.entityData.set(DATA_HEIGHT_ID, Float.valueOf($$0));
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID).floatValue();
    }

    private void setResponse(boolean $$0) {
        this.entityData.set(DATA_RESPONSE_ID, $$0);
    }

    private boolean getResponse() {
        return this.entityData.get(DATA_RESPONSE_ID);
    }

    private EntityDimensions getDimensions() {
        return EntityDimensions.scalable(this.getWidth(), this.getHeight());
    }

    @Override
    public EntityDimensions getDimensions(Pose $$0) {
        return this.getDimensions();
    }

    @Override
    protected AABB makeBoundingBox(Vec3 $$0) {
        return this.getDimensions().makeBoundingBox($$0);
    }

    record PlayerAction(UUID player, long timestamp) {
        public static final Codec<PlayerAction> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)UUIDUtil.CODEC.fieldOf("player").forGetter(PlayerAction::player), (App)Codec.LONG.fieldOf("timestamp").forGetter(PlayerAction::timestamp)).apply((Applicative)$$0, PlayerAction::new));
    }
}

