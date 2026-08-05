/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class ComponentArgument
extends ParserBasedArgument<Component> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "'hello world'", "\"\"", "{text:\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.component.invalid", $$0));
    private static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
    private static final CommandArgumentParser<Tag> TAG_PARSER = SnbtGrammar.createParser(OPS);

    private ComponentArgument(HolderLookup.Provider $$0) {
        super(TAG_PARSER.withCodec($$0.createSerializationContext(OPS), TAG_PARSER, ComponentSerialization.CODEC, ERROR_INVALID_COMPONENT));
    }

    public static Component getRawComponent(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Component)$$0.getArgument($$1, Component.class);
    }

    public static Component getResolvedComponent(CommandContext<CommandSourceStack> $$0, String $$1, @Nullable Entity $$2) throws CommandSyntaxException {
        return ComponentUtils.updateForEntity((CommandSourceStack)$$0.getSource(), ComponentArgument.getRawComponent($$0, $$1), $$2, 0);
    }

    public static Component getResolvedComponent(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ComponentArgument.getResolvedComponent($$0, $$1, ((CommandSourceStack)$$0.getSource()).getEntity());
    }

    public static ComponentArgument textComponent(CommandBuildContext $$0) {
        return new ComponentArgument($$0);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

