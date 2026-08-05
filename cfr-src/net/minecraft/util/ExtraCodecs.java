/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.google.common.primitives.UnsignedBytes
 *  com.google.gson.JsonElement
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Codec$ResultFunction
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.BaseMapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.apache.commons.lang3.StringEscapeUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.joml.AxisAngle4f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector2f
 *  org.joml.Vector2fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector3i
 *  org.joml.Vector3ic
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class ExtraCodecs {
    public static final Codec<JsonElement> JSON = ExtraCodecs.converter(JsonOps.INSTANCE);
    public static final Codec<Object> JAVA = ExtraCodecs.converter(JavaOps.INSTANCE);
    public static final Codec<Tag> NBT = ExtraCodecs.converter(NbtOps.INSTANCE);
    public static final Codec<Vector2fc> VECTOR2F = Codec.FLOAT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 2).map($$0 -> new Vector2f(((Float)$$0.get(0)).floatValue(), ((Float)$$0.get(1)).floatValue())), $$0 -> List.of(Float.valueOf($$0.x()), Float.valueOf($$0.y())));
    public static final Codec<Vector3fc> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 3).map($$0 -> new Vector3f(((Float)$$0.get(0)).floatValue(), ((Float)$$0.get(1)).floatValue(), ((Float)$$0.get(2)).floatValue())), $$0 -> List.of(Float.valueOf($$0.x()), Float.valueOf($$0.y()), Float.valueOf($$0.z())));
    public static final Codec<Vector3ic> VECTOR3I = Codec.INT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 3).map($$0 -> new Vector3i(((Integer)$$0.get(0)).intValue(), ((Integer)$$0.get(1)).intValue(), ((Integer)$$0.get(2)).intValue())), $$0 -> List.of(Integer.valueOf($$0.x()), Integer.valueOf($$0.y()), Integer.valueOf($$0.z())));
    public static final Codec<Vector4fc> VECTOR4F = Codec.FLOAT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 4).map($$0 -> new Vector4f(((Float)$$0.get(0)).floatValue(), ((Float)$$0.get(1)).floatValue(), ((Float)$$0.get(2)).floatValue(), ((Float)$$0.get(3)).floatValue())), $$0 -> List.of(Float.valueOf($$0.x()), Float.valueOf($$0.y()), Float.valueOf($$0.z()), Float.valueOf($$0.w())));
    public static final Codec<Quaternionfc> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 4).map($$0 -> new Quaternionf(((Float)$$0.get(0)).floatValue(), ((Float)$$0.get(1)).floatValue(), ((Float)$$0.get(2)).floatValue(), ((Float)$$0.get(3)).floatValue()).normalize()), $$0 -> List.of(Float.valueOf($$0.x()), Float.valueOf($$0.y()), Float.valueOf($$0.z()), Float.valueOf($$0.w())));
    public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create($$02 -> $$02.group((App)Codec.FLOAT.fieldOf("angle").forGetter($$0 -> Float.valueOf($$0.angle)), (App)VECTOR3F.fieldOf("axis").forGetter($$0 -> new Vector3f($$0.x, $$0.y, $$0.z))).apply((Applicative)$$02, AxisAngle4f::new));
    public static final Codec<Quaternionfc> QUATERNIONF = Codec.withAlternative(QUATERNIONF_COMPONENTS, (Codec)AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static final Codec<Matrix4fc> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 16).map($$0 -> {
        Matrix4f $$1 = new Matrix4f();
        for (int $$2 = 0; $$2 < $$0.size(); ++$$2) {
            $$1.setRowColumn($$2 >> 2, $$2 & 3, ((Float)$$0.get($$2)).floatValue());
        }
        return $$1.determineProperties();
    }), $$0 -> {
        FloatArrayList $$1 = new FloatArrayList(16);
        for (int $$2 = 0; $$2 < 16; ++$$2) {
            $$1.add($$0.getRowColumn($$2 >> 2, $$2 & 3));
        }
        return $$1;
    });
    private static final String HEX_COLOR_PREFIX = "#";
    public static final Codec<Integer> RGB_COLOR_CODEC = Codec.withAlternative((Codec)Codec.INT, VECTOR3F, $$0 -> ARGB.colorFromFloat(1.0f, $$0.x(), $$0.y(), $$0.z()));
    public static final Codec<Integer> ARGB_COLOR_CODEC = Codec.withAlternative((Codec)Codec.INT, VECTOR4F, $$0 -> ARGB.colorFromFloat($$0.w(), $$0.x(), $$0.y(), $$0.z()));
    public static final Codec<Integer> STRING_RGB_COLOR = Codec.withAlternative((Codec)ExtraCodecs.hexColor(6).xmap(ARGB::opaque, ARGB::transparent), RGB_COLOR_CODEC);
    public static final Codec<Integer> STRING_ARGB_COLOR = Codec.withAlternative(ExtraCodecs.hexColor(8), ARGB_COLOR_CODEC);
    public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE.flatComapMap(UnsignedBytes::toInt, $$0 -> {
        if ($$0 > 255) {
            return DataResult.error(() -> "Unsigned byte was too large: " + $$0 + " > 255");
        }
        return DataResult.success((Object)$$0.byteValue());
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, $$0 -> "Value must be non-negative: " + $$0);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, $$0 -> "Value must be positive: " + $$0);
    public static final Codec<Long> NON_NEGATIVE_LONG = ExtraCodecs.longRangeWithMessage(0L, Long.MAX_VALUE, $$0 -> "Value must be non-negative: " + $$0);
    public static final Codec<Long> POSITIVE_LONG = ExtraCodecs.longRangeWithMessage(1L, Long.MAX_VALUE, $$0 -> "Value must be positive: " + $$0);
    public static final Codec<Float> NON_NEGATIVE_FLOAT = ExtraCodecs.floatRangeMinInclusiveWithMessage(0.0f, Float.MAX_VALUE, $$0 -> "Value must be non-negative: " + $$0);
    public static final Codec<Float> POSITIVE_FLOAT = ExtraCodecs.floatRangeMinExclusiveWithMessage(0.0f, Float.MAX_VALUE, $$0 -> "Value must be positive: " + $$0);
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap($$0 -> {
        try {
            return DataResult.success((Object)Pattern.compile($$0));
        }
        catch (PatternSyntaxException $$1) {
            return DataResult.error(() -> "Invalid regex pattern '" + $$0 + "': " + $$1.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = ExtraCodecs.temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap($$0 -> {
        try {
            return DataResult.success((Object)Base64.getDecoder().decode((String)$$0));
        }
        catch (IllegalArgumentException $$1) {
            return DataResult.error(() -> "Malformed base64 string");
        }
    }, $$0 -> Base64.getEncoder().encodeToString((byte[])$$0));
    public static final Codec<String> ESCAPED_STRING = Codec.STRING.comapFlatMap($$0 -> DataResult.success((Object)StringEscapeUtils.unescapeJava((String)$$0)), StringEscapeUtils::escapeJava);
    public static final Codec<TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap($$02 -> $$02.startsWith(HEX_COLOR_PREFIX) ? Identifier.read($$02.substring(1)).map($$0 -> new TagOrElementLocation((Identifier)$$0, true)) : Identifier.read($$02).map($$0 -> new TagOrElementLocation((Identifier)$$0, false)), TagOrElementLocation::decoratedId);
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = $$0 -> $$0.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = $$0 -> $$0.isPresent() ? Optional.of($$0.getAsLong()) : Optional.empty();
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap($$0 -> BitSet.valueOf($$0.toArray()), $$0 -> Arrays.stream($$0.toLongArray()));
    public static final int MAX_PROPERTY_NAME_LENGTH = 64;
    public static final int MAX_PROPERTY_VALUE_LENGTH = Short.MAX_VALUE;
    public static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
    public static final int MAX_PROPERTIES = 16;
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create($$02 -> $$02.group((App)Codec.sizeLimitedString((int)64).fieldOf("name").forGetter(Property::name), (App)Codec.sizeLimitedString((int)Short.MAX_VALUE).fieldOf("value").forGetter(Property::value), (App)Codec.sizeLimitedString((int)1024).optionalFieldOf("signature").forGetter($$0 -> Optional.ofNullable($$0.signature()))).apply((Applicative)$$02, ($$0, $$1, $$2) -> new Property($$0, $$1, (String)$$2.orElse(null))));
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either((Codec)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING.listOf()).validate($$0 -> $$0.size() > 16 ? DataResult.error(() -> "Cannot have more than 16 properties, but was " + $$0.size()) : DataResult.success((Object)$$0)), (Codec)PROPERTY.sizeLimitedListOf(16)).xmap($$0 -> {
        ImmutableMultimap.Builder $$13 = ImmutableMultimap.builder();
        $$0.ifLeft($$12 -> $$12.forEach(($$1, $$2) -> {
            for (String $$3 : $$2) {
                $$13.put($$1, (Object)new Property($$1, $$3));
            }
        })).ifRight($$1 -> {
            for (Property $$2 : $$1) {
                $$13.put((Object)$$2.name(), (Object)$$2);
            }
        });
        return new PropertyMap((Multimap)$$13.build());
    }, $$0 -> Either.right($$0.values().stream().toList()));
    public static final Codec<String> PLAYER_NAME = Codec.string((int)0, (int)16).validate($$0 -> {
        if (StringUtil.isValidPlayerName($$0)) {
            return DataResult.success((Object)$$0);
        }
        return DataResult.error(() -> "Player name contained disallowed characters: '" + $$0 + "'");
    });
    public static final Codec<GameProfile> AUTHLIB_GAME_PROFILE = ExtraCodecs.gameProfileCodec(UUIDUtil.AUTHLIB_CODEC).codec();
    public static final MapCodec<GameProfile> STORED_GAME_PROFILE = ExtraCodecs.gameProfileCodec(UUIDUtil.CODEC);
    public static final Codec<String> NON_EMPTY_STRING = Codec.STRING.validate($$0 -> $$0.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success((Object)$$0));
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap($$0 -> {
        int[] $$1 = $$0.codePoints().toArray();
        if ($$1.length != 1) {
            return DataResult.error(() -> "Expected one codepoint, got: " + $$0);
        }
        return DataResult.success((Object)$$1[0]);
    }, Character::toString);
    public static final Codec<String> RESOURCE_PATH_CODEC = Codec.STRING.validate($$0 -> {
        if (!Identifier.isValidPath($$0)) {
            return DataResult.error(() -> "Invalid string to use as a resource path element: " + $$0);
        }
        return DataResult.success((Object)$$0);
    });
    public static final Codec<URI> UNTRUSTED_URI = Codec.STRING.comapFlatMap($$0 -> {
        try {
            return DataResult.success((Object)Util.parseAndValidateUntrustedUri($$0));
        }
        catch (URISyntaxException $$1) {
            return DataResult.error($$1::getMessage);
        }
    }, URI::toString);
    public static final Codec<String> CHAT_STRING = Codec.STRING.validate($$0 -> {
        for (int $$1 = 0; $$1 < $$0.length(); ++$$1) {
            char $$2 = $$0.charAt($$1);
            if (StringUtil.isAllowedChatCharacter($$2)) continue;
            return DataResult.error(() -> "Disallowed chat character: '" + $$2 + "'");
        }
        return DataResult.success((Object)$$0);
    });

    public static <T> Codec<T> converter(DynamicOps<T> $$0) {
        return Codec.PASSTHROUGH.xmap($$1 -> $$1.convert($$0).getValue(), $$1 -> new Dynamic($$0, $$1));
    }

    private static Codec<Integer> hexColor(int $$0) {
        long $$12 = (1L << $$0 * 4) - 1L;
        return Codec.STRING.comapFlatMap($$2 -> {
            if (!$$2.startsWith(HEX_COLOR_PREFIX)) {
                return DataResult.error(() -> "Hex color must begin with #");
            }
            int $$3 = $$2.length() - HEX_COLOR_PREFIX.length();
            if ($$3 != $$0) {
                return DataResult.error(() -> "Hex color is wrong size, expected " + $$0 + " digits but got " + $$3);
            }
            try {
                long $$4 = HexFormat.fromHexDigitsToLong($$2, HEX_COLOR_PREFIX.length(), $$2.length());
                if ($$4 < 0L || $$4 > $$12) {
                    return DataResult.error(() -> "Color value out of range: " + $$2);
                }
                return DataResult.success((Object)((int)$$4));
            }
            catch (NumberFormatException $$5) {
                return DataResult.error(() -> "Invalid color value: " + $$2);
            }
        }, $$1 -> HEX_COLOR_PREFIX + HexFormat.of().toHexDigits($$1.intValue(), $$0));
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> $$0, String $$13, String $$22, BiFunction<P, P, DataResult<I>> $$32, Function<I, P> $$4, Function<I, P> $$5) {
        Codec $$6 = Codec.list($$0).comapFlatMap($$12 -> Util.fixedSize($$12, 2).flatMap($$1 -> {
            Object $$2 = $$1.get(0);
            Object $$3 = $$1.get(1);
            return (DataResult)$$32.apply($$2, $$3);
        }), $$2 -> ImmutableList.of($$4.apply($$2), $$5.apply($$2)));
        Codec $$7 = RecordCodecBuilder.create($$3 -> $$3.group((App)$$0.fieldOf($$13).forGetter(Pair::getFirst), (App)$$0.fieldOf($$22).forGetter(Pair::getSecond)).apply((Applicative)$$3, Pair::of)).comapFlatMap($$1 -> (DataResult)$$32.apply($$1.getFirst(), $$1.getSecond()), $$2 -> Pair.of($$4.apply($$2), $$5.apply($$2)));
        Codec $$8 = Codec.withAlternative((Codec)$$6, (Codec)$$7);
        return Codec.either($$0, (Codec)$$8).comapFlatMap($$12 -> (DataResult)$$12.map($$1 -> (DataResult)$$32.apply($$1, $$1), DataResult::success), $$2 -> {
            Object $$4;
            Object $$3 = $$4.apply($$2);
            if (Objects.equals($$3, $$4 = $$5.apply($$2))) {
                return Either.left($$3);
            }
            return Either.right((Object)$$2);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A $$0) {
        return new Codec.ResultFunction<A>(){

            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> $$02, T $$1, DataResult<Pair<A, T>> $$2) {
                MutableObject $$3 = new MutableObject();
                Optional $$4 = $$2.resultOrPartial(arg_0 -> ((MutableObject)$$3).setValue(arg_0));
                if ($$4.isPresent()) {
                    return $$2;
                }
                return DataResult.error(() -> "(" + (String)$$3.get() + " -> using default)", (Object)Pair.of((Object)$$0, $$1));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> $$02, A $$1, DataResult<T> $$2) {
                return $$2;
            }

            public String toString() {
                return "OrElsePartial[" + String.valueOf($$0) + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> $$0, IntFunction<@Nullable E> $$12, int $$22) {
        return Codec.INT.flatXmap($$1 -> Optional.ofNullable($$12.apply((int)$$1)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element id: " + $$1)), $$2 -> {
            int $$3 = $$0.applyAsInt($$2);
            return $$3 == $$22 ? DataResult.error(() -> "Element with unknown id: " + String.valueOf($$2)) : DataResult.success((Object)$$3);
        });
    }

    public static <I, E> Codec<E> idResolverCodec(Codec<I> $$0, Function<I, @Nullable E> $$12, Function<E, @Nullable I> $$2) {
        return $$0.flatXmap($$1 -> {
            Object $$2 = $$12.apply($$1);
            return $$2 == null ? DataResult.error(() -> "Unknown element id: " + String.valueOf($$1)) : DataResult.success($$2);
        }, $$1 -> {
            Object $$2 = $$2.apply($$1);
            if ($$2 == null) {
                return DataResult.error(() -> "Element with unknown id: " + String.valueOf($$1));
            }
            return DataResult.success($$2);
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> $$0, final Codec<E> $$1) {
        return new Codec<E>(){

            public <T> DataResult<T> encode(E $$02, DynamicOps<T> $$12, T $$2) {
                if ($$12.compressMaps()) {
                    return $$1.encode($$02, $$12, $$2);
                }
                return $$0.encode($$02, $$12, $$2);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> $$02, T $$12) {
                if ($$02.compressMaps()) {
                    return $$1.decode($$02, $$12);
                }
                return $$0.decode($$02, $$12);
            }

            public String toString() {
                return String.valueOf($$0) + " orCompressed " + String.valueOf($$1);
            }
        };
    }

    public static <E> MapCodec<E> orCompressed(final MapCodec<E> $$0, final MapCodec<E> $$1) {
        return new MapCodec<E>(){

            public <T> RecordBuilder<T> encode(E $$02, DynamicOps<T> $$12, RecordBuilder<T> $$2) {
                if ($$12.compressMaps()) {
                    return $$1.encode($$02, $$12, $$2);
                }
                return $$0.encode($$02, $$12, $$2);
            }

            public <T> DataResult<E> decode(DynamicOps<T> $$02, MapLike<T> $$12) {
                if ($$02.compressMaps()) {
                    return $$1.decode($$02, $$12);
                }
                return $$0.decode($$02, $$12);
            }

            public <T> Stream<T> keys(DynamicOps<T> $$02) {
                return $$1.keys($$02);
            }

            public String toString() {
                return String.valueOf($$0) + " orCompressed " + String.valueOf($$1);
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> $$0, final Function<E, Lifecycle> $$1, final Function<E, Lifecycle> $$2) {
        return $$0.mapResult(new Codec.ResultFunction<E>(){

            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> $$0, T $$12, DataResult<Pair<E, T>> $$22) {
                return $$22.result().map($$2 -> $$22.setLifecycle((Lifecycle)$$1.apply($$2.getFirst()))).orElse($$22);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> $$0, E $$12, DataResult<T> $$22) {
                return $$22.setLifecycle((Lifecycle)$$2.apply($$12));
            }

            public String toString() {
                return "WithLifecycle[" + String.valueOf($$1) + " " + String.valueOf($$2) + "]";
            }
        });
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> $$0, Function<E, Lifecycle> $$1) {
        return ExtraCodecs.overrideLifecycle($$0, $$1, $$1);
    }

    public static <K, V> StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> $$0, Codec<V> $$1) {
        return new StrictUnboundedMapCodec<K, V>($$0, $$1);
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> $$0) {
        return ExtraCodecs.compactListCodec($$0, $$0.listOf());
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> $$03, Codec<List<E>> $$1) {
        return Codec.either($$1, $$03).xmap($$02 -> (List)$$02.map($$0 -> $$0, List::of), $$0 -> $$0.size() == 1 ? Either.right($$0.getFirst()) : Either.left((Object)$$0));
    }

    private static Codec<Integer> intRangeWithMessage(int $$0, int $$1, Function<Integer, String> $$2) {
        return Codec.INT.validate($$3 -> {
            if ($$3.compareTo($$0) >= 0 && $$3.compareTo($$1) <= 0) {
                return DataResult.success((Object)$$3);
            }
            return DataResult.error(() -> (String)$$2.apply((Integer)$$3));
        });
    }

    public static Codec<Integer> intRange(int $$0, int $$1) {
        return ExtraCodecs.intRangeWithMessage($$0, $$1, $$2 -> "Value must be within range [" + $$0 + ";" + $$1 + "]: " + $$2);
    }

    private static Codec<Long> longRangeWithMessage(long $$0, long $$1, Function<Long, String> $$2) {
        return Codec.LONG.validate($$3 -> {
            if ((long)$$3.compareTo($$0) >= 0L && (long)$$3.compareTo($$1) <= 0L) {
                return DataResult.success((Object)$$3);
            }
            return DataResult.error(() -> (String)$$2.apply((Long)$$3));
        });
    }

    public static Codec<Long> longRange(int $$0, int $$1) {
        return ExtraCodecs.longRangeWithMessage($$0, $$1, $$2 -> "Value must be within range [" + $$0 + ";" + $$1 + "]: " + $$2);
    }

    private static Codec<Float> floatRangeMinInclusiveWithMessage(float $$0, float $$1, Function<Float, String> $$2) {
        return Codec.FLOAT.validate($$3 -> {
            if ($$3.compareTo(Float.valueOf($$0)) >= 0 && $$3.compareTo(Float.valueOf($$1)) <= 0) {
                return DataResult.success((Object)$$3);
            }
            return DataResult.error(() -> (String)$$2.apply((Float)$$3));
        });
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float $$0, float $$1, Function<Float, String> $$2) {
        return Codec.FLOAT.validate($$3 -> {
            if ($$3.compareTo(Float.valueOf($$0)) > 0 && $$3.compareTo(Float.valueOf($$1)) <= 0) {
                return DataResult.success((Object)$$3);
            }
            return DataResult.error(() -> (String)$$2.apply((Float)$$3));
        });
    }

    public static Codec<Float> floatRange(float $$0, float $$1) {
        return ExtraCodecs.floatRangeMinInclusiveWithMessage($$0, $$1, $$2 -> "Value must be within range [" + $$0 + ";" + $$1 + "]: " + $$2);
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> $$02) {
        return $$02.validate($$0 -> $$0.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success((Object)$$0));
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> $$02) {
        return $$02.validate($$0 -> {
            if ($$0.unwrap().right().filter(List::isEmpty).isPresent()) {
                return DataResult.error(() -> "List must have contents");
            }
            return DataResult.success((Object)$$0);
        });
    }

    public static <M extends Map<?, ?>> Codec<M> nonEmptyMap(Codec<M> $$02) {
        return $$02.validate($$0 -> $$0.isEmpty() ? DataResult.error(() -> "Map must have contents") : DataResult.success((Object)$$0));
    }

    public static <E> MapCodec<E> retrieveContext(Function<DynamicOps<?>, DataResult<E>> $$0) {
        class ContextRetrievalCodec
        extends MapCodec<E> {
            final /* synthetic */ Function val$getter;

            ContextRetrievalCodec(Function function) {
                this.val$getter = function;
            }

            public <T> RecordBuilder<T> encode(E $$0, DynamicOps<T> $$1, RecordBuilder<T> $$2) {
                return $$2;
            }

            public <T> DataResult<E> decode(DynamicOps<T> $$0, MapLike<T> $$1) {
                return (DataResult)this.val$getter.apply($$0);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + String.valueOf(this.val$getter) + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> $$0) {
                return Stream.empty();
            }
        }
        return new ContextRetrievalCodec($$0);
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> $$0) {
        return $$1 -> {
            Iterator $$2 = $$1.iterator();
            if ($$2.hasNext()) {
                Object $$3 = $$0.apply($$2.next());
                while ($$2.hasNext()) {
                    Object $$4 = $$2.next();
                    Object $$5 = $$0.apply($$4);
                    if ($$5 == $$3) continue;
                    return DataResult.error(() -> "Mixed type list: element " + String.valueOf($$4) + " had type " + String.valueOf($$5) + ", but list is of type " + String.valueOf($$3));
                }
            }
            return DataResult.success((Object)$$1, (Lifecycle)Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> $$0) {
        return Codec.of($$0, (Decoder)new Decoder<A>(){

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> $$02, T $$1) {
                try {
                    return $$0.decode($$02, $$1);
                }
                catch (Exception $$2) {
                    return DataResult.error(() -> "Caught exception decoding " + String.valueOf($$1) + ": " + $$2.getMessage());
                }
            }
        });
    }

    public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter $$0) {
        return Codec.STRING.comapFlatMap($$1 -> {
            try {
                return DataResult.success((Object)$$0.parse((CharSequence)$$1));
            }
            catch (Exception $$2) {
                return DataResult.error($$2::getMessage);
            }
        }, $$0::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> $$0) {
        return $$0.xmap(toOptionalLong, fromOptionalLong);
    }

    private static MapCodec<GameProfile> gameProfileCodec(Codec<UUID> $$0) {
        return RecordCodecBuilder.mapCodec($$1 -> $$1.group((App)$$0.fieldOf("id").forGetter(GameProfile::id), (App)PLAYER_NAME.fieldOf("name").forGetter(GameProfile::name), (App)PROPERTY_MAP.optionalFieldOf("properties", (Object)PropertyMap.EMPTY).forGetter(GameProfile::properties)).apply((Applicative)$$1, GameProfile::new));
    }

    public static <K, V> Codec<Map<K, V>> sizeLimitedMap(Codec<Map<K, V>> $$0, int $$12) {
        return $$0.validate($$1 -> {
            if ($$1.size() > $$12) {
                return DataResult.error(() -> "Map is too long: " + $$1.size() + ", expected range [0-" + $$12 + "]");
            }
            return DataResult.success((Object)$$1);
        });
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> $$0) {
        return Codec.unboundedMap($$0, (Codec)Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    @Deprecated
    public static <K, V> MapCodec<V> dispatchOptionalValue(final String $$0, final String $$1, final Codec<K> $$2, final Function<? super V, ? extends K> $$3, final Function<? super K, ? extends Codec<? extends V>> $$4) {
        return new MapCodec<V>(){

            public <T> Stream<T> keys(DynamicOps<T> $$02) {
                return Stream.of($$02.createString($$0), $$02.createString($$1));
            }

            public <T> DataResult<V> decode(DynamicOps<T> $$02, MapLike<T> $$12) {
                Object $$22 = $$12.get($$0);
                if ($$22 == null) {
                    return DataResult.error(() -> "Missing \"" + $$0 + "\" in: " + String.valueOf($$12));
                }
                return $$2.decode($$02, $$22).flatMap($$4 -> {
                    Object $$5 = Objects.requireNonNullElseGet($$12.get($$1), () -> ((DynamicOps)$$02).emptyMap());
                    return ((Codec)$$4.apply($$4.getFirst())).decode($$02, $$5).map(Pair::getFirst);
                });
            }

            public <T> RecordBuilder<T> encode(V $$02, DynamicOps<T> $$12, RecordBuilder<T> $$22) {
                Object $$32 = $$3.apply($$02);
                $$22.add($$0, $$2.encodeStart($$12, $$32));
                DataResult<T> $$42 = this.encode((Codec)$$4.apply($$32), $$02, $$12);
                if ($$42.result().isEmpty() || !Objects.equals($$42.result().get(), $$12.emptyMap())) {
                    $$22.add($$1, $$42);
                }
                return $$22;
            }

            private <T, V2 extends V> DataResult<T> encode(Codec<V2> $$02, V $$12, DynamicOps<T> $$22) {
                return $$02.encodeStart($$22, $$12);
            }
        };
    }

    public static <A> Codec<Optional<A>> optionalEmptyMap(final Codec<A> $$0) {
        return new Codec<Optional<A>>(){

            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> $$02, T $$1) {
                if (7.isEmptyMap($$02, $$1)) {
                    return DataResult.success((Object)Pair.of(Optional.empty(), $$1));
                }
                return $$0.decode($$02, $$1).map($$0 -> $$0.mapFirst(Optional::of));
            }

            private static <T> boolean isEmptyMap(DynamicOps<T> $$02, T $$1) {
                Optional $$2 = $$02.getMap($$1).result();
                return $$2.isPresent() && ((MapLike)$$2.get()).entries().findAny().isEmpty();
            }

            public <T> DataResult<T> encode(Optional<A> $$02, DynamicOps<T> $$1, T $$2) {
                if ($$02.isEmpty()) {
                    return DataResult.success((Object)$$1.emptyMap());
                }
                return $$0.encode($$02.get(), $$1, $$2);
            }

            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((Optional)object, dynamicOps, object2);
            }
        };
    }

    @Deprecated
    public static <E extends Enum<E>> Codec<E> legacyEnum(Function<String, E> $$0) {
        return Codec.STRING.comapFlatMap($$1 -> {
            try {
                return DataResult.success((Object)((Enum)$$0.apply((String)$$1)));
            }
            catch (IllegalArgumentException $$2) {
                return DataResult.error(() -> "No value with id: " + $$1);
            }
        }, Enum::toString);
    }

    public record StrictUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>,
    BaseMapCodec<K, V>
    {
        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> $$0, MapLike<T> $$1) {
            ImmutableMap.Builder $$2 = ImmutableMap.builder();
            for (Pair $$3 : $$1.entries().toList()) {
                DataResult $$5;
                DataResult $$4 = this.keyCodec().parse($$0, $$3.getFirst());
                DataResult $$6 = $$4.apply2stable(Pair::of, $$5 = this.elementCodec().parse($$0, $$3.getSecond()));
                Optional $$7 = $$6.error();
                if ($$7.isPresent()) {
                    String $$8 = ((DataResult.Error)$$7.get()).message();
                    return DataResult.error(() -> {
                        if ($$4.result().isPresent()) {
                            return "Map entry '" + String.valueOf($$4.result().get()) + "' : " + $$8;
                        }
                        return $$8;
                    });
                }
                if ($$6.result().isPresent()) {
                    Pair $$9 = (Pair)$$6.result().get();
                    $$2.put($$9.getFirst(), $$9.getSecond());
                    continue;
                }
                return DataResult.error(() -> "Empty or invalid map contents are not allowed");
            }
            ImmutableMap $$10 = $$2.build();
            return DataResult.success((Object)$$10);
        }

        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> $$0, T $$12) {
            return $$0.getMap($$12).setLifecycle(Lifecycle.stable()).flatMap($$1 -> this.decode($$0, (Object)$$1)).map($$1 -> Pair.of((Object)$$1, (Object)$$12));
        }

        public <T> DataResult<T> encode(Map<K, V> $$0, DynamicOps<T> $$1, T $$2) {
            return this.encode($$0, $$1, $$1.mapBuilder()).build($$2);
        }

        @Override
        public String toString() {
            return "StrictUnboundedMapCodec[" + String.valueOf(this.keyCodec) + " -> " + String.valueOf(this.elementCodec) + "]";
        }

        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Map)object, dynamicOps, object2);
        }
    }

    public record TagOrElementLocation(Identifier id, boolean tag) {
        @Override
        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? ExtraCodecs.HEX_COLOR_PREFIX + String.valueOf(this.id) : this.id.toString();
        }
    }

    public static class LateBoundIdMapper<I, V> {
        private final BiMap<I, V> idToValue = HashBiMap.create();

        public Codec<V> codec(Codec<I> $$0) {
            BiMap $$1 = this.idToValue.inverse();
            return ExtraCodecs.idResolverCodec($$0, arg_0 -> this.idToValue.get(arg_0), arg_0 -> $$1.get(arg_0));
        }

        public LateBoundIdMapper<I, V> put(I $$0, V $$1) {
            Objects.requireNonNull($$1, () -> "Value for " + String.valueOf($$0) + " is null");
            this.idToValue.put($$0, $$1);
            return this;
        }

        public Set<V> values() {
            return Collections.unmodifiableSet(this.idToValue.values());
        }
    }
}

