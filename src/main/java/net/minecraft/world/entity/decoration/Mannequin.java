package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.ResolvableProfile.Static;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Mannequin extends net.minecraft.world.entity.Avatar {
   protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(
      Mannequin.class, EntityDataSerializers.RESOLVABLE_PROFILE
   );
   private static final EntityDataAccessor<Boolean> DATA_IMMOVABLE = SynchedEntityData.defineId(Mannequin.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<Component>> DATA_DESCRIPTION = SynchedEntityData.defineId(
      Mannequin.class, EntityDataSerializers.OPTIONAL_COMPONENT
   );
   private static final byte ALL_LAYERS = (byte)Arrays.stream(PlayerModelPart.values()).mapToInt(PlayerModelPart::getMask).reduce(0, ($$0, $$1) -> $$0 | $$1);
   private static final Set<net.minecraft.world.entity.Pose> VALID_POSES = Set.of(
      net.minecraft.world.entity.Pose.STANDING,
      net.minecraft.world.entity.Pose.CROUCHING,
      net.minecraft.world.entity.Pose.SWIMMING,
      net.minecraft.world.entity.Pose.FALL_FLYING,
      net.minecraft.world.entity.Pose.SLEEPING
   );
   public static final Codec<net.minecraft.world.entity.Pose> POSE_CODEC = net.minecraft.world.entity.Pose.CODEC
      .validate($$0 -> VALID_POSES.contains($$0) ? DataResult.success($$0) : DataResult.error(() -> "Invalid pose: " + $$0.getSerializedName()));
   private static final Codec<Byte> LAYERS_CODEC = PlayerModelPart.CODEC
      .listOf()
      .xmap(
         $$0 -> (byte)$$0.stream().mapToInt(PlayerModelPart::getMask).reduce(ALL_LAYERS, ($$0x, $$1) -> $$0x & ~$$1),
         $$0 -> Arrays.stream(PlayerModelPart.values()).filter($$1 -> ($$0 & $$1.getMask()) == 0).toList()
      );
   public static final ResolvableProfile DEFAULT_PROFILE = Static.EMPTY;
   private static final Component DEFAULT_DESCRIPTION = Component.translatable("entity.minecraft.mannequin.label");
   protected static net.minecraft.world.entity.EntityType.EntityFactory<Mannequin> constructor = Mannequin::new;
   private static final String PROFILE_FIELD = "profile";
   private static final String HIDDEN_LAYERS_FIELD = "hidden_layers";
   private static final String MAIN_HAND_FIELD = "main_hand";
   private static final String POSE_FIELD = "pose";
   private static final String IMMOVABLE_FIELD = "immovable";
   private static final String DESCRIPTION_FIELD = "description";
   private static final String HIDE_DESCRIPTION_FIELD = "hide_description";
   private Component description = DEFAULT_DESCRIPTION;
   private boolean hideDescription = false;

   public Mannequin(net.minecraft.world.entity.EntityType<Mannequin> $$0, Level $$1) {
      super($$0, $$1);
      this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, ALL_LAYERS);
   }

   protected Mannequin(Level $$0) {
      this(net.minecraft.world.entity.EntityType.MANNEQUIN, $$0);
   }

   @Nullable
   public static Mannequin create(net.minecraft.world.entity.EntityType<Mannequin> $$0, Level $$1) {
      return constructor.create($$0, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_PROFILE, DEFAULT_PROFILE);
      $$0.define(DATA_IMMOVABLE, false);
      $$0.define(DATA_DESCRIPTION, Optional.of(DEFAULT_DESCRIPTION));
   }

   protected ResolvableProfile getProfile() {
      return (ResolvableProfile)this.entityData.get(DATA_PROFILE);
   }

   private void setProfile(ResolvableProfile $$0) {
      this.entityData.set(DATA_PROFILE, $$0);
   }

   private boolean getImmovable() {
      return (Boolean)this.entityData.get(DATA_IMMOVABLE);
   }

   private void setImmovable(boolean $$0) {
      this.entityData.set(DATA_IMMOVABLE, $$0);
   }

   @Nullable
   protected Component getDescription() {
      return (Component)((Optional)this.entityData.get(DATA_DESCRIPTION)).orElse(null);
   }

   private void setDescription(Component $$0) {
      this.description = $$0;
      this.updateDescription();
   }

   private void setHideDescription(boolean $$0) {
      this.hideDescription = $$0;
      this.updateDescription();
   }

   private void updateDescription() {
      this.entityData.set(DATA_DESCRIPTION, this.hideDescription ? Optional.empty() : Optional.of(this.description));
   }

   @Override
   protected boolean isImmobile() {
      return this.getImmovable() || super.isImmobile();
   }

   @Override
   public boolean isEffectiveAi() {
      return !this.getImmovable() && super.isEffectiveAi();
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("profile", ResolvableProfile.CODEC, this.getProfile());
      $$0.store("hidden_layers", LAYERS_CODEC, (Byte)this.entityData.get(DATA_PLAYER_MODE_CUSTOMISATION));
      $$0.store("main_hand", net.minecraft.world.entity.HumanoidArm.CODEC, this.getMainArm());
      $$0.store("pose", POSE_CODEC, this.getPose());
      $$0.putBoolean("immovable", this.getImmovable());
      Component $$1 = this.getDescription();
      if ($$1 != null) {
         if (!$$1.equals(DEFAULT_DESCRIPTION)) {
            $$0.store("description", ComponentSerialization.CODEC, $$1);
         }
      } else {
         $$0.putBoolean("hide_description", true);
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      $$0.read("profile", ResolvableProfile.CODEC).ifPresent(this::setProfile);
      this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, $$0.read("hidden_layers", LAYERS_CODEC).orElse(ALL_LAYERS));
      this.setMainArm($$0.read("main_hand", net.minecraft.world.entity.HumanoidArm.CODEC).orElse(DEFAULT_MAIN_HAND));
      this.setPose($$0.read("pose", POSE_CODEC).orElse(net.minecraft.world.entity.Pose.STANDING));
      this.setImmovable($$0.getBooleanOr("immovable", false));
      this.setHideDescription($$0.getBooleanOr("hide_description", false));
      this.setDescription($$0.read("description", ComponentSerialization.CODEC).orElse(DEFAULT_DESCRIPTION));
   }

   @Nullable
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.PROFILE ? castComponentValue((DataComponentType<T>)$$0, this.getProfile()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.PROFILE);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.PROFILE) {
         this.setProfile(castComponentValue(DataComponents.PROFILE, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }
}
