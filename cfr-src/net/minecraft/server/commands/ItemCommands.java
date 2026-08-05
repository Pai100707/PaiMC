/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
    static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("commands.item.target.not_a_container", $$0, $$1, $$2));
    static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(($$0, $$1, $$2) -> Component.translatableEscape("commands.item.source.not_a_container", $$0, $$1, $$2));
    static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.item.target.no_such_slot", $$0));
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.item.source.no_such_slot", $$0));
    private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.item.target.no_changes", $$0));
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.item.target.no_changed.known_item", $$0, $$1));

    public static void register(CommandDispatcher<CommandSourceStack> $$02, CommandBuildContext $$1) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("item").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("replace").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item($$1)).executes($$0 -> ItemCommands.setBlockItem((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ItemArgument.getItem($$0, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer((int)1, (int)99)).executes($$0 -> ItemCommands.setBlockItem((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ItemArgument.getItem($$0, "item").createItemStack(IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), true))))))).then(((LiteralArgumentBuilder)Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes($$0 -> ItemCommands.blockToBlock((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot")))).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1)).executes($$0 -> ItemCommands.blockToBlock((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)$$0, "modifier")))))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes($$0 -> ItemCommands.entityToBlock((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot")))).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1)).executes($$0 -> ItemCommands.entityToBlock((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)$$0, "modifier")))))))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item($$1)).executes($$0 -> ItemCommands.setEntityItem((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ItemArgument.getItem($$0, "item").createItemStack(1, false)))).then(Commands.argument("count", IntegerArgumentType.integer((int)1, (int)99)).executes($$0 -> ItemCommands.setEntityItem((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ItemArgument.getItem($$0, "item").createItemStack(IntegerArgumentType.getInteger((CommandContext)$$0, (String)"count"), true))))))).then(((LiteralArgumentBuilder)Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes($$0 -> ItemCommands.blockToEntities((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot")))).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1)).executes($$0 -> ItemCommands.blockToEntities((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)$$0, "modifier")))))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(((RequiredArgumentBuilder)Commands.argument("sourceSlot", SlotArgument.slot()).executes($$0 -> ItemCommands.entityToEntities((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot")))).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1)).executes($$0 -> ItemCommands.entityToEntities((CommandSourceStack)$$0.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)$$0, "source"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "sourceSlot"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)$$0, "modifier"))))))))))))).then(((LiteralArgumentBuilder)Commands.literal("modify").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1)).executes($$0 -> ItemCommands.modifyBlockItem((CommandSourceStack)$$0.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)$$0, "pos"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)$$0, "modifier")))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier($$1)).executes($$0 -> ItemCommands.modifyEntityItem((CommandSourceStack)$$0.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)$$0, "targets"), SlotArgument.getSlot((CommandContext<CommandSourceStack>)$$0, "slot"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)$$0, "modifier")))))))));
    }

    private static int modifyBlockItem(CommandSourceStack $$0, BlockPos $$1, int $$2, Holder<LootItemFunction> $$3) throws CommandSyntaxException {
        Container $$4 = ItemCommands.getContainer($$0, $$1, ERROR_TARGET_NOT_A_CONTAINER);
        if ($$2 < 0 || $$2 >= $$4.getContainerSize()) {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create((Object)$$2);
        }
        ItemStack $$5 = ItemCommands.applyModifier($$0, $$3, $$4.getItem($$2));
        $$4.setItem($$2, $$5);
        $$0.sendSuccess(() -> Component.translatable("commands.item.block.set.success", $$1.getX(), $$1.getY(), $$1.getZ(), $$5.getDisplayName()), true);
        return 1;
    }

    private static int modifyEntityItem(CommandSourceStack $$0, Collection<? extends Entity> $$1, int $$2, Holder<LootItemFunction> $$3) throws CommandSyntaxException {
        HashMap $$4 = Maps.newHashMapWithExpectedSize((int)$$1.size());
        for (Entity entity : $$1) {
            ItemStack $$7;
            SlotAccess $$6 = entity.getSlot($$2);
            if ($$6 == null || !$$6.set($$7 = ItemCommands.applyModifier($$0, $$3, $$6.get().copy()))) continue;
            $$4.put(entity, $$7);
            if (!(entity instanceof ServerPlayer)) continue;
            ServerPlayer $$8 = (ServerPlayer)entity;
            $$8.containerMenu.broadcastChanges();
        }
        if ($$4.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES.create((Object)$$2);
        }
        if ($$4.size() == 1) {
            Map.Entry $$9 = $$4.entrySet().iterator().next();
            $$0.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.single", ((Entity)$$9.getKey()).getDisplayName(), ((ItemStack)$$9.getValue()).getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", $$4.size()), true);
        }
        return $$4.size();
    }

    private static int setBlockItem(CommandSourceStack $$0, BlockPos $$1, int $$2, ItemStack $$3) throws CommandSyntaxException {
        Container $$4 = ItemCommands.getContainer($$0, $$1, ERROR_TARGET_NOT_A_CONTAINER);
        if ($$2 < 0 || $$2 >= $$4.getContainerSize()) {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create((Object)$$2);
        }
        $$4.setItem($$2, $$3);
        $$0.sendSuccess(() -> Component.translatable("commands.item.block.set.success", $$1.getX(), $$1.getY(), $$1.getZ(), $$3.getDisplayName()), true);
        return 1;
    }

    static Container getContainer(CommandSourceStack $$0, BlockPos $$1, Dynamic3CommandExceptionType $$2) throws CommandSyntaxException {
        BlockEntity $$3 = $$0.getLevel().getBlockEntity($$1);
        if ($$3 instanceof Container) {
            Container $$4 = (Container)((Object)$$3);
            return $$4;
        }
        throw $$2.create((Object)$$1.getX(), (Object)$$1.getY(), (Object)$$1.getZ());
    }

    private static int setEntityItem(CommandSourceStack $$0, Collection<? extends Entity> $$1, int $$2, ItemStack $$3) throws CommandSyntaxException {
        ArrayList $$4 = Lists.newArrayListWithCapacity((int)$$1.size());
        for (Entity entity : $$1) {
            SlotAccess $$6 = entity.getSlot($$2);
            if ($$6 == null || !$$6.set($$3.copy())) continue;
            $$4.add(entity);
            if (!(entity instanceof ServerPlayer)) continue;
            ServerPlayer $$7 = (ServerPlayer)entity;
            $$7.containerMenu.broadcastChanges();
        }
        if ($$4.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create((Object)$$3.getDisplayName(), (Object)$$2);
        }
        if ($$4.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.single", ((Entity)$$4.getFirst()).getDisplayName(), $$3.getDisplayName()), true);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", $$4.size(), $$3.getDisplayName()), true);
        }
        return $$4.size();
    }

    private static int blockToEntities(CommandSourceStack $$0, BlockPos $$1, int $$2, Collection<? extends Entity> $$3, int $$4) throws CommandSyntaxException {
        return ItemCommands.setEntityItem($$0, $$3, $$4, ItemCommands.getBlockItem($$0, $$1, $$2));
    }

    private static int blockToEntities(CommandSourceStack $$0, BlockPos $$1, int $$2, Collection<? extends Entity> $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
        return ItemCommands.setEntityItem($$0, $$3, $$4, ItemCommands.applyModifier($$0, $$5, ItemCommands.getBlockItem($$0, $$1, $$2)));
    }

    private static int blockToBlock(CommandSourceStack $$0, BlockPos $$1, int $$2, BlockPos $$3, int $$4) throws CommandSyntaxException {
        return ItemCommands.setBlockItem($$0, $$3, $$4, ItemCommands.getBlockItem($$0, $$1, $$2));
    }

    private static int blockToBlock(CommandSourceStack $$0, BlockPos $$1, int $$2, BlockPos $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
        return ItemCommands.setBlockItem($$0, $$3, $$4, ItemCommands.applyModifier($$0, $$5, ItemCommands.getBlockItem($$0, $$1, $$2)));
    }

    private static int entityToBlock(CommandSourceStack $$0, Entity $$1, int $$2, BlockPos $$3, int $$4) throws CommandSyntaxException {
        return ItemCommands.setBlockItem($$0, $$3, $$4, ItemCommands.getItemInSlot($$1, $$2));
    }

    private static int entityToBlock(CommandSourceStack $$0, Entity $$1, int $$2, BlockPos $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
        return ItemCommands.setBlockItem($$0, $$3, $$4, ItemCommands.applyModifier($$0, $$5, ItemCommands.getItemInSlot($$1, $$2)));
    }

    private static int entityToEntities(CommandSourceStack $$0, Entity $$1, int $$2, Collection<? extends Entity> $$3, int $$4) throws CommandSyntaxException {
        return ItemCommands.setEntityItem($$0, $$3, $$4, ItemCommands.getItemInSlot($$1, $$2));
    }

    private static int entityToEntities(CommandSourceStack $$0, Entity $$1, int $$2, Collection<? extends Entity> $$3, int $$4, Holder<LootItemFunction> $$5) throws CommandSyntaxException {
        return ItemCommands.setEntityItem($$0, $$3, $$4, ItemCommands.applyModifier($$0, $$5, ItemCommands.getItemInSlot($$1, $$2)));
    }

    private static ItemStack applyModifier(CommandSourceStack $$0, Holder<LootItemFunction> $$1, ItemStack $$2) {
        ServerLevel $$3 = $$0.getLevel();
        LootParams $$4 = new LootParams.Builder($$3).withParameter(LootContextParams.ORIGIN, $$0.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, $$0.getEntity()).create(LootContextParamSets.COMMAND);
        LootContext $$5 = new LootContext.Builder($$4).create(Optional.empty());
        $$5.pushVisitedElement(LootContext.createVisitedEntry($$1.value()));
        ItemStack $$6 = (ItemStack)$$1.value().apply($$2, $$5);
        $$6.limitSize($$6.getMaxStackSize());
        return $$6;
    }

    private static ItemStack getItemInSlot(SlotProvider $$0, int $$1) throws CommandSyntaxException {
        SlotAccess $$2 = $$0.getSlot($$1);
        if ($$2 == null) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create((Object)$$1);
        }
        return $$2.get().copy();
    }

    private static ItemStack getBlockItem(CommandSourceStack $$0, BlockPos $$1, int $$2) throws CommandSyntaxException {
        Container $$3 = ItemCommands.getContainer($$0, $$1, ERROR_SOURCE_NOT_A_CONTAINER);
        return ItemCommands.getItemInSlot($$3, $$2);
    }
}

