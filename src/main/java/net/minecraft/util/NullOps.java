package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NullOps implements DynamicOps<net.minecraft.util.Unit> {
   public static final net.minecraft.util.NullOps INSTANCE = new net.minecraft.util.NullOps();
   private static final MapLike<net.minecraft.util.Unit> EMPTY_MAP = new MapLike<net.minecraft.util.Unit>() {
      
      public net.minecraft.util.Unit get(net.minecraft.util.Unit $$0) {
         return null;
      }

      
      public net.minecraft.util.Unit get(String $$0) {
         return null;
      }

      public Stream<Pair<net.minecraft.util.Unit, net.minecraft.util.Unit>> entries() {
         return Stream.empty();
      }
   };

   private NullOps() {
   }

   public <U> U convertTo(DynamicOps<U> $$0, net.minecraft.util.Unit $$1) {
      return (U)$$0.empty();
   }

   public net.minecraft.util.Unit empty() {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit emptyMap() {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit emptyList() {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createNumeric(Number $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createByte(byte $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createShort(short $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createInt(int $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createLong(long $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createFloat(float $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createDouble(double $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createBoolean(boolean $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createString(String $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public DataResult<Number> getNumberValue(net.minecraft.util.Unit $$0) {
      return DataResult.success(0);
   }

   public DataResult<Boolean> getBooleanValue(net.minecraft.util.Unit $$0) {
      return DataResult.success(false);
   }

   public DataResult<String> getStringValue(net.minecraft.util.Unit $$0) {
      return DataResult.success("");
   }

   public DataResult<net.minecraft.util.Unit> mergeToList(net.minecraft.util.Unit $$0, net.minecraft.util.Unit $$1) {
      return DataResult.success(net.minecraft.util.Unit.INSTANCE);
   }

   public DataResult<net.minecraft.util.Unit> mergeToList(net.minecraft.util.Unit $$0, List<net.minecraft.util.Unit> $$1) {
      return DataResult.success(net.minecraft.util.Unit.INSTANCE);
   }

   public DataResult<net.minecraft.util.Unit> mergeToMap(net.minecraft.util.Unit $$0, net.minecraft.util.Unit $$1, net.minecraft.util.Unit $$2) {
      return DataResult.success(net.minecraft.util.Unit.INSTANCE);
   }

   public DataResult<net.minecraft.util.Unit> mergeToMap(net.minecraft.util.Unit $$0, Map<net.minecraft.util.Unit, net.minecraft.util.Unit> $$1) {
      return DataResult.success(net.minecraft.util.Unit.INSTANCE);
   }

   public DataResult<net.minecraft.util.Unit> mergeToMap(net.minecraft.util.Unit $$0, MapLike<net.minecraft.util.Unit> $$1) {
      return DataResult.success(net.minecraft.util.Unit.INSTANCE);
   }

   public DataResult<Stream<Pair<net.minecraft.util.Unit, net.minecraft.util.Unit>>> getMapValues(net.minecraft.util.Unit $$0) {
      return DataResult.success(Stream.empty());
   }

   public DataResult<Consumer<BiConsumer<net.minecraft.util.Unit, net.minecraft.util.Unit>>> getMapEntries(net.minecraft.util.Unit $$0) {
      return DataResult.success((Consumer<BiConsumer>)$$0x -> {});
   }

   public DataResult<MapLike<net.minecraft.util.Unit>> getMap(net.minecraft.util.Unit $$0) {
      return DataResult.success(EMPTY_MAP);
   }

   public DataResult<Stream<net.minecraft.util.Unit>> getStream(net.minecraft.util.Unit $$0) {
      return DataResult.success(Stream.empty());
   }

   public DataResult<Consumer<Consumer<net.minecraft.util.Unit>>> getList(net.minecraft.util.Unit $$0) {
      return DataResult.success((Consumer<Consumer>)$$0x -> {});
   }

   public DataResult<ByteBuffer> getByteBuffer(net.minecraft.util.Unit $$0) {
      return DataResult.success(ByteBuffer.wrap(new byte[0]));
   }

   public DataResult<IntStream> getIntStream(net.minecraft.util.Unit $$0) {
      return DataResult.success(IntStream.empty());
   }

   public DataResult<LongStream> getLongStream(net.minecraft.util.Unit $$0) {
      return DataResult.success(LongStream.empty());
   }

   public net.minecraft.util.Unit createMap(Stream<Pair<net.minecraft.util.Unit, net.minecraft.util.Unit>> $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createMap(Map<net.minecraft.util.Unit, net.minecraft.util.Unit> $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createList(Stream<net.minecraft.util.Unit> $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createByteList(ByteBuffer $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createIntList(IntStream $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit createLongList(LongStream $$0) {
      return net.minecraft.util.Unit.INSTANCE;
   }

   public net.minecraft.util.Unit remove(net.minecraft.util.Unit $$0, String $$1) {
      return $$0;
   }

   public RecordBuilder<net.minecraft.util.Unit> mapBuilder() {
      return new net.minecraft.util.NullOps.NullMapBuilder(this);
   }

   public ListBuilder<net.minecraft.util.Unit> listBuilder() {
      return new net.minecraft.util.NullOps.NullListBuilder(this);
   }

   @Override
   public String toString() {
      return "Null";
   }

   static final class NullListBuilder extends net.minecraft.util.AbstractListBuilder<net.minecraft.util.Unit, net.minecraft.util.Unit> {
      public NullListBuilder(DynamicOps<net.minecraft.util.Unit> $$0) {
         super($$0);
      }

      protected net.minecraft.util.Unit initBuilder() {
         return net.minecraft.util.Unit.INSTANCE;
      }

      protected net.minecraft.util.Unit append(net.minecraft.util.Unit $$0, net.minecraft.util.Unit $$1) {
         return $$0;
      }

      protected DataResult<net.minecraft.util.Unit> build(net.minecraft.util.Unit $$0, net.minecraft.util.Unit $$1) {
         return DataResult.success($$0);
      }
   }

   static final class NullMapBuilder extends AbstractUniversalBuilder<net.minecraft.util.Unit, net.minecraft.util.Unit> {
      public NullMapBuilder(DynamicOps<net.minecraft.util.Unit> $$0) {
         super($$0);
      }

      protected net.minecraft.util.Unit initBuilder() {
         return net.minecraft.util.Unit.INSTANCE;
      }

      protected net.minecraft.util.Unit append(net.minecraft.util.Unit $$0, net.minecraft.util.Unit $$1, net.minecraft.util.Unit $$2) {
         return $$2;
      }

      protected DataResult<net.minecraft.util.Unit> build(net.minecraft.util.Unit $$0, net.minecraft.util.Unit $$1) {
         return DataResult.success($$1);
      }
   }
}
