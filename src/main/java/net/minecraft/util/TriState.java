package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;

public enum TriState implements net.minecraft.util.StringRepresentable {
   TRUE("true"),
   FALSE("false"),
   DEFAULT("default");

   public static final Codec<net.minecraft.util.TriState> CODEC = Codec.either(
         Codec.BOOL, net.minecraft.util.StringRepresentable.fromEnum(net.minecraft.util.TriState::values)
      )
      .xmap($$0 -> (net.minecraft.util.TriState)$$0.map(net.minecraft.util.TriState::from, Function.identity()), $$0 -> {
         return switch ($$0) {
            case TRUE -> Either.left(true);
            case FALSE -> Either.left(false);
            case DEFAULT -> Either.right($$0);
         };
      });
   private final String name;

   private TriState(final String $$0) {
      this.name = $$0;
   }

   public static net.minecraft.util.TriState from(boolean $$0) {
      return $$0 ? TRUE : FALSE;
   }

   public boolean toBoolean(boolean $$0) {
      return switch (this) {
         case TRUE -> true;
         case FALSE -> false;
         default -> $$0;
      };
   }

   @Override
   public String getSerializedName() {
      return this.name;
   }
}
