package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class PublishCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.publish.failed"));
   private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.publish.alreadyPublished", new Object[]{$$0})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("publish")
                  .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
               .executes($$0x -> publish((CommandSourceStack)$$0x.getSource(), HttpUtil.getAvailablePort(), false, null)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("allowCommands", BoolArgumentType.bool())
                     .executes(
                        $$0x -> publish(
                           (CommandSourceStack)$$0x.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool($$0x, "allowCommands"), null
                        )
                     ))
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode())
                           .executes(
                              $$0x -> publish(
                                 (CommandSourceStack)$$0x.getSource(),
                                 HttpUtil.getAvailablePort(),
                                 BoolArgumentType.getBool($$0x, "allowCommands"),
                                 GameModeArgument.getGameMode($$0x, "gamemode")
                              )
                           ))
                        .then(
                           Commands.argument("port", IntegerArgumentType.integer(0, 65535))
                              .executes(
                                 $$0x -> publish(
                                    (CommandSourceStack)$$0x.getSource(),
                                    IntegerArgumentType.getInteger($$0x, "port"),
                                    BoolArgumentType.getBool($$0x, "allowCommands"),
                                    GameModeArgument.getGameMode($$0x, "gamemode")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int publish(CommandSourceStack $$0, int $$1, boolean $$2, GameType $$3) throws CommandSyntaxException {
      if ($$0.getServer().isPublished()) {
         throw ERROR_ALREADY_PUBLISHED.create($$0.getServer().getPort());
      } else if (!$$0.getServer().publishServer($$3, $$2, $$1)) {
         throw ERROR_FAILED.create();
      } else {
         $$0.sendSuccess(() -> getSuccessMessage($$1), true);
         return $$1;
      }
   }

   public static MutableComponent getSuccessMessage(int $$0) {
      Component $$1 = ComponentUtils.copyOnClickText(String.valueOf($$0));
      return Component.translatable("commands.publish.started", new Object[]{$$1});
   }
}
