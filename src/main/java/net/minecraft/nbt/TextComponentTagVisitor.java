package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor implements net.minecraft.nbt.TagVisitor {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int INLINE_LIST_THRESHOLD = 8;
   private static final int MAX_DEPTH = 64;
   private static final int MAX_LENGTH = 128;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
   private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   private static final String LIST_OPEN = "[";
   private static final String LIST_CLOSE = "]";
   private static final String LIST_TYPE_SEPARATOR = ";";
   private static final String ELEMENT_SPACING = " ";
   private static final String STRUCT_OPEN = "{";
   private static final String STRUCT_CLOSE = "}";
   private static final String NEWLINE = "\n";
   private static final String NAME_VALUE_SEPARATOR = ": ";
   private static final String ELEMENT_SEPARATOR = String.valueOf(',');
   private static final String WRAPPED_ELEMENT_SEPARATOR = ELEMENT_SEPARATOR + "\n";
   private static final String SPACED_ELEMENT_SEPARATOR = ELEMENT_SEPARATOR + " ";
   private static final Component FOLDED = Component.literal("<...>").withStyle(ChatFormatting.GRAY);
   private static final Component BYTE_TYPE = Component.literal("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private static final Component SHORT_TYPE = Component.literal("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private static final Component INT_TYPE = Component.literal("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private static final Component LONG_TYPE = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private static final Component FLOAT_TYPE = Component.literal("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private static final Component DOUBLE_TYPE = Component.literal("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private static final Component BYTE_ARRAY_TYPE = Component.literal("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
   private final String indentation;
   private int indentDepth;
   private int depth;
   private final MutableComponent result = Component.empty();

   public TextComponentTagVisitor(String $$0) {
      this.indentation = $$0;
   }

   public Component visit(net.minecraft.nbt.Tag $$0) {
      $$0.accept(this);
      return this.result;
   }

   @Override
   public void visitString(net.minecraft.nbt.StringTag $$0) {
      String $$1 = net.minecraft.nbt.StringTag.quoteAndEscape($$0.value());
      String $$2 = $$1.substring(0, 1);
      Component $$3 = Component.literal($$1.substring(1, $$1.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
      this.result.append($$2).append($$3).append($$2);
   }

   @Override
   public void visitByte(net.minecraft.nbt.ByteTag $$0) {
      this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(BYTE_TYPE);
   }

   @Override
   public void visitShort(net.minecraft.nbt.ShortTag $$0) {
      this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(SHORT_TYPE);
   }

   @Override
   public void visitInt(net.minecraft.nbt.IntTag $$0) {
      this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
   }

   @Override
   public void visitLong(net.minecraft.nbt.LongTag $$0) {
      this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(LONG_TYPE);
   }

   @Override
   public void visitFloat(net.minecraft.nbt.FloatTag $$0) {
      this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(FLOAT_TYPE);
   }

   @Override
   public void visitDouble(net.minecraft.nbt.DoubleTag $$0) {
      this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(DOUBLE_TYPE);
   }

   @Override
   public void visitByteArray(net.minecraft.nbt.ByteArrayTag $$0) {
      this.result.append("[").append(BYTE_ARRAY_TYPE).append(";");
      byte[] $$1 = $$0.getAsByteArray();

      for (int $$2 = 0; $$2 < $$1.length && $$2 < 128; $$2++) {
         MutableComponent $$3 = Component.literal(String.valueOf($$1[$$2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
         this.result.append(" ").append($$3).append(BYTE_ARRAY_TYPE);
         if ($$2 != $$1.length - 1) {
            this.result.append(ELEMENT_SEPARATOR);
         }
      }

      if ($$1.length > 128) {
         this.result.append(FOLDED);
      }

      this.result.append("]");
   }

   @Override
   public void visitIntArray(net.minecraft.nbt.IntArrayTag $$0) {
      this.result.append("[").append(INT_TYPE).append(";");
      int[] $$1 = $$0.getAsIntArray();

      for (int $$2 = 0; $$2 < $$1.length && $$2 < 128; $$2++) {
         this.result.append(" ").append(Component.literal(String.valueOf($$1[$$2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
         if ($$2 != $$1.length - 1) {
            this.result.append(ELEMENT_SEPARATOR);
         }
      }

      if ($$1.length > 128) {
         this.result.append(FOLDED);
      }

      this.result.append("]");
   }

   @Override
   public void visitLongArray(net.minecraft.nbt.LongArrayTag $$0) {
      this.result.append("[").append(LONG_TYPE).append(";");
      long[] $$1 = $$0.getAsLongArray();

      for (int $$2 = 0; $$2 < $$1.length && $$2 < 128; $$2++) {
         Component $$3 = Component.literal(String.valueOf($$1[$$2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
         this.result.append(" ").append($$3).append(LONG_TYPE);
         if ($$2 != $$1.length - 1) {
            this.result.append(ELEMENT_SEPARATOR);
         }
      }

      if ($$1.length > 128) {
         this.result.append(FOLDED);
      }

      this.result.append("]");
   }

   private static boolean shouldWrapListElements(net.minecraft.nbt.ListTag $$0) {
      if ($$0.size() >= 8) {
         return false;
      } else {
         for (net.minecraft.nbt.Tag $$1 : $$0) {
            if (!($$1 instanceof net.minecraft.nbt.NumericTag)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void visitList(net.minecraft.nbt.ListTag $$0) {
      if ($$0.isEmpty()) {
         this.result.append("[]");
      } else if (this.depth >= 64) {
         this.result.append("[").append(FOLDED).append("]");
      } else if (!shouldWrapListElements($$0)) {
         this.result.append("[");

         for (int $$1 = 0; $$1 < $$0.size(); $$1++) {
            if ($$1 != 0) {
               this.result.append(SPACED_ELEMENT_SEPARATOR);
            }

            this.appendSubTag($$0.get($$1), false);
         }

         this.result.append("]");
      } else {
         this.result.append("[");
         if (!this.indentation.isEmpty()) {
            this.result.append("\n");
         }

         String $$2 = Strings.repeat(this.indentation, this.indentDepth + 1);

         for (int $$3 = 0; $$3 < $$0.size() && $$3 < 128; $$3++) {
            this.result.append($$2);
            this.appendSubTag($$0.get($$3), true);
            if ($$3 != $$0.size() - 1) {
               this.result.append(this.indentation.isEmpty() ? SPACED_ELEMENT_SEPARATOR : WRAPPED_ELEMENT_SEPARATOR);
            }
         }

         if ($$0.size() > 128) {
            this.result.append($$2).append(FOLDED);
         }

         if (!this.indentation.isEmpty()) {
            this.result.append("\n" + Strings.repeat(this.indentation, this.indentDepth));
         }

         this.result.append("]");
      }
   }

   @Override
   public void visitCompound(net.minecraft.nbt.CompoundTag $$0) {
      if ($$0.isEmpty()) {
         this.result.append("{}");
      } else if (this.depth >= 64) {
         this.result.append("{").append(FOLDED).append("}");
      } else {
         this.result.append("{");
         Collection<String> $$1 = $$0.keySet();
         if (LOGGER.isDebugEnabled()) {
            List<String> $$2 = Lists.newArrayList($$0.keySet());
            Collections.sort($$2);
            $$1 = $$2;
         }

         if (!this.indentation.isEmpty()) {
            this.result.append("\n");
         }

         String $$3 = Strings.repeat(this.indentation, this.indentDepth + 1);
         Iterator<String> $$4 = $$1.iterator();

         while ($$4.hasNext()) {
            String $$5 = $$4.next();
            this.result.append($$3).append(handleEscapePretty($$5)).append(": ");
            this.appendSubTag($$0.get($$5), true);
            if ($$4.hasNext()) {
               this.result.append(this.indentation.isEmpty() ? SPACED_ELEMENT_SEPARATOR : WRAPPED_ELEMENT_SEPARATOR);
            }
         }

         if (!this.indentation.isEmpty()) {
            this.result.append("\n" + Strings.repeat(this.indentation, this.indentDepth));
         }

         this.result.append("}");
      }
   }

   private void appendSubTag(net.minecraft.nbt.Tag $$0, boolean $$1) {
      if ($$1) {
         this.indentDepth++;
      }

      this.depth++;

      try {
         $$0.accept(this);
      } finally {
         if ($$1) {
            this.indentDepth--;
         }

         this.depth--;
      }
   }

   protected static Component handleEscapePretty(String $$0) {
      if (SIMPLE_VALUE.matcher($$0).matches()) {
         return Component.literal($$0).withStyle(SYNTAX_HIGHLIGHTING_KEY);
      } else {
         String $$1 = net.minecraft.nbt.StringTag.quoteAndEscape($$0);
         String $$2 = $$1.substring(0, 1);
         Component $$3 = Component.literal($$1.substring(1, $$1.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
         return Component.literal($$2).append($$3).append($$2);
      }
   }

   @Override
   public void visitEnd(net.minecraft.nbt.EndTag $$0) {
   }
}
