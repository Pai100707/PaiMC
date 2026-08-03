package net.minecraft.world.item.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record Equippable(
   EquipmentSlot slot,
   Holder<SoundEvent> equipSound,
   Optional<ResourceKey<EquipmentAsset>> assetId,
   Optional<Identifier> cameraOverlay,
   Optional<HolderSet<EntityType<?>>> allowedEntities,
   boolean dispensable,
   boolean swappable,
   boolean damageOnHurt,
   boolean equipOnInteract,
   boolean canBeSheared,
   Holder<SoundEvent> shearingSound
) {
   public static final Codec<Equippable> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            EquipmentSlot.CODEC.fieldOf("slot").forGetter(Equippable::slot),
            SoundEvent.CODEC.optionalFieldOf("equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC).forGetter(Equippable::equipSound),
            ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("asset_id").forGetter(Equippable::assetId),
            Identifier.CODEC.optionalFieldOf("camera_overlay").forGetter(Equippable::cameraOverlay),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("allowed_entities").forGetter(Equippable::allowedEntities),
            Codec.BOOL.optionalFieldOf("dispensable", true).forGetter(Equippable::dispensable),
            Codec.BOOL.optionalFieldOf("swappable", true).forGetter(Equippable::swappable),
            Codec.BOOL.optionalFieldOf("damage_on_hurt", true).forGetter(Equippable::damageOnHurt),
            Codec.BOOL.optionalFieldOf("equip_on_interact", false).forGetter(Equippable::equipOnInteract),
            Codec.BOOL.optionalFieldOf("can_be_sheared", false).forGetter(Equippable::canBeSheared),
            SoundEvent.CODEC
               .optionalFieldOf("shearing_sound", BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP))
               .forGetter(Equippable::shearingSound)
         )
         .apply($$0, Equippable::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, Equippable> STREAM_CODEC = StreamCodec.composite(
      EquipmentSlot.STREAM_CODEC,
      Equippable::slot,
      SoundEvent.STREAM_CODEC,
      Equippable::equipSound,
      ResourceKey.streamCodec(EquipmentAssets.ROOT_ID).apply(ByteBufCodecs::optional),
      Equippable::assetId,
      Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional),
      Equippable::cameraOverlay,
      ByteBufCodecs.holderSet(Registries.ENTITY_TYPE).apply(ByteBufCodecs::optional),
      Equippable::allowedEntities,
      ByteBufCodecs.BOOL,
      Equippable::dispensable,
      ByteBufCodecs.BOOL,
      Equippable::swappable,
      ByteBufCodecs.BOOL,
      Equippable::damageOnHurt,
      ByteBufCodecs.BOOL,
      Equippable::equipOnInteract,
      ByteBufCodecs.BOOL,
      Equippable::canBeSheared,
      SoundEvent.STREAM_CODEC,
      Equippable::shearingSound,
      Equippable::new
   );

   public static Equippable llamaSwag(net.minecraft.world.item.DyeColor $$0) {
      return builder(EquipmentSlot.BODY)
         .setEquipSound(SoundEvents.LLAMA_SWAG)
         .setAsset(EquipmentAssets.CARPETS.get($$0))
         .setAllowedEntities(EntityType.LLAMA, EntityType.TRADER_LLAMA)
         .setCanBeSheared(true)
         .setShearingSound(SoundEvents.LLAMA_CARPET_UNEQUIP)
         .build();
   }

   public static Equippable saddle() {
      HolderGetter<EntityType<?>> $$0 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
      return builder(EquipmentSlot.SADDLE)
         .setEquipSound(SoundEvents.HORSE_SADDLE)
         .setAsset(EquipmentAssets.SADDLE)
         .setAllowedEntities($$0.getOrThrow(EntityTypeTags.CAN_EQUIP_SADDLE))
         .setEquipOnInteract(true)
         .setCanBeSheared(true)
         .setShearingSound(SoundEvents.SADDLE_UNEQUIP)
         .build();
   }

   public static Equippable harness(net.minecraft.world.item.DyeColor $$0) {
      HolderGetter<EntityType<?>> $$1 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
      return builder(EquipmentSlot.BODY)
         .setEquipSound(SoundEvents.HARNESS_EQUIP)
         .setAsset(EquipmentAssets.HARNESSES.get($$0))
         .setAllowedEntities($$1.getOrThrow(EntityTypeTags.CAN_EQUIP_HARNESS))
         .setEquipOnInteract(true)
         .setCanBeSheared(true)
         .setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.HARNESS_UNEQUIP))
         .build();
   }

   public static Equippable.Builder builder(EquipmentSlot $$0) {
      return new Equippable.Builder($$0);
   }

   public InteractionResult swapWithEquipmentSlot(net.minecraft.world.item.ItemStack $$0, Player $$1) {
      if ($$1.canUseSlot(this.slot) && this.canBeEquippedBy($$1.getType())) {
         net.minecraft.world.item.ItemStack $$2 = $$1.getItemBySlot(this.slot);
         if ((!EnchantmentHelper.has($$2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || $$1.isCreative())
            && !net.minecraft.world.item.ItemStack.isSameItemSameComponents($$0, $$2)) {
            if (!$$1.level().isClientSide()) {
               $$1.awardStat(Stats.ITEM_USED.get($$0.getItem()));
            }

            if ($$0.getCount() <= 1) {
               net.minecraft.world.item.ItemStack $$3 = $$2.isEmpty() ? $$0 : $$2.copyAndClear();
               net.minecraft.world.item.ItemStack $$4 = $$1.isCreative() ? $$0.copy() : $$0.copyAndClear();
               $$1.setItemSlot(this.slot, $$4);
               return InteractionResult.SUCCESS.heldItemTransformedTo($$3);
            } else {
               net.minecraft.world.item.ItemStack $$5 = $$2.copyAndClear();
               net.minecraft.world.item.ItemStack $$6 = $$0.consumeAndReturn(1, $$1);
               $$1.setItemSlot(this.slot, $$6);
               if (!$$1.getInventory().add($$5)) {
                  $$1.drop($$5, false);
               }

               return InteractionResult.SUCCESS.heldItemTransformedTo($$0);
            }
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult equipOnTarget(Player $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2) {
      if ($$1.isEquippableInSlot($$2, this.slot) && !$$1.hasItemInSlot(this.slot) && $$1.isAlive()) {
         if (!$$0.level().isClientSide()) {
            $$1.setItemSlot(this.slot, $$2.split(1));
            if ($$1 instanceof Mob $$3) {
               $$3.setGuaranteedDrop(this.slot);
            }
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean canBeEquippedBy(EntityType<?> $$0) {
      return this.allowedEntities.isEmpty() || this.allowedEntities.get().contains($$0.builtInRegistryHolder());
   }

   public static class Builder {
      private final EquipmentSlot slot;
      private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_GENERIC;
      private Optional<ResourceKey<EquipmentAsset>> assetId = Optional.empty();
      private Optional<Identifier> cameraOverlay = Optional.empty();
      private Optional<HolderSet<EntityType<?>>> allowedEntities = Optional.empty();
      private boolean dispensable = true;
      private boolean swappable = true;
      private boolean damageOnHurt = true;
      private boolean equipOnInteract;
      private boolean canBeSheared;
      private Holder<SoundEvent> shearingSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP);

      Builder(EquipmentSlot $$0) {
         this.slot = $$0;
      }

      public Equippable.Builder setEquipSound(Holder<SoundEvent> $$0) {
         this.equipSound = $$0;
         return this;
      }

      public Equippable.Builder setAsset(ResourceKey<EquipmentAsset> $$0) {
         this.assetId = Optional.of($$0);
         return this;
      }

      public Equippable.Builder setCameraOverlay(Identifier $$0) {
         this.cameraOverlay = Optional.of($$0);
         return this;
      }

      public Equippable.Builder setAllowedEntities(EntityType<?>... $$0) {
         return this.setAllowedEntities(HolderSet.direct(EntityType::builtInRegistryHolder, $$0));
      }

      public Equippable.Builder setAllowedEntities(HolderSet<EntityType<?>> $$0) {
         this.allowedEntities = Optional.of($$0);
         return this;
      }

      public Equippable.Builder setDispensable(boolean $$0) {
         this.dispensable = $$0;
         return this;
      }

      public Equippable.Builder setSwappable(boolean $$0) {
         this.swappable = $$0;
         return this;
      }

      public Equippable.Builder setDamageOnHurt(boolean $$0) {
         this.damageOnHurt = $$0;
         return this;
      }

      public Equippable.Builder setEquipOnInteract(boolean $$0) {
         this.equipOnInteract = $$0;
         return this;
      }

      public Equippable.Builder setCanBeSheared(boolean $$0) {
         this.canBeSheared = $$0;
         return this;
      }

      public Equippable.Builder setShearingSound(Holder<SoundEvent> $$0) {
         this.shearingSound = $$0;
         return this;
      }

      public Equippable build() {
         return new Equippable(
            this.slot,
            this.equipSound,
            this.assetId,
            this.cameraOverlay,
            this.allowedEntities,
            this.dispensable,
            this.swappable,
            this.damageOnHurt,
            this.equipOnInteract,
            this.canBeSheared,
            this.shearingSound
         );
      }
   }
}
