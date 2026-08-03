package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {
   private final PathMatcher symlinkTargetAllowList;

   public DirectoryValidator(PathMatcher $$0) {
      this.symlinkTargetAllowList = $$0;
   }

   public void validateSymlink(Path $$0, List<ForbiddenSymlinkInfo> $$1) throws IOException {
      Path $$2 = Files.readSymbolicLink($$0);
      if (!this.symlinkTargetAllowList.matches($$2)) {
         $$1.add(new ForbiddenSymlinkInfo($$0, $$2));
      }
   }

   public List<ForbiddenSymlinkInfo> validateSymlink(Path $$0) throws IOException {
      List<ForbiddenSymlinkInfo> $$1 = new ArrayList<>();
      this.validateSymlink($$0, $$1);
      return $$1;
   }

   public List<ForbiddenSymlinkInfo> validateDirectory(Path $$0, boolean $$1) throws IOException {
      List<ForbiddenSymlinkInfo> $$2 = new ArrayList<>();

      BasicFileAttributes $$3;
      try {
         $$3 = Files.readAttributes($$0, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      } catch (NoSuchFileException var6) {
         return $$2;
      }

      if ($$3.isRegularFile()) {
         throw new IOException("Path " + $$0 + " is not a directory");
      } else {
         if ($$3.isSymbolicLink()) {
            if (!$$1) {
               this.validateSymlink($$0, $$2);
               return $$2;
            }

            $$0 = Files.readSymbolicLink($$0);
         }

         this.validateKnownDirectory($$0, $$2);
         return $$2;
      }
   }

   public void validateKnownDirectory(Path $$0, final List<ForbiddenSymlinkInfo> $$1) throws IOException {
      Files.walkFileTree($$0, new SimpleFileVisitor<Path>() {
         private void validateSymlink(Path $$0, BasicFileAttributes $$1x) throws IOException {
            if ($$1.isSymbolicLink()) {
               DirectoryValidator.this.validateSymlink($$0, $$1);
            }
         }

         public FileVisitResult preVisitDirectory(Path $$0, BasicFileAttributes $$1x) throws IOException {
            this.validateSymlink($$0, $$1);
            return super.preVisitDirectory($$0, $$1);
         }

         public FileVisitResult visitFile(Path $$0, BasicFileAttributes $$1x) throws IOException {
            this.validateSymlink($$0, $$1);
            return super.visitFile($$0, $$1);
         }
      });
   }
}
