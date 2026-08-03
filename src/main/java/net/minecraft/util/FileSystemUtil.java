package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.slf4j.Logger;

public class FileSystemUtil {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static Path safeGetPath(URI $$0) throws IOException {
      try {
         return Paths.get($$0);
      } catch (FileSystemNotFoundException var3) {
      } catch (Throwable var4) {
         LOGGER.warn("Unable to get path for: {}", $$0, var4);
      }

      try {
         FileSystems.newFileSystem($$0, Collections.emptyMap());
      } catch (FileSystemAlreadyExistsException var2) {
      }

      return Paths.get($$0);
   }
}
