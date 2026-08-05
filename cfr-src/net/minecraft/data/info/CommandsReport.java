/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.brigadier.CommandDispatcher
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class CommandsReport
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CommandsReport(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        this.output = $$0;
        this.registries = $$1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        Path $$1 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
        return this.registries.thenCompose($$2 -> {
            CommandDispatcher<CommandSourceStack> $$3 = new Commands(Commands.CommandSelection.ALL, Commands.createValidationContext($$2)).getDispatcher();
            return DataProvider.saveStable($$0, (JsonElement)ArgumentUtils.serializeNodeToJson($$3, $$3.getRoot()), $$1);
        });
    }

    @Override
    public final String getName() {
        return "Command Syntax";
    }
}

