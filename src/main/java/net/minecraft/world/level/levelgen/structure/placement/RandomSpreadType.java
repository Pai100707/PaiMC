package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum RandomSpreadType implements StringRepresentable {
   LINEAR("linear"),
   TRIANGULAR("triangular");

   public static final Codec<RandomSpreadType> CODEC = StringRepresentable.fromEnum(RandomSpreadType::values);
   private final String id;

   private RandomSpreadType(final String $$0) {
      this.id = $$0;
   }

   public String getSerializedName() {
      return this.id;
   }

   public int evaluate(RandomSource $$0, int $$1) {
      return switch (this) {
         case LINEAR -> $$0.nextInt($$1);
         case TRIANGULAR -> ($$0.nextInt($$1) + $$0.nextInt($$1)) / 2;
      };
   }
}
