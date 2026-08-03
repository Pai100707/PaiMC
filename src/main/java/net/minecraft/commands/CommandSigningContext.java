package net.minecraft.commands;

import java.util.Map;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jspecify.annotations.Nullable;

public interface CommandSigningContext {
   net.minecraft.commands.CommandSigningContext ANONYMOUS = new net.minecraft.commands.CommandSigningContext() {
      @Nullable
      @Override
      public PlayerChatMessage getArgument(String $$0) {
         return null;
      }
   };

   @Nullable
   PlayerChatMessage getArgument(String var1);

   public record SignedArguments(Map<String, PlayerChatMessage> arguments) implements net.minecraft.commands.CommandSigningContext {
      @Nullable
      @Override
      public PlayerChatMessage getArgument(String $$0) {
         return this.arguments.get($$0);
      }
   }
}
