package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class StateHolder<O, S> {
   public static final String NAME_TAG = "Name";
   public static final String PROPERTIES_TAG = "Properties";
   private static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Entry<Property<?>, Comparable<?>>, String>() {
      public String apply(Entry<Property<?>, Comparable<?>> $$0) {
         if ($$0 == null) {
            return "<NULL>";
         } else {
            Property<?> $$1 = $$0.getKey();
            return $$1.getName() + "=" + this.getName($$1, $$0.getValue());
         }
      }

      private <T extends Comparable<T>> String getName(Property<T> $$0, Comparable<?> $$1) {
         return $$0.getName((T)$$1);
      }
   };
   protected final O owner;
   private final Reference2ObjectArrayMap<Property<?>, Comparable<?>> values;
   private Map<Property<?>, S[]> neighbours;
   protected final MapCodec<S> propertiesCodec;

   protected StateHolder(O $$0, Reference2ObjectArrayMap<Property<?>, Comparable<?>> $$1, MapCodec<S> $$2) {
      this.owner = $$0;
      this.values = $$1;
      this.propertiesCodec = $$2;
   }

   public <T extends Comparable<T>> S cycle(Property<T> $$0) {
      return this.setValue($$0, findNextInCollection($$0.getPossibleValues(), this.getValue($$0)));
   }

   protected static <T> T findNextInCollection(List<T> $$0, T $$1) {
      int $$2 = $$0.indexOf($$1) + 1;
      return $$2 == $$0.size() ? $$0.getFirst() : $$0.get($$2);
   }

   @Override
   public String toString() {
      StringBuilder $$0 = new StringBuilder();
      $$0.append(this.owner);
      if (!this.getValues().isEmpty()) {
         $$0.append('[');
         $$0.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
         $$0.append(']');
      }

      return $$0.toString();
   }

   @Override
   public final boolean equals(Object $$0) {
      return super.equals($$0);
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   public Collection<Property<?>> getProperties() {
      return Collections.unmodifiableCollection(this.values.keySet());
   }

   public boolean hasProperty(Property<?> $$0) {
      return this.values.containsKey($$0);
   }

   public <T extends Comparable<T>> T getValue(Property<T> $$0) {
      Comparable<?> $$1 = (Comparable<?>)this.values.get($$0);
      if ($$1 == null) {
         throw new IllegalArgumentException("Cannot get property " + $$0 + " as it does not exist in " + this.owner);
      } else {
         return $$0.getValueClass().cast($$1);
      }
   }

   public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> $$0) {
      return Optional.ofNullable(this.getNullableValue($$0));
   }

   public <T extends Comparable<T>> T getValueOrElse(Property<T> $$0, T $$1) {
      return Objects.requireNonNullElse(this.getNullableValue($$0), $$1);
   }

   
   private <T extends Comparable<T>> T getNullableValue(Property<T> $$0) {
      Comparable<?> $$1 = (Comparable<?>)this.values.get($$0);
      return $$1 == null ? null : $$0.getValueClass().cast($$1);
   }

   public <T extends Comparable<T>, V extends T> S setValue(Property<T> $$0, V $$1) {
      Comparable<?> $$2 = (Comparable<?>)this.values.get($$0);
      if ($$2 == null) {
         throw new IllegalArgumentException("Cannot set property " + $$0 + " as it does not exist in " + this.owner);
      } else {
         return this.setValueInternal($$0, $$1, $$2);
      }
   }

   public <T extends Comparable<T>, V extends T> S trySetValue(Property<T> $$0, V $$1) {
      Comparable<?> $$2 = (Comparable<?>)this.values.get($$0);
      return (S)($$2 == null ? this : this.setValueInternal($$0, $$1, $$2));
   }

   private <T extends Comparable<T>, V extends T> S setValueInternal(Property<T> $$0, V $$1, Comparable<?> $$2) {
      if ($$2.equals($$1)) {
         return (S)this;
      } else {
         int $$3 = $$0.getInternalIndex((T)$$1);
         if ($$3 < 0) {
            throw new IllegalArgumentException("Cannot set property " + $$0 + " to " + $$1 + " on " + this.owner + ", it is not an allowed value");
         } else {
            return (S)this.neighbours.get($$0)[$$3];
         }
      }
   }

   public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> $$0) {
      if (this.neighbours != null) {
         throw new IllegalStateException();
      } else {
         Map<Property<?>, S[]> $$1 = new Reference2ObjectArrayMap(this.values.size());
         ObjectIterator var3 = this.values.entrySet().iterator();

         while (var3.hasNext()) {
            Entry<Property<?>, Comparable<?>> $$2 = (Entry<Property<?>, Comparable<?>>)var3.next();
            Property<?> $$3 = $$2.getKey();
            $$1.put($$3, $$3.getPossibleValues().stream().map($$2x -> $$0.get(this.makeNeighbourValues($$3, $$2x))).toArray());
         }

         this.neighbours = $$1;
      }
   }

   private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> $$0, Comparable<?> $$1) {
      Map<Property<?>, Comparable<?>> $$2 = new Reference2ObjectArrayMap(this.values);
      $$2.put($$0, $$1);
      return $$2;
   }

   public Map<Property<?>, Comparable<?>> getValues() {
      return this.values;
   }

   protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> $$0, Function<O, S> $$1) {
      return $$0.dispatch(
         "Name",
         $$0x -> $$0x.owner,
         $$1x -> {
            S $$2 = $$1.apply((O)$$1x);
            return $$2.getValues().isEmpty()
               ? MapCodec.unit($$2)
               : $$2.propertiesCodec.codec().lenientOptionalFieldOf("Properties").xmap($$1xx -> $$1xx.orElse($$2), Optional::of);
         }
      );
   }
}
