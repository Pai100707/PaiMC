package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public record WorldCoordinate(boolean relative, double value) {
   private static final char PREFIX_RELATIVE = '~';
   public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(Component.translatable("argument.pos.missing.double"));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(Component.translatable("argument.pos.missing.int"));

   public double get(double $$0) {
      return this.relative ? this.value + $$0 : this.value;
   }

   public static WorldCoordinate parseDouble(StringReader $$0, boolean $$1) throws CommandSyntaxException {
      if ($$0.canRead() && $$0.peek() == '^') {
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext($$0);
      } else if (!$$0.canRead()) {
         throw ERROR_EXPECTED_DOUBLE.createWithContext($$0);
      } else {
         boolean $$2 = isRelative($$0);
         int $$3 = $$0.getCursor();
         double $$4 = $$0.canRead() && $$0.peek() != ' ' ? $$0.readDouble() : 0.0;
         String $$5 = $$0.getString().substring($$3, $$0.getCursor());
         if ($$2 && $$5.isEmpty()) {
            return new WorldCoordinate(true, 0.0);
         } else {
            if (!$$5.contains(".") && !$$2 && $$1) {
               $$4 += 0.5;
            }

            return new WorldCoordinate($$2, $$4);
         }
      }
   }

   public static WorldCoordinate parseInt(StringReader $$0) throws CommandSyntaxException {
      if ($$0.canRead() && $$0.peek() == '^') {
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext($$0);
      } else if (!$$0.canRead()) {
         throw ERROR_EXPECTED_INT.createWithContext($$0);
      } else {
         boolean $$1 = isRelative($$0);
         double $$2;
         if ($$0.canRead() && $$0.peek() != ' ') {
            $$2 = $$1 ? $$0.readDouble() : $$0.readInt();
         } else {
            $$2 = 0.0;
         }

         return new WorldCoordinate($$1, $$2);
      }
   }

   public static boolean isRelative(StringReader $$0) {
      boolean $$1;
      if ($$0.peek() == '~') {
         $$1 = true;
         $$0.skip();
      } else {
         $$1 = false;
      }

      return $$1;
   }

   public boolean isRelative() {
      return this.relative;
   }
}
