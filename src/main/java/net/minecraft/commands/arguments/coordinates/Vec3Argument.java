package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class Vec3Argument implements ArgumentType<Coordinates> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.pos3d.incomplete"));
   public static final SimpleCommandExceptionType ERROR_MIXED_TYPE = new SimpleCommandExceptionType(Component.translatable("argument.pos.mixed"));
   private final boolean centerCorrect;

   public Vec3Argument(boolean $$0) {
      this.centerCorrect = $$0;
   }

   public static Vec3Argument vec3() {
      return new Vec3Argument(true);
   }

   public static Vec3Argument vec3(boolean $$0) {
      return new Vec3Argument($$0);
   }

   public static Vec3 getVec3(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return ((Coordinates)$$0.getArgument($$1, Coordinates.class)).getPosition((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static Coordinates getCoordinates(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Coordinates)$$0.getArgument($$1, Coordinates.class);
   }

   public Coordinates parse(StringReader $$0) throws CommandSyntaxException {
      return (Coordinates)($$0.canRead() && $$0.peek() == '^' ? LocalCoordinates.parse($$0) : WorldCoordinates.parseDouble($$0, this.centerCorrect));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      if (!($$0.getSource() instanceof net.minecraft.commands.SharedSuggestionProvider)) {
         return Suggestions.empty();
      } else {
         String $$2 = $$1.getRemaining();
         Collection<net.minecraft.commands.SharedSuggestionProvider.TextCoordinates> $$3;
         if (!$$2.isEmpty() && $$2.charAt(0) == '^') {
            $$3 = Collections.singleton(net.minecraft.commands.SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
         } else {
            $$3 = ((net.minecraft.commands.SharedSuggestionProvider)$$0.getSource()).getAbsoluteCoordinates();
         }

         return net.minecraft.commands.SharedSuggestionProvider.suggestCoordinates($$2, $$3, $$1, net.minecraft.commands.Commands.createValidator(this::parse));
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
