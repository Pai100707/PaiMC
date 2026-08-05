package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import org.apache.commons.lang3.Validate;

public class SingleValuePalette<T> implements Palette<T> {
   
   private T value;

   public SingleValuePalette(List<T> $$0) {
      if (!$$0.isEmpty()) {
         Validate.isTrue($$0.size() <= 1, "Can't initialize SingleValuePalette with %d values.", $$0.size());
         this.value = $$0.getFirst();
      }
   }

   public static <A> Palette<A> create(int $$0, List<A> $$1) {
      return new SingleValuePalette<>($$1);
   }

   @Override
   public int idFor(T $$0, PaletteResize<T> $$1) {
      if (this.value != null && this.value != $$0) {
         return $$1.onResize(1, $$0);
      } else {
         this.value = $$0;
         return 0;
      }
   }

   @Override
   public boolean maybeHas(Predicate<T> $$0) {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         return $$0.test(this.value);
      }
   }

   @Override
   public T valueFor(int $$0) {
      if (this.value != null && $$0 == 0) {
         return this.value;
      } else {
         throw new IllegalStateException("Missing Palette entry for id " + $$0 + ".");
      }
   }

   @Override
   public void read(FriendlyByteBuf $$0, IdMap<T> $$1) {
      this.value = (T)$$1.byIdOrThrow($$0.readVarInt());
   }

   @Override
   public void write(FriendlyByteBuf $$0, IdMap<T> $$1) {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         $$0.writeVarInt($$1.getId(this.value));
      }
   }

   @Override
   public int getSerializedSize(IdMap<T> $$0) {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         return VarInt.getByteSize($$0.getId(this.value));
      }
   }

   @Override
   public int getSize() {
      return 1;
   }

   @Override
   public Palette<T> copy() {
      if (this.value == null) {
         throw new IllegalStateException("Use of an uninitialized palette");
      } else {
         return this;
      }
   }
}
