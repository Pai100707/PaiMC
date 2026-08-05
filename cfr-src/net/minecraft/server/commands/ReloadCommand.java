/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class ReloadCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void reloadPacks(Collection<String> $$0, CommandSourceStack $$12) {
        $$12.getServer().reloadResources($$0).exceptionally($$1 -> {
            LOGGER.warn("Failed to execute reload", $$1);
            $$12.sendFailure(Component.translatable("commands.reload.failure"));
            return null;
        });
    }

    private static Collection<String> discoverNewPacks(PackRepository $$0, WorldData $$1, Collection<String> $$2) {
        $$0.reload();
        ArrayList $$3 = Lists.newArrayList($$2);
        List<String> $$4 = $$1.getDataConfiguration().dataPacks().getDisabled();
        for (String $$5 : $$0.getAvailableIds()) {
            if ($$4.contains($$5) || $$3.contains($$5)) continue;
            $$3.add($$5);
        }
        return $$3;
    }

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reload").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes($$0 -> {
            CommandSourceStack $$1 = (CommandSourceStack)$$0.getSource();
            MinecraftServer $$2 = $$1.getServer();
            PackRepository $$3 = $$2.getPackRepository();
            WorldData $$4 = $$2.getWorldData();
            Collection<String> $$5 = $$3.getSelectedIds();
            Collection<String> $$6 = ReloadCommand.discoverNewPacks($$3, $$4, $$5);
            $$1.sendSuccess(() -> Component.translatable("commands.reload.success"), true);
            ReloadCommand.reloadPacks($$6, $$1);
            return 0;
        }));
    }
}

