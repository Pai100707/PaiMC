package net.minecraft.nbt.visitors;

import java.util.HashMap;
import java.util.Map;

public record FieldTree(int depth, Map<String, net.minecraft.nbt.TagType<?>> selectedFields, Map<String, FieldTree> fieldsToRecurse) {
   private FieldTree(int $$0) {
      this($$0, new HashMap<>(), new HashMap<>());
   }

   public static FieldTree createRoot() {
      return new FieldTree(1);
   }

   public void addEntry(FieldSelector $$0) {
      if (this.depth <= $$0.path().size()) {
         this.fieldsToRecurse.computeIfAbsent($$0.path().get(this.depth - 1), $$0x -> new FieldTree(this.depth + 1)).addEntry($$0);
      } else {
         this.selectedFields.put($$0.name(), $$0.type());
      }
   }

   public boolean isSelected(net.minecraft.nbt.TagType<?> $$0, String $$1) {
      return $$0.equals(this.selectedFields().get($$1));
   }
}
