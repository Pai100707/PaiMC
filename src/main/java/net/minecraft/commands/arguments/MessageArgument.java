package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
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

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
   static final Dynamic2CommandExceptionType TOO_LONG = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("argument.message.too_long", new Object[]{$$0, $$1})
   );

   public static MessageArgument message() {
      return new MessageArgument();
   }

   public static Component getMessage(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      MessageArgument.Message $$2 = (MessageArgument.Message)$$0.getArgument($$1, MessageArgument.Message.class);
      return $$2.resolveComponent((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static void resolveChatMessage(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1, Consumer<PlayerChatMessage> $$2) throws CommandSyntaxException {
      MessageArgument.Message $$3 = (MessageArgument.Message)$$0.getArgument($$1, MessageArgument.Message.class);
      net.minecraft.commands.CommandSourceStack $$4 = (net.minecraft.commands.CommandSourceStack)$$0.getSource();
      Component $$5 = $$3.resolveComponent($$4);
      net.minecraft.commands.CommandSigningContext $$6 = $$4.getSigningContext();
      PlayerChatMessage $$7 = $$6.getArgument($$1);
      if ($$7 != null) {
         resolveSignedMessage($$2, $$4, $$7.withUnsignedContent($$5));
      } else {
         resolveDisguisedMessage($$2, $$4, PlayerChatMessage.system($$3.text).withUnsignedContent($$5));
      }
   }

   private static void resolveSignedMessage(Consumer<PlayerChatMessage> $$0, net.minecraft.commands.CommandSourceStack $$1, PlayerChatMessage $$2) {
      MinecraftServer $$3 = $$1.getServer();
      CompletableFuture<FilteredText> $$4 = filterPlainText($$1, $$2);
      Component $$5 = $$3.getChatDecorator().decorate($$1.getPlayer(), $$2.decoratedContent());
      $$1.getChatMessageChainer().append($$4, $$3x -> {
         PlayerChatMessage $$4x = $$2.withUnsignedContent($$5).filter($$3x.mask());
         $$0.accept($$4x);
      });
   }

   private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> $$0, net.minecraft.commands.CommandSourceStack $$1, PlayerChatMessage $$2) {
      ChatDecorator $$3 = $$1.getServer().getChatDecorator();
      Component $$4 = $$3.decorate($$1.getPlayer(), $$2.decoratedContent());
      $$0.accept($$2.withUnsignedContent($$4));
   }

   private static CompletableFuture<FilteredText> filterPlainText(net.minecraft.commands.CommandSourceStack $$0, PlayerChatMessage $$1) {
      ServerPlayer $$2 = $$0.getPlayer();
      return $$2 != null && $$1.hasSignatureFrom($$2.getUUID())
         ? $$2.getTextFilter().processStreamMessage($$1.signedContent())
         : CompletableFuture.completedFuture(FilteredText.passThrough($$1.signedContent()));
   }

   public MessageArgument.Message parse(StringReader $$0) throws CommandSyntaxException {
      return MessageArgument.Message.parseText($$0, true);
   }

   public <S> MessageArgument.Message parse(StringReader $$0, S $$1) throws CommandSyntaxException {
      return MessageArgument.Message.parseText($$0, EntitySelectorParser.allowSelectors($$1));
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public record Message(String text, MessageArgument.Part[] parts) {

      Component resolveComponent(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
         return this.toComponent($$0, $$0.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS));
      }

      public Component toComponent(net.minecraft.commands.CommandSourceStack $$0, boolean $$1) throws CommandSyntaxException {
         if (this.parts.length != 0 && $$1) {
            MutableComponent $$2 = Component.literal(this.text.substring(0, this.parts[0].start()));
            int $$3 = this.parts[0].start();

            for (MessageArgument.Part $$4 : this.parts) {
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
         } else {
            return Component.literal(this.text);
         }
      }

      public static MessageArgument.Message parseText(StringReader $$0, boolean $$1) throws CommandSyntaxException {
         if ($$0.getRemainingLength() > 256) {
            throw MessageArgument.TOO_LONG.create($$0.getRemainingLength(), 256);
         } else {
            String $$2 = $$0.getRemaining();
            if (!$$1) {
               $$0.setCursor($$0.getTotalLength());
               return new MessageArgument.Message($$2, new MessageArgument.Part[0]);
            } else {
               List<MessageArgument.Part> $$3 = Lists.newArrayList();
               int $$4 = $$0.getCursor();

               while (true) {
                  int $$5;
                  EntitySelector $$7;
                  while (true) {
                     if (!$$0.canRead()) {
                        return new MessageArgument.Message($$2, $$3.toArray(new MessageArgument.Part[0]));
                     }

                     if ($$0.peek() == '@') {
                        $$5 = $$0.getCursor();

                        try {
                           EntitySelectorParser $$6 = new EntitySelectorParser($$0, true);
                           $$7 = $$6.parse();
                           break;
                        } catch (CommandSyntaxException var8) {
                           if (var8.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE
                              && var8.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                              throw var8;
                           }

                           $$0.setCursor($$5 + 1);
                        }
                     } else {
                        $$0.skip();
                     }
                  }

                  $$3.add(new MessageArgument.Part($$5 - $$4, $$0.getCursor() - $$4, $$7));
               }
            }
         }
      }
   }

   public record Part(int start, int end, EntitySelector selector) {
      public Component toComponent(net.minecraft.commands.CommandSourceStack $$0) throws CommandSyntaxException {
         return EntitySelector.joinNames(this.selector.findEntities($$0));
      }
   }
}
