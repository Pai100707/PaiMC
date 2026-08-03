package net.minecraft.nbt.visitors;

import java.util.List;

public record FieldSelector(List<String> path, net.minecraft.nbt.TagType<?> type, String name) {
   public FieldSelector(net.minecraft.nbt.TagType<?> $$0, String $$1) {
      this(List.of(), $$0, $$1);
   }

   public FieldSelector(String $$0, net.minecraft.nbt.TagType<?> $$1, String $$2) {
      this(List.of($$0), $$1, $$2);
   }

   public FieldSelector(String $$0, String $$1, net.minecraft.nbt.TagType<?> $$2, String $$3) {
      this(List.of($$0, $$1), $$2, $$3);
   }
}
