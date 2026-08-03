package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ApplyEntityImpulse(Vec3 direction, Vec3 coordinateScale, LevelBasedValue magnitude) implements EnchantmentEntityEffect {
   public static final MapCodec<ApplyEntityImpulse> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Vec3.CODEC.fieldOf("direction").forGetter(ApplyEntityImpulse::direction),
            Vec3.CODEC.fieldOf("coordinate_scale").forGetter(ApplyEntityImpulse::coordinateScale),
            LevelBasedValue.CODEC.fieldOf("magnitude").forGetter(ApplyEntityImpulse::magnitude)
         )
         .apply($$0, ApplyEntityImpulse::new)
   );
   private static final int POST_IMPULSE_CONTEXT_RESET_GRACE_TIME_TICKS = 10;

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      Vec3 $$5 = $$3.getLookAngle();
      Vec3 $$6 = $$5.addLocalCoordinates(this.direction).multiply(this.coordinateScale).scale(this.magnitude.calculate($$1));
      $$3.addDeltaMovement($$6);
      $$3.hurtMarked = true;
      $$3.needsSync = true;
      if ($$3 instanceof Player $$7) {
         $$7.applyPostImpulseGraceTime(10);
      }
   }

   @Override
   public MapCodec<ApplyEntityImpulse> codec() {
      return CODEC;
   }
}
