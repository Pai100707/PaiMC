package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.util.Util;

public class SnbtPrinterTagVisitor implements net.minecraft.nbt.TagVisitor {
   private static final Map<String, List<String>> KEY_ORDER = (Map<String, List<String>>)Util.make(Maps.newHashMap(), $$0 -> {
      $$0.put("{}", Lists.newArrayList(new String[]{"DataVersion", "author", "size", "data", "entities", "palette", "palettes"}));
      $$0.put("{}.data.[].{}", Lists.newArrayList(new String[]{"pos", "state", "nbt"}));
      $$0.put("{}.entities.[].{}", Lists.newArrayList(new String[]{"blockPos", "pos"}));
   });
   private static final Set<String> NO_INDENTATION = Sets.newHashSet(new String[]{"{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}"});
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
   private static final String ELEMENT_SEPARATOR = String.valueOf(',');
   private static final String LIST_OPEN = "[";
   private static final String LIST_CLOSE = "]";
   private static final String LIST_TYPE_SEPARATOR = ";";
   private static final String ELEMENT_SPACING = " ";
   private static final String STRUCT_OPEN = "{";
   private static final String STRUCT_CLOSE = "}";
   private static final String NEWLINE = "\n";
   private final String indentation;
   private final int depth;
   private final List<String> path;
   private String result = "";

   public SnbtPrinterTagVisitor() {
      this("    ", 0, Lists.newArrayList());
   }

   public SnbtPrinterTagVisitor(String $$0, int $$1, List<String> $$2) {
      this.indentation = $$0;
      this.depth = $$1;
      this.path = $$2;
   }

   public String visit(net.minecraft.nbt.Tag $$0) {
      $$0.accept(this);
      return this.result;
   }

   @Override
   public void visitString(net.minecraft.nbt.StringTag $$0) {
      this.result = net.minecraft.nbt.StringTag.quoteAndEscape($$0.value());
   }

   @Override
   public void visitByte(net.minecraft.nbt.ByteTag $$0) {
      this.result = $$0.value() + "b";
   }

   @Override
   public void visitShort(net.minecraft.nbt.ShortTag $$0) {
      this.result = $$0.value() + "s";
   }

   @Override
   public void visitInt(net.minecraft.nbt.IntTag $$0) {
      this.result = String.valueOf($$0.value());
   }

   @Override
   public void visitLong(net.minecraft.nbt.LongTag $$0) {
      this.result = $$0.value() + "L";
   }

   @Override
   public void visitFloat(net.minecraft.nbt.FloatTag $$0) {
      this.result = $$0.value() + "f";
   }

   @Override
   public void visitDouble(net.minecraft.nbt.DoubleTag $$0) {
      this.result = $$0.value() + "d";
   }

   @Override
   public void visitByteArray(net.minecraft.nbt.ByteArrayTag $$0) {
      StringBuilder $$1 = new StringBuilder("[").append("B").append(";");
      byte[] $$2 = $$0.getAsByteArray();

      for (int $$3 = 0; $$3 < $$2.length; $$3++) {
         $$1.append(" ").append($$2[$$3]).append("B");
         if ($$3 != $$2.length - 1) {
            $$1.append(ELEMENT_SEPARATOR);
         }
      }

      $$1.append("]");
      this.result = $$1.toString();
   }

   @Override
   public void visitIntArray(net.minecraft.nbt.IntArrayTag $$0) {
      StringBuilder $$1 = new StringBuilder("[").append("I").append(";");
      int[] $$2 = $$0.getAsIntArray();

      for (int $$3 = 0; $$3 < $$2.length; $$3++) {
         $$1.append(" ").append($$2[$$3]);
         if ($$3 != $$2.length - 1) {
            $$1.append(ELEMENT_SEPARATOR);
         }
      }

      $$1.append("]");
      this.result = $$1.toString();
   }

