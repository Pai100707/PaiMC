/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.particle.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("particle").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)Commands.argument("name", ParticleArgument.particle($$1)).executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), ((CommandSourceStack)$$0.getSource()).getPosition(), Vec3.ZERO, 0.0f, 0, false, ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getPlayers()))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos"), Vec3.ZERO, 0.0f, 0, false, ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getPlayers()))).then(Commands.argument("delta", Vec3Argument.vec3(false)).then(Commands.argument("speed", FloatArgumentType.floatArg((float)0.0f)).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("count", IntegerArgumentType.integer((int)0)).executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "delta"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), false, ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getPlayers()))).then(((LiteralArgumentBuilder)Commands.literal("force").executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "delta"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), true, ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getPlayers()))).then(Commands.argument("viewers", EntityArgument.players()).executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "delta"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), true, EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "viewers")))))).then(((LiteralArgumentBuilder)Commands.literal("normal").executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "delta"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), false, ((CommandSourceStack)$$0.getSource()).getServer().getPlayerList().getPlayers()))).then(Commands.argument("viewers", EntityArgument.players()).executes($$0 -> ParticleCommand.sendParticles((CommandSourceStack)$$0.getSource(), ParticleArgument.getParticle((CommandContext<CommandSourceStack>)$$0, "name"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "pos"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)$$0, "delta"), FloatArgumentType.getFloat((CommandContext)$$0, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), false, EntityArgument.getPlayers((CommandContext<CommandSourceStack>)$$0, "viewers")))))))))));
    }

    private static int sendParticles(CommandSourceStack $$0, ParticleOptions $$1, Vec3 $$2, Vec3 $$3, float $$4, int $$5, boolean $$6, Collection<ServerPlayer> $$7) throws CommandSyntaxException {
        int $$8 = 0;
        for (ServerPlayer $$9 : $$7) {
            if (!$$0.getLevel().sendParticles($$9, $$1, $$6, false, $$2.x, $$2.y, $$2.z, $$5, $$3.x, $$3.y, $$3.z, $$4)) continue;
            ++$$8;
        }
        if ($$8 == 0) {
            throw ERROR_FAILED.create();
        }
        $$0.sendSuccess(() -> Component.translatable("commands.particle.success", BuiltInRegistries.PARTICLE_TYPE.getKey($$1.getType()).toString()), true);
        return $$8;
    }
}

