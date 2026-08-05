/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;

public class ServerPackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("serverpack").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("push").then(((RequiredArgumentBuilder)Commands.argument("url", StringArgumentType.string()).then(((RequiredArgumentBuilder)Commands.argument("uuid", UuidArgument.uuid()).then(Commands.argument("hash", StringArgumentType.word()).executes($$0 -> ServerPackCommand.pushPack((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"url"), Optional.of(UuidArgument.getUuid((CommandContext<CommandSourceStack>)$$0, "uuid")), Optional.of(StringArgumentType.getString((CommandContext)$$0, (String)"hash")))))).executes($$0 -> ServerPackCommand.pushPack((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"url"), Optional.of(UuidArgument.getUuid((CommandContext<CommandSourceStack>)$$0, "uuid")), Optional.empty())))).executes($$0 -> ServerPackCommand.pushPack((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"url"), Optional.empty(), Optional.empty()))))).then(Commands.literal("pop").then(Commands.argument("uuid", UuidArgument.uuid()).executes($$0 -> ServerPackCommand.popPack((CommandSourceStack)$$0.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)$$0, "uuid"))))));
    }

    private static void sendToAllConnections(CommandSourceStack $$0, Packet<?> $$12) {
        $$0.getServer().getConnection().getConnections().forEach($$1 -> $$1.send($$12));
    }

    private static int pushPack(CommandSourceStack $$0, String $$1, Optional<UUID> $$2, Optional<String> $$3) {
        UUID $$4 = $$2.orElseGet(() -> UUID.nameUUIDFromBytes($$1.getBytes(StandardCharsets.UTF_8)));
        String $$5 = $$3.orElse("");
        ClientboundResourcePackPushPacket $$6 = new ClientboundResourcePackPushPacket($$4, $$1, $$5, false, null);
        ServerPackCommand.sendToAllConnections($$0, $$6);
        return 0;
    }

    private static int popPack(CommandSourceStack $$0, UUID $$1) {
        ClientboundResourcePackPopPacket $$2 = new ClientboundResourcePackPopPacket(Optional.of($$1));
        ServerPackCommand.sendToAllConnections($$0, $$2);
        return 0;
    }
}