   @Override
   public void visitLongArray(net.minecraft.nbt.LongArrayTag $$0) {
      String $$1 = "L";
      StringBuilder $$2 = new StringBuilder("[").append("L").append(";");
      long[] $$3 = $$0.getAsLongArray();

      for (int $$4 = 0; $$4 < $$3.length; $$4++) {
         $$2.append(" ").append($$3[$$4]).append("L");
         if ($$4 != $$3.length - 1) {
            $$2.append(ELEMENT_SEPARATOR);
         }
      }

      $$2.append("]");
      this.result = $$2.toString();
   }

   @Override
   public void visitList(net.minecraft.nbt.ListTag $$0) {
      if ($$0.isEmpty()) {
         this.result = "[]";
      } else {
         StringBuilder $$1 = new StringBuilder("[");
         this.pushPath("[]");
         String $$2 = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
         if (!$$2.isEmpty()) {
            $$1.append("\n");
         }

         for (int $$3 = 0; $$3 < $$0.size(); $$3++) {
            $$1.append(Strings.repeat($$2, this.depth + 1));
            $$1.append(new net.minecraft.nbt.SnbtPrinterTagVisitor($$2, this.depth + 1, this.path).visit($$0.get($$3)));
            if ($$3 != $$0.size() - 1) {
               $$1.append(ELEMENT_SEPARATOR).append($$2.isEmpty() ? " " : "\n");
            }
         }

         if (!$$2.isEmpty()) {
            $$1.append("\n").append(Strings.repeat($$2, this.depth));
         }

         $$1.append("]");
         this.result = $$1.toString();
         this.popPath();
      }
   }

   @Override
   public void visitCompound(net.minecraft.nbt.CompoundTag $$0) {
      if ($$0.isEmpty()) {
         this.result = "{}";
      } else {
         StringBuilder $$1 = new StringBuilder("{");
         this.pushPath("{}");
         String $$2 = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
         if (!$$2.isEmpty()) {
            $$1.append("\n");
         }

         Collection<String> $$3 = this.getKeys($$0);
         Iterator<String> $$4 = $$3.iterator();

         while ($$4.hasNext()) {
            String $$5 = $$4.next();
            net.minecraft.nbt.Tag $$6 = $$0.get($$5);
            this.pushPath($$5);
            $$1.append(Strings.repeat($$2, this.depth + 1))
               .append(handleEscapePretty($$5))
               .append(NAME_VALUE_SEPARATOR)
               .append(" ")
               .append(new net.minecraft.nbt.SnbtPrinterTagVisitor($$2, this.depth + 1, this.path).visit($$6));
            this.popPath();
            if ($$4.hasNext()) {
               $$1.append(ELEMENT_SEPARATOR).append($$2.isEmpty() ? " " : "\n");
            }
         }

         if (!$$2.isEmpty()) {
            $$1.append("\n").append(Strings.repeat($$2, this.depth));
         }

         $$1.append("}");
         this.result = $$1.toString();
         this.popPath();
      }
   }

   private void popPath() {
      this.path.remove(this.path.size() - 1);
   }

   private void pushPath(String $$0) {
      this.path.add($$0);
   }

   protected List<String> getKeys(net.minecraft.nbt.CompoundTag $$0) {
      Set<String> $$1 = Sets.newHashSet($$0.keySet());
      List<String> $$2 = Lists.newArrayList();
      List<String> $$3 = KEY_ORDER.get(this.pathString());
      if ($$3 != null) {
         for (String $$4 : $$3) {
            if ($$1.remove($$4)) {
               $$2.add($$4);
            }
         }

         if (!$$1.isEmpty()) {
            $$1.stream().sorted().forEach($$2::add);
         }
      } else {
         $$2.addAll($$1);
         Collections.sort($$2);
      }

      return $$2;
   }

   public String pathString() {
      return String.join(".", this.path);
   }

   protected static String handleEscapePretty(String $$0) {
      return SIMPLE_VALUE.matcher($$0).matches() ? $$0 : net.minecraft.nbt.StringTag.quoteAndEscape($$0);
   }

   @Override
   public void visitEnd(net.minecraft.nbt.EndTag $$0) {
   }
}
