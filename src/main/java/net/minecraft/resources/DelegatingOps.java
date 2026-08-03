package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class DelegatingOps<T> implements DynamicOps<T> {
   protected final DynamicOps<T> delegate;

   protected DelegatingOps(DynamicOps<T> $$0) {
      this.delegate = $$0;
   }

   public T empty() {
      return (T)this.delegate.empty();
   }

   public T emptyMap() {
      return (T)this.delegate.emptyMap();
   }

   public T emptyList() {
      return (T)this.delegate.emptyList();
   }

   public <U> U convertTo(DynamicOps<U> $$0, T $$1) {
      return (U)(Objects.equals($$0, this.delegate) ? $$1 : this.delegate.convertTo($$0, $$1));
   }

   public DataResult<Number> getNumberValue(T $$0) {
      return this.delegate.getNumberValue($$0);
   }

   public T createNumeric(Number $$0) {
      return (T)this.delegate.createNumeric($$0);
   }

   public T createByte(byte $$0) {
      return (T)this.delegate.createByte($$0);
   }

   public T createShort(short $$0) {
      return (T)this.delegate.createShort($$0);
   }

   public T createInt(int $$0) {
      return (T)this.delegate.createInt($$0);
   }

   public T createLong(long $$0) {
      return (T)this.delegate.createLong($$0);
   }

   public T createFloat(float $$0) {
      return (T)this.delegate.createFloat($$0);
   }

   public T createDouble(double $$0) {
      return (T)this.delegate.createDouble($$0);
   }

   public DataResult<Boolean> getBooleanValue(T $$0) {
      return this.delegate.getBooleanValue($$0);
   }

   public T createBoolean(boolean $$0) {
      return (T)this.delegate.createBoolean($$0);
   }

   public DataResult<String> getStringValue(T $$0) {
      return this.delegate.getStringValue($$0);
   }

   public T createString(String $$0) {
      return (T)this.delegate.createString($$0);
   }

   public DataResult<T> mergeToList(T $$0, T $$1) {
      return this.delegate.mergeToList($$0, $$1);
   }

   public DataResult<T> mergeToList(T $$0, List<T> $$1) {
      return this.delegate.mergeToList($$0, $$1);
   }

   public DataResult<T> mergeToMap(T $$0, T $$1, T $$2) {
      return this.delegate.mergeToMap($$0, $$1, $$2);
   }

   public DataResult<T> mergeToMap(T $$0, MapLike<T> $$1) {
      return this.delegate.mergeToMap($$0, $$1);
   }

   public DataResult<T> mergeToMap(T $$0, Map<T, T> $$1) {
      return this.delegate.mergeToMap($$0, $$1);
   }

   public DataResult<T> mergeToPrimitive(T $$0, T $$1) {
      return this.delegate.mergeToPrimitive($$0, $$1);
   }

   public DataResult<Stream<Pair<T, T>>> getMapValues(T $$0) {
      return this.delegate.getMapValues($$0);
   }

   public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T $$0) {
      return this.delegate.getMapEntries($$0);
   }

   public T createMap(Map<T, T> $$0) {
      return (T)this.delegate.createMap($$0);
   }

   public T createMap(Stream<Pair<T, T>> $$0) {
      return (T)this.delegate.createMap($$0);
   }

   public DataResult<MapLike<T>> getMap(T $$0) {
      return this.delegate.getMap($$0);
   }

   public DataResult<Stream<T>> getStream(T $$0) {
      return this.delegate.getStream($$0);
   }

   public DataResult<Consumer<Consumer<T>>> getList(T $$0) {
      return this.delegate.getList($$0);
   }

   public T createList(Stream<T> $$0) {
      return (T)this.delegate.createList($$0);
   }

   public DataResult<ByteBuffer> getByteBuffer(T $$0) {
      return this.delegate.getByteBuffer($$0);
   }

   public T createByteList(ByteBuffer $$0) {
      return (T)this.delegate.createByteList($$0);
   }

   public DataResult<IntStream> getIntStream(T $$0) {
      return this.delegate.getIntStream($$0);
   }

   public T createIntList(IntStream $$0) {
      return (T)this.delegate.createIntList($$0);
   }

   public DataResult<LongStream> getLongStream(T $$0) {
      return this.delegate.getLongStream($$0);
   }

   public T createLongList(LongStream $$0) {
      return (T)this.delegate.createLongList($$0);
   }

   public T remove(T $$0, String $$1) {
      return (T)this.delegate.remove($$0, $$1);
   }

   public boolean compressMaps() {
      return this.delegate.compressMaps();
   }

   public ListBuilder<T> listBuilder() {
      return new net.minecraft.resources.DelegatingOps.DelegateListBuilder(this.delegate.listBuilder());
   }

   public RecordBuilder<T> mapBuilder() {
      return new net.minecraft.resources.DelegatingOps.DelegateRecordBuilder(this.delegate.mapBuilder());
   }

   protected class DelegateListBuilder implements ListBuilder<T> {
      private final ListBuilder<T> original;

      protected DelegateListBuilder(final ListBuilder<T> $$1) {
         this.original = $$1;
      }

      public DynamicOps<T> ops() {
         return DelegatingOps.this;
      }

      public DataResult<T> build(T $$0) {
         return this.original.build($$0);
      }

      public ListBuilder<T> add(T $$0) {
         this.original.add($$0);
         return this;
      }

      public ListBuilder<T> add(DataResult<T> $$0) {
         this.original.add($$0);
         return this;
      }

      public <E> ListBuilder<T> add(E $$0, Encoder<E> $$1) {
         this.original.add($$1.encodeStart(this.ops(), $$0));
         return this;
      }

      public <E> ListBuilder<T> addAll(Iterable<E> $$0, Encoder<E> $$1) {
         $$0.forEach($$1x -> this.original.add($$1.encode($$1x, this.ops(), this.ops().empty())));
         return this;
      }

      public ListBuilder<T> withErrorsFrom(DataResult<?> $$0) {
         this.original.withErrorsFrom($$0);
         return this;
      }

      public ListBuilder<T> mapError(UnaryOperator<String> $$0) {
         this.original.mapError($$0);
         return this;
      }

      public DataResult<T> build(DataResult<T> $$0) {
         return this.original.build($$0);
      }
   }

   protected class DelegateRecordBuilder implements RecordBuilder<T> {
      private final RecordBuilder<T> original;

      protected DelegateRecordBuilder(final RecordBuilder<T> $$1) {
         this.original = $$1;
      }

      public DynamicOps<T> ops() {
         return DelegatingOps.this;
      }

      public RecordBuilder<T> add(T $$0, T $$1) {
         this.original.add($$0, $$1);
         return this;
      }

      public RecordBuilder<T> add(T $$0, DataResult<T> $$1) {
         this.original.add($$0, $$1);
         return this;
      }

      public RecordBuilder<T> add(DataResult<T> $$0, DataResult<T> $$1) {
         this.original.add($$0, $$1);
         return this;
      }

      public RecordBuilder<T> add(String $$0, T $$1) {
         this.original.add($$0, $$1);
         return this;
      }

      public RecordBuilder<T> add(String $$0, DataResult<T> $$1) {
         this.original.add($$0, $$1);
         return this;
      }

      public <E> RecordBuilder<T> add(String $$0, E $$1, Encoder<E> $$2) {
         return this.original.add($$0, $$2.encodeStart(this.ops(), $$1));
      }

      public RecordBuilder<T> withErrorsFrom(DataResult<?> $$0) {
         this.original.withErrorsFrom($$0);
         return this;
      }

      public RecordBuilder<T> setLifecycle(Lifecycle $$0) {
         this.original.setLifecycle($$0);
         return this;
      }

      public RecordBuilder<T> mapError(UnaryOperator<String> $$0) {
         this.original.mapError($$0);
         return this;
      }

      public DataResult<T> build(T $$0) {
         return this.original.build($$0);
      }

      public DataResult<T> build(DataResult<T> $$0) {
         return this.original.build($$0);
      }
   }
}
