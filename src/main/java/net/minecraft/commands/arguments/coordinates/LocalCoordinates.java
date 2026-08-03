package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record LocalCoordinates(double left, double up, double forwards) implements Coordinates {
   public static final char PREFIX_LOCAL_COORDINATE = '^';

   @Override
   public Vec3 getPosition(net.minecraft.commands.CommandSourceStack $$0) {
      Vec3 $$1 = $$0.getAnchor().apply($$0);
      return Vec3.applyLocalCoordinatesToRotation($$0.getRotation(), new Vec3(this.left, this.up, this.forwards)).add($$1.x, $$1.y, $$1.z);
   }

   @Override
   public Vec2 getRotation(net.minecraft.commands.CommandSourceStack $$0) {
      return Vec2.ZERO;
   }

   @Override
   public boolean isXRelative() {
      return true;
   }

   @Override
   public boolean isYRelative() {
      return true;
   }

   @Override
   public boolean isZRelative() {
      return true;
   }

   public static LocalCoordinates parse(StringReader $$0) throws CommandSyntaxException {
      int $$1 = $$0.getCursor();
      double $$2 = readDouble($$0, $$1);
      if ($$0.canRead() && $$0.peek() == ' ') {
         $$0.skip();
         double $$3 = readDouble($$0, $$1);
         if ($$0.canRead() && $$0.peek() == ' ') {
            $$0.skip();
            double $$4 = readDouble($$0, $$1);
            return new LocalCoordinates($$2, $$3, $$4);
         } else {
            $$0.setCursor($$1);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext($$0);
         }
      } else {
         $$0.setCursor($$1);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext($$0);
      }
   }

   private static double readDouble(StringReader $$0, int $$1) throws CommandSyntaxException {
      if (!$$0.canRead()) {
         throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext($$0);
      } else if ($$0.peek() != '^') {
         $$0.setCursor($$1);
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext($$0);
      } else {
         $$0.skip();
         return $$0.canRead() && $$0.peek() != ' ' ? $$0.readDouble() : 0.0;
      }
   }
}
