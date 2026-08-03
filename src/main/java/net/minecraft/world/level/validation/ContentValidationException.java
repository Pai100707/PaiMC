package net.minecraft.world.level.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ContentValidationException extends Exception {
   private final Path directory;
   private final List<ForbiddenSymlinkInfo> entries;

   public ContentValidationException(Path $$0, List<ForbiddenSymlinkInfo> $$1) {
      this.directory = $$0;
      this.entries = $$1;
   }

   @Override
   public String getMessage() {
      return getMessage(this.directory, this.entries);
   }

   public static String getMessage(Path $$0, List<ForbiddenSymlinkInfo> $$1) {
      return "Failed to validate '"
         + $$0
         + "'. Found forbidden symlinks: "
         + $$1.stream().map($$0x -> $$0x.link() + "->" + $$0x.target()).collect(Collectors.joining(", "));
   }
}
