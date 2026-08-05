/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

public class VersionCommand {
    private static final Component HEADER = Component.translatable("commands.version.header");
    private static final Component STABLE = Component.translatable("commands.version.stable.yes");
    private static final Component UNSTABLE = Component.translatable("commands.version.stable.no");

    public static void register(CommandDispatcher<CommandSourceStack> $$02, boolean $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("version").requires(Commands.hasPermission($$1 ? Commands.LEVEL_GAMEMASTERS : Commands.LEVEL_ALL))).executes($$0 -> {
            CommandSourceStack $$1 = (CommandSourceStack)$$0.getSource();
            $$1.sendSystemMessage(HEADER);
            VersionCommand.dumpVersion($$1::sendSystemMessage);
            return 1;
        }));
    }

    public static void dumpVersion(Consumer<Component> $$0) {
        WorldVersion $$1 = SharedConstants.getCurrentVersion();
        $$0.accept(Component.translatable("commands.version.id", $$1.id()));
        $$0.accept(Component.translatable("commands.version.name", $$1.name()));
        $$0.accept(Component.translatable("commands.version.data", $$1.dataVersion().version()));
        $$0.accept(Component.translatable("commands.version.series", $$1.dataVersion().series()));
        $$0.accept(Component.translatable("commands.version.protocol", $$1.protocolVersion(), "0x" + Integer.toHexString($$1.protocolVersion())));
        $$0.accept(Component.translatable("commands.version.build_time", Component.translationArg($$1.buildTime())));
        $$0.accept(Component.translatable("commands.version.pack.resource", $$1.packVersion(PackType.CLIENT_RESOURCES).toString()));
        $$0.accept(Component.translatable("commands.version.pack.data", $$1.packVersion(PackType.SERVER_DATA).toString()));
        $$0.accept($$1.stable() ? STABLE : UNSTABLE);
    }
}

