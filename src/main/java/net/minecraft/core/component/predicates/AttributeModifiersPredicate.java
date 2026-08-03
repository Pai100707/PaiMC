package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;

public record AttributeModifiersPredicate(Optional<CollectionPredicate<Entry, AttributeModifiersPredicate.EntryPredicate>> modifiers)
   implements SingleComponentItemPredicate<ItemAttributeModifiers> {
   public static final Codec<AttributeModifiersPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            CollectionPredicate.codec(AttributeModifiersPredicate.EntryPredicate.CODEC)
               .optionalFieldOf("modifiers")
               .forGetter(AttributeModifiersPredicate::modifiers)
         )
         .apply($$0, AttributeModifiersPredicate::new)
   );

   public DataComponentType<ItemAttributeModifiers> componentType() {
      return DataComponents.ATTRIBUTE_MODIFIERS;
   }

   public boolean matches(ItemAttributeModifiers $$0) {
      return !this.modifiers.isPresent() || this.modifiers.get().test($$0.modifiers());
   }

   public record EntryPredicate(
      Optional<net.minecraft.core.HolderSet<Attribute>> attribute,
      Optional<Identifier> id,
      Doubles amount,
      Optional<Operation> operation,
      Optional<EquipmentSlotGroup> slot
   ) implements Predicate<Entry> {
      public static final Codec<AttributeModifiersPredicate.EntryPredicate> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               net.minecraft.core.RegistryCodecs.homogeneousList(Registries.ATTRIBUTE)
                  .optionalFieldOf("attribute")
                  .forGetter(AttributeModifiersPredicate.EntryPredicate::attribute),
               Identifier.CODEC.optionalFieldOf("id").forGetter(AttributeModifiersPredicate.EntryPredicate::id),
               Doubles.CODEC.optionalFieldOf("amount", Doubles.ANY).forGetter(AttributeModifiersPredicate.EntryPredicate::amount),
               Operation.CODEC.optionalFieldOf("operation").forGetter(AttributeModifiersPredicate.EntryPredicate::operation),
               EquipmentSlotGroup.CODEC.optionalFieldOf("slot").forGetter(AttributeModifiersPredicate.EntryPredicate::slot)
            )
            .apply($$0, AttributeModifiersPredicate.EntryPredicate::new)
      );

      public boolean test(Entry $$0) {
         if (this.attribute.isPresent() && !this.attribute.get().contains($$0.attribute())) {
            return false;
         } else if (this.id.isPresent() && !this.id.get().equals($$0.modifier().id())) {
            return false;
         } else if (!this.amount.matches($$0.modifier().amount())) {
            return false;
         } else {
            return this.operation.isPresent() && this.operation.get() != $$0.modifier().operation()
               ? false
               : !this.slot.isPresent() || this.slot.get() == $$0.slot();
         }
      }
   }
}
