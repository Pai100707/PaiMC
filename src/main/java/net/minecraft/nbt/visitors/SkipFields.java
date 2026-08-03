package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;

public class SkipFields extends CollectToTag {
   private final Deque<FieldTree> stack = new ArrayDeque<>();

   public SkipFields(FieldSelector... $$0) {
      FieldTree $$1 = FieldTree.createRoot();

      for (FieldSelector $$2 : $$0) {
         $$1.addEntry($$2);
      }

      this.stack.push($$1);
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0, String $$1) {
      FieldTree $$2 = this.stack.element();
      if ($$2.isSelected($$0, $$1)) {
         return net.minecraft.nbt.StreamTagVisitor.EntryResult.SKIP;
      } else {
         if ($$0 == net.minecraft.nbt.CompoundTag.TYPE) {
            FieldTree $$3 = $$2.fieldsToRecurse().get($$1);
            if ($$3 != null) {
               this.stack.push($$3);
            }
         }

         return super.visitEntry($$0, $$1);
      }
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }
}
