package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T> {
   private final CrudeIncrementalIntIdentityHashBiMap<T> values;
   private final int bits;

   public HashMapPalette(int $$0, List<T> $$1) {
      this($$0);
      $$1.forEach(this.values::add);
   }

   public HashMapPalette(int $$0) {
      this($$0, CrudeIncrementalIntIdentityHashBiMap.create(1 << $$0));
   }

   private HashMapPalette(int $$0, CrudeIncrementalIntIdentityHashBiMap<T> $$1) {
      this.bits = $$0;
      this.values = $$1;
   }

   public static <A> Palette<A> create(int $$0, List<A> $$1) {
      return new HashMapPalette<>($$0, $$1);
   }

   @Override
   public int idFor(T $$0, PaletteResize<T> $$1) {
      int $$2 = this.values.getId($$0);
      if ($$2 == -1) {
         $$2 = this.values.add($$0);
         if ($$2 >= 1 << this.bits) {
            $$2 = $$1.onResize(this.bits + 1, $$0);
         }
      }

      return $$2;
   }

   @Override
   public boolean maybeHas(Predicate<T> $$0) {
      for (int $$1 = 0; $$1 < this.getSize(); $$1++) {
         if ($$0.test((T)this.values.byId($$1))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public T valueFor(int $$0) {
      T $$1 = (T)this.values.byId($$0);
      if ($$1 == null) {
         throw new MissingPaletteEntryException($$0);
      } else {
         return $$1;
      }
   }

   @Override
   public void read(FriendlyByteBuf $$0, IdMap<T> $$1) {
      this.values.clear();
      int $$2 = $$0.readVarInt();

      for (int $$3 = 0; $$3 < $$2; $$3++) {
         this.values.add($$1.byIdOrThrow($$0.readVarInt()));
      }
   }

   @Override
   public void write(FriendlyByteBuf $$0, IdMap<T> $$1) {
      int $$2 = this.getSize();
      $$0.writeVarInt($$2);

      for (int $$3 = 0; $$3 < $$2; $$3++) {
         $$0.writeVarInt($$1.getId(this.values.byId($$3)));
      }
   }

   @Override
   public int getSerializedSize(IdMap<T> $$0) {
      int $$1 = VarInt.getByteSize(this.getSize());

      for (int $$2 = 0; $$2 < this.getSize(); $$2++) {
         $$1 += VarInt.getByteSize($$0.getId(this.values.byId($$2)));
      }

      return $$1;
   }

   public List<T> getEntries() {
      ArrayList<T> $$0 = new ArrayList<>();
      this.values.iterator().forEachRemaining($$0::add);
      return $$0;
   }

   @Override
   public int getSize() {
      return this.values.size();
   }

   @Override
   public Palette<T> copy() {
      return new HashMapPalette<>(this.bits, this.values.copy());
   }
}
