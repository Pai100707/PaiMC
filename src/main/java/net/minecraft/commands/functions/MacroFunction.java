package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MacroFunction<T extends net.minecraft.commands.ExecutionCommandSource<T>> implements CommandFunction<T> {
   private static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat)Util.make(
      new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ROOT)), $$0 -> $$0.setMaximumFractionDigits(15)
   );
   private static final int MAX_CACHE_ENTRIES = 8;
   private final List<String> parameters;
   private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25F);
   private final Identifier id;
   private final List<MacroFunction.Entry<T>> entries;

   public MacroFunction(Identifier $$0, List<MacroFunction.Entry<T>> $$1, List<String> $$2) {
      this.id = $$0;
      this.entries = $$1;
      this.parameters = $$2;
   }

   @Override
   public Identifier id() {
      return this.id;
   }

   @Override
   public InstantiatedFunction<T> instantiate(@Nullable CompoundTag $$0, CommandDispatcher<T> $$1) throws net.minecraft.commands.FunctionInstantiationException {
      if ($$0 == null) {
         throw new net.minecraft.commands.FunctionInstantiationException(
            Component.translatable("commands.function.error.missing_arguments", new Object[]{Component.translationArg(this.id())})
         );
      } else {
         List<String> $$2 = new ArrayList<>(this.parameters.size());

         for (String $$3 : this.parameters) {
            Tag $$4 = $$0.get($$3);
            if ($$4 == null) {
               throw new net.minecraft.commands.FunctionInstantiationException(
                  Component.translatable("commands.function.error.missing_argument", new Object[]{Component.translationArg(this.id()), $$3})
               );
            }

            $$2.add(stringify($$4));
         }

         InstantiatedFunction<T> $$5 = (InstantiatedFunction<T>)this.cache.getAndMoveToLast($$2);
         if ($$5 != null) {
            return $$5;
         } else {
            if (this.cache.size() >= 8) {
               this.cache.removeFirst();
            }

            InstantiatedFunction<T> $$6 = this.substituteAndParse(this.parameters, $$2, $$1);
            this.cache.put($$2, $$6);
            return $$6;
         }
      }
   }

   private static String stringify(Tag $$0) {
      return switch ($$0) {
         case FloatTag var3 -> {
            FloatTag var39 = var3;

            try {
               var40 = var39.value();
            } catch (Throwable var23) {
               throw new MatchException(var23.toString(), var23);
            }

            float var24 = var40;
            yield DECIMAL_FORMAT.format(var24);
         }
         case DoubleTag var5 -> {
            DoubleTag var37 = var5;

            try {
               var38 = var37.value();
            } catch (Throwable var22) {
               throw new MatchException(var22.toString(), var22);
            }

            double var25 = var38;
            yield DECIMAL_FORMAT.format(var25);
         }
         case ByteTag var8 -> {
            ByteTag var35 = var8;

            try {
               var36 = var35.value();
            } catch (Throwable var21) {
               throw new MatchException(var21.toString(), var21);
            }

            byte var26 = var36;
            yield String.valueOf((int)var26);
         }
         case ShortTag var10 -> {
            ShortTag var33 = var10;

            try {
               var34 = var33.value();
            } catch (Throwable var20) {
               throw new MatchException(var20.toString(), var20);
            }

            short var27 = var34;
            yield String.valueOf((int)var27);
         }
         case LongTag var12 -> {
            LongTag var31 = var12;

            try {
               var32 = var31.value();
            } catch (Throwable var19) {
               throw new MatchException(var19.toString(), var19);
            }

            long var28 = var32;
            yield String.valueOf(var28);
         }
         case StringTag var15 -> {
            StringTag var29 = var15;

            try {
               var30 = var29.value();
            } catch (Throwable var18) {
               throw new MatchException(var18.toString(), var18);
            }

            String var17 = var30;
            yield var17;
         }
         default -> $$0.toString();
      };
   }

   private static void lookupValues(List<String> $$0, IntList $$1, List<String> $$2) {
      $$2.clear();
      $$1.forEach($$2x -> $$2.add($$0.get($$2x)));
   }

   private InstantiatedFunction<T> substituteAndParse(List<String> $$0, List<String> $$1, CommandDispatcher<T> $$2) throws net.minecraft.commands.FunctionInstantiationException {
      List<UnboundEntryAction<T>> $$3 = new ArrayList<>(this.entries.size());
      List<String> $$4 = new ArrayList<>($$1.size());

      for (MacroFunction.Entry<T> $$5 : this.entries) {
         lookupValues($$1, $$5.parameters(), $$4);
         $$3.add($$5.instantiate($$4, $$2, this.id));
      }

      return new PlainTextFunction<>(this.id().withPath($$1x -> $$1x + "/" + $$0.hashCode()), $$3);
   }

   interface Entry<T> {
      IntList parameters();

      UnboundEntryAction<T> instantiate(List<String> var1, CommandDispatcher<T> var2, Identifier var3) throws net.minecraft.commands.FunctionInstantiationException;
   }

   static class MacroEntry<T extends net.minecraft.commands.ExecutionCommandSource<T>> implements MacroFunction.Entry<T> {
      private final StringTemplate template;
      private final IntList parameters;
      private final T compilationContext;

      public MacroEntry(StringTemplate $$0, IntList $$1, T $$2) {
         this.template = $$0;
         this.parameters = $$1;
         this.compilationContext = $$2;
      }

      @Override
      public IntList parameters() {
         return this.parameters;
      }

      @Override
      public UnboundEntryAction<T> instantiate(List<String> $$0, CommandDispatcher<T> $$1, Identifier $$2) throws net.minecraft.commands.FunctionInstantiationException {
         String $$3 = this.template.substitute($$0);

         try {
            return CommandFunction.parseCommand($$1, this.compilationContext, new StringReader($$3));
         } catch (CommandSyntaxException var6) {
            throw new net.minecraft.commands.FunctionInstantiationException(
               Component.translatable("commands.function.error.parse", new Object[]{Component.translationArg($$2), $$3, var6.getMessage()})
            );
         }
      }
   }

   static class PlainTextEntry<T> implements MacroFunction.Entry<T> {
      private final UnboundEntryAction<T> compiledAction;

      public PlainTextEntry(UnboundEntryAction<T> $$0) {
         this.compiledAction = $$0;
      }

      @Override
      public IntList parameters() {
         return IntLists.emptyList();
      }

      @Override
      public UnboundEntryAction<T> instantiate(List<String> $$0, CommandDispatcher<T> $$1, Identifier $$2) {
         return this.compiledAction;
      }
   }
}
