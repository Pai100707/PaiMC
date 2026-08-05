/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AngleArgument
implements ArgumentType<SingleAngle> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType((Message)Component.translatable("argument.angle.incomplete"));
    public static final SimpleCommandExceptionType ERROR_INVALID_ANGLE = new SimpleCommandExceptionType((Message)Component.translatable("argument.angle.invalid"));

    public static AngleArgument angle() {
        return new AngleArgument();
    }

    public static float getAngle(CommandContext<CommandSourceStack> $$0, String $$1) {
        return ((SingleAngle)$$0.getArgument($$1, SingleAngle.class)).getAngle((CommandSourceStack)$$0.getSource());
    }

    public SingleAngle parse(StringReader $$0) throws CommandSyntaxException {
        float $$2;
        if (!$$0.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)$$0);
        }
        boolean $$1 = WorldCoordinate.isRelative($$0);
        float f = $$2 = $$0.canRead() && $$0.peek() != ' ' ? $$0.readFloat() : 0.0f;
        if (Float.isNaN($$2) || Float.isInfinite($$2)) {
            throw ERROR_INVALID_ANGLE.createWithContext((ImmutableStringReader)$$0);
        }
        return new SingleAngle($$2, $$1);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static final class SingleAngle {
        private final float angle;
        private final boolean isRelative;

        SingleAngle(float $$0, boolean $$1) {
            this.angle = $$0;
            this.isRelative = $$1;
        }

        public float getAngle(CommandSourceStack $$0) {
            return Mth.wrapDegrees(this.isRelative ? this.angle + $$0.getRotation().y : this.angle);
        }
    }
}

