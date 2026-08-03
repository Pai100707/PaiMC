package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ItemArgument implements ArgumentType<ItemInput> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
   private final ItemParser parser;

   public ItemArgument(net.minecraft.commands.CommandBuildContext $$0) {
      this.parser = new ItemParser($$0);
   }

   public static ItemArgument item(net.minecraft.commands.CommandBuildContext $$0) {
      return new ItemArgument($$0);
   }

   public ItemInput parse(StringReader $$0) throws CommandSyntaxException {
      ItemParser.ItemResult $$1 = this.parser.parse($$0);
      return new ItemInput($$1.item(), $$1.components());
   }

   public static <S> ItemInput getItem(CommandContext<S> $$0, String $$1) {
      return (ItemInput)$$0.getArgument($$1, ItemInput.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return this.parser.fillSuggestions($$1);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
