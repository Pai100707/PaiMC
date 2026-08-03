package net.minecraft.network.codec;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Mth;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface ByteBufCodecs {
   int MAX_INITIAL_COLLECTION_SIZE = 65536;
   StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
      public Boolean decode(ByteBuf $$0) {
         return $$0.readBoolean();
      }

      public void encode(ByteBuf $$0, Boolean $$1) {
         $$0.writeBoolean($$1);
      }
   };
   StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
      public Byte decode(ByteBuf $$0) {
         return $$0.readByte();
      }

      public void encode(ByteBuf $$0, Byte $$1) {
         $$0.writeByte($$1);
      }
   };
   StreamCodec<ByteBuf, Float> ROTATION_BYTE = BYTE.map(Mth::unpackDegrees, Mth::packDegrees);
   StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>() {
      public Short decode(ByteBuf $$0) {
         return $$0.readShort();
      }

      public void encode(ByteBuf $$0, Short $$1) {
         $$0.writeShort($$1);
      }
   };
   StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>() {
      public Integer decode(ByteBuf $$0) {
         return $$0.readUnsignedShort();
      }

      public void encode(ByteBuf $$0, Integer $$1) {
         $$0.writeShort($$1);
      }
   };
   StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>() {
      public Integer decode(ByteBuf $$0) {
         return $$0.readInt();
      }

      public void encode(ByteBuf $$0, Integer $$1) {
         $$0.writeInt($$1);
      }
   };
   StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>() {
      public Integer decode(ByteBuf $$0) {
         return net.minecraft.network.VarInt.read($$0);
      }

      public void encode(ByteBuf $$0, Integer $$1) {
         net.minecraft.network.VarInt.write($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, OptionalInt> OPTIONAL_VAR_INT = VAR_INT.map(
      $$0 -> $$0 == 0 ? OptionalInt.empty() : OptionalInt.of($$0 - 1), $$0 -> $$0.isPresent() ? $$0.getAsInt() + 1 : 0
   );
   StreamCodec<ByteBuf, Long> LONG = new StreamCodec<ByteBuf, Long>() {
      public Long decode(ByteBuf $$0) {
         return $$0.readLong();
      }

      public void encode(ByteBuf $$0, Long $$1) {
         $$0.writeLong($$1);
      }
   };
   StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>() {
      public Long decode(ByteBuf $$0) {
         return net.minecraft.network.VarLong.read($$0);
      }

      public void encode(ByteBuf $$0, Long $$1) {
         net.minecraft.network.VarLong.write($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>() {
      public Float decode(ByteBuf $$0) {
         return $$0.readFloat();
      }

      public void encode(ByteBuf $$0, Float $$1) {
         $$0.writeFloat($$1);
      }
   };
   StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>() {
      public Double decode(ByteBuf $$0) {
         return $$0.readDouble();
      }

      public void encode(ByteBuf $$0, Double $$1) {
         $$0.writeDouble($$1);
      }
   };
   StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>() {
      public byte[] decode(ByteBuf $$0) {
         return net.minecraft.network.FriendlyByteBuf.readByteArray($$0);
      }

      public void encode(ByteBuf $$0, byte[] $$1) {
         net.minecraft.network.FriendlyByteBuf.writeByteArray($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, long[]> LONG_ARRAY = new StreamCodec<ByteBuf, long[]>() {
      public long[] decode(ByteBuf $$0) {
         return net.minecraft.network.FriendlyByteBuf.readLongArray($$0);
      }

      public void encode(ByteBuf $$0, long[] $$1) {
         net.minecraft.network.FriendlyByteBuf.writeLongArray($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
   StreamCodec<ByteBuf, Tag> TAG = tagCodec(NbtAccounter::defaultQuota);
   StreamCodec<ByteBuf, Tag> TRUSTED_TAG = tagCodec(NbtAccounter::unlimitedHeap);
   StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = compoundTagCodec(NbtAccounter::defaultQuota);
   StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = compoundTagCodec(NbtAccounter::unlimitedHeap);
   StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>() {
      public Optional<CompoundTag> decode(ByteBuf $$0) {
         return Optional.ofNullable(net.minecraft.network.FriendlyByteBuf.readNbt($$0));
      }

      public void encode(ByteBuf $$0, Optional<CompoundTag> $$1) {
         net.minecraft.network.FriendlyByteBuf.writeNbt($$0, (Tag)$$1.orElse(null));
      }
   };
   StreamCodec<ByteBuf, Vector3fc> VECTOR3F = new StreamCodec<ByteBuf, Vector3fc>() {
      public Vector3fc decode(ByteBuf $$0) {
         return net.minecraft.network.FriendlyByteBuf.readVector3f($$0);
      }

      public void encode(ByteBuf $$0, Vector3fc $$1) {
         net.minecraft.network.FriendlyByteBuf.writeVector3f($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, Quaternionfc> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionfc>() {
      public Quaternionfc decode(ByteBuf $$0) {
         return net.minecraft.network.FriendlyByteBuf.readQuaternion($$0);
      }

      public void encode(ByteBuf $$0, Quaternionfc $$1) {
         net.minecraft.network.FriendlyByteBuf.writeQuaternion($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, Integer> CONTAINER_ID = new StreamCodec<ByteBuf, Integer>() {
      public Integer decode(ByteBuf $$0) {
         return net.minecraft.network.FriendlyByteBuf.readContainerId($$0);
      }

      public void encode(ByteBuf $$0, Integer $$1) {
         net.minecraft.network.FriendlyByteBuf.writeContainerId($$0, $$1);
      }
   };
   StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>() {
      public PropertyMap decode(ByteBuf $$0) {
         int $$1 = ByteBufCodecs.readCount($$0, 16);
         Builder<String, Property> $$2 = ImmutableMultimap.builder();

         for (int $$3 = 0; $$3 < $$1; $$3++) {
            String $$4 = net.minecraft.network.Utf8String.read($$0, 64);
            String $$5 = net.minecraft.network.Utf8String.read($$0, 32767);
            String $$6 = net.minecraft.network.FriendlyByteBuf.readNullable($$0, $$0x -> net.minecraft.network.Utf8String.read($$0x, 1024));
            Property $$7 = new Property($$4, $$5, $$6);
            $$2.put($$7.name(), $$7);
         }

         return new PropertyMap($$2.build());
      }

      public void encode(ByteBuf $$0, PropertyMap $$1) {
         ByteBufCodecs.writeCount($$0, $$1.size(), 16);

         for (Property $$2 : $$1.values()) {
            net.minecraft.network.Utf8String.write($$0, $$2.name(), 64);
            net.minecraft.network.Utf8String.write($$0, $$2.value(), 32767);
            net.minecraft.network.FriendlyByteBuf.writeNullable($$0, $$2.signature(), ($$0x, $$1x) -> net.minecraft.network.Utf8String.write($$0x, $$1x, 1024));
         }
      }
   };
   StreamCodec<ByteBuf, String> PLAYER_NAME = stringUtf8(16);
   StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = StreamCodec.composite(
      UUIDUtil.STREAM_CODEC, GameProfile::id, PLAYER_NAME, GameProfile::name, GAME_PROFILE_PROPERTIES, GameProfile::properties, GameProfile::new
   );
   StreamCodec<ByteBuf, Integer> RGB_COLOR = new StreamCodec<ByteBuf, Integer>() {
      public Integer decode(ByteBuf $$0) {
         return ARGB.color($$0.readByte() & 0xFF, $$0.readByte() & 0xFF, $$0.readByte() & 0xFF);
      }

      public void encode(ByteBuf $$0, Integer $$1) {
         $$0.writeByte(ARGB.red($$1));
         $$0.writeByte(ARGB.green($$1));
         $$0.writeByte(ARGB.blue($$1));
      }
   };

   static StreamCodec<ByteBuf, byte[]> byteArray(final int $$0) {
      return new StreamCodec<ByteBuf, byte[]>() {
         public byte[] decode(ByteBuf $$0x) {
            return net.minecraft.network.FriendlyByteBuf.readByteArray($$0, $$0);
         }

         public void encode(ByteBuf $$0x, byte[] $$1) {
            if ($$1.length > $$0) {
               throw new EncoderException("ByteArray with size " + $$1.length + " is bigger than allowed " + $$0);
            } else {
               net.minecraft.network.FriendlyByteBuf.writeByteArray($$0, $$1);
            }
         }
      };
   }

   static StreamCodec<ByteBuf, String> stringUtf8(final int $$0) {
      return new StreamCodec<ByteBuf, String>() {
         public String decode(ByteBuf $$0x) {
            return net.minecraft.network.Utf8String.read($$0, $$0);
         }

         public void encode(ByteBuf $$0x, String $$1) {
            net.minecraft.network.Utf8String.write($$0, $$1, $$0);
         }
      };
   }

   static StreamCodec<ByteBuf, Optional<Tag>> optionalTagCodec(final Supplier<NbtAccounter> $$0) {
      return new StreamCodec<ByteBuf, Optional<Tag>>() {
         public Optional<Tag> decode(ByteBuf $$0x) {
            return Optional.ofNullable(net.minecraft.network.FriendlyByteBuf.readNbt($$0, $$0.get()));
         }

         public void encode(ByteBuf $$0x, Optional<Tag> $$1) {
            net.minecraft.network.FriendlyByteBuf.writeNbt($$0, $$1.orElse(null));
         }
      };
   }

   static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> $$0) {
      return new StreamCodec<ByteBuf, Tag>() {
         public Tag decode(ByteBuf $$0x) {
            Tag $$1 = net.minecraft.network.FriendlyByteBuf.readNbt($$0, $$0.get());
            if ($$1 == null) {
               throw new DecoderException("Expected non-null compound tag");
            } else {
               return $$1;
            }
         }

         public void encode(ByteBuf $$0x, Tag $$1) {
            if ($$1 == EndTag.INSTANCE) {
               throw new EncoderException("Expected non-null compound tag");
            } else {
               net.minecraft.network.FriendlyByteBuf.writeNbt($$0, $$1);
            }
         }
      };
   }

   static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> $$0) {
      return tagCodec($$0).map($$0x -> {
         if ($$0x instanceof CompoundTag $$1) {
            return $$1;
         } else {
            throw new DecoderException("Not a compound tag: " + $$0x);
         }
      }, $$0x -> $$0x);
   }

   static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> $$0) {
      return fromCodec($$0, NbtAccounter::unlimitedHeap);
   }

   static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> $$0) {
      return fromCodec($$0, NbtAccounter::defaultQuota);
   }

   static <T, B extends ByteBuf, V> StreamCodec.CodecOperation<B, T, V> fromCodec(DynamicOps<T> $$0, Codec<V> $$1) {
      return $$2 -> new StreamCodec<B, V>() {
         public V decode(B $$0x) {
            T $$1x = (T)$$2.decode($$0);
            return (V)$$1.parse($$0, $$1x).getOrThrow($$1xxx -> new DecoderException("Failed to decode: " + $$1xxx + " " + $$1));
         }

         public void encode(B $$0x, V $$1x) {
            T $$2x = (T)$$1.encodeStart($$0, $$1).getOrThrow($$1xxx -> new EncoderException("Failed to encode: " + $$1xxx + " " + $$1));
            $$2.encode($$0, $$2x);
         }
      };
   }

   static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> $$0, Supplier<NbtAccounter> $$1) {
      return tagCodec($$1).apply(fromCodec(NbtOps.INSTANCE, $$0));
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> $$0) {
      return fromCodecWithRegistries($$0, NbtAccounter::unlimitedHeap);
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> $$0) {
      return fromCodecWithRegistries($$0, NbtAccounter::defaultQuota);
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> $$0, Supplier<NbtAccounter> $$1) {
      final StreamCodec<ByteBuf, Tag> $$2 = tagCodec($$1);
      return new StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T>() {
         public T decode(net.minecraft.network.RegistryFriendlyByteBuf $$0x) {
            Tag $$1x = $$2.decode($$0);
            RegistryOps<Tag> $$2x = $$0.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            return (T)$$0.parse($$2x, $$1x).getOrThrow($$1xx -> new DecoderException("Failed to decode: " + $$1xx + " " + $$1));
         }

         public void encode(net.minecraft.network.RegistryFriendlyByteBuf $$0x, T $$1x) {
            RegistryOps<Tag> $$2x = $$0.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            Tag $$3 = (Tag)$$0.encodeStart($$2x, $$1x).getOrThrow($$1xx -> new EncoderException("Failed to encode: " + $$1xx + " " + $$1));
            $$2.encode($$0, $$3);
         }
      };
   }

   static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<? super B, V> $$0) {
      return new StreamCodec<B, Optional<V>>() {
         public Optional<V> decode(B $$0x) {
            return $$0.readBoolean() ? Optional.of($$0.decode($$0)) : Optional.empty();
         }

         public void encode(B $$0x, Optional<V> $$1) {
            if ($$1.isPresent()) {
               $$0.writeBoolean(true);
               $$0.encode($$0, $$1.get());
            } else {
               $$0.writeBoolean(false);
            }
         }
      };
   }

   static int readCount(ByteBuf $$0, int $$1) {
      int $$2 = net.minecraft.network.VarInt.read($$0);
      if ($$2 > $$1) {
         throw new DecoderException($$2 + " elements exceeded max size of: " + $$1);
      } else {
         return $$2;
      }
   }

   static void writeCount(ByteBuf $$0, int $$1, int $$2) {
      if ($$1 > $$2) {
         throw new EncoderException($$1 + " elements exceeded max size of: " + $$2);
      } else {
         net.minecraft.network.VarInt.write($$0, $$1);
      }
   }

   static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> $$0, StreamCodec<? super B, V> $$1) {
      return collection($$0, $$1, Integer.MAX_VALUE);
   }

   static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(
      final IntFunction<C> $$0, final StreamCodec<? super B, V> $$1, final int $$2
   ) {
      return new StreamCodec<B, C>() {
         public C decode(B $$0x) {
            int $$1x = ByteBufCodecs.readCount($$0, $$2);
            C $$2x = $$0.apply(Math.min($$1x, 65536));

            for (int $$3 = 0; $$3 < $$1x; $$3++) {
               $$2x.add($$1.decode($$0));
            }

            return $$2x;
         }

         public void encode(B $$0x, C $$1x) {
            ByteBufCodecs.writeCount($$0, $$1.size(), $$2);

            for (V $$2x : $$1) {
               $$1.encode($$0, $$2x);
            }
         }
      };
   }

   static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> $$0) {
      return $$1 -> collection($$0, $$1);
   }

   static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
      return $$0 -> collection(ArrayList::new, $$0);
   }

   static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int $$0) {
      return $$1 -> collection(ArrayList::new, $$1, $$0);
   }

   static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
      IntFunction<? extends M> $$0, StreamCodec<? super B, K> $$1, StreamCodec<? super B, V> $$2
   ) {
      return map($$0, $$1, $$2, Integer.MAX_VALUE);
   }

   static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
      final IntFunction<? extends M> $$0, final StreamCodec<? super B, K> $$1, final StreamCodec<? super B, V> $$2, final int $$3
   ) {
      return new StreamCodec<B, M>() {
         public void encode(B $$0x, M $$1x) {
            ByteBufCodecs.writeCount($$0, $$1.size(), $$3);
            $$1.forEach(($$3xx, $$4) -> {
               $$1.encode($$0, (K)$$3xx);
               $$2.encode($$0, (V)$$4);
            });
         }

         public M decode(B $$0x) {
            int $$1x = ByteBufCodecs.readCount($$0, $$3);
            M $$2x = (M)$$0.apply(Math.min($$1x, 65536));

            for (int $$3x = 0; $$3x < $$1x; $$3x++) {
               K $$4 = $$1.decode($$0);
               V $$5 = $$2.decode($$0);
               $$2x.put($$4, $$5);
            }

            return $$2x;
         }
      };
   }

   static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(final StreamCodec<? super B, L> $$0, final StreamCodec<? super B, R> $$1) {
      return new StreamCodec<B, Either<L, R>>() {
         public Either<L, R> decode(B $$0x) {
            return $$0.readBoolean() ? Either.left($$0.decode($$0)) : Either.right($$1.decode($$0));
         }

         public void encode(B $$0x, Either<L, R> $$1x) {
            $$1.ifLeft($$2 -> {
               $$0.writeBoolean(true);
               $$0.encode($$0, (L)$$2);
            }).ifRight($$2 -> {
               $$0.writeBoolean(false);
               $$1.encode($$0, (R)$$2);
            });
         }
      };
   }

   static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, V> lengthPrefixed(int $$0, BiFunction<B, ByteBuf, B> $$1) {
      return $$2 -> new StreamCodec<B, V>() {
         public V decode(B $$0x) {
            int $$1x = net.minecraft.network.VarInt.read($$0);
            if ($$1x > $$0) {
               throw new DecoderException("Buffer size " + $$1x + " is larger than allowed limit of " + $$0);
            } else {
               int $$2x = $$0.readerIndex();
               B $$3 = (B)((ByteBuf)$$1.apply($$0, $$0.slice($$2x, $$1x)));
               $$0.readerIndex($$2x + $$1x);
               return (V)$$2.decode($$3);
            }
         }

         public void encode(B $$0x, V $$1x) {
            B $$2x = (B)((ByteBuf)$$1.apply($$0, $$0.alloc().buffer()));

            try {
               $$2.encode($$2x, $$1);
               int $$3 = $$2x.readableBytes();
               if ($$3 > $$0) {
                  throw new EncoderException("Buffer size " + $$3 + " is  larger than allowed limit of " + $$0);
               }

               net.minecraft.network.VarInt.write($$0, $$3);
               $$0.writeBytes($$2x);
            } finally {
               $$2x.release();
            }
         }
      };
   }

   static <V> StreamCodec.CodecOperation<ByteBuf, V, V> lengthPrefixed(int $$0) {
      return lengthPrefixed($$0, ($$0x, $$1) -> $$1);
   }

   static <V> StreamCodec.CodecOperation<net.minecraft.network.RegistryFriendlyByteBuf, V, V> registryFriendlyLengthPrefixed(int $$0) {
      return lengthPrefixed($$0, ($$0x, $$1) -> new net.minecraft.network.RegistryFriendlyByteBuf($$1, $$0x.registryAccess()));
   }

   static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> $$0, final ToIntFunction<T> $$1) {
      return new StreamCodec<ByteBuf, T>() {
         public T decode(ByteBuf $$0x) {
            int $$1x = net.minecraft.network.VarInt.read($$0);
            return $$0.apply($$1x);
         }

         public void encode(ByteBuf $$0x, T $$1x) {
            int $$2 = $$1.applyAsInt($$1);
            net.minecraft.network.VarInt.write($$0, $$2);
         }
      };
   }

   static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> $$0) {
      return idMapper($$0::byIdOrThrow, $$0::getIdOrThrow);
   }

   private static <T, R> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, R> registry(
      final ResourceKey<? extends Registry<T>> $$0, final Function<Registry<T>, IdMap<R>> $$1
   ) {
      return new StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, R>() {
         private IdMap<R> getRegistryOrThrow(net.minecraft.network.RegistryFriendlyByteBuf $$0x) {
            return $$1.apply($$0.registryAccess().lookupOrThrow($$0));
         }

         public R decode(net.minecraft.network.RegistryFriendlyByteBuf $$0x) {
            int $$1x = net.minecraft.network.VarInt.read($$0);
            return (R)this.getRegistryOrThrow($$0).byIdOrThrow($$1x);
         }

         public void encode(net.minecraft.network.RegistryFriendlyByteBuf $$0x, R $$1x) {
            int $$2 = this.getRegistryOrThrow($$0).getIdOrThrow($$1);
            net.minecraft.network.VarInt.write($$0, $$2);
         }
      };
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> $$0) {
      return registry($$0, $$0x -> $$0x);
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> $$0) {
      return registry($$0, Registry::asHolderIdMap);
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Holder<T>> holder(
      final ResourceKey<? extends Registry<T>> $$0, final StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> $$1
   ) {
      return new StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Holder<T>>() {
         private static final int DIRECT_HOLDER_ID = 0;

         private IdMap<Holder<T>> getRegistryOrThrow(net.minecraft.network.RegistryFriendlyByteBuf $$0x) {
            return $$0.registryAccess().lookupOrThrow($$0).asHolderIdMap();
         }

         public Holder<T> decode(net.minecraft.network.RegistryFriendlyByteBuf $$0x) {
            int $$1x = net.minecraft.network.VarInt.read($$0);
            return $$1x == 0 ? Holder.direct($$1.decode($$0)) : (Holder)this.getRegistryOrThrow($$0).byIdOrThrow($$1x - 1);
         }

         public void encode(net.minecraft.network.RegistryFriendlyByteBuf $$0x, Holder<T> $$1x) {
            switch ($$1.kind()) {
               case REFERENCE:
                  int $$2 = this.getRegistryOrThrow($$0).getIdOrThrow($$1);
                  net.minecraft.network.VarInt.write($$0, $$2 + 1);
                  break;
               case DIRECT:
                  net.minecraft.network.VarInt.write($$0, 0);
                  $$1.encode($$0, (T)$$1.value());
            }
         }
      };
   }

   static <T> StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> $$0) {
      return new StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, HolderSet<T>>() {
         private static final int NAMED_SET = -1;
         private final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Holder<T>> holderCodec = ByteBufCodecs.holderRegistry($$0);

         public HolderSet<T> decode(net.minecraft.network.RegistryFriendlyByteBuf $$0x) {
            int $$1 = net.minecraft.network.VarInt.read($$0) - 1;
            if ($$1 == -1) {
               Registry<T> $$2 = $$0.registryAccess().lookupOrThrow($$0);
               return (HolderSet<T>)$$2.get(TagKey.create($$0, (Identifier)Identifier.STREAM_CODEC.decode($$0))).orElseThrow();
            } else {
               List<Holder<T>> $$3 = new ArrayList<>(Math.min($$1, 65536));

               for (int $$4 = 0; $$4 < $$1; $$4++) {
                  $$3.add(this.holderCodec.decode($$0));
               }

               return HolderSet.direct($$3);
            }
         }

         public void encode(net.minecraft.network.RegistryFriendlyByteBuf $$0x, HolderSet<T> $$1) {
            Optional<TagKey<T>> $$2 = $$1.unwrapKey();
            if ($$2.isPresent()) {
               net.minecraft.network.VarInt.write($$0, 0);
               Identifier.STREAM_CODEC.encode($$0, $$2.get().location());
            } else {
               net.minecraft.network.VarInt.write($$0, $$1.size() + 1);

               for (Holder<T> $$3 : $$1) {
                  this.holderCodec.encode($$0, $$3);
               }
            }
         }
      };
   }

   static StreamCodec<ByteBuf, JsonElement> lenientJson(final int $$0) {
      return new StreamCodec<ByteBuf, JsonElement>() {
         private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

         public JsonElement decode(ByteBuf $$0x) {
            String $$1 = net.minecraft.network.Utf8String.read($$0, $$0);

            try {
               return LenientJsonParser.parse($$1);
            } catch (JsonSyntaxException var4) {
               throw new DecoderException("Failed to parse JSON", var4);
            }
         }

         public void encode(ByteBuf $$0x, JsonElement $$1) {
            String $$2 = GSON.toJson($$1);
            net.minecraft.network.Utf8String.write($$0, $$2, $$0);
         }
      };
   }
}
