package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import org.jspecify.annotations.Nullable;

public class CollectToTag implements net.minecraft.nbt.StreamTagVisitor {
   private final Deque<CollectToTag.ContainerBuilder> containerStack = new ArrayDeque<>();

   public CollectToTag() {
      this.containerStack.addLast(new CollectToTag.RootBuilder());
   }

   @Nullable
   public net.minecraft.nbt.Tag getResult() {
      return this.containerStack.getFirst().build();
   }

   protected int depth() {
      return this.containerStack.size() - 1;
   }

   private void appendEntry(net.minecraft.nbt.Tag $$0) {
      this.containerStack.getLast().acceptValue($$0);
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitEnd() {
      this.appendEntry(net.minecraft.nbt.EndTag.INSTANCE);
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(String $$0) {
      this.appendEntry(net.minecraft.nbt.StringTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(byte $$0) {
      this.appendEntry(net.minecraft.nbt.ByteTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(short $$0) {
      this.appendEntry(net.minecraft.nbt.ShortTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(int $$0) {
      this.appendEntry(net.minecraft.nbt.IntTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(long $$0) {
      this.appendEntry(net.minecraft.nbt.LongTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(float $$0) {
      this.appendEntry(net.minecraft.nbt.FloatTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(double $$0) {
      this.appendEntry(net.minecraft.nbt.DoubleTag.valueOf($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(byte[] $$0) {
      this.appendEntry(new net.minecraft.nbt.ByteArrayTag($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(int[] $$0) {
      this.appendEntry(new net.minecraft.nbt.IntArrayTag($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visit(long[] $$0) {
      this.appendEntry(new net.minecraft.nbt.LongArrayTag($$0));
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitList(net.minecraft.nbt.TagType<?> $$0, int $$1) {
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.EntryResult visitElement(net.minecraft.nbt.TagType<?> $$0, int $$1) {
      this.enterContainerIfNeeded($$0);
      return net.minecraft.nbt.StreamTagVisitor.EntryResult.ENTER;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0) {
      return net.minecraft.nbt.StreamTagVisitor.EntryResult.ENTER;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.EntryResult visitEntry(net.minecraft.nbt.TagType<?> $$0, String $$1) {
      this.containerStack.getLast().acceptKey($$1);
      this.enterContainerIfNeeded($$0);
      return net.minecraft.nbt.StreamTagVisitor.EntryResult.ENTER;
   }

   private void enterContainerIfNeeded(net.minecraft.nbt.TagType<?> $$0) {
      if ($$0 == net.minecraft.nbt.ListTag.TYPE) {
         this.containerStack.addLast(new CollectToTag.ListBuilder());
      } else if ($$0 == net.minecraft.nbt.CompoundTag.TYPE) {
         this.containerStack.addLast(new CollectToTag.CompoundBuilder());
      }
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitContainerEnd() {
      CollectToTag.ContainerBuilder $$0 = this.containerStack.removeLast();
      net.minecraft.nbt.Tag $$1 = $$0.build();
      if ($$1 != null) {
         this.containerStack.getLast().acceptValue($$1);
      }

      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult visitRootEntry(net.minecraft.nbt.TagType<?> $$0) {
      this.enterContainerIfNeeded($$0);
      return net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE;
   }

   static class CompoundBuilder implements CollectToTag.ContainerBuilder {
      private final net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
      private String lastId = "";

      @Override
      public void acceptKey(String $$0) {
         this.lastId = $$0;
      }

      @Override
      public void acceptValue(net.minecraft.nbt.Tag $$0) {
         this.compound.put(this.lastId, $$0);
      }

      @Override
      public net.minecraft.nbt.Tag build() {
         return this.compound;
      }
   }

   interface ContainerBuilder {
      default void acceptKey(String $$0) {
      }

      void acceptValue(net.minecraft.nbt.Tag var1);

      @Nullable
      net.minecraft.nbt.Tag build();
   }

   static class ListBuilder implements CollectToTag.ContainerBuilder {
      private final net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();

      @Override
      public void acceptValue(net.minecraft.nbt.Tag $$0) {
         this.list.addAndUnwrap($$0);
      }

      @Override
      public net.minecraft.nbt.Tag build() {
         return this.list;
      }
   }

   static class RootBuilder implements CollectToTag.ContainerBuilder {
      @Nullable
      private net.minecraft.nbt.Tag result;

      @Override
      public void acceptValue(net.minecraft.nbt.Tag $$0) {
         this.result = $$0;
      }

      @Nullable
      @Override
      public net.minecraft.nbt.Tag build() {
         return this.result;
      }
   }
}
