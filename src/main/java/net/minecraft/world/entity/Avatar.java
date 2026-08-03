package net.minecraft.world.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Avatar extends net.minecraft.world.entity.LivingEntity {
   public static final net.minecraft.world.entity.HumanoidArm DEFAULT_MAIN_HAND = net.minecraft.world.entity.HumanoidArm.RIGHT;
   public static final int DEFAULT_MODEL_CUSTOMIZATION = 0;
   public static final float DEFAULT_EYE_HEIGHT = 1.62F;
   public static final Vec3 DEFAULT_VEHICLE_ATTACHMENT = new Vec3(0.0, 0.6, 0.0);
   private static final float CROUCH_BB_HEIGHT = 1.5F;
   private static final float SWIMMING_BB_WIDTH = 0.6F;
   public static final float SWIMMING_BB_HEIGHT = 0.6F;
   protected static final net.minecraft.world.entity.EntityDimensions STANDING_DIMENSIONS = net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 1.8F)
      .withEyeHeight(1.62F)
      .withAttachments(
         net.minecraft.world.entity.EntityAttachments.builder().attach(net.minecraft.world.entity.EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT)
      );
   protected static final Map<net.minecraft.world.entity.Pose, net.minecraft.world.entity.EntityDimensions> POSES = ImmutableMap.builder()
      .put(net.minecraft.world.entity.Pose.STANDING, STANDING_DIMENSIONS)
      .put(net.minecraft.world.entity.Pose.SLEEPING, SLEEPING_DIMENSIONS)
      .put(net.minecraft.world.entity.Pose.FALL_FLYING, net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
      .put(net.minecraft.world.entity.Pose.SWIMMING, net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
      .put(net.minecraft.world.entity.Pose.SPIN_ATTACK, net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
      .put(
         net.minecraft.world.entity.Pose.CROUCHING,
         net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 1.5F)
            .withEyeHeight(1.27F)
            .withAttachments(
               net.minecraft.world.entity.EntityAttachments.builder().attach(net.minecraft.world.entity.EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT)
            )
      )
      .put(net.minecraft.world.entity.Pose.DYING, net.minecraft.world.entity.EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(1.62F))
      .build();
   protected static final EntityDataAccessor<net.minecraft.world.entity.HumanoidArm> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(
      net.minecraft.world.entity.Avatar.class, EntityDataSerializers.HUMANOID_ARM
   );
   protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(
      net.minecraft.world.entity.Avatar.class, EntityDataSerializers.BYTE
   );

   protected Avatar(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_PLAYER_MAIN_HAND, DEFAULT_MAIN_HAND);
      $$0.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
   }

   @Override
   public net.minecraft.world.entity.HumanoidArm getMainArm() {
      return (net.minecraft.world.entity.HumanoidArm)this.entityData.get(DATA_PLAYER_MAIN_HAND);
   }

   public void setMainArm(net.minecraft.world.entity.HumanoidArm $$0) {
      this.entityData.set(DATA_PLAYER_MAIN_HAND, $$0);
   }

   public boolean isModelPartShown(PlayerModelPart $$0) {
      return ((Byte)this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & $$0.getMask()) == $$0.getMask();
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return POSES.getOrDefault($$0, STANDING_DIMENSIONS);
   }
}
