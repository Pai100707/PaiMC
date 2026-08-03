package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FileUtil;
import net.minecraft.util.FileZipper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class PerfCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.notRunning"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
      Component.translatable("commands.perf.alreadyRunning")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("perf")
                  .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
               .then(Commands.literal("start").executes($$0x -> startProfilingDedicatedServer((CommandSourceStack)$$0x.getSource()))))
            .then(Commands.literal("stop").executes($$0x -> stopProfilingDedicatedServer((CommandSourceStack)$$0x.getSource())))
      );
   }

   private static int startProfilingDedicatedServer(CommandSourceStack $$0) throws CommandSyntaxException {
      net.minecraft.server.MinecraftServer $$1 = $$0.getServer();
      if ($$1.isRecordingMetrics()) {
         throw ERROR_ALREADY_RUNNING.create();
      } else {
         Consumer<ProfileResults> $$2 = $$1x -> whenStopped($$0, $$1x);
         Consumer<Path> $$3 = $$2x -> saveResults($$0, $$2x, $$1);
         $$1.startRecordingMetrics($$2, $$3);
         $$0.sendSuccess(() -> Component.translatable("commands.perf.started"), false);
         return 0;
      }
   }

   private static int stopProfilingDedicatedServer(CommandSourceStack $$0) throws CommandSyntaxException {
      net.minecraft.server.MinecraftServer $$1 = $$0.getServer();
      if (!$$1.isRecordingMetrics()) {
         throw ERROR_NOT_RUNNING.create();
      } else {
         $$1.finishRecordingMetrics();
         return 0;
      }
   }

   private static void saveResults(CommandSourceStack $$0, Path $$1, net.minecraft.server.MinecraftServer $$2) {
      String $$3 = String.format(
         Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), $$2.getWorldData().getLevelName(), SharedConstants.getCurrentVersion().id()
      );

      String $$4;
      try {
         $$4 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, $$3, ".zip");
      } catch (IOException var11) {
         $$0.sendFailure(Component.translatable("commands.perf.reportFailed"));
         LOGGER.error("Failed to create report name", var11);
         return;
      }

      FileZipper $$7 = new FileZipper(MetricsPersister.PROFILING_RESULTS_DIR.resolve($$4));

      try {
         $$7.add(Paths.get("system.txt"), $$2.fillSystemReport(new SystemReport()).toLineSeparatedString());
         $$7.add($$1);
      } catch (Throwable var10) {
         try {
            $$7.close();
         } catch (Throwable var8) {
            var10.addSuppressed(var8);
         }

         throw var10;
      }

      $$7.close();

      try {
         FileUtils.forceDelete($$1.toFile());
      } catch (IOException var9) {
         LOGGER.warn("Failed to delete temporary profiling file {}", $$1, var9);
      }

      $$0.sendSuccess(() -> Component.translatable("commands.perf.reportSaved", new Object[]{$$4}), false);
   }

   private static void whenStopped(CommandSourceStack $$0, ProfileResults $$1) {
      if ($$1 != EmptyProfileResults.EMPTY) {
         int $$2 = $$1.getTickDuration();
         double $$3 = (double)$$1.getNanoDuration() / TimeUtil.NANOSECONDS_PER_SECOND;
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.perf.stopped", new Object[]{String.format(Locale.ROOT, "%.2f", $$3), $$2, String.format(Locale.ROOT, "%.2f", $$2 / $$3)}
            ),
            false
         );
      }
   }
}
