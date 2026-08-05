/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class RangedAttribute
extends Attribute {
    private final double minValue;
    private final double maxValue;

    public RangedAttribute(String $$0, double $$1, double $$2, double $$3) {
        super($$0, $$1);
        this.minValue = $$2;
        this.maxValue = $$3;
        if ($$2 > $$3) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        }
        if ($$1 < $$2) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        }
        if ($$1 > $$3) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }
    }

    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    @Override
    public double sanitizeValue(double $$0) {
        if (Double.isNaN($$0)) {
            return this.minValue;
        }
        return Mth.clamp($$0, this.minValue, this.maxValue);
    }
}

