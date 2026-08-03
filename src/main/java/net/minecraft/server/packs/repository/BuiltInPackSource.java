package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BuiltInPackSource implements RepositorySource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String VANILLA_ID = "vanilla";
   public static final String TESTS_ID = "tests";
   public static final KnownPack CORE_PACK_INFO = KnownPack.vanilla("core");
   private final PackType packType;
   private final VanillaPackResources vanillaPack;
   private final Identifier packDir;
   private final DirectoryValidator validator;

   public BuiltInPackSource(PackType $$0, VanillaPackResources $$1, Identifier $$2, DirectoryValidator $$3) {
      this.packType = $$0;
      this.vanillaPack = $$1;
      this.packDir = $$2;
      this.validator = $$3;
   }

   @Override
   public void loadPacks(Consumer<Pack> $$0) {
      Pack $$1 = this.createVanillaPack(this.vanillaPack);
      if ($$1 != null) {
         $$0.accept($$1);
      }

      this.listBundledPacks($$0);
   }

   @Nullable
   protected abstract Pack createVanillaPack(PackResources var1);

   protected abstract Component getPackTitle(String var1);

   public VanillaPackResources getVanillaPack() {
      return this.vanillaPack;
   }

   private void listBundledPacks(Consumer<Pack> $$0) {
      Map<String, Function<String, Pack>> $$1 = new HashMap<>();
      this.populatePackList($$1::put);
      $$1.forEach(($$1x, $$2) -> {
         Pack $$3 = $$2.apply($$1x);
         if ($$3 != null) {
            $$0.accept($$3);
         }
      });
   }

   protected void populatePackList(BiConsumer<String, Function<String, Pack>> $$0) {
      this.vanillaPack.listRawPaths(this.packType, this.packDir, $$1 -> this.discoverPacksInPath($$1, $$0));
   }

   protected void discoverPacksInPath(@Nullable Path $$0, BiConsumer<String, Function<String, Pack>> $$1) {
      if ($$0 != null && Files.isDirectory($$0)) {
         try {
            FolderRepositorySource.discoverPacks(
               $$0, this.validator, ($$1x, $$2) -> $$1.accept(pathToId($$1x), $$1xx -> this.createBuiltinPack($$1xx, $$2, this.getPackTitle($$1xx)))
            );
         } catch (IOException var4) {
            LOGGER.warn("Failed to discover packs in {}", $$0, var4);
         }
      }
   }

   private static String pathToId(Path $$0) {
      return StringUtils.removeEnd($$0.getFileName().toString(), ".zip");
   }

   @Nullable
   protected abstract Pack createBuiltinPack(String var1, Pack.ResourcesSupplier var2, Component var3);

   protected static Pack.ResourcesSupplier fixedResources(final PackResources $$0) {
      return new Pack.ResourcesSupplier() {
         @Override
         public PackResources openPrimary(PackLocationInfo $$0x) {
            return $$0;
         }

         @Override
         public PackResources openFull(PackLocationInfo $$0x, Pack.Metadata $$1) {
            return $$0;
         }
      };
   }
}
