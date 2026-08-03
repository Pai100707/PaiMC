package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;

public class GameRuleCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      final LiteralArgumentBuilder<CommandSourceStack> $$2 = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("gamerule")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
      new GameRules($$1.enabledFeatures()).visitGameRuleTypes(new GameRuleTypeVisitor() {
         public <T> void visit(GameRule<T> $$0) {
            LiteralArgumentBuilder<CommandSourceStack> $$1x = Commands.literal($$0.id());
            LiteralArgumentBuilder<CommandSourceStack> $$2x = Commands.literal($$0.getIdentifier().toString());
            ((LiteralArgumentBuilder)$$2.then(GameRuleCommand.buildRuleArguments($$0, $$1x))).then(GameRuleCommand.buildRuleArguments($$0, $$2x));
         }
      });
      $$0.register($$2);
   }

   static <T> LiteralArgumentBuilder<CommandSourceStack> buildRuleArguments(GameRule<T> $$0, LiteralArgumentBuilder<CommandSourceStack> $$1) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)$$1.executes($$1x -> queryRule((CommandSourceStack)$$1x.getSource(), $$0)))
         .then(Commands.argument("value", $$0.argument()).executes($$1x -> setRule($$1x, $$0)));
   }

   private static <T> int setRule(CommandContext<CommandSourceStack> $$0, GameRule<T> $$1) {
      CommandSourceStack $$2 = (CommandSourceStack)$$0.getSource();
      T $$3 = (T)$$0.getArgument("value", $$1.valueClass());
      $$2.getLevel().getGameRules().set($$1, $$3, ((CommandSourceStack)$$0.getSource()).getServer());
      $$2.sendSuccess(() -> Component.translatable("commands.gamerule.set", new Object[]{$$1.id(), $$1.serialize($$3)}), true);
      return $$1.getCommandResult($$3);
   }

   private static <T> int queryRule(CommandSourceStack $$0, GameRule<T> $$1) {
      T $$2 = (T)$$0.getLevel().getGameRules().get($$1);
      $$0.sendSuccess(() -> Component.translatable("commands.gamerule.query", new Object[]{$$1.id(), $$1.serialize($$2)}), false);
      return $$1.getCommandResult($$2);
   }
}
