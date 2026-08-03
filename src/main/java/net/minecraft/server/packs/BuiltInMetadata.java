package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public class BuiltInMetadata {
   private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
   private final Map<MetadataSectionType<?>, ?> values;

   private BuiltInMetadata(Map<MetadataSectionType<?>, ?> $$0) {
      this.values = $$0;
   }

   public <T> T get(MetadataSectionType<T> $$0) {
      return (T)this.values.get($$0);
   }

   public static BuiltInMetadata of() {
      return EMPTY;
   }

   public static <T> BuiltInMetadata of(MetadataSectionType<T> $$0, T $$1) {
      return new BuiltInMetadata(Map.of($$0, $$1));
   }

   public static <T1, T2> BuiltInMetadata of(MetadataSectionType<T1> $$0, T1 $$1, MetadataSectionType<T2> $$2, T2 $$3) {
      return new BuiltInMetadata(Map.of($$0, $$1, $$2, (T1)$$3));
   }
}
