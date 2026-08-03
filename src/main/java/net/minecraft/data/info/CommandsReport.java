package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup.Provider;

public class CommandsReport implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput output;
   private final CompletableFuture<Provider> registries;

   public CommandsReport(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      this.output = $$0;
      this.registries = $$1;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      Path $$1 = this.output.getOutputFolder(net.minecraft.data.PackOutput.Target.REPORTS).resolve("commands.json");
      return this.registries.thenCompose($$2 -> {
         CommandDispatcher<CommandSourceStack> $$3 = new Commands(CommandSelection.ALL, Commands.createValidationContext($$2)).getDispatcher();
         return net.minecraft.data.DataProvider.saveStable($$0, ArgumentUtils.serializeNodeToJson($$3, $$3.getRoot()), $$1);
      });
   }

   @Override
   public final String getName() {
      return "Command Syntax";
   }
}
