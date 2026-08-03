package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class SeedCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0, boolean $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("seed")
               .requires(Commands.hasPermission($$1 ? Commands.LEVEL_GAMEMASTERS : Commands.LEVEL_ALL)))
            .executes($$0x -> {
               long $$1x = ((CommandSourceStack)$$0x.getSource()).getLevel().getSeed();
               Component $$2 = ComponentUtils.copyOnClickText(String.valueOf($$1x));
               ((CommandSourceStack)$$0x.getSource()).sendSuccess(() -> Component.translatable("commands.seed.success", new Object[]{$$2}), false);
               return (int)$$1x;
            })
      );
   }
}
