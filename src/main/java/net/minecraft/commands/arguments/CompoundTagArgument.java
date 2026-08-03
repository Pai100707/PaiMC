package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public class CompoundTagArgument implements ArgumentType<CompoundTag> {
   private static final Collection<String> EXAMPLES = Arrays.asList("{}", "{foo=bar}");

   private CompoundTagArgument() {
   }

   public static CompoundTagArgument compoundTag() {
      return new CompoundTagArgument();
   }

   public static <S> CompoundTag getCompoundTag(CommandContext<S> $$0, String $$1) {
      return (CompoundTag)$$0.getArgument($$1, CompoundTag.class);
   }

   public CompoundTag parse(StringReader $$0) throws CommandSyntaxException {
      return TagParser.parseCompoundAsArgument($$0);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
