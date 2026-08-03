package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static <D, R> CompletableFuture<R> load(
      net.minecraft.server.WorldLoader.InitConfig $$0,
      net.minecraft.server.WorldLoader.WorldDataSupplier<D> $$1,
      net.minecraft.server.WorldLoader.ResultFactory<D, R> $$2,
      Executor $$3,
      Executor $$4
   ) {
      try {
         Pair<WorldDataConfiguration, CloseableResourceManager> $$5 = $$0.packConfig.createResourceManager();
         CloseableResourceManager $$6 = (CloseableResourceManager)$$5.getSecond();
         LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$7 = net.minecraft.server.RegistryLayer.createRegistryAccess();
         List<PendingTags<?>> $$8 = TagLoader.loadTagsForExistingRegistries($$6, $$7.getLayer(net.minecraft.server.RegistryLayer.STATIC));
         Frozen $$9 = $$7.getAccessForLoading(net.minecraft.server.RegistryLayer.WORLDGEN);
         List<RegistryLookup<?>> $$10 = TagLoader.buildUpdatedLookups($$9, $$8);
         Frozen $$11 = RegistryDataLoader.load($$6, $$10, RegistryDataLoader.WORLDGEN_REGISTRIES);
         List<RegistryLookup<?>> $$12 = Stream.concat($$10.stream(), $$11.listRegistries()).toList();
         Frozen $$13 = RegistryDataLoader.load($$6, $$12, RegistryDataLoader.DIMENSION_REGISTRIES);
         WorldDataConfiguration $$14 = (WorldDataConfiguration)$$5.getFirst();
         Provider $$15 = Provider.create($$12.stream());
         net.minecraft.server.WorldLoader.DataLoadOutput<D> $$16 = $$1.get(new net.minecraft.server.WorldLoader.DataLoadContext($$6, $$14, $$15, $$13));
         LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$17 = $$7.replaceFrom(
            net.minecraft.server.RegistryLayer.WORLDGEN, new Frozen[]{$$11, $$16.finalDimensions}
         );
         return net.minecraft.server.ReloadableServerResources.loadResources(
               $$6, $$17, $$8, $$14.enabledFeatures(), $$0.commandSelection(), $$0.functionCompilationPermissions(), $$3, $$4
            )
            .whenComplete(($$1x, $$2x) -> {
               if ($$2x != null) {
                  $$6.close();
               }
            })
            .thenApplyAsync($$4x -> {
               $$4x.updateStaticRegistryTags();
               return $$2.create($$6, $$4x, $$17, $$16.cookie);
            }, $$4);
      } catch (Exception var18) {
         return CompletableFuture.failedFuture(var18);
      }
   }

   public record DataLoadContext(ResourceManager resources, WorldDataConfiguration dataConfiguration, Provider datapackWorldgen, Frozen datapackDimensions) {
   }

   public record DataLoadOutput<D>(D cookie, Frozen finalDimensions) {
   }

   public record InitConfig(
      net.minecraft.server.WorldLoader.PackConfig packConfig, CommandSelection commandSelection, PermissionSet functionCompilationPermissions
   ) {
   }

   public record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
      public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
         WorldDataConfiguration $$0 = net.minecraft.server.MinecraftServer.configurePackRepository(
            this.packRepository, this.initialDataConfig, this.initMode, this.safeMode
         );
         List<PackResources> $$1 = this.packRepository.openAllSelected();
         CloseableResourceManager $$2 = new MultiPackResourceManager(PackType.SERVER_DATA, $$1);
         return Pair.of($$0, $$2);
      }
   }

   @FunctionalInterface
   public interface ResultFactory<D, R> {
      R create(
         CloseableResourceManager var1,
         net.minecraft.server.ReloadableServerResources var2,
         LayeredRegistryAccess<net.minecraft.server.RegistryLayer> var3,
         D var4
      );
   }

   @FunctionalInterface
   public interface WorldDataSupplier<D> {
      net.minecraft.server.WorldLoader.DataLoadOutput<D> get(net.minecraft.server.WorldLoader.DataLoadContext var1);
   }
}
