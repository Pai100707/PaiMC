package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class AttributeInstance {
   private final Holder<Attribute> attribute;
   private final Map<AttributeModifier.Operation, Map<Identifier, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
   private final Map<Identifier, AttributeModifier> modifierById = new Object2ObjectArrayMap();
   private final Map<Identifier, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap();
   private double baseValue;
   private boolean dirty = true;
   private double cachedValue;
   private final Consumer<AttributeInstance> onDirty;

   public AttributeInstance(Holder<Attribute> $$0, Consumer<AttributeInstance> $$1) {
      this.attribute = $$0;
      this.onDirty = $$1;
      this.baseValue = ((Attribute)$$0.value()).getDefaultValue();
   }

   public Holder<Attribute> getAttribute() {
      return this.attribute;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double $$0) {
      if ($$0 != this.baseValue) {
         this.baseValue = $$0;
         this.setDirty();
      }
   }

   @VisibleForTesting
   Map<Identifier, AttributeModifier> getModifiers(AttributeModifier.Operation $$0) {
      return this.modifiersByOperation.computeIfAbsent($$0, $$0x -> new Object2ObjectOpenHashMap());
   }

   public Set<AttributeModifier> getModifiers() {
      return ImmutableSet.copyOf(this.modifierById.values());
   }

   public Set<AttributeModifier> getPermanentModifiers() {
      return ImmutableSet.copyOf(this.permanentModifiers.values());
   }

   @Nullable
   public AttributeModifier getModifier(Identifier $$0) {
      return this.modifierById.get($$0);
   }

   public boolean hasModifier(Identifier $$0) {
      return this.modifierById.get($$0) != null;
   }

   private void addModifier(AttributeModifier $$0) {
      AttributeModifier $$1 = this.modifierById.putIfAbsent($$0.id(), $$0);
      if ($$1 != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         this.getModifiers($$0.operation()).put($$0.id(), $$0);
         this.setDirty();
      }
   }

   public void addOrUpdateTransientModifier(AttributeModifier $$0) {
      AttributeModifier $$1 = this.modifierById.put($$0.id(), $$0);
      if ($$0 != $$1) {
         this.getModifiers($$0.operation()).put($$0.id(), $$0);
         this.setDirty();
      }
   }

   public void addTransientModifier(AttributeModifier $$0) {
      this.addModifier($$0);
   }

   public void addOrReplacePermanentModifier(AttributeModifier $$0) {
      this.removeModifier($$0.id());
      this.addModifier($$0);
      this.permanentModifiers.put($$0.id(), $$0);
   }

   public void addPermanentModifier(AttributeModifier $$0) {
      this.addModifier($$0);
      this.permanentModifiers.put($$0.id(), $$0);
   }

   public void addPermanentModifiers(Collection<AttributeModifier> $$0) {
      for (AttributeModifier $$1 : $$0) {
         this.addPermanentModifier($$1);
      }
   }

   protected void setDirty() {
      this.dirty = true;
      this.onDirty.accept(this);
   }

   public void removeModifier(AttributeModifier $$0) {
      this.removeModifier($$0.id());
   }

   public boolean removeModifier(Identifier $$0) {
      AttributeModifier $$1 = this.modifierById.remove($$0);
      if ($$1 == null) {
         return false;
      } else {
         this.getModifiers($$1.operation()).remove($$0);
         this.permanentModifiers.remove($$0);
         this.setDirty();
         return true;
      }
   }

   public void removeModifiers() {
      for (AttributeModifier $$0 : this.getModifiers()) {
         this.removeModifier($$0);
      }
   }

   public double getValue() {
      if (this.dirty) {
         this.cachedValue = this.calculateValue();
         this.dirty = false;
      }

      return this.cachedValue;
   }

   private double calculateValue() {
      double $$0 = this.getBaseValue();

      for (AttributeModifier $$1 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
         $$0 += $$1.amount();
      }

      double $$2 = $$0;

      for (AttributeModifier $$3 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
         $$2 += $$0 * $$3.amount();
      }

      for (AttributeModifier $$4 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
         $$2 *= 1.0 + $$4.amount();
      }

      return ((Attribute)this.attribute.value()).sanitizeValue($$2);
   }

   private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation $$0) {
      return this.modifiersByOperation.getOrDefault($$0, Map.of()).values();
   }

   public void replaceFrom(AttributeInstance $$0) {
      this.baseValue = $$0.baseValue;
      this.modifierById.clear();
      this.modifierById.putAll($$0.modifierById);
      this.permanentModifiers.clear();
      this.permanentModifiers.putAll($$0.permanentModifiers);
      this.modifiersByOperation.clear();
      $$0.modifiersByOperation.forEach(($$0x, $$1) -> this.getModifiers($$0x).putAll((Map<? extends Identifier, ? extends AttributeModifier>)$$1));
      this.setDirty();
   }

   public AttributeInstance.Packed pack() {
      return new AttributeInstance.Packed(this.attribute, this.baseValue, List.copyOf(this.permanentModifiers.values()));
   }

   public void apply(AttributeInstance.Packed $$0) {
      this.baseValue = $$0.baseValue;

      for (AttributeModifier $$1 : $$0.modifiers) {
         this.modifierById.put($$1.id(), $$1);
         this.getModifiers($$1.operation()).put($$1.id(), $$1);
         this.permanentModifiers.put($$1.id(), $$1);
      }

      this.setDirty();
   }

   public record Packed(Holder<Attribute> attribute, double baseValue, List<AttributeModifier> modifiers) {
      public static final Codec<AttributeInstance.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("id").forGetter(AttributeInstance.Packed::attribute),
               Codec.DOUBLE.fieldOf("base").orElse(0.0).forGetter(AttributeInstance.Packed::baseValue),
               AttributeModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(AttributeInstance.Packed::modifiers)
            )
            .apply($$0, AttributeInstance.Packed::new)
      );
      public static final Codec<List<AttributeInstance.Packed>> LIST_CODEC = CODEC.listOf();
   }
}
