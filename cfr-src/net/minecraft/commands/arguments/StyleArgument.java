/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;

public class StyleArgument
extends ParserBasedArgument<Style> {
    private static final Collection<String> EXAMPLES = List.of("{bold: true}", "{color: 'red'}", "{}");
    public static final DynamicCommandExceptionType ERROR_INVALID_STYLE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.style.invalid", $$0));
    private static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
    private static final CommandArgumentParser<Tag> TAG_PARSER = SnbtGrammar.createParser(OPS);

    private StyleArgument(HolderLookup.Provider $$0) {
        super(TAG_PARSER.withCodec($$0.createSerializationContext(OPS), TAG_PARSER, Style.Serializer.CODEC, ERROR_INVALID_STYLE));
    }

    public static Style getStyle(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Style)$$0.getArgument($$1, Style.class);
    }

    public static StyleArgument style(CommandBuildContext $$0) {
        return new StyleArgument($$0);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

