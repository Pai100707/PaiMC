package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {
   static <B, V> StreamCodec<B, V> of(final StreamEncoder<B, V> $$0, final StreamDecoder<B, V> $$1) {
      return new StreamCodec<B, V>() {
         @Override
         public V decode(B $$0x) {
            return $$1.decode($$0);
         }

         @Override
         public void encode(B $$0x, V $$1x) {
            $$0.encode($$0, $$1);
         }
      };
   }

   static <B, V> StreamCodec<B, V> ofMember(final StreamMemberEncoder<B, V> $$0, final StreamDecoder<B, V> $$1) {
      return new StreamCodec<B, V>() {
         @Override
         public V decode(B $$0x) {
            return $$1.decode($$0);
         }

         @Override
         public void encode(B $$0x, V $$1x) {
            $$0.encode($$1, $$0);
         }
      };
   }

   static <B, V> StreamCodec<B, V> unit(final V $$0) {
      return new StreamCodec<B, V>() {
         @Override
         public V decode(B $$0x) {
            return $$0;
         }

         @Override
         public void encode(B $$0x, V $$1) {
            if (!$$1.equals($$0)) {
               throw new IllegalStateException("Can't encode '" + $$1 + "', expected '" + $$0 + "'");
            }
         }
      };
   }

   default <O> StreamCodec<B, O> apply(StreamCodec.CodecOperation<B, V, O> $$0) {
      return $$0.apply(this);
   }

   default <O> StreamCodec<B, O> map(final Function<? super V, ? extends O> $$0, final Function<? super O, ? extends V> $$1) {
      return new StreamCodec<B, O>() {
         @Override
         public O decode(B $$0x) {
            return (O)$$0.apply(StreamCodec.this.decode($$0));
         }

         @Override
         public void encode(B $$0x, O $$1x) {
            StreamCodec.this.encode($$0, (V)$$1.apply($$1));
         }
      };
   }

   default <O extends ByteBuf> StreamCodec<O, V> mapStream(final Function<O, ? extends B> $$0) {
      return new StreamCodec<O, V>() {
         public V decode(O $$0x) {
            B $$1 = (B)$$0.apply($$0);
            return StreamCodec.this.decode($$1);
         }

         public void encode(O $$0x, V $$1) {
            B $$2 = (B)$$0.apply($$0);
            StreamCodec.this.encode($$2, $$1);
         }
      };
   }

   default <U> StreamCodec<B, U> dispatch(
      final Function<? super U, ? extends V> $$0, final Function<? super V, ? extends StreamCodec<? super B, ? extends U>> $$1
   ) {
      return new StreamCodec<B, U>() {
         @Override
         public U decode(B $$0x) {
            V $$1x = StreamCodec.this.decode($$0);
            StreamCodec<? super B, ? extends U> $$2 = (StreamCodec<? super B, ? extends U>)$$1.apply($$1x);
            return (U)$$2.decode($$0);
         }

         @Override
         public void encode(B $$0x, U $$1x) {
            V $$2 = (V)$$0.apply($$1);
            StreamCodec<B, U> $$3 = (StreamCodec<B, U>)$$1.apply($$2);
            StreamCodec.this.encode($$0, $$2);
            $$3.encode($$0, $$1);
         }
      };
   }

   static <B, C, T1> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> $$0, final Function<C, T1> $$1, final Function<T1, C> $$2) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            return $$2.apply($$1x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
         }
      };
   }

   static <B, C, T1, T2> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final BiFunction<T1, T2, C> $$4
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            return $$4.apply($$1x, $$2x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final Function3<T1, T2, T3, C> $$6
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            return (C)$$6.apply($$1x, $$2x, $$3x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final Function4<T1, T2, T3, T4, C> $$8
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            return (C)$$8.apply($$1x, $$2x, $$3x, $$4x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final Function5<T1, T2, T3, T4, T5, C> $$10
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            return (C)$$10.apply($$1x, $$2x, $$3x, $$4x, $$5x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final Function6<T1, T2, T3, T4, T5, T6, C> $$12
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            return (C)$$12.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final StreamCodec<? super B, T7> $$12,
      final Function<C, T7> $$13,
      final Function7<T1, T2, T3, T4, T5, T6, T7, C> $$14
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            T7 $$7x = $$12.decode($$0);
            return (C)$$14.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x, $$7x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
            $$12.encode($$0, $$13.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final StreamCodec<? super B, T7> $$12,
      final Function<C, T7> $$13,
      final StreamCodec<? super B, T8> $$14,
      final Function<C, T8> $$15,
      final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> $$16
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            T7 $$7x = $$12.decode($$0);
            T8 $$8x = $$14.decode($$0);
            return (C)$$16.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x, $$7x, $$8x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
            $$12.encode($$0, $$13.apply($$1));
            $$14.encode($$0, $$15.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final StreamCodec<? super B, T7> $$12,
      final Function<C, T7> $$13,
      final StreamCodec<? super B, T8> $$14,
      final Function<C, T8> $$15,
      final StreamCodec<? super B, T9> $$16,
      final Function<C, T9> $$17,
      final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> $$18
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            T7 $$7x = $$12.decode($$0);
            T8 $$8x = $$14.decode($$0);
            T9 $$9x = $$16.decode($$0);
            return (C)$$18.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x, $$7x, $$8x, $$9x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
            $$12.encode($$0, $$13.apply($$1));
            $$14.encode($$0, $$15.apply($$1));
            $$16.encode($$0, $$17.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final StreamCodec<? super B, T7> $$12,
      final Function<C, T7> $$13,
      final StreamCodec<? super B, T8> $$14,
      final Function<C, T8> $$15,
      final StreamCodec<? super B, T9> $$16,
      final Function<C, T9> $$17,
      final StreamCodec<? super B, T10> $$18,
      final Function<C, T10> $$19,
      final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> $$20
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            T7 $$7x = $$12.decode($$0);
            T8 $$8x = $$14.decode($$0);
            T9 $$9x = $$16.decode($$0);
            T10 $$10x = $$18.decode($$0);
            return (C)$$20.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x, $$7x, $$8x, $$9x, $$10x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
            $$12.encode($$0, $$13.apply($$1));
            $$14.encode($$0, $$15.apply($$1));
            $$16.encode($$0, $$17.apply($$1));
            $$18.encode($$0, $$19.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final StreamCodec<? super B, T7> $$12,
      final Function<C, T7> $$13,
      final StreamCodec<? super B, T8> $$14,
      final Function<C, T8> $$15,
      final StreamCodec<? super B, T9> $$16,
      final Function<C, T9> $$17,
      final StreamCodec<? super B, T10> $$18,
      final Function<C, T10> $$19,
      final StreamCodec<? super B, T11> $$20,
      final Function<C, T11> $$21,
      final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> $$22
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            T7 $$7x = $$12.decode($$0);
            T8 $$8x = $$14.decode($$0);
            T9 $$9x = $$16.decode($$0);
            T10 $$10x = $$18.decode($$0);
            T11 $$11x = $$20.decode($$0);
            return (C)$$22.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x, $$7x, $$8x, $$9x, $$10x, $$11x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
            $$12.encode($$0, $$13.apply($$1));
            $$14.encode($$0, $$15.apply($$1));
            $$16.encode($$0, $$17.apply($$1));
            $$18.encode($$0, $$19.apply($$1));
            $$20.encode($$0, $$21.apply($$1));
         }
      };
   }

   static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(
      final StreamCodec<? super B, T1> $$0,
      final Function<C, T1> $$1,
      final StreamCodec<? super B, T2> $$2,
      final Function<C, T2> $$3,
      final StreamCodec<? super B, T3> $$4,
      final Function<C, T3> $$5,
      final StreamCodec<? super B, T4> $$6,
      final Function<C, T4> $$7,
      final StreamCodec<? super B, T5> $$8,
      final Function<C, T5> $$9,
      final StreamCodec<? super B, T6> $$10,
      final Function<C, T6> $$11,
      final StreamCodec<? super B, T7> $$12,
      final Function<C, T7> $$13,
      final StreamCodec<? super B, T8> $$14,
      final Function<C, T8> $$15,
      final StreamCodec<? super B, T9> $$16,
      final Function<C, T9> $$17,
      final StreamCodec<? super B, T10> $$18,
      final Function<C, T10> $$19,
      final StreamCodec<? super B, T11> $$20,
      final Function<C, T11> $$21,
      final StreamCodec<? super B, T12> $$22,
      final Function<C, T12> $$23,
      final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> $$24
   ) {
      return new StreamCodec<B, C>() {
         @Override
         public C decode(B $$0x) {
            T1 $$1x = $$0.decode($$0);
            T2 $$2x = $$2.decode($$0);
            T3 $$3x = $$4.decode($$0);
            T4 $$4x = $$6.decode($$0);
            T5 $$5x = $$8.decode($$0);
            T6 $$6x = $$10.decode($$0);
            T7 $$7x = $$12.decode($$0);
            T8 $$8x = $$14.decode($$0);
            T9 $$9x = $$16.decode($$0);
            T10 $$10x = $$18.decode($$0);
            T11 $$11x = $$20.decode($$0);
            T12 $$12x = $$22.decode($$0);
            return (C)$$24.apply($$1x, $$2x, $$3x, $$4x, $$5x, $$6x, $$7x, $$8x, $$9x, $$10x, $$11x, $$12x);
         }

         @Override
         public void encode(B $$0x, C $$1x) {
            $$0.encode($$0, $$1.apply($$1));
            $$2.encode($$0, $$3.apply($$1));
            $$4.encode($$0, $$5.apply($$1));
            $$6.encode($$0, $$7.apply($$1));
            $$8.encode($$0, $$9.apply($$1));
            $$10.encode($$0, $$11.apply($$1));
            $$12.encode($$0, $$13.apply($$1));
            $$14.encode($$0, $$15.apply($$1));
            $$16.encode($$0, $$17.apply($$1));
            $$18.encode($$0, $$19.apply($$1));
            $$20.encode($$0, $$21.apply($$1));
            $$22.encode($$0, $$23.apply($$1));
         }
      };
   }

   static <B, T> StreamCodec<B, T> recursive(final UnaryOperator<StreamCodec<B, T>> $$0) {
      return new StreamCodec<B, T>() {
         private final Supplier<StreamCodec<B, T>> inner = Suppliers.memoize(() -> $$0.apply(this));

         @Override
         public T decode(B $$0x) {
            return this.inner.get().decode($$0);
         }

         @Override
         public void encode(B $$0x, T $$1) {
            this.inner.get().encode($$0, $$1);
         }
      };
   }

   default <S extends B> StreamCodec<S, V> cast() {
      return this;
   }

   @FunctionalInterface
   public interface CodecOperation<B, S, T> {
      StreamCodec<B, T> apply(StreamCodec<B, S> var1);
   }
}
