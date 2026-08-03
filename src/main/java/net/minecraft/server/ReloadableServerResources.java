package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

public class ReloadableServerResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
   private final net.minecraft.server.ReloadableServerRegistries.Holder fullRegistryHolder;
   private final Commands commands;
   private final RecipeManager recipes;
   private final net.minecraft.server.ServerAdvancementManager advancements;
   private final net.minecraft.server.ServerFunctionLibrary functionLibrary;
   private final List<PendingTags<?>> postponedTags;

   private ReloadableServerResources(
      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$0,
      Provider $$1,
      FeatureFlagSet $$2,
      CommandSelection $$3,
      List<PendingTags<?>> $$4,
      PermissionSet $$5
   ) {
      this.fullRegistryHolder = new net.minecraft.server.ReloadableServerRegistries.Holder($$0.compositeAccess());
      this.postponedTags = $$4;
      this.recipes = new RecipeManager($$1);
      this.commands = new Commands($$3, CommandBuildContext.simple($$1, $$2));
      this.advancements = new net.minecraft.server.ServerAdvancementManager($$1);
      this.functionLibrary = new net.minecraft.server.ServerFunctionLibrary($$5, this.commands.getDispatcher());
   }

   public net.minecraft.server.ServerFunctionLibrary getFunctionLibrary() {
      return this.functionLibrary;
   }

   public net.minecraft.server.ReloadableServerRegistries.Holder fullRegistries() {
      return this.fullRegistryHolder;
   }

   public RecipeManager getRecipeManager() {
      return this.recipes;
   }

   public Commands getCommands() {
      return this.commands;
   }

   public net.minecraft.server.ServerAdvancementManager getAdvancements() {
      return this.advancements;
   }

   public List<PreparableReloadListener> listeners() {
      return List.of(this.recipes, this.functionLibrary, this.advancements);
   }

   public static CompletableFuture<net.minecraft.server.ReloadableServerResources> loadResources(
      ResourceManager $$0,
      LayeredRegistryAccess<net.minecraft.server.RegistryLayer> $$1,
      List<PendingTags<?>> $$2,
      FeatureFlagSet $$3,
      CommandSelection $$4,
      PermissionSet $$5,
      Executor $$6,
      Executor $$7
   ) {
      return net.minecraft.server.ReloadableServerRegistries.reload($$1, $$2, $$0, $$6)
         .thenCompose(
            $$7x -> {
               net.minecraft.server.ReloadableServerResources $$8 = new net.minecraft.server.ReloadableServerResources(
                  $$7x.layers(), $$7x.lookupWithUpdatedTags(), $$3, $$4, $$2, $$5
               );
               return SimpleReloadInstance.create($$0, $$8.listeners(), $$6, $$7, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled())
                  .done()
                  .thenApply($$1xx -> $$8);
            }
         );
   }

   public void updateStaticRegistryTags() {
      this.postponedTags.forEach(PendingTags::apply);
   }
}
