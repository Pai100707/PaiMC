/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public enum BooleanModifier implements AttributeModifier<Boolean, Boolean>
{
    AND,
    NAND,
    OR,
    NOR,
    XOR,
    XNOR;


    @Override
    public Boolean apply(Boolean $$0, Boolean $$1) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> $$1 != false && $$0 != false;
            case 1 -> $$1 == false || $$0 == false;
            case 2 -> $$1 != false || $$0 != false;
            case 3 -> $$1 == false && $$0 == false;
            case 4 -> $$1 ^ $$0;
            case 5 -> $$1 == $$0;
        };
    }

    @Override
    public Codec<Boolean> argumentCodec(EnvironmentAttribute<Boolean> $$0) {
        return Codec.BOOL;
    }

    @Override
    public LerpFunction<Boolean> argumentKeyframeLerp(EnvironmentAttribute<Boolean> $$0) {
        return LerpFunction.ofConstant();
    }

    @Override
    public /* synthetic */ Object apply(Object object, Object object2) {
        return this.apply((Boolean)object, (Boolean)object2);
    }
}

