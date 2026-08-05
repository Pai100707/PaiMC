package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<O, S>> {
   static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final O owner;
   private final ImmutableSortedMap<String, Property<?>> propertiesByName;
   private final ImmutableList<S> states;

   protected StateDefinition(Function<O, S> $$0, O $$1, StateDefinition.Factory<O, S> $$2, Map<String, Property<?>> $$3) {
      this.owner = $$1;
      this.propertiesByName = ImmutableSortedMap.copyOf($$3);
      Supplier<S> $$4 = () -> $$0.apply($$1);
      MapCodec<S> $$5 = MapCodec.of(Encoder.empty(), Decoder.unit($$4));
      UnmodifiableIterator $$7 = this.propertiesByName.entrySet().iterator();

      while ($$7.hasNext()) {
         Entry<String, Property<?>> $$6 = (Entry<String, Property<?>>)$$7.next();
         $$5 = appendPropertyCodec($$5, $$4, $$6.getKey(), $$6.getValue());
      }

      MapCodec<S> $$7x = $$5;
      Map<Map<Property<?>, Comparable<?>>, S> $$8 = Maps.newLinkedHashMap();
      List<S> $$9 = Lists.newArrayList();
      Stream<List<Pair<Property<?>, Comparable<?>>>> $$10 = Stream.of(Collections.emptyList());
      UnmodifiableIterator var11 = this.propertiesByName.values().iterator();

      while (var11.hasNext()) {
         Property<?> $$11 = (Property<?>)var11.next();
         $$10 = $$10.flatMap($$1x -> $$11.getPossibleValues().stream().map($$2x -> {
            List<Pair<Property<?>, Comparable<?>>> $$3x = Lists.newArrayList($$1x);
            $$3x.add(Pair.of($$11, $$2x));
            return $$3x;
         }));
      }

      $$10.forEach($$5x -> {
         Reference2ObjectArrayMap<Property<?>, Comparable<?>> $$6 = new Reference2ObjectArrayMap($$5x.size());

         for (Pair<Property<?>, Comparable<?>> $$7xx : $$5x) {
            $$6.put((Property)$$7xx.getFirst(), (Comparable)$$7xx.getSecond());
         }

         S $$8x = $$2.create($$1, $$6, $$7);
         $$8.put($$6, $$8x);
         $$9.add($$8x);
      });

      for (S $$12 : $$9) {
         $$12.populateNeighbours($$8);
      }

      this.states = ImmutableList.copyOf($$9);
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(
      MapCodec<S> $$0, Supplier<S> $$1, String $$2, Property<T> $$3
   ) {
      return Codec.mapPair($$0, $$3.valueCodec().fieldOf($$2).orElseGet($$0x -> {}, () -> $$3.value($$1.get())))
         .xmap(
            $$1x -> (StateHolder)((StateHolder)$$1x.getFirst()).setValue($$3, ((Property.Value)$$1x.getSecond()).value()),
            $$1x -> Pair.of($$1x, $$3.value($$1x))
         );
   }

   public ImmutableList<S> getPossibleStates() {
      return this.states;
   }

   public S any() {
      return (S)this.states.get(0);
   }

   public O getOwner() {
      return this.owner;
   }

   public Collection<Property<?>> getProperties() {
      return this.propertiesByName.values();
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
         .add("block", this.owner)
         .add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList()))
         .toString();
   }

   
   public Property<?> getProperty(String $$0) {
      return (Property<?>)this.propertiesByName.get($$0);
   }

   public static class Builder<O, S extends StateHolder<O, S>> {
      private final O owner;
      private final Map<String, Property<?>> properties = Maps.newHashMap();

      public Builder(O $$0) {
         this.owner = $$0;
      }

      public StateDefinition.Builder<O, S> add(Property<?>... $$0) {
         for (Property<?> $$1 : $$0) {
            this.validateProperty($$1);
            this.properties.put($$1.getName(), $$1);
         }

         return this;
      }

      private <T extends Comparable<T>> void validateProperty(Property<T> $$0) {
         String $$1 = $$0.getName();
         if (!StateDefinition.NAME_PATTERN.matcher($$1).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + $$1);
         } else {
            Collection<T> $$2 = $$0.getPossibleValues();
            if ($$2.size() <= 1) {
               throw new IllegalArgumentException(this.owner + " attempted use property " + $$1 + " with <= 1 possible values");
            } else {
               for (T $$3 : $$2) {
                  String $$4 = $$0.getName($$3);
                  if (!StateDefinition.NAME_PATTERN.matcher($$4).matches()) {
                     throw new IllegalArgumentException(this.owner + " has property: " + $$1 + " with invalidly named value: " + $$4);
                  }
               }

               if (this.properties.containsKey($$1)) {
                  throw new IllegalArgumentException(this.owner + " has duplicate property: " + $$1);
               }
            }
         }
      }

      public StateDefinition<O, S> create(Function<O, S> $$0, StateDefinition.Factory<O, S> $$1) {
         return new StateDefinition<>($$0, this.owner, $$1, this.properties);
      }
   }

   public interface Factory<O, S> {
      S create(O var1, Reference2ObjectArrayMap<Property<?>, Comparable<?>> var2, MapCodec<S> var3);
   }
}
