/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
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

    public static <D, R> CompletableFuture<R> load(InitConfig $$0, WorldDataSupplier<D> $$12, ResultFactory<D, R> $$22, Executor $$3, Executor $$42) {
        try {
            Pair<WorldDataConfiguration, CloseableResourceManager> $$5 = $$0.packConfig.createResourceManager();
            CloseableResourceManager $$6 = (CloseableResourceManager)$$5.getSecond();
            LayeredRegistryAccess<RegistryLayer> $$7 = RegistryLayer.createRegistryAccess();
            List<Registry.PendingTags<?>> $$8 = TagLoader.loadTagsForExistingRegistries($$6, $$7.getLayer(RegistryLayer.STATIC));
            RegistryAccess.Frozen $$9 = $$7.getAccessForLoading(RegistryLayer.WORLDGEN);
            List<HolderLookup.RegistryLookup<?>> $$10 = TagLoader.buildUpdatedLookups($$9, $$8);
            RegistryAccess.Frozen $$11 = RegistryDataLoader.load($$6, $$10, RegistryDataLoader.WORLDGEN_REGISTRIES);
            List<HolderLookup.RegistryLookup<?>> $$122 = Stream.concat($$10.stream(), $$11.listRegistries()).toList();
            RegistryAccess.Frozen $$13 = RegistryDataLoader.load($$6, $$122, RegistryDataLoader.DIMENSION_REGISTRIES);
            WorldDataConfiguration $$14 = (WorldDataConfiguration)$$5.getFirst();
            HolderLookup.Provider $$15 = HolderLookup.Provider.create($$122.stream());
            DataLoadOutput<D> $$16 = $$12.get(new DataLoadContext($$6, $$14, $$15, $$13));
            LayeredRegistryAccess<RegistryLayer> $$17 = $$7.replaceFrom(RegistryLayer.WORLDGEN, $$11, $$16.finalDimensions);
            return ((CompletableFuture)ReloadableServerResources.loadResources($$6, $$17, $$8, $$14.enabledFeatures(), $$0.commandSelection(), $$0.functionCompilationPermissions(), $$3, $$42).whenComplete(($$1, $$2) -> {
                if ($$2 != null) {
                    $$6.close();
                }
            })).thenApplyAsync($$4 -> {
                $$4.updateStaticRegistryTags();
                return $$22.create($$6, (ReloadableServerResources)$$4, $$17, $$3.cookie);
            }, $$42);
        }
        catch (Exception $$18) {
            return CompletableFuture.failedFuture($$18);
        }
    }

    public static final class InitConfig
    extends Record {
        final PackConfig packConfig;
        private final Commands.CommandSelection commandSelection;
        private final PermissionSet functionCompilationPermissions;

        public InitConfig(PackConfig $$0, Commands.CommandSelection $$1, PermissionSet $$2) {
            this.packConfig = $$0;
            this.commandSelection = $$1;
            this.functionCompilationPermissions = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "packConfig", "commandSelection", "functionCompilationPermissions"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "packConfig", "commandSelection", "functionCompilationPermissions"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InitConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "packConfig", "commandSelection", "functionCompilationPermissions"}, this, $$0);
        }

        public PackConfig packConfig() {
            return this.packConfig;
        }

        public Commands.CommandSelection commandSelection() {
            return this.commandSelection;
        }

        public PermissionSet functionCompilationPermissions() {
            return this.functionCompilationPermissions;
        }
    }

    public record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
            WorldDataConfiguration $$0 = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig, this.initMode, this.safeMode);
            List<PackResources> $$1 = this.packRepository.openAllSelected();
            MultiPackResourceManager $$2 = new MultiPackResourceManager(PackType.SERVER_DATA, $$1);
            return Pair.of((Object)$$0, (Object)$$2);
        }
    }

    public record DataLoadContext(ResourceManager resources, WorldDataConfiguration dataConfiguration, HolderLookup.Provider datapackWorldgen, RegistryAccess.Frozen datapackDimensions) {
    }

    @FunctionalInterface
    public static interface WorldDataSupplier<D> {
        public DataLoadOutput<D> get(DataLoadContext var1);
    }

    public static final class DataLoadOutput<D>
    extends Record {
        final D cookie;
        final RegistryAccess.Frozen finalDimensions;

        public DataLoadOutput(D $$0, RegistryAccess.Frozen $$1) {
            this.cookie = $$0;
            this.finalDimensions = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataLoadOutput.class, "cookie;finalDimensions", "cookie", "finalDimensions"}, this, $$0);
        }

        public D cookie() {
            return this.cookie;
        }

        public RegistryAccess.Frozen finalDimensions() {
            return this.finalDimensions;
        }
    }

    @FunctionalInterface
    public static interface ResultFactory<D, R> {
        public R create(CloseableResourceManager var1, ReloadableServerResources var2, LayeredRegistryAccess<RegistryLayer> var3, D var4);
    }
}

