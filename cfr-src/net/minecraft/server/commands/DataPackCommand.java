/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.FileUtil;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class DataPackCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.unknown", $$0));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.enable.failed", $$0));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.disable.failed", $$0));
    private static final DynamicCommandExceptionType ERROR_CANNOT_DISABLE_FEATURE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.disable.failed.feature", $$0));
    private static final Dynamic2CommandExceptionType ERROR_PACK_FEATURES_NOT_ENABLED = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.datapack.enable.failed.no_flags", $$0, $$1));
    private static final DynamicCommandExceptionType ERROR_PACK_INVALID_NAME = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.create.invalid_name", $$0));
    private static final DynamicCommandExceptionType ERROR_PACK_INVALID_FULL_NAME = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.create.invalid_full_name", $$0));
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_EXISTS = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.create.already_exists", $$0));
    private static final Dynamic2CommandExceptionType ERROR_PACK_METADATA_ENCODE_FAILURE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.datapack.create.metadata_encode_failure", $$0, $$1));
    private static final DynamicCommandExceptionType ERROR_PACK_IO_FAILURE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.datapack.create.io_failure", $$0));
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = ($$0, $$1) -> SharedSuggestionProvider.suggest(((CommandSourceStack)$$0.getSource()).getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), $$1);
    private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = ($$0, $$12) -> {
        PackRepository $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getPackRepository();
        Collection<String> $$3 = $$2.getSelectedIds();
        FeatureFlagSet $$4 = ((CommandSourceStack)$$0.getSource()).enabledFeatures();
        return SharedSuggestionProvider.suggest($$2.getAvailablePacks().stream().filter($$1 -> $$1.getRequestedFeatures().isSubsetOf($$4)).map(Pack::getId).filter($$1 -> !$$3.contains($$1)).map(StringArgumentType::escapeIfRequired), $$12);
    };

    public static void register(CommandDispatcher<CommandSourceStack> $$03, CommandBuildContext $$1) {
        $$03.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("datapack").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("enable").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.string()).suggests(UNSELECTED_PACKS).executes($$02 -> DataPackCommand.enablePack((CommandSourceStack)$$02.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$02, "name", true), ($$0, $$1) -> $$1.getDefaultPosition().insert($$0, $$1, Pack::selectionConfig, false)))).then(Commands.literal("after").then(Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes($$0 -> DataPackCommand.enablePack((CommandSourceStack)$$0.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$0, "name", true), ($$1, $$2) -> $$1.add($$1.indexOf(DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$0, "existing", false)) + 1, $$2)))))).then(Commands.literal("before").then(Commands.argument("existing", StringArgumentType.string()).suggests(SELECTED_PACKS).executes($$0 -> DataPackCommand.enablePack((CommandSourceStack)$$0.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$0, "name", true), ($$1, $$2) -> $$1.add($$1.indexOf(DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$0, "existing", false)), $$2)))))).then(Commands.literal("last").executes($$0 -> DataPackCommand.enablePack((CommandSourceStack)$$0.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$0, "name", true), List::add)))).then(Commands.literal("first").executes($$02 -> DataPackCommand.enablePack((CommandSourceStack)$$02.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$02, "name", true), ($$0, $$1) -> $$0.add(0, $$1))))))).then(Commands.literal("disable").then(Commands.argument("name", StringArgumentType.string()).suggests(SELECTED_PACKS).executes($$0 -> DataPackCommand.disablePack((CommandSourceStack)$$0.getSource(), DataPackCommand.getPack((CommandContext<CommandSourceStack>)$$0, "name", false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes($$0 -> DataPackCommand.listPacks((CommandSourceStack)$$0.getSource()))).then(Commands.literal("available").executes($$0 -> DataPackCommand.listAvailablePacks((CommandSourceStack)$$0.getSource())))).then(Commands.literal("enabled").executes($$0 -> DataPackCommand.listEnabledPacks((CommandSourceStack)$$0.getSource()))))).then(((LiteralArgumentBuilder)Commands.literal("create").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))).then(Commands.argument("id", StringArgumentType.string()).then(Commands.argument("description", ComponentArgument.textComponent($$1)).executes($$0 -> DataPackCommand.createPack((CommandSourceStack)$$0.getSource(), StringArgumentType.getString((CommandContext)$$0, (String)"id"), ComponentArgument.getResolvedComponent((CommandContext<CommandSourceStack>)$$0, "description")))))));
    }

    private static int createPack(CommandSourceStack $$0, String $$1, Component $$2) throws CommandSyntaxException {
        Path $$3 = $$0.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
        if (!FileUtil.isValidPathSegment($$1)) {
            throw ERROR_PACK_INVALID_NAME.create((Object)$$1);
        }
        if (!FileUtil.isPathPartPortable($$1)) {
            throw ERROR_PACK_INVALID_FULL_NAME.create((Object)$$1);
        }
        Path $$4 = $$3.resolve($$1);
        if (Files.exists($$4, new LinkOption[0])) {
            throw ERROR_PACK_ALREADY_EXISTS.create((Object)$$1);
        }
        PackMetadataSection $$5 = new PackMetadataSection($$2, SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minorRange());
        DataResult $$6 = PackMetadataSection.SERVER_TYPE.codec().encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)$$5);
        Optional $$7 = $$6.error();
        if ($$7.isPresent()) {
            throw ERROR_PACK_METADATA_ENCODE_FAILURE.create((Object)$$1, (Object)((DataResult.Error)$$7.get()).message());
        }
        JsonObject $$8 = new JsonObject();
        $$8.add(PackMetadataSection.SERVER_TYPE.name(), (JsonElement)$$6.getOrThrow());
        try {
            Files.createDirectory($$4, new FileAttribute[0]);
            Files.createDirectory($$4.resolve(PackType.SERVER_DATA.getDirectory()), new FileAttribute[0]);
            try (BufferedWriter $$9 = Files.newBufferedWriter($$4.resolve("pack.mcmeta"), StandardCharsets.UTF_8, new OpenOption[0]);
                 JsonWriter $$10 = new JsonWriter((Writer)$$9);){
                $$10.setSerializeNulls(false);
                $$10.setIndent("  ");
                GsonHelper.writeValue($$10, (JsonElement)$$8, null);
            }
        }
        catch (IOException $$11) {
            LOGGER.warn("Failed to create pack at {}", (Object)$$3.toAbsolutePath(), (Object)$$11);
            throw ERROR_PACK_IO_FAILURE.create((Object)$$1);
        }
        $$0.sendSuccess(() -> Component.translatable("commands.datapack.create.success", $$1), true);
        return 1;
    }

    private static int enablePack(CommandSourceStack $$0, Pack $$1, Inserter $$2) throws CommandSyntaxException {
        PackRepository $$3 = $$0.getServer().getPackRepository();
        ArrayList $$4 = Lists.newArrayList($$3.getSelectedPacks());
        $$2.apply($$4, $$1);
        $$0.sendSuccess(() -> Component.translatable("commands.datapack.modify.enable", $$1.getChatLink(true)), true);
        ReloadCommand.reloadPacks($$4.stream().map(Pack::getId).collect(Collectors.toList()), $$0);
        return $$4.size();
    }

    private static int disablePack(CommandSourceStack $$0, Pack $$1) {
        PackRepository $$2 = $$0.getServer().getPackRepository();
        ArrayList $$3 = Lists.newArrayList($$2.getSelectedPacks());
        $$3.remove($$1);
        $$0.sendSuccess(() -> Component.translatable("commands.datapack.modify.disable", $$1.getChatLink(true)), true);
        ReloadCommand.reloadPacks($$3.stream().map(Pack::getId).collect(Collectors.toList()), $$0);
        return $$3.size();
    }

    private static int listPacks(CommandSourceStack $$0) {
        return DataPackCommand.listEnabledPacks($$0) + DataPackCommand.listAvailablePacks($$0);
    }

    private static int listAvailablePacks(CommandSourceStack $$0) {
        PackRepository $$1 = $$0.getServer().getPackRepository();
        $$1.reload();
        Collection<Pack> $$22 = $$1.getSelectedPacks();
        Collection<Pack> $$3 = $$1.getAvailablePacks();
        FeatureFlagSet $$4 = $$0.enabledFeatures();
        List<Pack> $$5 = $$3.stream().filter($$2 -> !$$22.contains($$2) && $$2.getRequestedFeatures().isSubsetOf($$4)).toList();
        if ($$5.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.datapack.list.available.none"), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.datapack.list.available.success", $$5.size(), ComponentUtils.formatList($$5, $$0 -> $$0.getChatLink(false))), false);
        }
        return $$5.size();
    }

    private static int listEnabledPacks(CommandSourceStack $$0) {
        PackRepository $$1 = $$0.getServer().getPackRepository();
        $$1.reload();
        Collection<Pack> $$2 = $$1.getSelectedPacks();
        if ($$2.isEmpty()) {
            $$0.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.none"), false);
        } else {
            $$0.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.success", $$2.size(), ComponentUtils.formatList($$2, $$0 -> $$0.getChatLink(true))), false);
        }
        return $$2.size();
    }

    private static Pack getPack(CommandContext<CommandSourceStack> $$0, String $$1, boolean $$2) throws CommandSyntaxException {
        String $$3 = StringArgumentType.getString($$0, (String)$$1);
        PackRepository $$4 = ((CommandSourceStack)$$0.getSource()).getServer().getPackRepository();
        Pack $$5 = $$4.getPack($$3);
        if ($$5 == null) {
            throw ERROR_UNKNOWN_PACK.create((Object)$$3);
        }
        boolean $$6 = $$4.getSelectedPacks().contains($$5);
        if ($$2 && $$6) {
            throw ERROR_PACK_ALREADY_ENABLED.create((Object)$$3);
        }
        if (!$$2 && !$$6) {
            throw ERROR_PACK_ALREADY_DISABLED.create((Object)$$3);
        }
        FeatureFlagSet $$7 = ((CommandSourceStack)$$0.getSource()).enabledFeatures();
        FeatureFlagSet $$8 = $$5.getRequestedFeatures();
        if (!$$2 && !$$8.isEmpty() && $$5.getPackSource() == PackSource.FEATURE) {
            throw ERROR_CANNOT_DISABLE_FEATURE.create((Object)$$3);
        }
        if (!$$8.isSubsetOf($$7)) {
            throw ERROR_PACK_FEATURES_NOT_ENABLED.create((Object)$$3, (Object)FeatureFlags.printMissingFlags($$7, $$8));
        }
        return $$5;
    }

    static interface Inserter {
        public void apply(List<Pack> var1, Pack var2) throws CommandSyntaxException;
    }
}

