package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

public final class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {
   private final List<T> values;
   private final Map<String, T> names;
   private final int[] ordinalToIndex;

   private EnumProperty(String $$0, Class<T> $$1, List<T> $$2) {
      super($$0, $$1);
      if ($$2.isEmpty()) {
         throw new IllegalArgumentException("Trying to make empty EnumProperty '" + $$0 + "'");
      } else {
         this.values = List.copyOf($$2);
         T[] $$3 = $$1.getEnumConstants();
         this.ordinalToIndex = new int[$$3.length];

         for (T $$4 : $$3) {
            this.ordinalToIndex[$$4.ordinal()] = $$2.indexOf($$4);
         }

         Builder<String, T> $$5 = ImmutableMap.builder();

         for (T $$6 : $$2) {
            String $$7 = $$6.getSerializedName();
            $$5.put($$7, $$6);
         }

         this.names = $$5.buildOrThrow();
      }
   }

   @Override
   public List<T> getPossibleValues() {
      return this.values;
   }

   @Override
   public Optional<T> getValue(String $$0) {
      return Optional.ofNullable(this.names.get($$0));
   }

   public String getName(T $$0) {
      return $$0.getSerializedName();
   }

   public int getInternalIndex(T $$0) {
      return this.ordinalToIndex[$$0.ordinal()];
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return $$0 instanceof EnumProperty<?> $$1 && super.equals($$0) ? this.values.equals($$1.values) : false;
      }
   }

   @Override
   public int generateHashCode() {
      int $$0 = super.generateHashCode();
      return 31 * $$0 + this.values.hashCode();
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String $$0, Class<T> $$1) {
      return create($$0, $$1, $$0x -> true);
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String $$0, Class<T> $$1, Predicate<T> $$2) {
      return create($$0, $$1, Arrays.<T>stream($$1.getEnumConstants()).filter($$2).collect(Collectors.toList()));
   }

   @SafeVarargs
   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String $$0, Class<T> $$1, T... $$2) {
      return create($$0, $$1, List.of($$2));
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String $$0, Class<T> $$1, List<T> $$2) {
      return new EnumProperty<>($$0, $$1, $$2);
   }
}
