package net.minecraft.world.level.chunk;

public class MissingPaletteEntryException extends RuntimeException {
   public MissingPaletteEntryException(int $$0) {
      super("Missing Palette entry for index " + $$0 + ".");
   }
}
