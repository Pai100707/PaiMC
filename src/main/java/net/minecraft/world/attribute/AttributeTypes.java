package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.TriState;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.MoonPhase;

public interface AttributeTypes {
   net.minecraft.world.attribute.AttributeType<Boolean> BOOLEAN = register(
      "boolean", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(Codec.BOOL, AttributeModifier.BOOLEAN_LIBRARY)
   );
   net.minecraft.world.attribute.AttributeType<TriState> TRI_STATE = register(
      "tri_state", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(TriState.CODEC)
   );
   net.minecraft.world.attribute.AttributeType<Float> FLOAT = register(
      "float",
      net.minecraft.world.attribute.AttributeType.ofInterpolated(
         Codec.FLOAT, AttributeModifier.FLOAT_LIBRARY, net.minecraft.world.attribute.LerpFunction.ofFloat()
      )
   );
   net.minecraft.world.attribute.AttributeType<Float> ANGLE_DEGREES = register(
      "angle_degrees",
      net.minecraft.world.attribute.AttributeType.ofInterpolated(
         Codec.FLOAT,
         AttributeModifier.FLOAT_LIBRARY,
         net.minecraft.world.attribute.LerpFunction.ofFloat(),
         net.minecraft.world.attribute.LerpFunction.ofDegrees(90.0F)
      )
   );
   net.minecraft.world.attribute.AttributeType<Integer> RGB_COLOR = register(
      "rgb_color",
      net.minecraft.world.attribute.AttributeType.ofInterpolated(
         ExtraCodecs.STRING_RGB_COLOR, AttributeModifier.RGB_COLOR_LIBRARY, net.minecraft.world.attribute.LerpFunction.ofColor()
      )
   );
   net.minecraft.world.attribute.AttributeType<Integer> ARGB_COLOR = register(
      "argb_color",
      net.minecraft.world.attribute.AttributeType.ofInterpolated(
         ExtraCodecs.STRING_ARGB_COLOR, AttributeModifier.ARGB_COLOR_LIBRARY, net.minecraft.world.attribute.LerpFunction.ofColor()
      )
   );
   net.minecraft.world.attribute.AttributeType<MoonPhase> MOON_PHASE = register(
      "moon_phase", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(MoonPhase.CODEC)
   );
   net.minecraft.world.attribute.AttributeType<Activity> ACTIVITY = register(
      "activity", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(BuiltInRegistries.ACTIVITY.byNameCodec())
   );
   net.minecraft.world.attribute.AttributeType<net.minecraft.world.attribute.BedRule> BED_RULE = register(
      "bed_rule", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(net.minecraft.world.attribute.BedRule.CODEC)
   );
   net.minecraft.world.attribute.AttributeType<ParticleOptions> PARTICLE = register(
      "particle", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(ParticleTypes.CODEC)
   );
   net.minecraft.world.attribute.AttributeType<List<net.minecraft.world.attribute.AmbientParticle>> AMBIENT_PARTICLES = register(
      "ambient_particles", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(net.minecraft.world.attribute.AmbientParticle.CODEC.listOf())
   );
   net.minecraft.world.attribute.AttributeType<net.minecraft.world.attribute.BackgroundMusic> BACKGROUND_MUSIC = register(
      "background_music", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(net.minecraft.world.attribute.BackgroundMusic.CODEC)
   );
   net.minecraft.world.attribute.AttributeType<net.minecraft.world.attribute.AmbientSounds> AMBIENT_SOUNDS = register(
      "ambient_sounds", net.minecraft.world.attribute.AttributeType.ofNotInterpolated(net.minecraft.world.attribute.AmbientSounds.CODEC)
   );
   Codec<net.minecraft.world.attribute.AttributeType<?>> CODEC = BuiltInRegistries.ATTRIBUTE_TYPE.byNameCodec();

   static net.minecraft.world.attribute.AttributeType<?> bootstrap(Registry<net.minecraft.world.attribute.AttributeType<?>> $$0) {
      return BOOLEAN;
   }

   static <Value> net.minecraft.world.attribute.AttributeType<Value> register(String $$0, net.minecraft.world.attribute.AttributeType<Value> $$1) {
      Registry.register(BuiltInRegistries.ATTRIBUTE_TYPE, Identifier.withDefaultNamespace($$0), $$1);
      return $$1;
   }
}
