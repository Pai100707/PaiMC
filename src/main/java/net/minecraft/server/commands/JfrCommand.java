package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

public class JfrCommand {
   private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.jfr.start.failed"));
   private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.jfr.dump.failed", new Object[]{$$0})
   );

   private JfrCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("jfr")
                  .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
               .then(Commands.literal("start").executes($$0x -> startJfr((CommandSourceStack)$$0x.getSource()))))
            .then(Commands.literal("stop").executes($$0x -> stopJfr((CommandSourceStack)$$0x.getSource())))
      );
   }

   private static int startJfr(CommandSourceStack $$0) throws CommandSyntaxException {
      Environment $$1 = Environment.from($$0.getServer());
      if (!JvmProfiler.INSTANCE.start($$1)) {
         throw START_FAILED.create();
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.jfr.started"), false);
         return 1;
      }
   }

   private static int stopJfr(CommandSourceStack $$0) throws CommandSyntaxException {
      try {
         Path $$1 = Paths.get(".").relativize(JvmProfiler.INSTANCE.stop().normalize());
         Path $$2 = $$0.getServer().isPublished() && !SharedConstants.IS_RUNNING_IN_IDE ? $$1 : $$1.toAbsolutePath();
         Component $$3 = Component.literal($$1.toString())
            .withStyle(ChatFormatting.UNDERLINE)
            .withStyle($$1x -> $$1x.withClickEvent(new CopyToClipboard($$2.toString())).withHoverEvent(new ShowText(Component.translatable("chat.copy.click"))));
         $$0.sendSuccess(() -> Component.translatable("commands.jfr.stopped", new Object[]{$$3}), false);
         return 1;
      } catch (Throwable var4) {
         throw DUMP_FAILED.create(var4.getMessage());
      }
   }
}
