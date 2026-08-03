package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SaveOffCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_OFF = new SimpleCommandExceptionType(Component.translatable("commands.save.alreadyOff"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("save-off").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
            .executes($$0x -> {
               CommandSourceStack $$1 = (CommandSourceStack)$$0x.getSource();
               boolean $$2 = $$1.getServer().setAutoSave(false);
               if (!$$2) {
                  throw ERROR_ALREADY_OFF.create();
               } else {
                  $$1.sendSuccess(() -> Component.translatable("commands.save.disabled"), true);
                  return 1;
               }
            })
      );
   }
}
