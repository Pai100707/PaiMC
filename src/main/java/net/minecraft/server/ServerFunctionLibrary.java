package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagLoader.EntryWithSource;
import org.slf4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceKey<Registry<CommandFunction<CommandSourceStack>>> TYPE_KEY = ResourceKey.createRegistryKey(
      Identifier.withDefaultNamespace("function")
   );
   private static final FileToIdConverter LISTER = new FileToIdConverter(Registries.elementsDirPath(TYPE_KEY), ".mcfunction");
   private volatile Map<Identifier, CommandFunction<CommandSourceStack>> functions = ImmutableMap.of();
   private final TagLoader<CommandFunction<CommandSourceStack>> tagsLoader = new TagLoader(
      ($$0x, $$1x) -> this.getFunction($$0x), Registries.tagsDirPath(TYPE_KEY)
   );
   private volatile Map<Identifier, List<CommandFunction<CommandSourceStack>>> tags = Map.of();
   private final PermissionSet functionCompilationPermissions;
   private final CommandDispatcher<CommandSourceStack> dispatcher;

   public Optional<CommandFunction<CommandSourceStack>> getFunction(Identifier $$0) {
      return Optional.ofNullable(this.functions.get($$0));
   }

   public Map<Identifier, CommandFunction<CommandSourceStack>> getFunctions() {
      return this.functions;
   }

   public List<CommandFunction<CommandSourceStack>> getTag(Identifier $$0) {
      return this.tags.getOrDefault($$0, List.of());
   }

   public Iterable<Identifier> getAvailableTags() {
      return this.tags.keySet();
   }

   public ServerFunctionLibrary(PermissionSet $$0, CommandDispatcher<CommandSourceStack> $$1) {
      this.functionCompilationPermissions = $$0;
      this.dispatcher = $$1;
   }

   @Override
   public CompletableFuture<Void> reload(PreparableReloadListener.SharedState $$0, Executor $$1, PreparableReloadListener.PreparationBarrier $$2, Executor $$3) {
      ResourceManager $$4 = $$0.resourceManager();
      CompletableFuture<Map<Identifier, List<EntryWithSource>>> $$5 = CompletableFuture.supplyAsync(() -> this.tagsLoader.load($$4), $$1);
      CompletableFuture<Map<Identifier, CompletableFuture<CommandFunction<CommandSourceStack>>>> $$6 = CompletableFuture.<Map>supplyAsync(
            () -> LISTER.listMatchingResources($$4), $$1
         )
         .thenCompose($$1x -> {
            Map<Identifier, CompletableFuture<CommandFunction<CommandSourceStack>>> $$2x = Maps.newHashMap();
            CommandSourceStack $$3x = Commands.createCompilationContext(this.functionCompilationPermissions);

            for (Entry<Identifier, Resource> $$4x : $$1x.entrySet()) {
               Identifier $$5x = $$4x.getKey();
               Identifier $$6x = LISTER.fileToId($$5x);
               $$2x.put($$6x, CompletableFuture.supplyAsync(() -> {
                  List<String> $$3xx = readLines($$4x.getValue());
                  return CommandFunction.fromLines($$6x, this.dispatcher, $$3x, $$3xx);
               }, $$1));
            }

            CompletableFuture<?>[] $$7 = $$2x.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf($$7).handle(($$1xx, $$2xx) -> $$2x);
         });
      return $$5.thenCombine($$6, Pair::of)
         .thenCompose($$2::wait)
         .thenAcceptAsync(
            $$0x -> {
               Map<Identifier, CompletableFuture<CommandFunction<CommandSourceStack>>> $$1x = (Map<Identifier, CompletableFuture<CommandFunction<CommandSourceStack>>>)$$0x.getSecond();
               Builder<Identifier, CommandFunction<CommandSourceStack>> $$2x = ImmutableMap.builder();
               $$1x.forEach(($$1xx, $$2xx) -> $$2xx.handle(($$2xxx, $$3x) -> {
                  if ($$3x != null) {
                     LOGGER.error("Failed to load function {}", $$1xx, $$3x);
                  } else {
                     $$2x.put($$1xx, $$2xxx);
                  }

                  return null;
               }).join());
               this.functions = $$2x.build();
               this.tags = this.tagsLoader.build((Map)$$0x.getFirst());
            },
            $$3
         );
   }

   private static List<String> readLines(Resource $$0) {
      try {
         List var2;
         try (BufferedReader $$1 = $$0.openAsReader()) {
            var2 = $$1.lines().toList();
         }

         return var2;
      } catch (IOException var6) {
         throw new CompletionException(var6);
      }
   }
}
