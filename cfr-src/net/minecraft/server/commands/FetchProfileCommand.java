/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;

public class FetchProfileCommand {
    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fetchprofile").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("name").then(Commands.argument("name", StringArgumentType.greedyString()).executes($$0 -> FetchProfileCommand.resolveName((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"name")))))).then(Commands.literal("id").then(Commands.argument("id", UuidArgument.uuid()).executes($$0 -> FetchProfileCommand.resolveId((CommandSourceStack)$$0.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)$$0, "id"))))));
    }

    private static void reportResolvedProfile(CommandSourceStack $$0, GameProfile $$12, String $$2, Component $$3) {
        ResolvableProfile $$42 = ResolvableProfile.createResolved($$12);
        ResolvableProfile.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)$$42).ifSuccess($$4 -> {
            String $$52 = $$4.toString();
            MutableComponent $$6 = Component.object(new PlayerSprite($$42, true));
            ComponentSerialization.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)$$6).ifSuccess($$5 -> {
                String $$6 = $$5.toString();
                $$0.sendSuccess(() -> {
                    Object $$5 = ComponentUtils.formatList(List.of(Component.translatable("commands.fetchprofile.copy_component").withStyle($$1 -> $$1.withClickEvent(new ClickEvent.CopyToClipboard($$52))), Component.translatable("commands.fetchprofile.give_item").withStyle($$1 -> $$1.withClickEvent(new ClickEvent.RunCommand("give @s minecraft:player_head[profile=" + $$52 + "]"))), Component.translatable("commands.fetchprofile.summon_mannequin").withStyle($$1 -> $$1.withClickEvent(new ClickEvent.RunCommand("summon minecraft:mannequin ~ ~ ~ {profile:" + $$52 + "}"))), Component.translatable("commands.fetchprofile.copy_text", $$6.withStyle(ChatFormatting.WHITE)).withStyle($$1 -> $$1.withClickEvent(new ClickEvent.CopyToClipboard($$6)))), CommonComponents.SPACE, $$0 -> ComponentUtils.wrapInSquareBrackets($$0.withStyle(ChatFormatting.GREEN)));
                    return Component.translatable($$2, $$3, $$5);
                }, false);
            }).ifError($$1 -> $$0.sendFailure(Component.translatable("commands.fetchprofile.failed_to_serialize", $$1.message())));
        }).ifError($$1 -> $$0.sendFailure(Component.translatable("commands.fetchprofile.failed_to_serialize", $$1.message())));
    }

    private static int resolveName(CommandSourceStack $$0, String $$1) {
        MinecraftServer $$2 = $$0.getServer();
        ProfileResolver $$3 = $$2.services().profileResolver();
        Util.nonCriticalIoPool().execute(() -> {
            MutableComponent $$4 = Component.literal($$1);
            Optional<GameProfile> $$5 = $$3.fetchByName($$1);
            $$2.execute(() -> $$5.ifPresentOrElse($$2 -> FetchProfileCommand.reportResolvedProfile($$0, $$2, "commands.fetchprofile.name.success", $$4), () -> $$0.sendFailure(Component.translatable("commands.fetchprofile.name.failure", $$4))));
        });
        return 1;
    }

    private static int resolveId(CommandSourceStack $$0, UUID $$1) {
        MinecraftServer $$2 = $$0.getServer();
        ProfileResolver $$3 = $$2.services().profileResolver();
        Util.nonCriticalIoPool().execute(() -> {
            Component $$4 = Component.translationArg($$1);
            Optional<GameProfile> $$5 = $$3.fetchById($$1);
            $$2.execute(() -> $$5.ifPresentOrElse($$2 -> FetchProfileCommand.reportResolvedProfile($$0, $$2, "commands.fetchprofile.id.success", $$4), () -> $$0.sendFailure(Component.translatable("commands.fetchprofile.id.failure", $$4))));
        });
        return 1;
    }
}

