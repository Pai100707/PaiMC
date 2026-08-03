package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class EndTag implements net.minecraft.nbt.Tag {
   private static final int SELF_SIZE_IN_BYTES = 8;
   public static final net.minecraft.nbt.TagType<net.minecraft.nbt.EndTag> TYPE = new net.minecraft.nbt.TagType<net.minecraft.nbt.EndTag>() {
      public net.minecraft.nbt.EndTag load(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) {
         $$1.accountBytes(8L);
         return net.minecraft.nbt.EndTag.INSTANCE;
      }

      @Override
      public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) {
         $$2.accountBytes(8L);
         return $$1.visitEnd();
      }

      @Override
      public void skip(DataInput $$0, int $$1, net.minecraft.nbt.NbtAccounter $$2) {
      }

      @Override
      public void skip(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) {
      }

      @Override
      public String getName() {
         return "END";
      }

      @Override
      public String getPrettyName() {
         return "TAG_End";
      }
   };
   public static final net.minecraft.nbt.EndTag INSTANCE = new net.minecraft.nbt.EndTag();

   private EndTag() {
   }

   @Override
   public void write(DataOutput $$0) throws IOException {
   }

   @Override
   public int sizeInBytes() {
      return 8;
   }

   @Override
   public byte getId() {
      return 0;
   }

   @Override
   public net.minecraft.nbt.TagType<net.minecraft.nbt.EndTag> getType() {
      return TYPE;
   }

   @Override
   public String toString() {
      net.minecraft.nbt.StringTagVisitor $$0 = new net.minecraft.nbt.StringTagVisitor();
      $$0.visitEnd(this);
      return $$0.build();
   }

   public net.minecraft.nbt.EndTag copy() {
      return this;
   }

   @Override
   public void accept(net.minecraft.nbt.TagVisitor $$0) {
      $$0.visitEnd(this);
   }

   @Override
   public net.minecraft.nbt.StreamTagVisitor.ValueResult accept(net.minecraft.nbt.StreamTagVisitor $$0) {
      return $$0.visitEnd();
   }
}
