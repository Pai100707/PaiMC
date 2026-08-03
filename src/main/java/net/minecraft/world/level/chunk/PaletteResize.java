package net.minecraft.world.level.chunk;

public interface PaletteResize<T> {
   int onResize(int var1, T var2);

   static <T> PaletteResize<T> noResizeExpected() {
      return ($$0, $$1) -> {
         throw new IllegalArgumentException("Unexpected palette resize, bits = " + $$0 + ", added value = " + $$1);
      };
   }
}
