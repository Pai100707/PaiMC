package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class PlayerDataStorage {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final File playerDir;
   protected final DataFixer fixerUpper;

   public PlayerDataStorage(LevelStorageSource.LevelStorageAccess $$0, DataFixer $$1) {
      this.fixerUpper = $$1;
      this.playerDir = $$0.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
      this.playerDir.mkdirs();
   }

   public void save(Player $$0) {
      try {
         ScopedCollector $$1 = new ScopedCollector($$0.problemPath(), LOGGER);

         try {
            TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0.registryAccess());
            $$0.saveWithoutId($$2);
            Path $$3 = this.playerDir.toPath();
            Path $$4 = Files.createTempFile($$3, $$0.getStringUUID() + "-", ".dat");
            CompoundTag $$5 = $$2.buildResult();
            NbtIo.writeCompressed($$5, $$4);
            Path $$6 = $$3.resolve($$0.getStringUUID() + ".dat");
            Path $$7 = $$3.resolve($$0.getStringUUID() + ".dat_old");
            Util.safeReplaceFile($$6, $$4, $$7);
         } catch (Throwable var10) {
            try {
               $$1.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         $$1.close();
      } catch (Exception var11) {
         LOGGER.warn("Failed to save player data for {}", $$0.getPlainTextName());
      }
   }

   private void backup(NameAndId $$0, String $$1) {
      Path $$2 = this.playerDir.toPath();
      String $$3 = $$0.id().toString();
      Path $$4 = $$2.resolve($$3 + $$1);
      Path $$5 = $$2.resolve($$3 + "_corrupted_" + ZonedDateTime.now().format(FileNameDateFormatter.FORMATTER) + $$1);
      if (Files.isRegularFile($$4)) {
         try {
            Files.copy($$4, $$5, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
         } catch (Exception var8) {
            LOGGER.warn("Failed to copy the player.dat file for {}", $$0.name(), var8);
         }
      }
   }

   private Optional<CompoundTag> load(NameAndId $$0, String $$1) {
      File $$2 = new File(this.playerDir, $$0.id() + $$1);
      if ($$2.exists() && $$2.isFile()) {
         try {
            return Optional.of(NbtIo.readCompressed($$2.toPath(), NbtAccounter.unlimitedHeap()));
         } catch (Exception var5) {
            LOGGER.warn("Failed to load player data for {}", $$0.name());
         }
      }

      return Optional.empty();
   }

   public Optional<CompoundTag> load(NameAndId $$0) {
      Optional<CompoundTag> $$1 = this.load($$0, ".dat");
      if ($$1.isEmpty()) {
         this.backup($$0, ".dat");
      }

      return $$1.or(() -> this.load($$0, ".dat_old")).map($$0x -> {
         int $$1x = NbtUtils.getDataVersion($$0x);
         return DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, $$0x, $$1x);
      });
   }
}
