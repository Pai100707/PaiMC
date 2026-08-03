package net.minecraft.world.entity;

import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public record ConversionParams(net.minecraft.world.entity.ConversionType type, boolean keepEquipment, boolean preserveCanPickUpLoot, @Nullable PlayerTeam team) {
   public static net.minecraft.world.entity.ConversionParams single(net.minecraft.world.entity.Mob $$0, boolean $$1, boolean $$2) {
      return new net.minecraft.world.entity.ConversionParams(net.minecraft.world.entity.ConversionType.SINGLE, $$1, $$2, $$0.getTeam());
   }

   @FunctionalInterface
   public interface AfterConversion<T extends net.minecraft.world.entity.Mob> {
      void finalizeConversion(T var1);
   }
}
