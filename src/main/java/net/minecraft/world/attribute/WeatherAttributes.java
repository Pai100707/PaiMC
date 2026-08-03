package net.minecraft.world.attribute;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.modifier.ColorModifier;
import net.minecraft.world.attribute.modifier.FloatModifier;
import net.minecraft.world.attribute.modifier.FloatWithAlpha;
import net.minecraft.world.level.Level;
import net.minecraft.world.timeline.Timelines;

public class WeatherAttributes {
   public static final net.minecraft.world.attribute.EnvironmentAttributeMap RAIN = net.minecraft.world.attribute.EnvironmentAttributeMap.builder()
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.SKY_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.6F, 0.75F))
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.FOG_COLOR, ColorModifier.MULTIPLY_RGB, ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 0.6F))
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.CLOUD_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.24F, 0.5F))
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_LEVEL, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(4.0F, 0.3125F))
      .modify(
         net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_COLOR, ColorModifier.ALPHA_BLEND, ARGB.color(0.3125F, Timelines.NIGHT_SKY_LIGHT_COLOR)
      )
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_FACTOR, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(0.24F, 0.3125F))
      .set(net.minecraft.world.attribute.EnvironmentAttributes.STAR_BRIGHTNESS, 0.0F)
      .modify(
         net.minecraft.world.attribute.EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ColorModifier.MULTIPLY_ARGB, ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 0.6F)
      )
      .set(net.minecraft.world.attribute.EnvironmentAttributes.BEES_STAY_IN_HIVE, true)
      .build();
   public static final net.minecraft.world.attribute.EnvironmentAttributeMap THUNDER = net.minecraft.world.attribute.EnvironmentAttributeMap.builder()
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.SKY_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.24F, 0.94F))
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.FOG_COLOR, ColorModifier.MULTIPLY_RGB, ARGB.colorFromFloat(1.0F, 0.25F, 0.25F, 0.3F))
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.CLOUD_COLOR, ColorModifier.BLEND_TO_GRAY, new ColorModifier.BlendToGray(0.095F, 0.94F))
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_LEVEL, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(4.0F, 0.52734375F))
      .modify(
         net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_COLOR,
         ColorModifier.ALPHA_BLEND,
         ARGB.color(0.52734375F, Timelines.NIGHT_SKY_LIGHT_COLOR)
      )
      .modify(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_FACTOR, FloatModifier.ALPHA_BLEND, new FloatWithAlpha(0.24F, 0.52734375F))
      .set(net.minecraft.world.attribute.EnvironmentAttributes.STAR_BRIGHTNESS, 0.0F)
      .modify(
         net.minecraft.world.attribute.EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ColorModifier.MULTIPLY_ARGB, ARGB.colorFromFloat(1.0F, 0.25F, 0.25F, 0.3F)
      )
      .set(net.minecraft.world.attribute.EnvironmentAttributes.BEES_STAY_IN_HIVE, true)
      .build();
   private static final Set<net.minecraft.world.attribute.EnvironmentAttribute<?>> WEATHER_ATTRIBUTES = Sets.union(RAIN.keySet(), THUNDER.keySet());

   public static void addBuiltinLayers(
      net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder $$0, net.minecraft.world.attribute.WeatherAttributes.WeatherAccess $$1
   ) {
      for (net.minecraft.world.attribute.EnvironmentAttribute<?> $$2 : WEATHER_ATTRIBUTES) {
         addLayer($$0, $$1, $$2);
      }
   }

   private static <Value> void addLayer(
      net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder $$0,
      net.minecraft.world.attribute.WeatherAttributes.WeatherAccess $$1,
      net.minecraft.world.attribute.EnvironmentAttribute<Value> $$2
   ) {
      net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?> $$3 = RAIN.get($$2);
      net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?> $$4 = THUNDER.get($$2);
      $$0.addTimeBasedLayer($$2, ($$4x, $$5) -> {
         float $$6 = $$1.thunderLevel();
         float $$7 = $$1.rainLevel() - $$6;
         if ($$3 != null && $$7 > 0.0F) {
            Value $$8 = $$3.applyModifier((Value)$$4x);
            $$4x = $$2.type().stateChangeLerp().apply($$7, (Value)$$4x, $$8);
         }

         if ($$4 != null && $$6 > 0.0F) {
            Value $$9 = $$4.applyModifier((Value)$$4x);
            $$4x = $$2.type().stateChangeLerp().apply($$6, (Value)$$4x, $$9);
         }

         return (Value)$$4x;
      });
   }

   public interface WeatherAccess {
      static net.minecraft.world.attribute.WeatherAttributes.WeatherAccess from(final Level $$0) {
         return new net.minecraft.world.attribute.WeatherAttributes.WeatherAccess() {
            @Override
            public float rainLevel() {
               return $$0.getRainLevel(1.0F);
            }

            @Override
            public float thunderLevel() {
               return $$0.getThunderLevel(1.0F);
            }
         };
      }

      float rainLevel();

      float thunderLevel();
   }
}
