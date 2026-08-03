package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public class SlotArgument implements ArgumentType<Integer> {
   private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "weapon");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("slot.unknown", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_ONLY_SINGLE_SLOT_ALLOWED = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("slot.only_single_allowed", new Object[]{$$0})
   );

   public static SlotArgument slot() {
      return new SlotArgument();
   }

   public static int getSlot(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Integer)$$0.getArgument($$1, Integer.class);
   }

   public Integer parse(StringReader $$0) throws CommandSyntaxException {
      String $$1 = net.minecraft.commands.ParserUtils.readWhile($$0, $$0x -> $$0x != ' ');
      SlotRange $$2 = SlotRanges.nameToIds($$1);
      if ($$2 == null) {
         throw ERROR_UNKNOWN_SLOT.createWithContext($$0, $$1);
      } else if ($$2.size() != 1) {
         throw ERROR_ONLY_SINGLE_SLOT_ALLOWED.createWithContext($$0, $$1);
      } else {
         return $$2.slots().getInt(0);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.suggest(SlotRanges.singleSlotNames(), $$1);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
