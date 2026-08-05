/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  org.joml.Quaternionfc
 *  org.joml.Vector3fc
 */
package net.minecraft.network.codec;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamCodec;
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
    public static final int MAX_INITIAL_COLLECTION_SIZE = 65536;
    public static final StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>(){

        @Override
        public Boolean decode(ByteBuf $$0) {
            return $$0.readBoolean();
        }

        @Override
        public void encode(ByteBuf $$0, Boolean $$1) {
            $$0.writeBoolean($$1.booleanValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Boolean)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>(){

        @Override
        public Byte decode(ByteBuf $$0) {
            return $$0.readByte();
        }

        @Override
        public void encode(ByteBuf $$0, Byte $$1) {
            $$0.writeByte((int)$$1.byteValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Byte)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Float> ROTATION_BYTE = BYTE.map(Mth::unpackDegrees, Mth::packDegrees);
    public static final StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>(){

        @Override
        public Short decode(ByteBuf $$0) {
            return $$0.readShort();
        }

        @Override
        public void encode(ByteBuf $$0, Short $$1) {
            $$0.writeShort((int)$$1.shortValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Short)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf $$0) {
            return $$0.readUnsignedShort();
        }

        @Override
        public void encode(ByteBuf $$0, Integer $$1) {
            $$0.writeShort($$1.intValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf $$0) {
            return $$0.readInt();
        }

        @Override
        public void encode(ByteBuf $$0, Integer $$1) {
            $$0.writeInt($$1.intValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf $$0) {
            return VarInt.read($$0);
        }

        @Override
        public void encode(ByteBuf $$0, Integer $$1) {
            VarInt.write($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_VAR_INT = VAR_INT.map($$0 -> $$0 == 0 ? OptionalInt.empty() : OptionalInt.of($$0 - 1), $$0 -> $$0.isPresent() ? $$0.getAsInt() + 1 : 0);
    public static final StreamCodec<ByteBuf, Long> LONG = new StreamCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf $$0) {
            return $$0.readLong();
        }

        @Override
        public void encode(ByteBuf $$0, Long $$1) {
            $$0.writeLong($$1.longValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>(){

        @Override
        public Long decode(ByteBuf $$0) {
            return VarLong.read($$0);
        }

        @Override
        public void encode(ByteBuf $$0, Long $$1) {
            VarLong.write($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Long)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>(){

        @Override
        public Float decode(ByteBuf $$0) {
            return Float.valueOf($$0.readFloat());
        }

        @Override
        public void encode(ByteBuf $$0, Float $$1) {
            $$0.writeFloat($$1.floatValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Float)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>(){

        @Override
        public Double decode(ByteBuf $$0) {
            return $$0.readDouble();
        }

        @Override
        public void encode(ByteBuf $$0, Double $$1) {
            $$0.writeDouble($$1.doubleValue());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Double)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>(){

        @Override
        public byte[] decode(ByteBuf $$0) {
            return FriendlyByteBuf.readByteArray($$0);
        }

        @Override
        public void encode(ByteBuf $$0, byte[] $$1) {
            FriendlyByteBuf.writeByteArray($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (byte[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, long[]> LONG_ARRAY = new StreamCodec<ByteBuf, long[]>(){

        @Override
        public long[] decode(ByteBuf $$0) {
            return FriendlyByteBuf.readLongArray($$0);
        }

        @Override
        public void encode(ByteBuf $$0, long[] $$1) {
            FriendlyByteBuf.writeLongArray($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (long[])object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = ByteBufCodecs.stringUtf8(Short.MAX_VALUE);
    public static final StreamCodec<ByteBuf, Tag> TAG = ByteBufCodecs.tagCodec(NbtAccounter::defaultQuota);
    public static final StreamCodec<ByteBuf, Tag> TRUSTED_TAG = ByteBufCodecs.tagCodec(NbtAccounter::unlimitedHeap);
    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = ByteBufCodecs.compoundTagCodec(NbtAccounter::defaultQuota);
    public static final StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = ByteBufCodecs.compoundTagCodec(NbtAccounter::unlimitedHeap);
    public static final StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>(){

        @Override
        public Optional<CompoundTag> decode(ByteBuf $$0) {
            return Optional.ofNullable(FriendlyByteBuf.readNbt($$0));
        }

        @Override
        public void encode(ByteBuf $$0, Optional<CompoundTag> $$1) {
            FriendlyByteBuf.writeNbt($$0, $$1.orElse(null));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Vector3fc> VECTOR3F = new StreamCodec<ByteBuf, Vector3fc>(){

        @Override
        public Vector3fc decode(ByteBuf $$0) {
            return FriendlyByteBuf.readVector3f($$0);
        }

        @Override
        public void encode(ByteBuf $$0, Vector3fc $$1) {
            FriendlyByteBuf.writeVector3f($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Vector3fc)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Quaternionfc> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionfc>(){

        @Override
        public Quaternionfc decode(ByteBuf $$0) {
            return FriendlyByteBuf.readQuaternion($$0);
        }

        @Override
        public void encode(ByteBuf $$0, Quaternionfc $$1) {
            FriendlyByteBuf.writeQuaternion($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Quaternionfc)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, Integer> CONTAINER_ID = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf $$0) {
            return FriendlyByteBuf.readContainerId($$0);
        }

        @Override
        public void encode(ByteBuf $$0, Integer $$1) {
            FriendlyByteBuf.writeContainerId($$0, $$1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>(){

        @Override
        public PropertyMap decode(ByteBuf $$02) {
            int $$1 = ByteBufCodecs.readCount($$02, 16);
            ImmutableMultimap.Builder $$2 = ImmutableMultimap.builder();
            for (int $$3 = 0; $$3 < $$1; ++$$3) {
                String $$4 = Utf8String.read($$02, 64);
                String $$5 = Utf8String.read($$02, Short.MAX_VALUE);
                String $$6 = FriendlyByteBuf.readNullable($$02, $$0 -> Utf8String.read($$0, 1024));
                Property $$7 = new Property($$4, $$5, $$6);
                $$2.put((Object)$$7.name(), (Object)$$7);
            }
            return new PropertyMap((Multimap)$$2.build());
        }

        @Override
        public void encode(ByteBuf $$02, PropertyMap $$12) {
            ByteBufCodecs.writeCount($$02, $$12.size(), 16);
            for (Property $$2 : $$12.values()) {
                Utf8String.write($$02, $$2.name(), 64);
                Utf8String.write($$02, $$2.value(), Short.MAX_VALUE);
                FriendlyByteBuf.writeNullable($$02, $$2.signature(), ($$0, $$1) -> Utf8String.write($$0, $$1, 1024));
            }
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (PropertyMap)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final StreamCodec<ByteBuf, String> PLAYER_NAME = ByteBufCodecs.stringUtf8(16);
    public static final StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = StreamCodec.composite(UUIDUtil.STREAM_CODEC, GameProfile::id, PLAYER_NAME, GameProfile::name, GAME_PROFILE_PROPERTIES, GameProfile::properties, GameProfile::new);
    public static final StreamCodec<ByteBuf, Integer> RGB_COLOR = new StreamCodec<ByteBuf, Integer>(){

        @Override
        public Integer decode(ByteBuf $$0) {
            return ARGB.color($$0.readByte() & 0xFF, $$0.readByte() & 0xFF, $$0.readByte() & 0xFF);
        }

        @Override
        public void encode(ByteBuf $$0, Integer $$1) {
            $$0.writeByte(ARGB.red($$1));
            $$0.writeByte(ARGB.green($$1));
            $$0.writeByte(ARGB.blue($$1));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Integer)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public static StreamCodec<ByteBuf, byte[]> byteArray(final int $$0) {
        return new StreamCodec<ByteBuf, byte[]>(){

            @Override
            public byte[] decode(ByteBuf $$02) {
                return FriendlyByteBuf.readByteArray($$02, $$0);
            }

            @Override
            public void encode(ByteBuf $$02, byte[] $$1) {
                if ($$1.length > $$0) {
                    throw new EncoderException("ByteArray with size " + $$1.length + " is bigger than allowed " + $$0);
                }
                FriendlyByteBuf.writeByteArray($$02, $$1);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (byte[])object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, String> stringUtf8(final int $$0) {
        return new StreamCodec<ByteBuf, String>(){

            @Override
            public String decode(ByteBuf $$02) {
                return Utf8String.read($$02, $$0);
            }

            @Override
            public void encode(ByteBuf $$02, String $$1) {
                Utf8String.write($$02, $$1, $$0);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (String)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, Optional<Tag>> optionalTagCodec(final Supplier<NbtAccounter> $$0) {
        return new StreamCodec<ByteBuf, Optional<Tag>>(){

            @Override
            public Optional<Tag> decode(ByteBuf $$02) {
                return Optional.ofNullable(FriendlyByteBuf.readNbt($$02, (NbtAccounter)$$0.get()));
            }

            @Override
            public void encode(ByteBuf $$02, Optional<Tag> $$1) {
                FriendlyByteBuf.writeNbt($$02, $$1.orElse(null));
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> $$0) {
        return new StreamCodec<ByteBuf, Tag>(){

            @Override
            public Tag decode(ByteBuf $$02) {
                Tag $$1 = FriendlyByteBuf.readNbt($$02, (NbtAccounter)$$0.get());
                if ($$1 == null) {
                    throw new DecoderException("Expected non-null compound tag");
                }
                return $$1;
            }

            @Override
            public void encode(ByteBuf $$02, Tag $$1) {
                if ($$1 == EndTag.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                }
                FriendlyByteBuf.writeNbt($$02, $$1);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (Tag)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> $$02) {
        return ByteBufCodecs.tagCodec($$02).map($$0 -> {
            if ($$0 instanceof CompoundTag) {
                CompoundTag $$1 = (CompoundTag)$$0;
                return $$1;
            }
            throw new DecoderException("Not a compound tag: " + String.valueOf($$0));
        }, $$0 -> $$0);
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> $$0) {
        return ByteBufCodecs.fromCodec($$0, NbtAccounter::unlimitedHeap);
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> $$0) {
        return ByteBufCodecs.fromCodec($$0, NbtAccounter::defaultQuota);
    }

    public static <T, B extends ByteBuf, V> StreamCodec.CodecOperation<B, T, V> fromCodec(final DynamicOps<T> $$0, final Codec<V> $$1) {
        return $$2 -> new StreamCodec<B, V>(){

            @Override
            public V decode(B $$02) {
                Object $$12 = $$2.decode($$02);
                return $$1.parse($$0, $$12).getOrThrow($$1 -> new DecoderException("Failed to decode: " + $$1 + " " + String.valueOf($$12)));
            }

            @Override
            public void encode(B $$02, V $$12) {
                Object $$22 = $$1.encodeStart($$0, $$12).getOrThrow($$1 -> new EncoderException("Failed to encode: " + $$1 + " " + String.valueOf($$12)));
                $$2.encode($$02, $$22);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> $$0, Supplier<NbtAccounter> $$1) {
        return ByteBufCodecs.tagCodec($$1).apply(ByteBufCodecs.fromCodec(NbtOps.INSTANCE, $$0));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> $$0) {
        return ByteBufCodecs.fromCodecWithRegistries($$0, NbtAccounter::unlimitedHeap);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> $$0) {
        return ByteBufCodecs.fromCodecWithRegistries($$0, NbtAccounter::defaultQuota);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> $$0, Supplier<NbtAccounter> $$1) {
        final StreamCodec<ByteBuf, Tag> $$2 = ByteBufCodecs.tagCodec($$1);
        return new StreamCodec<RegistryFriendlyByteBuf, T>(){

            @Override
            public T decode(RegistryFriendlyByteBuf $$02) {
                Tag $$12 = (Tag)$$2.decode($$02);
                RegistryOps<Tag> $$22 = $$02.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                return $$0.parse($$22, (Object)$$12).getOrThrow($$1 -> new DecoderException("Failed to decode: " + $$1 + " " + String.valueOf($$12)));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf $$02, T $$12) {
                RegistryOps<Tag> $$22 = $$02.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                Tag $$3 = (Tag)$$0.encodeStart($$22, $$12).getOrThrow($$1 -> new EncoderException("Failed to encode: " + $$1 + " " + String.valueOf($$12)));
                $$2.encode($$02, $$3);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<? super B, V> $$0) {
        return new StreamCodec<B, Optional<V>>(){

            @Override
            public Optional<V> decode(B $$02) {
                if ($$02.readBoolean()) {
                    return Optional.of($$0.decode($$02));
                }
                return Optional.empty();
            }

            @Override
            public void encode(B $$02, Optional<V> $$1) {
                if ($$1.isPresent()) {
                    $$02.writeBoolean(true);
                    $$0.encode($$02, $$1.get());
                } else {
                    $$02.writeBoolean(false);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Optional)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static int readCount(ByteBuf $$0, int $$1) {
        int $$2 = VarInt.read($$0);
        if ($$2 > $$1) {
            throw new DecoderException($$2 + " elements exceeded max size of: " + $$1);
        }
        return $$2;
    }

    public static void writeCount(ByteBuf $$0, int $$1, int $$2) {
        if ($$1 > $$2) {
            throw new EncoderException($$1 + " elements exceeded max size of: " + $$2);
        }
        VarInt.write($$0, $$1);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> $$0, StreamCodec<? super B, V> $$1) {
        return ByteBufCodecs.collection($$0, $$1, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(final IntFunction<C> $$0, final StreamCodec<? super B, V> $$1, final int $$2) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B $$02) {
                int $$12 = ByteBufCodecs.readCount($$02, $$2);
                Collection $$22 = (Collection)$$0.apply(Math.min($$12, 65536));
                for (int $$3 = 0; $$3 < $$12; ++$$3) {
                    $$22.add($$1.decode($$02));
                }
                return $$22;
            }

            @Override
            public void encode(B $$02, C $$12) {
                ByteBufCodecs.writeCount($$02, $$12.size(), $$2);
                for (Object $$22 : $$12) {
                    $$1.encode($$02, $$22);
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (C)((Collection)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> $$0) {
        return $$1 -> ByteBufCodecs.collection($$0, $$1);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return $$0 -> ByteBufCodecs.collection(ArrayList::new, $$0);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int $$0) {
        return $$1 -> ByteBufCodecs.collection(ArrayList::new, $$1, $$0);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(IntFunction<? extends M> $$0, StreamCodec<? super B, K> $$1, StreamCodec<? super B, V> $$2) {
        return ByteBufCodecs.map($$0, $$1, $$2, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(final IntFunction<? extends M> $$0, final StreamCodec<? super B, K> $$1, final StreamCodec<? super B, V> $$2, final int $$3) {
        return new StreamCodec<B, M>(){

            @Override
            public void encode(B $$02, M $$12) {
                ByteBufCodecs.writeCount($$02, $$12.size(), $$3);
                $$12.forEach(($$3, $$4) -> {
                    $$1.encode($$02, $$3);
                    $$2.encode($$02, $$4);
                });
            }

            @Override
            public M decode(B $$02) {
                int $$12 = ByteBufCodecs.readCount($$02, $$3);
                Map $$22 = (Map)$$0.apply(Math.min($$12, 65536));
                for (int $$32 = 0; $$32 < $$12; ++$$32) {
                    Object $$4 = $$1.decode($$02);
                    Object $$5 = $$2.decode($$02);
                    $$22.put($$4, $$5);
                }
                return $$22;
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (M)((Map)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(final StreamCodec<? super B, L> $$0, final StreamCodec<? super B, R> $$1) {
        return new StreamCodec<B, Either<L, R>>(){

            @Override
            public Either<L, R> decode(B $$02) {
                if ($$02.readBoolean()) {
                    return Either.left($$0.decode($$02));
                }
                return Either.right($$1.decode($$02));
            }

            @Override
            public void encode(B $$02, Either<L, R> $$12) {
                $$12.ifLeft($$2 -> {
                    $$02.writeBoolean(true);
                    $$0.encode($$02, $$2);
                }).ifRight($$2 -> {
                    $$02.writeBoolean(false);
                    $$1.encode($$02, $$2);
                });
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((ByteBuf)object), (Either)((Either)object2));
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, V> lengthPrefixed(final int $$0, final BiFunction<B, ByteBuf, B> $$1) {
        return $$2 -> new StreamCodec<B, V>(){

            @Override
            public V decode(B $$02) {
                int $$12 = VarInt.read($$02);
                if ($$12 > $$0) {
                    throw new DecoderException("Buffer size " + $$12 + " is larger than allowed limit of " + $$0);
                }
                int $$22 = $$02.readerIndex();
                ByteBuf $$3 = (ByteBuf)$$1.apply($$02, $$02.slice($$22, $$12));
                $$02.readerIndex($$22 + $$12);
                return $$2.decode($$3);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void encode(B $$02, V $$12) {
                ByteBuf $$22 = (ByteBuf)$$1.apply($$02, $$02.alloc().buffer());
                try {
                    $$2.encode($$22, $$12);
                    int $$3 = $$22.readableBytes();
                    if ($$3 > $$0) {
                        throw new EncoderException("Buffer size " + $$3 + " is  larger than allowed limit of " + $$0);
                    }
                    VarInt.write($$02, $$3);
                    $$02.writeBytes($$22);
                }
                finally {
                    $$22.release();
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((B)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((ByteBuf)object));
            }
        };
    }

    public static <V> StreamCodec.CodecOperation<ByteBuf, V, V> lengthPrefixed(int $$02) {
        return ByteBufCodecs.lengthPrefixed($$02, ($$0, $$1) -> $$1);
    }

    public static <V> StreamCodec.CodecOperation<RegistryFriendlyByteBuf, V, V> registryFriendlyLengthPrefixed(int $$02) {
        return ByteBufCodecs.lengthPrefixed($$02, ($$0, $$1) -> new RegistryFriendlyByteBuf((ByteBuf)$$1, $$0.registryAccess()));
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> $$0, final ToIntFunction<T> $$1) {
        return new StreamCodec<ByteBuf, T>(){

            @Override
            public T decode(ByteBuf $$02) {
                int $$12 = VarInt.read($$02);
                return $$0.apply($$12);
            }

            @Override
            public void encode(ByteBuf $$02, T $$12) {
                int $$2 = $$1.applyAsInt($$12);
                VarInt.write($$02, $$2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> $$0) {
        return ByteBufCodecs.idMapper($$0::byIdOrThrow, $$0::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(final ResourceKey<? extends Registry<T>> $$0, final Function<Registry<T>, IdMap<R>> $$1) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>(){

            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf $$02) {
                return (IdMap)$$1.apply($$02.registryAccess().lookupOrThrow($$0));
            }

            @Override
            public R decode(RegistryFriendlyByteBuf $$02) {
                int $$12 = VarInt.read($$02);
                return this.getRegistryOrThrow($$02).byIdOrThrow($$12);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf $$02, R $$12) {
                int $$2 = this.getRegistryOrThrow($$02).getIdOrThrow($$12);
                VarInt.write($$02, $$2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> $$02) {
        return ByteBufCodecs.registry($$02, $$0 -> $$0);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> $$0) {
        return ByteBufCodecs.registry($$0, Registry::asHolderIdMap);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(final ResourceKey<? extends Registry<T>> $$0, final StreamCodec<? super RegistryFriendlyByteBuf, T> $$1) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>(){
            private static final int DIRECT_HOLDER_ID = 0;

            private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf $$02) {
                return $$02.registryAccess().lookupOrThrow($$0).asHolderIdMap();
            }

            @Override
            public Holder<T> decode(RegistryFriendlyByteBuf $$02) {
                int $$12 = VarInt.read($$02);
                if ($$12 == 0) {
                    return Holder.direct($$1.decode($$02));
                }
                return this.getRegistryOrThrow($$02).byIdOrThrow($$12 - 1);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf $$02, Holder<T> $$12) {
                switch ($$12.kind()) {
                    case REFERENCE: {
                        int $$2 = this.getRegistryOrThrow($$02).getIdOrThrow($$12);
                        VarInt.write($$02, $$2 + 1);
                        break;
                    }
                    case DIRECT: {
                        VarInt.write($$02, 0);
                        $$1.encode($$02, $$12.value());
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (Holder)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> $$0) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>(){
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec;
            {
                this.holderCodec = ByteBufCodecs.holderRegistry($$0);
            }

            @Override
            public HolderSet<T> decode(RegistryFriendlyByteBuf $$02) {
                int $$1 = VarInt.read($$02) - 1;
                if ($$1 == -1) {
                    HolderLookup.RegistryLookup $$2 = $$02.registryAccess().lookupOrThrow($$0);
                    return (HolderSet)$$2.get(TagKey.create($$0, (Identifier)Identifier.STREAM_CODEC.decode($$02))).orElseThrow();
                }
                ArrayList<Holder> $$3 = new ArrayList<Holder>(Math.min($$1, 65536));
                for (int $$4 = 0; $$4 < $$1; ++$$4) {
                    $$3.add((Holder)this.holderCodec.decode($$02));
                }
                return HolderSet.direct($$3);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf $$02, HolderSet<T> $$1) {
                Optional $$2 = $$1.unwrapKey();
                if ($$2.isPresent()) {
                    VarInt.write($$02, 0);
                    Identifier.STREAM_CODEC.encode($$02, $$2.get().location());
                } else {
                    VarInt.write($$02, $$1.size() + 1);
                    for (Holder holder : $$1) {
                        this.holderCodec.encode($$02, holder);
                    }
                }
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (HolderSet)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static StreamCodec<ByteBuf, JsonElement> lenientJson(final int $$0) {
        return new StreamCodec<ByteBuf, JsonElement>(){
            private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

            @Override
            public JsonElement decode(ByteBuf $$02) {
                String $$1 = Utf8String.read($$02, $$0);
                try {
                    return LenientJsonParser.parse($$1);
                }
                catch (JsonSyntaxException $$2) {
                    throw new DecoderException("Failed to parse JSON", (Throwable)$$2);
                }
            }

            @Override
            public void encode(ByteBuf $$02, JsonElement $$1) {
                String $$2 = GSON.toJson($$1);
                Utf8String.write($$02, $$2, $$0);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((ByteBuf)object, (JsonElement)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((ByteBuf)object);
            }
        };
    }
}

