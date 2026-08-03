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
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult.Error;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.unknown", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.enable.failed", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.disable.failed", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_CANNOT_DISABLE_FEATURE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.disable.failed.feature", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_PACK_FEATURES_NOT_ENABLED = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.datapack.enable.failed.no_flags", new Object[]{$$0, $$1})
   );
   private static final DynamicCommandExceptionType ERROR_PACK_INVALID_NAME = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.create.invalid_name", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_PACK_INVALID_FULL_NAME = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.create.invalid_full_name", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_EXISTS = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.create.already_exists", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_PACK_METADATA_ENCODE_FAILURE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.datapack.create.metadata_encode_failure", new Object[]{$$0, $$1})
   );
   private static final DynamicCommandExceptionType ERROR_PACK_IO_FAILURE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.datapack.create.io_failure", new Object[]{$$0})
   );
   private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = ($$0, $$1) -> SharedSuggestionProvider.suggest(
      ((CommandSourceStack)$$0.getSource()).getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), $$1
   );
   private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = ($$0, $$1) -> {
      PackRepository $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getPackRepository();
      Collection<String> $$3 = $$2.getSelectedIds();
      FeatureFlagSet $$4 = ((CommandSourceStack)$$0.getSource()).enabledFeatures();
      return SharedSuggestionProvider.suggest(
         $$2.getAvailablePacks()
            .stream()
            .filter($$1x -> $$1x.getRequestedFeatures().isSubsetOf($$4))
            .map(Pack::getId)
            .filter($$1x -> !$$3.contains($$1x))
            .map(StringArgumentType::escapeIfRequired),
         $$1
      );
   };

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                           "datapack"
                        )
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                     .then(
                        Commands.literal("enable")
                           .then(
                              ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                "name", StringArgumentType.string()
                                             )
                                             .suggests(UNSELECTED_PACKS)
                                             .executes(
                                                $$0x -> enablePack(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   getPack($$0x, "name", true),
                                                   ($$0xx, $$1x) -> $$1x.getDefaultPosition().insert($$0xx, $$1x, Pack::selectionConfig, false)
                                                )
                                             ))
                                          .then(
                                             Commands.literal("after")
                                                .then(
                                                   Commands.argument("existing", StringArgumentType.string())
                                                      .suggests(SELECTED_PACKS)
                                                      .executes(
                                                         $$0x -> enablePack(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            getPack($$0x, "name", true),
                                                            ($$1x, $$2) -> $$1x.add($$1x.indexOf(getPack($$0x, "existing", false)) + 1, $$2)
                                                         )
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("before")
                                             .then(
                                                Commands.argument("existing", StringArgumentType.string())
                                                   .suggests(SELECTED_PACKS)
                                                   .executes(
                                                      $$0x -> enablePack(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         getPack($$0x, "name", true),
                                                         ($$1x, $$2) -> $$1x.add($$1x.indexOf(getPack($$0x, "existing", false)), $$2)
                                                      )
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("last")
                                          .executes($$0x -> enablePack((CommandSourceStack)$$0x.getSource(), getPack($$0x, "name", true), List::add))
                                    ))
                                 .then(
                                    Commands.literal("first")
                                       .executes(
                                          $$0x -> enablePack(
                                             (CommandSourceStack)$$0x.getSource(), getPack($$0x, "name", true), ($$0xx, $$1x) -> $$0xx.add(0, $$1x)
                                          )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("disable")
                        .then(
                           Commands.argument("name", StringArgumentType.string())
                              .suggests(SELECTED_PACKS)
                              .executes($$0x -> disablePack((CommandSourceStack)$$0x.getSource(), getPack($$0x, "name", false)))
                        )
                  ))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes($$0x -> listPacks((CommandSourceStack)$$0x.getSource())))
                        .then(Commands.literal("available").executes($$0x -> listAvailablePacks((CommandSourceStack)$$0x.getSource()))))
                     .then(Commands.literal("enabled").executes($$0x -> listEnabledPacks((CommandSourceStack)$$0x.getSource())))
               ))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("create").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)))
                  .then(
                     Commands.argument("id", StringArgumentType.string())
                        .then(
                           Commands.argument("description", ComponentArgument.textComponent($$1))
                              .executes(
                                 $$0x -> createPack(
                                    (CommandSourceStack)$$0x.getSource(),
                                    StringArgumentType.getString($$0x, "id"),
                                    ComponentArgument.getResolvedComponent($$0x, "description")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int createPack(CommandSourceStack $$0, String $$1, Component $$2) throws CommandSyntaxException {
      Path $$3 = $$0.getServer().getWorldPath(LevelResource.DATAPACK_DIR);
      if (!FileUtil.isValidPathSegment($$1)) {
         throw ERROR_PACK_INVALID_NAME.create($$1);
      } else if (!FileUtil.isPathPartPortable($$1)) {
         throw ERROR_PACK_INVALID_FULL_NAME.create($$1);
      } else {
         Path $$4 = $$3.resolve($$1);
         if (Files.exists($$4)) {
            throw ERROR_PACK_ALREADY_EXISTS.create($$1);
         } else {
            PackMetadataSection $$5 = new PackMetadataSection($$2, SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minorRange());
            DataResult<JsonElement> $$6 = PackMetadataSection.SERVER_TYPE.codec().encodeStart(JsonOps.INSTANCE, $$5);
            Optional<Error<JsonElement>> $$7 = $$6.error();
            if ($$7.isPresent()) {
               throw ERROR_PACK_METADATA_ENCODE_FAILURE.create($$1, $$7.get().message());
            } else {
               JsonObject $$8 = new JsonObject();
               $$8.add(PackMetadataSection.SERVER_TYPE.name(), (JsonElement)$$6.getOrThrow());

               try {
                  Files.createDirectory($$4);
                  Files.createDirectory($$4.resolve(PackType.SERVER_DATA.getDirectory()));

                  try (BufferedWriter $$9 = Files.newBufferedWriter($$4.resolve("pack.mcmeta"), StandardCharsets.UTF_8)) {
                     JsonWriter $$10 = new JsonWriter($$9);

                     try {
                        $$10.setSerializeNulls(false);
                        $$10.setIndent("  ");
                        GsonHelper.writeValue($$10, $$8, null);
                     } catch (Throwable var15) {
                        try {
                           $$10.close();
                        } catch (Throwable var14) {
                           var15.addSuppressed(var14);
                        }

                        throw var15;
                     }

                     $$10.close();
                  }
               } catch (IOException var17) {
                  LOGGER.warn("Failed to create pack at {}", $$3.toAbsolutePath(), var17);
                  throw ERROR_PACK_IO_FAILURE.create($$1);
               }

               $$0.sendSuccess(() -> Component.translatable("commands.datapack.create.success", new Object[]{$$1}), true);
               return 1;
            }
         }
      }
   }

   private static int enablePack(CommandSourceStack $$0, Pack $$1, DataPackCommand.Inserter $$2) throws CommandSyntaxException {
      PackRepository $$3 = $$0.getServer().getPackRepository();
      List<Pack> $$4 = Lists.newArrayList($$3.getSelectedPacks());
      $$2.apply($$4, $$1);
      $$0.sendSuccess(() -> Component.translatable("commands.datapack.modify.enable", new Object[]{$$1.getChatLink(true)}), true);
      ReloadCommand.reloadPacks($$4.stream().map(Pack::getId).collect(Collectors.toList()), $$0);
      return $$4.size();
   }

   private static int disablePack(CommandSourceStack $$0, Pack $$1) {
      PackRepository $$2 = $$0.getServer().getPackRepository();
      List<Pack> $$3 = Lists.newArrayList($$2.getSelectedPacks());
      $$3.remove($$1);
      $$0.sendSuccess(() -> Component.translatable("commands.datapack.modify.disable", new Object[]{$$1.getChatLink(true)}), true);
      ReloadCommand.reloadPacks($$3.stream().map(Pack::getId).collect(Collectors.toList()), $$0);
      return $$3.size();
   }

   private static int listPacks(CommandSourceStack $$0) {
      return listEnabledPacks($$0) + listAvailablePacks($$0);
   }

   private static int listAvailablePacks(CommandSourceStack $$0) {
      PackRepository $$1 = $$0.getServer().getPackRepository();
      $$1.reload();
      Collection<Pack> $$2 = $$1.getSelectedPacks();
      Collection<Pack> $$3 = $$1.getAvailablePacks();
      FeatureFlagSet $$4 = $$0.enabledFeatures();
      List<Pack> $$5 = $$3.stream().filter($$2x -> !$$2.contains($$2x) && $$2x.getRequestedFeatures().isSubsetOf($$4)).toList();
      if ($$5.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.datapack.list.available.none"), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.datapack.list.available.success", new Object[]{$$5.size(), ComponentUtils.formatList($$5, $$0xx -> $$0xx.getChatLink(false))}
            ),
            false
         );
      }

      return $$5.size();
   }

   private static int listEnabledPacks(CommandSourceStack $$0) {
      PackRepository $$1 = $$0.getServer().getPackRepository();
      $$1.reload();
      Collection<? extends Pack> $$2 = $$1.getSelectedPacks();
      if ($$2.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.datapack.list.enabled.none"), false);
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.datapack.list.enabled.success", new Object[]{$$2.size(), ComponentUtils.formatList($$2, $$0xx -> $$0xx.getChatLink(true))}
            ),
            false
         );
      }

      return $$2.size();
   }

   private static Pack getPack(CommandContext<CommandSourceStack> $$0, String $$1, boolean $$2) throws CommandSyntaxException {
      String $$3 = StringArgumentType.getString($$0, $$1);
      PackRepository $$4 = ((CommandSourceStack)$$0.getSource()).getServer().getPackRepository();
      Pack $$5 = $$4.getPack($$3);
      if ($$5 == null) {
         throw ERROR_UNKNOWN_PACK.create($$3);
      } else {
         boolean $$6 = $$4.getSelectedPacks().contains($$5);
         if ($$2 && $$6) {
            throw ERROR_PACK_ALREADY_ENABLED.create($$3);
         } else if (!$$2 && !$$6) {
            throw ERROR_PACK_ALREADY_DISABLED.create($$3);
         } else {
            FeatureFlagSet $$7 = ((CommandSourceStack)$$0.getSource()).enabledFeatures();
            FeatureFlagSet $$8 = $$5.getRequestedFeatures();
            if (!$$2 && !$$8.isEmpty() && $$5.getPackSource() == PackSource.FEATURE) {
               throw ERROR_CANNOT_DISABLE_FEATURE.create($$3);
            } else if (!$$8.isSubsetOf($$7)) {
               throw ERROR_PACK_FEATURES_NOT_ENABLED.create($$3, FeatureFlags.printMissingFlags($$7, $$8));
            } else {
               return $$5;
            }
         }
      }
   }

   interface Inserter {
      void apply(List<Pack> var1, Pack var2) throws CommandSyntaxException;
   }
}
