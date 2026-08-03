package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SaveOnCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_ON = new SimpleCommandExceptionType(Component.translatable("commands.save.alreadyOn"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("save-on").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
            .executes($$0x -> {
               CommandSourceStack $$1 = (CommandSourceStack)$$0x.getSource();
               boolean $$2 = $$1.getServer().setAutoSave(true);
               if (!$$2) {
                  throw ERROR_ALREADY_ON.create();
               } else {
                  $$1.sendSuccess(() -> Component.translatable("commands.save.enabled"), true);
                  return 1;
               }
            })
      );
   }
}
