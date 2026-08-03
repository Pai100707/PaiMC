package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SaveAllCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.save.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("save-all")
                  .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
               .executes($$0x -> saveAll((CommandSourceStack)$$0x.getSource(), false)))
            .then(Commands.literal("flush").executes($$0x -> saveAll((CommandSourceStack)$$0x.getSource(), true)))
      );
   }

   private static int saveAll(CommandSourceStack $$0, boolean $$1) throws CommandSyntaxException {
      $$0.sendSuccess(() -> Component.translatable("commands.save.saving"), false);
      net.minecraft.server.MinecraftServer $$2 = $$0.getServer();
      boolean $$3 = $$2.saveEverything(true, $$1, true);
      if (!$$3) {
         throw ERROR_FAILED.create();
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.save.success"), true);
         return 1;
      }
   }
}
