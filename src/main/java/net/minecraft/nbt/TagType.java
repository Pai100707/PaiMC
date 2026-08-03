package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends net.minecraft.nbt.Tag> {
   T load(DataInput var1, net.minecraft.nbt.NbtAccounter var2) throws IOException;

   net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput var1, net.minecraft.nbt.StreamTagVisitor var2, net.minecraft.nbt.NbtAccounter var3) throws IOException;

   default void parseRoot(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
      switch ($$1.visitRootEntry(this)) {
         case CONTINUE:
            this.parse($$0, $$1, $$2);
         case HALT:
         default:
            break;
         case BREAK:
            this.skip($$0, $$2);
      }
   }

   void skip(DataInput var1, int var2, net.minecraft.nbt.NbtAccounter var3) throws IOException;

   void skip(DataInput var1, net.minecraft.nbt.NbtAccounter var2) throws IOException;

   String getName();

   String getPrettyName();

   static net.minecraft.nbt.TagType<net.minecraft.nbt.EndTag> createInvalid(final int $$0) {
      return new net.minecraft.nbt.TagType<net.minecraft.nbt.EndTag>() {
         private IOException createException() {
            return new IOException("Invalid tag id: " + $$0);
         }

         public net.minecraft.nbt.EndTag load(DataInput $$0x, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
            throw this.createException();
         }

         @Override
         public net.minecraft.nbt.StreamTagVisitor.ValueResult parse(DataInput $$0x, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
            throw this.createException();
         }

         @Override
         public void skip(DataInput $$0x, int $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
            throw this.createException();
         }

         @Override
         public void skip(DataInput $$0x, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
            throw this.createException();
         }

         @Override
         public String getName() {
            return "INVALID[" + $$0 + "]";
         }

         @Override
         public String getPrettyName() {
            return "UNKNOWN_" + $$0;
         }
      };
   }

   public interface StaticSize<T extends net.minecraft.nbt.Tag> extends net.minecraft.nbt.TagType<T> {
      @Override
      default void skip(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
         $$0.skipBytes(this.size());
      }

      @Override
      default void skip(DataInput $$0, int $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         $$0.skipBytes(this.size() * $$1);
      }

      int size();
   }

   public interface VariableSize<T extends net.minecraft.nbt.Tag> extends net.minecraft.nbt.TagType<T> {
      @Override
      default void skip(DataInput $$0, int $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
         for (int $$3 = 0; $$3 < $$1; $$3++) {
            this.skip($$0, $$2);
         }
      }
   }
}
