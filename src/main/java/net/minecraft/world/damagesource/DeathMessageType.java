package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DeathMessageType implements StringRepresentable {
   DEFAULT("default"),
   FALL_VARIANTS("fall_variants"),
   INTENTIONAL_GAME_DESIGN("intentional_game_design");

   public static final Codec<net.minecraft.world.damagesource.DeathMessageType> CODEC = StringRepresentable.fromEnum(
      net.minecraft.world.damagesource.DeathMessageType::values
   );
   private final String id;

   private DeathMessageType(final String $$0) {
      this.id = $$0;
   }

   public String getSerializedName() {
      return this.id;
   }
}
