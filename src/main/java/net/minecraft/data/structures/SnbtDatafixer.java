package net.minecraft.data.structures;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;

public class SnbtDatafixer {
   public static void main(String[] $$0) throws IOException {
      SharedConstants.setVersion(DetectedVersion.BUILT_IN);
      Bootstrap.bootStrap();

      for (String $$1 : $$0) {
         updateInDirectory($$1);
      }
   }

   private static void updateInDirectory(String $$0) throws IOException {
      try (Stream<Path> $$1 = Files.walk(Paths.get($$0))) {
         $$1.filter($$0x -> $$0x.toString().endsWith(".snbt")).forEach($$0x -> {
            try {
               String $$1x = Files.readString($$0x);
               CompoundTag $$2 = NbtUtils.snbtToStructure($$1x);
               CompoundTag $$3 = StructureUpdater.update($$0x.toString(), $$2);
               NbtToSnbt.writeSnbt(net.minecraft.data.CachedOutput.NO_CACHE, $$0x, NbtUtils.structureToSnbt($$3));
            } catch (IOException | CommandSyntaxException var4) {
               throw new RuntimeException(var4);
            }
         });
      }
   }
}
