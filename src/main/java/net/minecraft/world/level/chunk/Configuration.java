package net.minecraft.world.level.chunk;

import java.util.List;

public interface Configuration {
   boolean alwaysRepack();

   int bitsInMemory();

   int bitsInStorage();

   <T> Palette<T> createPalette(Strategy<T> var1, List<T> var2);

   public record Global(int bitsInMemory, int bitsInStorage) implements Configuration {
      @Override
      public boolean alwaysRepack() {
         return true;
      }

      @Override
      public <T> Palette<T> createPalette(Strategy<T> $$0, List<T> $$1) {
         return $$0.globalPalette();
      }
   }

   public record Simple(Palette.Factory factory, int bits) implements Configuration {
      @Override
      public boolean alwaysRepack() {
         return false;
      }

      @Override
      public <T> Palette<T> createPalette(Strategy<T> $$0, List<T> $$1) {
         return this.factory.create(this.bits, $$1);
      }

      @Override
      public int bitsInMemory() {
         return this.bits;
      }

      @Override
      public int bitsInStorage() {
         return this.bits;
      }
   }
}
