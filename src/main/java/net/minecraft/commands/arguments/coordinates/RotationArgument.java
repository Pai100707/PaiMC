package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.network.chat.Component;

public class RotationArgument implements ArgumentType<Coordinates> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~-5 ~5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.rotation.incomplete"));

   public static RotationArgument rotation() {
      return new RotationArgument();
   }

   public static Coordinates getRotation(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Coordinates)$$0.getArgument($$1, Coordinates.class);
   }

   public Coordinates parse(StringReader $$0) throws CommandSyntaxException {
      int $$1 = $$0.getCursor();
      if (!$$0.canRead()) {
         throw ERROR_NOT_COMPLETE.createWithContext($$0);
      } else {
         WorldCoordinate $$2 = WorldCoordinate.parseDouble($$0, false);
         if ($$0.canRead() && $$0.peek() == ' ') {
            $$0.skip();
            WorldCoordinate $$3 = WorldCoordinate.parseDouble($$0, false);
            return new WorldCoordinates($$3, $$2, new WorldCoordinate(true, 0.0));
         } else {
            $$0.setCursor($$1);
            throw ERROR_NOT_COMPLETE.createWithContext($$0);
         }
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
