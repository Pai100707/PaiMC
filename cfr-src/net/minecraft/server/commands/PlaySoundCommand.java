/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType((Message)Component.translatable("commands.playsound.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        RequiredArgumentBuilder $$1 = (RequiredArgumentBuilder)Commands.argument("sound", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS)).executes($$0 -> PlaySoundCommand.playSound((CommandSourceStack)$$0.getSource(), PlaySoundCommand.getCallingPlayerAsCollection(((CommandSourceStack)$$0.getSource()).getPlayer()), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "sound"), SoundSource.MASTER, ((CommandSourceStack)$$0.getSource()).getPosition(), 1.0f, 1.0f, 0.0f));
        for (SoundSource $$2 : SoundSource.values()) {
            $$1.then(PlaySoundCommand.source($$2));
        }
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("playsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then((ArgumentBuilder)$$1));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource $$0) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal($$0.getName()).executes($$1 -> PlaySoundCommand.playSound((CommandSourceStack)$$1.getSource(), PlaySoundCommand.getCallingPlayerAsCollection(((CommandSourceStack)$$1.getSource()).getPlayer()), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound"), $$0, ((CommandSourceStack)$$1.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes($$1 -> PlaySoundCommand.playSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound"), $$0, ((CommandSourceStack)$$1.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes($$1 -> PlaySoundCommand.playSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound"), $$0, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$1, "pos"), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("volume", FloatArgumentType.floatArg((float)0.0f)).executes($$1 -> PlaySoundCommand.playSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound"), $$0, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$1, "pos"), ((Float)$$1.getArgument("volume", Float.class)).floatValue(), 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pitch", FloatArgumentType.floatArg((float)0.0f, (float)2.0f)).executes($$1 -> PlaySoundCommand.playSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound"), $$0, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$1, "pos"), ((Float)$$1.getArgument("volume", Float.class)).floatValue(), ((Float)$$1.getArgument("pitch", Float.class)).floatValue(), 0.0f))).then(Commands.argument("minVolume", FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes($$1 -> PlaySoundCommand.playSound((CommandSourceStack)$$1.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$1, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$1, "sound"), $$0, Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$1, "pos"), ((Float)$$1.getArgument("volume", Float.class)).floatValue(), ((Float)$$1.getArgument("pitch", Float.class)).floatValue(), ((Float)$$1.getArgument("minVolume", Float.class)).floatValue())))))));
    }

    private static Collection<ServerPlayer> getCallingPlayerAsCollection(@Nullable ServerPlayer $$0) {
        return $$0 != null ? List.of($$0) : List.of();
    }

    private static int playSound(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Identifier $$2, SoundSource $$3, Vec3 $$4, float $$5, float $$6, float $$7) throws CommandSyntaxException {
        Holder<SoundEvent> $$8 = Holder.direct(SoundEvent.createVariableRangeEvent($$2));
        double $$9 = Mth.square($$8.value().getRange($$5));
        ServerLevel $$10 = $$0.getLevel();
        long $$11 = $$10.getRandom().nextLong();
        ArrayList<ServerPlayer> $$12 = new ArrayList<ServerPlayer>();
        for (ServerPlayer $$13 : $$1) {
            if ($$13.level() != $$10) continue;
            double $$14 = $$4.x - $$13.getX();
            double $$15 = $$4.y - $$13.getY();
            double $$16 = $$4.z - $$13.getZ();
            double $$17 = $$14 * $$14 + $$15 * $$15 + $$16 * $$16;
            Vec3 $$18 = $$4;
            float $$19 = $$5;
            if ($$17 > $$9) {
                if ($$7 <= 0.0f) continue;
                double $$20 = Math.sqrt($$17);
                $$18 = new Vec3($$13.getX() + $$14 / $$20 * 2.0, $$13.getY() + $$15 / $$20 * 2.0, $$13.getZ() + $$16 / $$20 * 2.0);
                $$19 = $$7;
            }
            $$13.connection.send(new ClientboundSoundPacket($$8, $$3, $$18.x(), $$18.y(), $$18.z(), $$19, $$6, $$11));
            $$12.add($$13);
        }
        int $$21 = $$12.size();
        if ($$21 == 0) {
            throw ERROR_TOO_FAR.create();
        }
        if ($$21 == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.playsound.success.single", Component.translationArg($$2), ((ServerPlayer)$$12.getFirst()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.playsound.success.multiple", Component.translationArg($$2), $$21), true);
        }
        return $$21;
    }
}

