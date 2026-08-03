package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DamageScaling implements StringRepresentable {
   NEVER("never"),
   WHEN_CAUSED_BY_LIVING_NON_PLAYER("when_caused_by_living_non_player"),
   ALWAYS("always");

   public static final Codec<net.minecraft.world.damagesource.DamageScaling> CODEC = StringRepresentable.fromEnum(
      net.minecraft.world.damagesource.DamageScaling::values
   );
   private final String id;

   private DamageScaling(final String $$0) {
      this.id = $$0;
   }

   public String getSerializedName() {
      return this.id;
   }
}
