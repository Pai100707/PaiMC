package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class CollectFields extends CollectToTag {
   private int fieldsToGetCount;
   private final Set<net.minecraft.nbt.TagType<?>> wantedTypes;
   private final Deque<FieldTree> stack = new ArrayDeque<>();

   public CollectFields(FieldSelector... $$0) {
      this.fieldsToGetCount = $$0.length;
      Builder<net.minecraft.nbt.TagType<?>> $$1 = ImmutableSet.builder();
      FieldTree $$2 = FieldTree.createRoot();

      for (FieldSelector $$3 : $$0) {
         $$2.addEntry($$3);
         $$1.add($$3.type());
      }

      this.stack.push($$2);
      $$1.add(net.minecraft.nbt.CompoundTag.TYPE);
      this.wantedTypes = $$1.build();
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitRootEntry(net.minecraft.nbt.TagType<?> $$0) {
      return $$0 != net.minecraft.nbt.CompoundTag.TYPE ? net.minecraft.nbt.StreamTagVisitor.ValueResult.HALT : super.visitRootEntry($$0);
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0) {
      FieldTree $$1 = this.stack.element();
      if (this.depth() > $$1.depth()) {
         return super.visitEntry($$0);
      } else if (this.fieldsToGetCount <= 0) {
         return net.minecraft.nbt.StreamTagVisitor.EntryResult.BREAK;
      } else {
         return !this.wantedTypes.contains($$0) ? net.minecraft.nbt.StreamTagVisitor.EntryResult.SKIP : super.visitEntry($$0);
      }
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0, String $$1) {
      FieldTree $$2 = this.stack.element();
      if (this.depth() > $$2.depth()) {
         return super.visitEntry($$0, $$1);
      } else if ($$2.selectedFields().remove($$1, $$0)) {
         this.fieldsToGetCount--;
         return super.visitEntry($$0, $$1);
      } else {
         if ($$0 == net.minecraft.nbt.CompoundTag.TYPE) {
            FieldTree $$3 = $$2.fieldsToRecurse().get($$1);
            if ($$3 != null) {
               this.stack.push($$3);
               return super.visitEntry($$0, $$1);
            }
         }

         return net.minecraft.nbt.StreamTagVisitor.EntryResult.SKIP;
      }
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }

   public int getMissingFieldCount() {
      return this.fieldsToGetCount;
   }
}
