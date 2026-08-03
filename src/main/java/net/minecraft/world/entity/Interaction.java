package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Interaction extends net.minecraft.world.entity.Entity implements net.minecraft.world.entity.Attackable, net.minecraft.world.entity.Targeting {
   private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(
      net.minecraft.world.entity.Interaction.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(
      net.minecraft.world.entity.Interaction.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(
      net.minecraft.world.entity.Interaction.class, EntityDataSerializers.BOOLEAN
   );
   private static final String TAG_WIDTH = "width";
   private static final String TAG_HEIGHT = "height";
   private static final String TAG_ATTACK = "attack";
   private static final String TAG_INTERACTION = "interaction";
   private static final String TAG_RESPONSE = "response";
   private static final float DEFAULT_WIDTH = 1.0F;
   private static final float DEFAULT_HEIGHT = 1.0F;
   private static final boolean DEFAULT_RESPONSE = false;
   @Nullable
   private net.minecraft.world.entity.Interaction.PlayerAction attack;
   @Nullable
   private net.minecraft.world.entity.Interaction.PlayerAction interaction;

   public Interaction(net.minecraft.world.entity.EntityType<?> $$0, Level $$1) {
      super($$0, $$1);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_WIDTH_ID, 1.0F);
      $$0.define(DATA_HEIGHT_ID, 1.0F);
      $$0.define(DATA_RESPONSE_ID, false);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setWidth($$0.getFloatOr("width", 1.0F));
      this.setHeight($$0.getFloatOr("height", 1.0F));
      this.attack = (net.minecraft.world.entity.Interaction.PlayerAction)$$0.read("attack", net.minecraft.world.entity.Interaction.PlayerAction.CODEC)
         .orElse(null);
      this.interaction = (net.minecraft.world.entity.Interaction.PlayerAction)$$0.read("interaction", net.minecraft.world.entity.Interaction.PlayerAction.CODEC)
         .orElse(null);
      this.setResponse($$0.getBooleanOr("response", false));
      this.setBoundingBox(this.makeBoundingBox());
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.putFloat("width", this.getWidth());
      $$0.putFloat("height", this.getHeight());
      $$0.storeNullable("attack", net.minecraft.world.entity.Interaction.PlayerAction.CODEC, this.attack);
      $$0.storeNullable("interaction", net.minecraft.world.entity.Interaction.PlayerAction.CODEC, this.interaction);
      $$0.putBoolean("response", this.getResponse());
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
   public boolean skipAttackInteraction(net.minecraft.world.entity.Entity $$0) {
      if ($$0 instanceof Player $$1) {
         this.attack = new net.minecraft.world.entity.Interaction.PlayerAction($$1.getUUID(), this.level().getGameTime());
         if ($$1 instanceof ServerPlayer $$2) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger($$2, this, $$1.damageSources().generic(), 1.0F, 1.0F, false);
         }

         return !this.getResponse();
      } else {
         return false;
      }
   }

   @Override
   public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return false;
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      if (this.level().isClientSide()) {
         return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
      } else {
         this.interaction = new net.minecraft.world.entity.Interaction.PlayerAction($$0.getUUID(), this.level().getGameTime());
         return InteractionResult.CONSUME;
      }
   }

   @Override
   public void tick() {
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.LivingEntity getLastAttacker() {
      return this.attack != null ? this.level().getPlayerByUUID(this.attack.player()) : null;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.LivingEntity getTarget() {
      return this.interaction != null ? this.level().getPlayerByUUID(this.interaction.player()) : null;
   }

   private void setWidth(float $$0) {
      this.entityData.set(DATA_WIDTH_ID, $$0);
   }

   private float getWidth() {
      return (Float)this.entityData.get(DATA_WIDTH_ID);
   }

   private void setHeight(float $$0) {
      this.entityData.set(DATA_HEIGHT_ID, $$0);
   }

   private float getHeight() {
      return (Float)this.entityData.get(DATA_HEIGHT_ID);
   }

   private void setResponse(boolean $$0) {
      this.entityData.set(DATA_RESPONSE_ID, $$0);
   }

   private boolean getResponse() {
      return (Boolean)this.entityData.get(DATA_RESPONSE_ID);
   }

   private net.minecraft.world.entity.EntityDimensions getDimensions() {
      return net.minecraft.world.entity.EntityDimensions.scalable(this.getWidth(), this.getHeight());
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.getDimensions();
   }

   @Override
   protected AABB makeBoundingBox(Vec3 $$0) {
      return this.getDimensions().makeBoundingBox($$0);
   }

   record PlayerAction(UUID player, long timestamp) {
      public static final Codec<net.minecraft.world.entity.Interaction.PlayerAction> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               UUIDUtil.CODEC.fieldOf("player").forGetter(net.minecraft.world.entity.Interaction.PlayerAction::player),
               Codec.LONG.fieldOf("timestamp").forGetter(net.minecraft.world.entity.Interaction.PlayerAction::timestamp)
            )
            .apply($$0, net.minecraft.world.entity.Interaction.PlayerAction::new)
      );
   }
}
