package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import org.apache.commons.lang3.Validate;

public class LinearPalette<T> implements Palette<T> {
   private final T[] values;
   private final int bits;
   private int size;

   private LinearPalette(int $$0, List<T> $$1) {
      this.values = (T[])(new Object[1 << $$0]);
      this.bits = $$0;
      Validate.isTrue(
         $$1.size() <= this.values.length, "Can't initialize LinearPalette of size %d with %d entries", new Object[]{this.values.length, $$1.size()}
      );

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         this.values[$$2] = $$1.get($$2);
      }

      this.size = $$1.size();
   }

   private LinearPalette(T[] $$0, int $$1, int $$2) {
      this.values = $$0;
      this.bits = $$1;
      this.size = $$2;
   }

   public static <A> Palette<A> create(int $$0, List<A> $$1) {
      return new LinearPalette<>($$0, $$1);
   }

   @Override
   public int idFor(T $$0, PaletteResize<T> $$1) {
      for (int $$2 = 0; $$2 < this.size; $$2++) {
         if (this.values[$$2] == $$0) {
            return $$2;
         }
      }

      int $$3 = this.size;
      if ($$3 < this.values.length) {
         this.values[$$3] = $$0;
         this.size++;
         return $$3;
      } else {
         return $$1.onResize(this.bits + 1, $$0);
      }
   }

   @Override
   public boolean maybeHas(Predicate<T> $$0) {
      for (int $$1 = 0; $$1 < this.size; $$1++) {
         if ($$0.test(this.values[$$1])) {
            return true;
         }
      }

      return false;
   }

   @Override
   public T valueFor(int $$0) {
      if ($$0 >= 0 && $$0 < this.size) {
         return this.values[$$0];
      } else {
         throw new MissingPaletteEntryException($$0);
      }
   }

   @Override
   public void read(FriendlyByteBuf $$0, IdMap<T> $$1) {
      this.size = $$0.readVarInt();

      for (int $$2 = 0; $$2 < this.size; $$2++) {
         this.values[$$2] = (T)$$1.byIdOrThrow($$0.readVarInt());
      }
   }

   @Override
   public void write(FriendlyByteBuf $$0, IdMap<T> $$1) {
      $$0.writeVarInt(this.size);

      for (int $$2 = 0; $$2 < this.size; $$2++) {
         $$0.writeVarInt($$1.getId(this.values[$$2]));
      }
   }

   @Override
   public int getSerializedSize(IdMap<T> $$0) {
      int $$1 = VarInt.getByteSize(this.getSize());

      for (int $$2 = 0; $$2 < this.getSize(); $$2++) {
         $$1 += VarInt.getByteSize($$0.getId(this.values[$$2]));
      }

      return $$1;
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public Palette<T> copy() {
      return new LinearPalette<>((T[])((Object[])this.values.clone()), this.bits, this.size);
   }
}
