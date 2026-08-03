package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class IdDispatchCodec<B extends ByteBuf, V, T> implements StreamCodec<B, V> {
   private static final int UNKNOWN_TYPE = -1;
   private final Function<V, ? extends T> typeGetter;
   private final List<IdDispatchCodec.Entry<B, V, T>> byId;
   private final Object2IntMap<T> toId;

   IdDispatchCodec(Function<V, ? extends T> $$0, List<IdDispatchCodec.Entry<B, V, T>> $$1, Object2IntMap<T> $$2) {
      this.typeGetter = $$0;
      this.byId = $$1;
      this.toId = $$2;
   }

   public V decode(B $$0) {
      int $$1 = net.minecraft.network.VarInt.read($$0);
      if ($$1 >= 0 && $$1 < this.byId.size()) {
         IdDispatchCodec.Entry<B, V, T> $$2 = this.byId.get($$1);

         try {
            return (V)$$2.serializer.decode($$0);
         } catch (Exception var5) {
            if (var5 instanceof IdDispatchCodec.DontDecorateException) {
               throw var5;
            } else {
               throw new DecoderException("Failed to decode packet '" + $$2.type + "'", var5);
            }
         }
      } else {
         throw new DecoderException("Received unknown packet id " + $$1);
      }
   }

   public void encode(B $$0, V $$1) {
      T $$2 = (T)this.typeGetter.apply($$1);
      int $$3 = this.toId.getOrDefault($$2, -1);
      if ($$3 == -1) {
         throw new EncoderException("Sending unknown packet '" + $$2 + "'");
      } else {
         net.minecraft.network.VarInt.write($$0, $$3);
         IdDispatchCodec.Entry<B, V, T> $$4 = this.byId.get($$3);

         try {
            StreamCodec<? super B, V> $$5 = (StreamCodec<? super B, V>)$$4.serializer;
            $$5.encode($$0, $$1);
         } catch (Exception var7) {
            if (var7 instanceof IdDispatchCodec.DontDecorateException) {
               throw var7;
            } else {
               throw new EncoderException("Failed to encode packet '" + $$2 + "'", var7);
            }
         }
      }
   }

   public static <B extends ByteBuf, V, T> IdDispatchCodec.Builder<B, V, T> builder(Function<V, ? extends T> $$0) {
      return new IdDispatchCodec.Builder<>($$0);
   }

   public static class Builder<B extends ByteBuf, V, T> {
      private final List<IdDispatchCodec.Entry<B, V, T>> entries = new ArrayList<>();
      private final Function<V, ? extends T> typeGetter;

      Builder(Function<V, ? extends T> $$0) {
         this.typeGetter = $$0;
      }

      public IdDispatchCodec.Builder<B, V, T> add(T $$0, StreamCodec<? super B, ? extends V> $$1) {
         this.entries.add(new IdDispatchCodec.Entry<>($$1, $$0));
         return this;
      }

      public IdDispatchCodec<B, V, T> build() {
         Object2IntOpenHashMap<T> $$0 = new Object2IntOpenHashMap();
         $$0.defaultReturnValue(-2);

         for (IdDispatchCodec.Entry<B, V, T> $$1 : this.entries) {
            int $$2 = $$0.size();
            int $$3 = $$0.putIfAbsent($$1.type, $$2);
            if ($$3 != -2) {
               throw new IllegalStateException("Duplicate registration for type " + $$1.type);
            }
         }

         return new IdDispatchCodec<>(this.typeGetter, List.copyOf(this.entries), $$0);
      }
   }

   public interface DontDecorateException {
   }

   record Entry<B, V, T>(StreamCodec<? super B, ? extends V> serializer, T type) {
   }
}
