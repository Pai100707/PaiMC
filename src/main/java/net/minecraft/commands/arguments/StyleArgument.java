package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Style.Serializer;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;

public class StyleArgument extends ParserBasedArgument<Style> {
   private static final Collection<String> EXAMPLES = List.of("{bold: true}", "{color: 'red'}", "{}");
   public static final DynamicCommandExceptionType ERROR_INVALID_STYLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.style.invalid", new Object[]{$$0})
   );
   private static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
   private static final CommandArgumentParser<Tag> TAG_PARSER = SnbtGrammar.createParser(OPS);

   private StyleArgument(Provider $$0) {
      super(TAG_PARSER.withCodec($$0.createSerializationContext(OPS), TAG_PARSER, Serializer.CODEC, ERROR_INVALID_STYLE));
   }

   public static Style getStyle(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Style)$$0.getArgument($$1, Style.class);
   }

   public static StyleArgument style(net.minecraft.commands.CommandBuildContext $$0) {
      return new StyleArgument($$0);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
