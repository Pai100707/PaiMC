/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.attribute.failed.entity", $$0));
    private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.attribute.failed.no_attribute", $$0, $$1));
    private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("commands.attribute.failed.no_modifier", $$1, $$0, $$2));
    private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("commands.attribute.failed.modifier_already_present", $$2, $$1, $$0));

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$12) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("attribute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("target", EntityArgument.entity()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("attribute", ResourceArgument.resource($$12, Registries.ATTRIBUTE)).then(((LiteralArgumentBuilder)Commands.literal("get").executes($$0 -> AttributeCommand.getAttributeValue((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes($$0 -> AttributeCommand.getAttributeValue((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"scale")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("base").then(Commands.literal("set").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes($$0 -> AttributeCommand.setAttributeBase((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"value")))))).then(((LiteralArgumentBuilder)Commands.literal("get").executes($$0 -> AttributeCommand.getAttributeBase((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes($$0 -> AttributeCommand.getAttributeBase((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"scale")))))).then(Commands.literal("reset").executes($$0 -> AttributeCommand.resetAttributeBase((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("modifier").then(Commands.literal("add").then(Commands.argument("id", IdentifierArgument.id()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("value", DoubleArgumentType.doubleArg()).then(Commands.literal("add_value").executes($$0 -> AttributeCommand.addModifier((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"value"), AttributeModifier.Operation.ADD_VALUE)))).then(Commands.literal("add_multiplied_base").executes($$0 -> AttributeCommand.addModifier((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"value"), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)))).then(Commands.literal("add_multiplied_total").executes($$0 -> AttributeCommand.addModifier((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"value"), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))))))).then(Commands.literal("remove").then(Commands.argument("id", IdentifierArgument.id()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggestResource(AttributeCommand.getAttributeModifiers(EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute")), $$1)).executes($$0 -> AttributeCommand.removeModifier((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id")))))).then(Commands.literal("value").then(Commands.literal("get").then(((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(($$0, $$1) -> SharedSuggestionProvider.suggestResource(AttributeCommand.getAttributeModifiers(EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute")), $$1)).executes($$0 -> AttributeCommand.getAttributeModifier((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes($$0 -> AttributeCommand.getAttributeModifier((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)$$0, "attribute"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)$$0, "id"), DoubleArgumentType.getDouble((CommandContext)$$0, (String)"scale")))))))))));
    }

    private static AttributeInstance getAttributeInstance(Entity $$0, Holder<Attribute> $$1) throws CommandSyntaxException {
        AttributeInstance $$2 = AttributeCommand.getLivingEntity($$0).getAttributes().getInstance($$1);
        if ($$2 == null) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create((Object)$$0.getName(), (Object)AttributeCommand.getAttributeDescription($$1));
        }
        return $$2;
    }

    private static LivingEntity getLivingEntity(Entity $$0) throws CommandSyntaxException {
        if (!($$0 instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING_ENTITY.create((Object)$$0.getName());
        }
        return (LivingEntity)$$0;
    }

    private static LivingEntity getEntityWithAttribute(Entity $$0, Holder<Attribute> $$1) throws CommandSyntaxException {
        LivingEntity $$2 = AttributeCommand.getLivingEntity($$0);
        if (!$$2.getAttributes().hasAttribute($$1)) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create((Object)$$0.getName(), (Object)AttributeCommand.getAttributeDescription($$1));
        }
        return $$2;
    }

    private static int getAttributeValue(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, double $$3) throws CommandSyntaxException {
        LivingEntity $$4 = AttributeCommand.getEntityWithAttribute($$1, $$2);
        double $$5 = $$4.getAttributeValue($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.attribute.value.get.success", AttributeCommand.getAttributeDescription($$2), $$1.getName(), $$5), false);
        return (int)($$5 * $$3);
    }

    private static int getAttributeBase(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, double $$3) throws CommandSyntaxException {
        LivingEntity $$4 = AttributeCommand.getEntityWithAttribute($$1, $$2);
        double $$5 = $$4.getAttributeBaseValue($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.attribute.base_value.get.success", AttributeCommand.getAttributeDescription($$2), $$1.getName(), $$5), false);
        return (int)($$5 * $$3);
    }

    private static int getAttributeModifier(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, Identifier $$3, double $$4) throws CommandSyntaxException {
        LivingEntity $$5 = AttributeCommand.getEntityWithAttribute($$1, $$2);
        AttributeMap $$6 = $$5.getAttributes();
        if (!$$6.hasModifier($$2, $$3)) {
            throw ERROR_NO_SUCH_MODIFIER.create((Object)$$1.getName(), (Object)AttributeCommand.getAttributeDescription($$2), (Object)$$3);
        }
        double $$7 = $$6.getModifierValue($$2, $$3);
        $$0.sendSuccess(() -> Component.translatable("commands.attribute.modifier.value.get.success", Component.translationArg($$3), AttributeCommand.getAttributeDescription($$2), $$1.getName(), $$7), false);
        return (int)($$7 * $$4);
    }

    private static Stream<Identifier> getAttributeModifiers(Entity $$0, Holder<Attribute> $$1) throws CommandSyntaxException {
        AttributeInstance $$2 = AttributeCommand.getAttributeInstance($$0, $$1);
        return $$2.getModifiers().stream().map(AttributeModifier::id);
    }

    private static int setAttributeBase(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, double $$3) throws CommandSyntaxException {
        AttributeCommand.getAttributeInstance($$1, $$2).setBaseValue($$3);
        $$0.sendSuccess(() -> Component.translatable("commands.attribute.base_value.set.success", AttributeCommand.getAttributeDescription($$2), $$1.getName(), $$3), false);
        return 1;
    }

    private static int resetAttributeBase(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2) throws CommandSyntaxException {
        LivingEntity $$3 = AttributeCommand.getLivingEntity($$1);
        if (!$$3.getAttributes().resetBaseValue($$2)) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create((Object)$$1.getName(), (Object)AttributeCommand.getAttributeDescription($$2));
        }
        double $$4 = $$3.getAttributeBaseValue($$2);
        $$0.sendSuccess(() -> Component.translatable("commands.attribute.base_value.reset.success", AttributeCommand.getAttributeDescription($$2), $$1.getName(), $$4), false);
        return 1;
    }

    private static int addModifier(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, Identifier $$3, double $$4, AttributeModifier.Operation $$5) throws CommandSyntaxException {
        AttributeInstance $$6 = AttributeCommand.getAttributeInstance($$1, $$2);
        AttributeModifier $$7 = new AttributeModifier($$3, $$4, $$5);
        if ($$6.hasModifier($$3)) {
            throw ERROR_MODIFIER_ALREADY_PRESENT.create((Object)$$1.getName(), (Object)AttributeCommand.getAttributeDescription($$2), (Object)$$3);
        }
        $$6.addPermanentModifier($$7);
        $$0.sendSuccess(() -> Component.translatable("commands.attribute.modifier.add.success", Component.translationArg($$3), AttributeCommand.getAttributeDescription($$2), $$1.getName()), false);
        return 1;
    }

    private static int removeModifier(CommandSourceStack $$0, Entity $$1, Holder<Attribute> $$2, Identifier $$3) throws CommandSyntaxException {
        AttributeInstance $$4 = AttributeCommand.getAttributeInstance($$1, $$2);
        if ($$4.removeModifier($$3)) {
            $$0.sendSuccess(() -> Component.translatable("commands.attribute.modifier.remove.success", Component.translationArg($$3), AttributeCommand.getAttributeDescription($$2), $$1.getName()), false);
            return 1;
        }
        throw ERROR_NO_SUCH_MODIFIER.create((Object)$$1.getName(), (Object)AttributeCommand.getAttributeDescription($$2), (Object)$$3);
    }

    private static Component getAttributeDescription(Holder<Attribute> $$0) {
        return Component.translatable($$0.value().getDescriptionId());
    }
}

