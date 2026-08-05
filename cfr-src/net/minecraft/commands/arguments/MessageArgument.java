/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.SignedArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.permissions.Permissions;
import org.jspecify.annotations.Nullable;

public class MessageArgument
implements SignedArgument<Message> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    static final Dynamic2CommandExceptionType TOO_LONG = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("argument.message.too_long", $$0, $$1));

    public static MessageArgument message() {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        Message $$2 = (Message)$$0.getArgument($$1, Message.class);
        return $$2.resolveComponent((CommandSourceStack)$$0.getSource());
    }

    public static void resolveChatMessage(CommandContext<CommandSourceStack> $$0, String $$1, Consumer<PlayerChatMessage> $$2) throws CommandSyntaxException {
        Message $$3 = (Message)$$0.getArgument($$1, Message.class);
        CommandSourceStack $$4 = (CommandSourceStack)$$0.getSource();
        Component $$5 = $$3.resolveComponent($$4);
        CommandSigningContext $$6 = $$4.getSigningContext();
        PlayerChatMessage $$7 = $$6.getArgument($$1);
        if ($$7 != null) {
            MessageArgument.resolveSignedMessage($$2, $$4, $$7.withUnsignedContent($$5));
        } else {
            MessageArgument.resolveDisguisedMessage($$2, $$4, PlayerChatMessage.system($$3.text).withUnsignedContent($$5));
        }
    }

    private static void resolveSignedMessage(Consumer<PlayerChatMessage> $$0, CommandSourceStack $$1, PlayerChatMessage $$2) {
        MinecraftServer $$32 = $$1.getServer();
        CompletableFuture<FilteredText> $$4 = MessageArgument.filterPlainText($$1, $$2);
        Component $$5 = $$32.getChatDecorator().decorate($$1.getPlayer(), $$2.decoratedContent());
        $$1.getChatMessageChainer().append($$4, $$3 -> {
            PlayerChatMessage $$4 = $$2.withUnsignedContent($$5).filter($$3.mask());
            $$0.accept($$4);
        });
    }

    private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> $$0, CommandSourceStack $$1, PlayerChatMessage $$2) {
        ChatDecorator $$3 = $$1.getServer().getChatDecorator();
        Component $$4 = $$3.decorate($$1.getPlayer(), $$2.decoratedContent());
        $$0.accept($$2.withUnsignedContent($$4));
    }

    private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack $$0, PlayerChatMessage $$1) {
        ServerPlayer $$2 = $$0.getPlayer();
        if ($$2 != null && $$1.hasSignatureFrom($$2.getUUID())) {
            return $$2.getTextFilter().processStreamMessage($$1.signedContent());
        }
        return CompletableFuture.completedFuture(FilteredText.passThrough($$1.signedContent()));
    }

    public Message parse(StringReader $$0) throws CommandSyntaxException {
        return Message.parseText($$0, true);
    }

    public <S> Message parse(StringReader $$0, @Nullable S $$1) throws CommandSyntaxException {
        return Message.parseText($$0, EntitySelectorParser.allowSelectors($$1));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader, @Nullable Object object) throws CommandSyntaxException {
        return this.parse(stringReader, object);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static final class Message
    extends Record {
        final String text;
        private final Part[] parts;

        public Message(String $$0, Part[] $$1) {
            this.text = $$0;
            this.parts = $$1;
        }

        Component resolveComponent(CommandSourceStack $$0) throws CommandSyntaxException {
            return this.toComponent($$0, $$0.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS));
        }

        public Component toComponent(CommandSourceStack $$0, boolean $$1) throws CommandSyntaxException {
            if (this.parts.length == 0 || !$$1) {
                return Component.literal(this.text);
            }
            MutableComponent $$2 = Component.literal(this.text.substring(0, this.parts[0].start()));
            int $$3 = this.parts[0].start();
            for (Part $$4 : this.parts) {
                Component $$5 = $$4.toComponent($$0);
                if ($$3 < $$4.start()) {
                    $$2.append(this.text.substring($$3, $$4.start()));
                }
                $$2.append($$5);
                $$3 = $$4.end();
            }
            if ($$3 < this.text.length()) {
                $$2.append(this.text.substring($$3));
            }
            return $$2;
        }

        /*
         * WARNING - void declaration
         */
        public static Message parseText(StringReader $$0, boolean $$1) throws CommandSyntaxException {
            if ($$0.getRemainingLength() > 256) {
                throw TOO_LONG.create((Object)$$0.getRemainingLength(), (Object)256);
            }
            String $$2 = $$0.getRemaining();
            if (!$$1) {
                $$0.setCursor($$0.getTotalLength());
                return new Message($$2, new Part[0]);
            }
            ArrayList $$3 = Lists.newArrayList();
            int $$4 = $$0.getCursor();
            while ($$0.canRead()) {
                if ($$0.peek() == '@') {
                    void $$9;
                    int $$5 = $$0.getCursor();
                    try {
                        EntitySelectorParser $$6 = new EntitySelectorParser($$0, true);
                        EntitySelector $$7 = $$6.parse();
                    }
                    catch (CommandSyntaxException $$8) {
                        if ($$8.getType() == EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE || $$8.getType() == EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                            $$0.setCursor($$5 + 1);
                            continue;
                        }
                        throw $$8;
                    }
                    $$3.add(new Part($$5 - $$4, $$0.getCursor() - $$4, (EntitySelector)$$9));
                    continue;
                }
                $$0.skip();
            }
            return new Message($$2, $$3.toArray(new Part[0]));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Message.class, "text;parts", "text", "parts"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Message.class, "text;parts", "text", "parts"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Message.class, "text;parts", "text", "parts"}, this, $$0);
        }

        public String text() {
            return this.text;
        }

        public Part[] parts() {
            return this.parts;
        }
    }

    public record Part(int start, int end, EntitySelector selector) {
        public Component toComponent(CommandSourceStack $$0) throws CommandSyntaxException {
            return EntitySelector.joinNames(this.selector.findEntities($$0));
        }
    }
}

