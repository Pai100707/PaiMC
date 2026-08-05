/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dialog.input;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record NumberRangeInput(int width, Component label, String labelFormat, RangeInfo rangeInfo) implements InputControl
{
    public static final MapCodec<NumberRangeInput> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)200).forGetter(NumberRangeInput::width), (App)ComponentSerialization.CODEC.fieldOf("label").forGetter(NumberRangeInput::label), (App)Codec.STRING.optionalFieldOf("label_format", (Object)"options.generic_value").forGetter(NumberRangeInput::labelFormat), (App)RangeInfo.MAP_CODEC.forGetter(NumberRangeInput::rangeInfo)).apply((Applicative)$$0, NumberRangeInput::new));

    public MapCodec<NumberRangeInput> mapCodec() {
        return MAP_CODEC;
    }

    public Component computeLabel(String $$0) {
        return Component.translatable(this.labelFormat, this.label, $$0);
    }

    public record RangeInfo(float start, float end, Optional<Float> initial, Optional<Float> step) {
        public static final MapCodec<RangeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.FLOAT.fieldOf("start").forGetter(RangeInfo::start), (App)Codec.FLOAT.fieldOf("end").forGetter(RangeInfo::end), (App)Codec.FLOAT.optionalFieldOf("initial").forGetter(RangeInfo::initial), (App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("step").forGetter(RangeInfo::step)).apply((Applicative)$$0, RangeInfo::new)).validate($$0 -> {
            if ($$0.initial.isPresent()) {
                double $$1 = $$0.initial.get().floatValue();
                double $$2 = Math.min($$0.start, $$0.end);
                double $$3 = Math.max($$0.start, $$0.end);
                if ($$1 < $$2 || $$1 > $$3) {
                    return DataResult.error(() -> "Initial value " + $$1 + " is outside of range [" + $$2 + ", " + $$3 + "]");
                }
            }
            return DataResult.success((Object)$$0);
        });

        public float computeScaledValue(float $$0) {
            float $$4;
            int $$5;
            float $$1 = Mth.lerp($$0, this.start, this.end);
            if (this.step.isEmpty()) {
                return $$1;
            }
            float $$2 = this.step.get().floatValue();
            float $$3 = this.initialScaledValue();
            float $$6 = $$3 + (float)($$5 = Math.round(($$4 = $$1 - $$3) / $$2)) * $$2;
            if (!this.isOutOfRange($$6)) {
                return $$6;
            }
            int $$7 = $$5 - Mth.sign($$5);
            return $$3 + (float)$$7 * $$2;
        }

        private boolean isOutOfRange(float $$0) {
            float $$1 = this.scaledValueToSlider($$0);
            return (double)$$1 < 0.0 || (double)$$1 > 1.0;
        }

        private float initialScaledValue() {
            if (this.initial.isPresent()) {
                return this.initial.get().floatValue();
            }
            return (this.start + this.end) / 2.0f;
        }

        public float initialSliderValue() {
            float $$0 = this.initialScaledValue();
            return this.scaledValueToSlider($$0);
        }

        private float scaledValueToSlider(float $$0) {
            if (this.start == this.end) {
                return 0.5f;
            }
            return Mth.inverseLerp($$0, this.start, this.end);
        }
    }
}

