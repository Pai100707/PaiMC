package net.minecraft.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class StringTagVisitor implements net.minecraft.nbt.TagVisitor {
   private static final Pattern UNQUOTED_KEY_MATCH = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");
   private final StringBuilder builder = new StringBuilder();

   public String build() {
      return this.builder.toString();
   }

   @Override
   public void visitString(net.minecraft.nbt.StringTag $$0) {
      this.builder.append(net.minecraft.nbt.StringTag.quoteAndEscape($$0.value()));
   }

   @Override
   public void visitByte(net.minecraft.nbt.ByteTag $$0) {
      this.builder.append($$0.value()).append('b');
   }

   @Override
   public void visitShort(net.minecraft.nbt.ShortTag $$0) {
      this.builder.append($$0.value()).append('s');
   }

   @Override
   public void visitInt(net.minecraft.nbt.IntTag $$0) {
      this.builder.append($$0.value());
   }

   @Override
   public void visitLong(net.minecraft.nbt.LongTag $$0) {
      this.builder.append($$0.value()).append('L');
   }

   @Override
   public void visitFloat(net.minecraft.nbt.FloatTag $$0) {
      this.builder.append($$0.value()).append('f');
   }

   @Override
   public void visitDouble(net.minecraft.nbt.DoubleTag $$0) {
      this.builder.append($$0.value()).append('d');
   }

   @Override
   public void visitByteArray(net.minecraft.nbt.ByteArrayTag $$0) {
      this.builder.append("[B;");
      byte[] $$1 = $$0.getAsByteArray();

      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         if ($$2 != 0) {
            this.builder.append(',');
         }

         this.builder.append($$1[$$2]).append('B');
      }

      this.builder.append(']');
   }

   @Override
   public void visitIntArray(net.minecraft.nbt.IntArrayTag $$0) {
      this.builder.append("[I;");
      int[] $$1 = $$0.getAsIntArray();

      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         if ($$2 != 0) {
            this.builder.append(',');
         }

         this.builder.append($$1[$$2]);
      }

      this.builder.append(']');
   }

   @Override
   public void visitLongArray(net.minecraft.nbt.LongArrayTag $$0) {
      this.builder.append("[L;");
      long[] $$1 = $$0.getAsLongArray();

      for (int $$2 = 0; $$2 < $$1.length; $$2++) {
         if ($$2 != 0) {
            this.builder.append(',');
         }

         this.builder.append($$1[$$2]).append('L');
      }

      this.builder.append(']');
   }

   @Override
   public void visitList(net.minecraft.nbt.ListTag $$0) {
      this.builder.append('[');

      for (int $$1 = 0; $$1 < $$0.size(); $$1++) {
         if ($$1 != 0) {
            this.builder.append(',');
         }

         $$0.get($$1).accept(this);
      }

      this.builder.append(']');
   }

   @Override
   public void visitCompound(net.minecraft.nbt.CompoundTag $$0) {
      this.builder.append('{');
      List<Entry<String, net.minecraft.nbt.Tag>> $$1 = new ArrayList<>($$0.entrySet());
      $$1.sort(Entry.comparingByKey());

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         Entry<String, net.minecraft.nbt.Tag> $$3 = $$1.get($$2);
         if ($$2 != 0) {
            this.builder.append(',');
         }

         this.handleKeyEscape($$3.getKey());
         this.builder.append(':');
         $$3.getValue().accept(this);
      }

      this.builder.append('}');
   }

   private void handleKeyEscape(String $$0) {
      if (!$$0.equalsIgnoreCase("true") && !$$0.equalsIgnoreCase("false") && UNQUOTED_KEY_MATCH.matcher($$0).matches()) {
         this.builder.append($$0);
      } else {
         net.minecraft.nbt.StringTag.quoteAndEscape($$0, this.builder);
      }
   }

   @Override
   public void visitEnd(net.minecraft.nbt.EndTag $$0) {
      this.builder.append("END");
   }
}
