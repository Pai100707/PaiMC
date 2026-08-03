package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public record ItemAttributeModifiers(List<ItemAttributeModifiers.Entry> modifiers) {
   public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of());
   public static final Codec<ItemAttributeModifiers> CODEC = ItemAttributeModifiers.Entry.CODEC
      .listOf()
      .xmap(ItemAttributeModifiers::new, ItemAttributeModifiers::modifiers);
   public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(
      ItemAttributeModifiers.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemAttributeModifiers::modifiers, ItemAttributeModifiers::new
   );
   public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

   public static ItemAttributeModifiers.Builder builder() {
      return new ItemAttributeModifiers.Builder();
   }

   public ItemAttributeModifiers withModifierAdded(Holder<Attribute> $$0, AttributeModifier $$1, EquipmentSlotGroup $$2) {
      com.google.common.collect.ImmutableList.Builder<ItemAttributeModifiers.Entry> $$3 = ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);

      for (ItemAttributeModifiers.Entry $$4 : this.modifiers) {
         if (!$$4.matches($$0, $$1.id())) {
            $$3.add($$4);
         }
      }

      $$3.add(new ItemAttributeModifiers.Entry($$0, $$1, $$2));
      return new ItemAttributeModifiers($$3.build());
   }

   public void forEach(EquipmentSlotGroup $$0, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> $$1) {
      for (ItemAttributeModifiers.Entry $$2 : this.modifiers) {
         if ($$2.slot.equals($$0)) {
            $$1.accept($$2.attribute, $$2.modifier, $$2.display);
         }
      }
   }

   public void forEach(EquipmentSlotGroup $$0, BiConsumer<Holder<Attribute>, AttributeModifier> $$1) {
      for (ItemAttributeModifiers.Entry $$2 : this.modifiers) {
         if ($$2.slot.equals($$0)) {
            $$1.accept($$2.attribute, $$2.modifier);
         }
      }
   }

   public void forEach(EquipmentSlot $$0, BiConsumer<Holder<Attribute>, AttributeModifier> $$1) {
      for (ItemAttributeModifiers.Entry $$2 : this.modifiers) {
         if ($$2.slot.test($$0)) {
            $$1.accept($$2.attribute, $$2.modifier);
         }
      }
   }

   public double compute(Holder<Attribute> $$0, double $$1, EquipmentSlot $$2) {
      double $$3 = $$1;

      for (ItemAttributeModifiers.Entry $$4 : this.modifiers) {
         if ($$4.slot.test($$2) && $$4.attribute == $$0) {
            double $$5 = $$4.modifier.amount();

            $$3 += switch ($$4.modifier.operation()) {
               case ADD_VALUE -> $$5;
               case ADD_MULTIPLIED_BASE -> $$5 * $$1;
               case ADD_MULTIPLIED_TOTAL -> $$5 * $$3;
               default -> throw new MatchException(null, null);
            };
         }
      }

      return $$3;
   }

   public static class Builder {
      private final com.google.common.collect.ImmutableList.Builder<ItemAttributeModifiers.Entry> entries = ImmutableList.builder();

      Builder() {
      }

      public ItemAttributeModifiers.Builder add(Holder<Attribute> $$0, AttributeModifier $$1, EquipmentSlotGroup $$2) {
         this.entries.add(new ItemAttributeModifiers.Entry($$0, $$1, $$2));
         return this;
      }

      public ItemAttributeModifiers.Builder add(Holder<Attribute> $$0, AttributeModifier $$1, EquipmentSlotGroup $$2, ItemAttributeModifiers.Display $$3) {
         this.entries.add(new ItemAttributeModifiers.Entry($$0, $$1, $$2, $$3));
         return this;
      }

      public ItemAttributeModifiers build() {
         return new ItemAttributeModifiers(this.entries.build());
      }
   }

   public interface Display {
      Codec<ItemAttributeModifiers.Display> CODEC = ItemAttributeModifiers.Display.Type.CODEC
         .dispatch("type", ItemAttributeModifiers.Display::type, $$0 -> $$0.codec);
      StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Display> STREAM_CODEC = ItemAttributeModifiers.Display.Type.STREAM_CODEC
         .cast()
         .dispatch(ItemAttributeModifiers.Display::type, ItemAttributeModifiers.Display.Type::streamCodec);

      static ItemAttributeModifiers.Display attributeModifiers() {
         return ItemAttributeModifiers.Display.Default.INSTANCE;
      }

      static ItemAttributeModifiers.Display hidden() {
         return ItemAttributeModifiers.Display.Hidden.INSTANCE;
      }

      static ItemAttributeModifiers.Display override(Component $$0) {
         return new ItemAttributeModifiers.Display.OverrideText($$0);
      }

      ItemAttributeModifiers.Display.Type type();

      void apply(Consumer<Component> var1, @Nullable Player var2, Holder<Attribute> var3, AttributeModifier var4);

      public record Default() implements ItemAttributeModifiers.Display {
         static final ItemAttributeModifiers.Display.Default INSTANCE = new ItemAttributeModifiers.Display.Default();
         static final MapCodec<ItemAttributeModifiers.Display.Default> CODEC = MapCodec.unit(INSTANCE);
         static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Display.Default> STREAM_CODEC = StreamCodec.unit(INSTANCE);

         @Override
         public ItemAttributeModifiers.Display.Type type() {
            return ItemAttributeModifiers.Display.Type.DEFAULT;
         }

         @Override
         public void apply(Consumer<Component> $$0, @Nullable Player $$1, Holder<Attribute> $$2, AttributeModifier $$3) {
            double $$4 = $$3.amount();
            boolean $$5 = false;
            if ($$1 != null) {
               if ($$3.is(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID)) {
                  $$4 += $$1.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                  $$5 = true;
               } else if ($$3.is(net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID)) {
                  $$4 += $$1.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                  $$5 = true;
               }
            }

            double $$6;
            if ($$3.operation() == Operation.ADD_MULTIPLIED_BASE || $$3.operation() == Operation.ADD_MULTIPLIED_TOTAL) {
               $$6 = $$4 * 100.0;
            } else if ($$2.is(Attributes.KNOCKBACK_RESISTANCE)) {
               $$6 = $$4 * 10.0;
            } else {
               $$6 = $$4;
            }

            if ($$5) {
               $$0.accept(
                  CommonComponents.space()
                     .append(
                        Component.translatable(
                           "attribute.modifier.equals." + $$3.operation().id(),
                           new Object[]{
                              ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format($$6), Component.translatable(((Attribute)$$2.value()).getDescriptionId())
                           }
                        )
                     )
                     .withStyle(ChatFormatting.DARK_GREEN)
               );
            } else if ($$4 > 0.0) {
               $$0.accept(
                  Component.translatable(
                        "attribute.modifier.plus." + $$3.operation().id(),
                        new Object[]{
                           ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format($$6), Component.translatable(((Attribute)$$2.value()).getDescriptionId())
                        }
                     )
                     .withStyle(((Attribute)$$2.value()).getStyle(true))
               );
            } else if ($$4 < 0.0) {
               $$0.accept(
                  Component.translatable(
                        "attribute.modifier.take." + $$3.operation().id(),
                        new Object[]{
                           ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-$$6), Component.translatable(((Attribute)$$2.value()).getDescriptionId())
                        }
                     )
                     .withStyle(((Attribute)$$2.value()).getStyle(false))
               );
            }
         }
      }

      public record Hidden() implements ItemAttributeModifiers.Display {
         static final ItemAttributeModifiers.Display.Hidden INSTANCE = new ItemAttributeModifiers.Display.Hidden();
         static final MapCodec<ItemAttributeModifiers.Display.Hidden> CODEC = MapCodec.unit(INSTANCE);
         static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Display.Hidden> STREAM_CODEC = StreamCodec.unit(INSTANCE);

         @Override
         public ItemAttributeModifiers.Display.Type type() {
            return ItemAttributeModifiers.Display.Type.HIDDEN;
         }

         @Override
         public void apply(Consumer<Component> $$0, @Nullable Player $$1, Holder<Attribute> $$2, AttributeModifier $$3) {
         }
      }

      public record OverrideText(Component component) implements ItemAttributeModifiers.Display {
         static final MapCodec<ItemAttributeModifiers.Display.OverrideText> CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(ComponentSerialization.CODEC.fieldOf("value").forGetter(ItemAttributeModifiers.Display.OverrideText::component))
               .apply($$0, ItemAttributeModifiers.Display.OverrideText::new)
         );
         static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Display.OverrideText> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, ItemAttributeModifiers.Display.OverrideText::component, ItemAttributeModifiers.Display.OverrideText::new
         );

         @Override
         public ItemAttributeModifiers.Display.Type type() {
            return ItemAttributeModifiers.Display.Type.OVERRIDE;
         }

         @Override
         public void apply(Consumer<Component> $$0, @Nullable Player $$1, Holder<Attribute> $$2, AttributeModifier $$3) {
            $$0.accept(this.component);
         }
      }

      public static enum Type implements StringRepresentable {
         DEFAULT("default", 0, ItemAttributeModifiers.Display.Default.CODEC, ItemAttributeModifiers.Display.Default.STREAM_CODEC),
         HIDDEN("hidden", 1, ItemAttributeModifiers.Display.Hidden.CODEC, ItemAttributeModifiers.Display.Hidden.STREAM_CODEC),
         OVERRIDE("override", 2, ItemAttributeModifiers.Display.OverrideText.CODEC, ItemAttributeModifiers.Display.OverrideText.STREAM_CODEC);

         static final Codec<ItemAttributeModifiers.Display.Type> CODEC = StringRepresentable.fromEnum(ItemAttributeModifiers.Display.Type::values);
         private static final IntFunction<ItemAttributeModifiers.Display.Type> BY_ID = ByIdMap.continuous(
            ItemAttributeModifiers.Display.Type::id, values(), OutOfBoundsStrategy.ZERO
         );
         static final StreamCodec<ByteBuf, ItemAttributeModifiers.Display.Type> STREAM_CODEC = ByteBufCodecs.idMapper(
            BY_ID, ItemAttributeModifiers.Display.Type::id
         );
         private final String name;
         private final int id;
         final MapCodec<? extends ItemAttributeModifiers.Display> codec;
         private final StreamCodec<RegistryFriendlyByteBuf, ? extends ItemAttributeModifiers.Display> streamCodec;

         private Type(
            final String $$0,
            final int $$1,
            final MapCodec<? extends ItemAttributeModifiers.Display> $$2,
            final StreamCodec<RegistryFriendlyByteBuf, ? extends ItemAttributeModifiers.Display> $$3
         ) {
            this.name = $$0;
            this.id = $$1;
            this.codec = $$2;
            this.streamCodec = $$3;
         }

         public String getSerializedName() {
            return this.name;
         }

         private int id() {
            return this.id;
         }

         private StreamCodec<RegistryFriendlyByteBuf, ? extends ItemAttributeModifiers.Display> streamCodec() {
            return this.streamCodec;
         }
      }
   }

   public record Entry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot, ItemAttributeModifiers.Display display) {
      public static final Codec<ItemAttributeModifiers.Entry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Attribute.CODEC.fieldOf("type").forGetter(ItemAttributeModifiers.Entry::attribute),
               AttributeModifier.MAP_CODEC.forGetter(ItemAttributeModifiers.Entry::modifier),
               EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(ItemAttributeModifiers.Entry::slot),
               ItemAttributeModifiers.Display.CODEC
                  .optionalFieldOf("display", ItemAttributeModifiers.Display.Default.INSTANCE)
                  .forGetter(ItemAttributeModifiers.Entry::display)
            )
            .apply($$0, ItemAttributeModifiers.Entry::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Entry> STREAM_CODEC = StreamCodec.composite(
         Attribute.STREAM_CODEC,
         ItemAttributeModifiers.Entry::attribute,
         AttributeModifier.STREAM_CODEC,
         ItemAttributeModifiers.Entry::modifier,
         EquipmentSlotGroup.STREAM_CODEC,
         ItemAttributeModifiers.Entry::slot,
         ItemAttributeModifiers.Display.STREAM_CODEC,
         ItemAttributeModifiers.Entry::display,
         ItemAttributeModifiers.Entry::new
      );

      public Entry(Holder<Attribute> $$0, AttributeModifier $$1, EquipmentSlotGroup $$2) {
         this($$0, $$1, $$2, ItemAttributeModifiers.Display.attributeModifiers());
      }

      public boolean matches(Holder<Attribute> $$0, Identifier $$1) {
         return $$0.equals(this.attribute) && this.modifier.is($$1);
      }
   }
}
