package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public class GlobalPalette<T> implements Palette<T> {
   private final IdMap<T> registry;

   public GlobalPalette(IdMap<T> $$0) {
      this.registry = $$0;
   }

   @Override
   public int idFor(T $$0, PaletteResize<T> $$1) {
      int $$2 = this.registry.getId($$0);
      return $$2 == -1 ? 0 : $$2;
   }

   @Override
   public boolean maybeHas(Predicate<T> $$0) {
      return true;
   }

   @Override
   public T valueFor(int $$0) {
      T $$1 = (T)this.registry.byId($$0);
      if ($$1 == null) {
         throw new MissingPaletteEntryException($$0);
      } else {
         return $$1;
      }
   }

   @Override
   public void read(FriendlyByteBuf $$0, IdMap<T> $$1) {
   }

   @Override
   public void write(FriendlyByteBuf $$0, IdMap<T> $$1) {
   }

   @Override
   public int getSerializedSize(IdMap<T> $$0) {
      return 0;
   }

   @Override
   public int getSize() {
      return this.registry.size();
   }

   @Override
   public Palette<T> copy() {
      return this;
   }
}
