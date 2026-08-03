package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetAttributesFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetAttributesFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               SetAttributesFunction.Modifier.CODEC.listOf().fieldOf("modifiers").forGetter($$0x -> $$0x.modifiers),
               Codec.BOOL.optionalFieldOf("replace", true).forGetter($$0x -> $$0x.replace)
            )
         )
         .apply($$0, SetAttributesFunction::new)
   );
   private final List<SetAttributesFunction.Modifier> modifiers;
   private final boolean replace;

   SetAttributesFunction(List<LootItemCondition> $$0, List<SetAttributesFunction.Modifier> $$1, boolean $$2) {
      super($$0);
      this.modifiers = List.copyOf($$1);
      this.replace = $$2;
   }

   @Override
   public LootItemFunctionType<SetAttributesFunction> getType() {
      return LootItemFunctions.SET_ATTRIBUTES;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.modifiers.stream().flatMap($$0 -> $$0.amount.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if (this.replace) {
         $$0.set(DataComponents.ATTRIBUTE_MODIFIERS, this.updateModifiers($$1, ItemAttributeModifiers.EMPTY));
      } else {
         $$0.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, $$1x -> this.updateModifiers($$1, $$1x));
      }

      return $$0;
   }

   private ItemAttributeModifiers updateModifiers(LootContext $$0, ItemAttributeModifiers $$1) {
      RandomSource $$2 = $$0.getRandom();

      for (SetAttributesFunction.Modifier $$3 : this.modifiers) {
         EquipmentSlotGroup $$4 = (EquipmentSlotGroup)Util.getRandom($$3.slots, $$2);
         $$1 = $$1.withModifierAdded($$3.attribute, new AttributeModifier($$3.id, $$3.amount.getFloat($$0), $$3.operation), $$4);
      }

      return $$1;
   }

   public static SetAttributesFunction.ModifierBuilder modifier(Identifier $$0, Holder<Attribute> $$1, Operation $$2, NumberProvider $$3) {
      return new SetAttributesFunction.ModifierBuilder($$0, $$1, $$2, $$3);
   }

   public static SetAttributesFunction.Builder setAttributes() {
      return new SetAttributesFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
      private final boolean replace;
      private final List<SetAttributesFunction.Modifier> modifiers = Lists.newArrayList();

      public Builder(boolean $$0) {
         this.replace = $$0;
      }

      public Builder() {
         this(false);
      }

      protected SetAttributesFunction.Builder getThis() {
         return this;
      }

      public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder $$0) {
         this.modifiers.add($$0.build());
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new SetAttributesFunction(this.getConditions(), this.modifiers, this.replace);
      }
   }

   record Modifier(Identifier id, Holder<Attribute> attribute, Operation operation, NumberProvider amount, List<EquipmentSlotGroup> slots) {
      private static final Codec<List<EquipmentSlotGroup>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(EquipmentSlotGroup.CODEC));
      public static final Codec<SetAttributesFunction.Modifier> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Identifier.CODEC.fieldOf("id").forGetter(SetAttributesFunction.Modifier::id),
               Attribute.CODEC.fieldOf("attribute").forGetter(SetAttributesFunction.Modifier::attribute),
               Operation.CODEC.fieldOf("operation").forGetter(SetAttributesFunction.Modifier::operation),
               NumberProviders.CODEC.fieldOf("amount").forGetter(SetAttributesFunction.Modifier::amount),
               SLOTS_CODEC.fieldOf("slot").forGetter(SetAttributesFunction.Modifier::slots)
            )
            .apply($$0, SetAttributesFunction.Modifier::new)
      );
   }

   public static class ModifierBuilder {
      private final Identifier id;
      private final Holder<Attribute> attribute;
      private final Operation operation;
      private final NumberProvider amount;
      private final Set<EquipmentSlotGroup> slots = EnumSet.noneOf(EquipmentSlotGroup.class);

      public ModifierBuilder(Identifier $$0, Holder<Attribute> $$1, Operation $$2, NumberProvider $$3) {
         this.id = $$0;
         this.attribute = $$1;
         this.operation = $$2;
         this.amount = $$3;
      }

      public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlotGroup $$0) {
         this.slots.add($$0);
         return this;
      }

      public SetAttributesFunction.Modifier build() {
         return new SetAttributesFunction.Modifier(this.id, this.attribute, this.operation, this.amount, List.copyOf(this.slots));
      }
   }
}
