package net.minecraft.server.packs.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public abstract class PackDetector<T> {
   private final DirectoryValidator validator;

   protected PackDetector(DirectoryValidator $$0) {
      this.validator = $$0;
   }

   
   public T detectPackResources(Path $$0, List<ForbiddenSymlinkInfo> $$1) throws IOException {
      Path $$2 = $$0;

      BasicFileAttributes $$3;
      try {
         $$3 = Files.readAttributes($$0, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      } catch (NoSuchFileException var6) {
         return null;
      }

      if ($$3.isSymbolicLink()) {
         this.validator.validateSymlink($$0, $$1);
         if (!$$1.isEmpty()) {
            return null;
         }

         $$2 = Files.readSymbolicLink($$0);
         $$3 = Files.readAttributes($$2, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      }

      if ($$3.isDirectory()) {
         this.validator.validateKnownDirectory($$2, $$1);
         if (!$$1.isEmpty()) {
            return null;
         } else {
            return !Files.isRegularFile($$2.resolve("pack.mcmeta")) ? null : this.createDirectoryPack($$2);
         }
      } else {
         return $$3.isRegularFile() && $$2.getFileName().toString().endsWith(".zip") ? this.createZipPack($$2) : null;
      }
   }

   
   protected abstract T createZipPack(Path var1) throws IOException;

   
   protected abstract T createDirectoryPack(Path var1) throws IOException;
}
