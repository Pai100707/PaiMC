package net.minecraft.server.dialog.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record NumberRangeInput(int width, Component label, String labelFormat, NumberRangeInput.RangeInfo rangeInfo) implements InputControl {
   public static final MapCodec<NumberRangeInput> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(NumberRangeInput::width),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(NumberRangeInput::label),
            Codec.STRING.optionalFieldOf("label_format", "options.generic_value").forGetter(NumberRangeInput::labelFormat),
            NumberRangeInput.RangeInfo.MAP_CODEC.forGetter(NumberRangeInput::rangeInfo)
         )
         .apply($$0, NumberRangeInput::new)
   );

   @Override
   public MapCodec<NumberRangeInput> mapCodec() {
      return MAP_CODEC;
   }

   public Component computeLabel(String $$0) {
      return Component.translatable(this.labelFormat, new Object[]{this.label, $$0});
   }

   public record RangeInfo(float start, float end, Optional<Float> initial, Optional<Float> step) {
      public static final MapCodec<NumberRangeInput.RangeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  Codec.FLOAT.fieldOf("start").forGetter(NumberRangeInput.RangeInfo::start),
                  Codec.FLOAT.fieldOf("end").forGetter(NumberRangeInput.RangeInfo::end),
                  Codec.FLOAT.optionalFieldOf("initial").forGetter(NumberRangeInput.RangeInfo::initial),
                  ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("step").forGetter(NumberRangeInput.RangeInfo::step)
               )
               .apply($$0, NumberRangeInput.RangeInfo::new)
         )
         .validate($$0 -> {
            if ($$0.initial.isPresent()) {
               double $$1 = $$0.initial.get().floatValue();
               double $$2 = Math.min($$0.start, $$0.end);
               double $$3 = Math.max($$0.start, $$0.end);
               if ($$1 < $$2 || $$1 > $$3) {
                  return DataResult.error(() -> "Initial value " + $$1 + " is outside of range [" + $$2 + ", " + $$3 + "]");
               }
            }

            return DataResult.success($$0);
         });

      public float computeScaledValue(float $$0) {
         float $$1 = Mth.lerp($$0, this.start, this.end);
         if (this.step.isEmpty()) {
            return $$1;
         } else {
            float $$2 = this.step.get();
            float $$3 = this.initialScaledValue();
            float $$4 = $$1 - $$3;
            int $$5 = Math.round($$4 / $$2);
            float $$6 = $$3 + $$5 * $$2;
            if (!this.isOutOfRange($$6)) {
               return $$6;
            } else {
               int $$7 = $$5 - Mth.sign($$5);
               return $$3 + $$7 * $$2;
            }
         }
      }

      private boolean isOutOfRange(float $$0) {
         float $$1 = this.scaledValueToSlider($$0);
         return $$1 < 0.0 || $$1 > 1.0;
      }

      private float initialScaledValue() {
         return this.initial.isPresent() ? this.initial.get() : (this.start + this.end) / 2.0F;
      }

      public float initialSliderValue() {
         float $$0 = this.initialScaledValue();
         return this.scaledValueToSlider($$0);
      }

      private float scaledValueToSlider(float $$0) {
         return this.start == this.end ? 0.5F : Mth.inverseLerp($$0, this.start, this.end);
      }
   }
}
