package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jspecify.annotations.Nullable;

public record EntityEquipmentPredicate(
   Optional<ItemPredicate> head,
   Optional<ItemPredicate> chest,
   Optional<ItemPredicate> legs,
   Optional<ItemPredicate> feet,
   Optional<ItemPredicate> body,
   Optional<ItemPredicate> mainhand,
   Optional<ItemPredicate> offhand
) {
   public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head),
            ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest),
            ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs),
            ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet),
            ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body),
            ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand),
            ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)
         )
         .apply($$0, EntityEquipmentPredicate::new)
   );

   public static EntityEquipmentPredicate captainPredicate(HolderGetter<Item> $$0, HolderGetter<BannerPattern> $$1) {
      return EntityEquipmentPredicate.Builder.equipment()
         .head(
            ItemPredicate.Builder.item()
               .of($$0, Items.WHITE_BANNER)
               .withComponents(
                  DataComponentMatchers.Builder.components()
                     .exact(
                        DataComponentExactPredicate.someOf(
                           Raid.getOminousBannerInstance($$1).getComponents(),
                           new DataComponentType[]{DataComponents.BANNER_PATTERNS, DataComponents.ITEM_NAME}
                        )
                     )
                     .build()
               )
         )
         .build();
   }

   public boolean matches(@Nullable Entity $$0) {
      if ($$0 instanceof LivingEntity $$1) {
         if (this.head.isPresent() && !this.head.get().test($$1.getItemBySlot(EquipmentSlot.HEAD))) {
            return false;
         } else if (this.chest.isPresent() && !this.chest.get().test($$1.getItemBySlot(EquipmentSlot.CHEST))) {
            return false;
         } else if (this.legs.isPresent() && !this.legs.get().test($$1.getItemBySlot(EquipmentSlot.LEGS))) {
            return false;
         } else if (this.feet.isPresent() && !this.feet.get().test($$1.getItemBySlot(EquipmentSlot.FEET))) {
            return false;
         } else if (this.body.isPresent() && !this.body.get().test($$1.getItemBySlot(EquipmentSlot.BODY))) {
            return false;
         } else {
            return this.mainhand.isPresent() && !this.mainhand.get().test($$1.getItemBySlot(EquipmentSlot.MAINHAND))
               ? false
               : !this.offhand.isPresent() || this.offhand.get().test($$1.getItemBySlot(EquipmentSlot.OFFHAND));
         }
      } else {
         return false;
      }
   }

   public static class Builder {
      private Optional<ItemPredicate> head = Optional.empty();
      private Optional<ItemPredicate> chest = Optional.empty();
      private Optional<ItemPredicate> legs = Optional.empty();
      private Optional<ItemPredicate> feet = Optional.empty();
      private Optional<ItemPredicate> body = Optional.empty();
      private Optional<ItemPredicate> mainhand = Optional.empty();
      private Optional<ItemPredicate> offhand = Optional.empty();

      public static EntityEquipmentPredicate.Builder equipment() {
         return new EntityEquipmentPredicate.Builder();
      }

      public EntityEquipmentPredicate.Builder head(ItemPredicate.Builder $$0) {
         this.head = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate.Builder chest(ItemPredicate.Builder $$0) {
         this.chest = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate.Builder legs(ItemPredicate.Builder $$0) {
         this.legs = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate.Builder feet(ItemPredicate.Builder $$0) {
         this.feet = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate.Builder body(ItemPredicate.Builder $$0) {
         this.body = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate.Builder mainhand(ItemPredicate.Builder $$0) {
         this.mainhand = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate.Builder offhand(ItemPredicate.Builder $$0) {
         this.offhand = Optional.of($$0.build());
         return this;
      }

      public EntityEquipmentPredicate build() {
         return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
      }
   }
}
