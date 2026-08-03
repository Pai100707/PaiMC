package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class TimeArgument implements ArgumentType<Integer> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0s", "0t", "0");
   private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new SimpleCommandExceptionType(Component.translatable("argument.time.invalid_unit"));
   private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_LOW = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("argument.time.tick_count_too_low", new Object[]{$$1, $$0})
   );
   private static final Object2IntMap<String> UNITS = new Object2IntOpenHashMap();
   final int minimum;

   private TimeArgument(int $$0) {
      this.minimum = $$0;
   }

   public static TimeArgument time() {
      return new TimeArgument(0);
   }

   public static TimeArgument time(int $$0) {
      return new TimeArgument($$0);
   }

   public Integer parse(StringReader $$0) throws CommandSyntaxException {
      float $$1 = $$0.readFloat();
      String $$2 = $$0.readUnquotedString();
      int $$3 = UNITS.getOrDefault($$2, 0);
      if ($$3 == 0) {
         throw ERROR_INVALID_UNIT.createWithContext($$0);
      } else {
         int $$4 = Math.round($$1 * $$3);
         if ($$4 < this.minimum) {
            throw ERROR_TICK_COUNT_TOO_LOW.createWithContext($$0, $$4, this.minimum);
         } else {
            return $$4;
         }
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      StringReader $$2 = new StringReader($$1.getRemaining());

      try {
         $$2.readFloat();
      } catch (CommandSyntaxException var5) {
         return $$1.buildFuture();
      }

      return net.minecraft.commands.SharedSuggestionProvider.suggest(UNITS.keySet(), $$1.createOffset($$1.getStart() + $$2.getCursor()));
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static {
      UNITS.put("d", 24000);
      UNITS.put("s", 20);
      UNITS.put("t", 1);
      UNITS.put("", 1);
   }

   public static class Info implements ArgumentTypeInfo<TimeArgument, TimeArgument.Info.Template> {
      public void serializeToNetwork(TimeArgument.Info.Template $$0, FriendlyByteBuf $$1) {
         $$1.writeInt($$0.min);
      }

      public TimeArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         int $$1 = $$0.readInt();
         return new TimeArgument.Info.Template($$1);
      }

      public void serializeToJson(TimeArgument.Info.Template $$0, JsonObject $$1) {
         $$1.addProperty("min", $$0.min);
      }

      public TimeArgument.Info.Template unpack(TimeArgument $$0) {
         return new TimeArgument.Info.Template($$0.minimum);
      }

      public final class Template implements ArgumentTypeInfo.Template<TimeArgument> {
         final int min;

         Template(final int $$1) {
            this.min = $$1;
         }

         public TimeArgument instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return TimeArgument.time(this.min);
         }

         @Override
         public ArgumentTypeInfo<TimeArgument, ?> type() {
            return Info.this;
         }
      }
   }
}
