package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public class SlotsArgument implements ArgumentType<SlotRange> {
   private static final Collection<String> EXAMPLES = List.of("container.*", "container.5", "weapon");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("slot.unknown", new Object[]{$$0})
   );

   public static SlotsArgument slots() {
      return new SlotsArgument();
   }

   public static SlotRange getSlots(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (SlotRange)$$0.getArgument($$1, SlotRange.class);
   }

   public SlotRange parse(StringReader $$0) throws CommandSyntaxException {
      String $$1 = net.minecraft.commands.ParserUtils.readWhile($$0, $$0x -> $$0x != ' ');
      SlotRange $$2 = SlotRanges.nameToIds($$1);
      if ($$2 == null) {
         throw ERROR_UNKNOWN_SLOT.createWithContext($$0, $$1);
      } else {
         return $$2;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.suggest(SlotRanges.allNames(), $$1);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
