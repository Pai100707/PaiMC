/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.context.CommandContextBuilder
 *  com.mojang.brigadier.context.ParsedArgument
 *  com.mojang.brigadier.context.ParsedCommandNode
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;
import org.jspecify.annotations.Nullable;

public record SignableCommand<S>(List<Argument<S>> arguments) {
    public static <S> boolean hasSignableArguments(ParseResults<S> $$0) {
        return !SignableCommand.of($$0).arguments().isEmpty();
    }

    public static <S> SignableCommand<S> of(ParseResults<S> $$0) {
        CommandContextBuilder $$5;
        CommandContextBuilder $$2;
        String $$1 = $$0.getReader().getString();
        CommandContextBuilder $$3 = $$2 = $$0.getContext();
        List<Argument<S>> $$4 = SignableCommand.collectArguments($$1, $$3);
        while (($$5 = $$3.getChild()) != null && $$5.getRootNode() != $$2.getRootNode()) {
            $$4.addAll(SignableCommand.collectArguments($$1, $$5));
            $$3 = $$5;
        }
        return new SignableCommand<S>($$4);
    }

    private static <S> List<Argument<S>> collectArguments(String $$0, CommandContextBuilder<S> $$1) {
        ArrayList<Argument<S>> $$2 = new ArrayList<Argument<S>>();
        for (ParsedCommandNode $$3 : $$1.getNodes()) {
            ParsedArgument $$5;
            ArgumentCommandNode $$4;
            CommandNode commandNode = $$3.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !(($$4 = (ArgumentCommandNode)commandNode).getType() instanceof SignedArgument) || ($$5 = (ParsedArgument)$$1.getArguments().get($$4.getName())) == null) continue;
            String $$6 = $$5.getRange().get($$0);
            $$2.add(new Argument($$4, $$6));
        }
        return $$2;
    }

    public @Nullable Argument<S> getArgument(String $$0) {
        for (Argument<S> $$1 : this.arguments) {
            if (!$$0.equals($$1.name())) continue;
            return $$1;
        }
        return null;
    }

    public record Argument<S>(ArgumentCommandNode<S, ?> node, String value) {
        public String name() {
            return this.node.getName();
        }
    }
}

