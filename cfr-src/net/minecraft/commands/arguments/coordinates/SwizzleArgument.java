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
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class SwizzleArgument
implements ArgumentType<EnumSet<Direction.Axis>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("xyz", "x");
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType((Message)Component.translatable("arguments.swizzle.invalid"));

    public static SwizzleArgument swizzle() {
        return new SwizzleArgument();
    }

    public static EnumSet<Direction.Axis> getSwizzle(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (EnumSet)$$0.getArgument($$1, EnumSet.class);
    }

    /*
     * WARNING - void declaration
     */
    public EnumSet<Direction.Axis> parse(StringReader $$0) throws CommandSyntaxException {
        EnumSet<Direction.Axis> $$1 = EnumSet.noneOf(Direction.Axis.class);
        while ($$0.canRead() && $$0.peek() != ' ') {
            void $$6;
            char $$2 = $$0.read();
            switch ($$2) {
                case 'x': {
                    Direction.Axis $$3 = Direction.Axis.X;
                    break;
                }
                case 'y': {
                    Direction.Axis $$4 = Direction.Axis.Y;
                    break;
                }
                case 'z': {
                    Direction.Axis $$5 = Direction.Axis.Z;
                    break;
                }
                default: {
                    throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0);
                }
            }
            if ($$1.contains($$6)) {
                throw ERROR_INVALID.createWithContext((ImmutableStringReader)$$0);
            }
            $$1.add((Direction.Axis)$$6);
        }
        return $$1;
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

