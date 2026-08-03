package net.minecraft.world.entity.variant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public record MoonBrightnessCheck(Doubles range) implements SpawnCondition {
   public static final MapCodec<MoonBrightnessCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Doubles.CODEC.fieldOf("range").forGetter(MoonBrightnessCheck::range)).apply($$0, MoonBrightnessCheck::new)
   );

   public boolean test(SpawnContext $$0) {
      MoonPhase $$1 = (MoonPhase)$$0.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, Vec3.atCenterOf($$0.pos()));
      float $$2 = DimensionType.MOON_BRIGHTNESS_PER_PHASE[$$1.index()];
      return this.range.matches($$2);
   }

   @Override
   public MapCodec<MoonBrightnessCheck> codec() {
      return MAP_CODEC;
   }
}
