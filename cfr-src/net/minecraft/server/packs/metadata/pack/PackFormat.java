/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.metadata.pack;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record PackFormat(int major, int minor) implements Comparable<PackFormat>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<PackFormat> BOTTOM_CODEC = PackFormat.fullCodec(0);
    public static final Codec<PackFormat> TOP_CODEC = PackFormat.fullCodec(Integer.MAX_VALUE);

    private static Codec<PackFormat> fullCodec(int $$0) {
        return ExtraCodecs.compactListCodec(ExtraCodecs.NON_NEGATIVE_INT, ExtraCodecs.NON_NEGATIVE_INT.listOf(1, 256)).xmap($$1 -> $$1.size() > 1 ? PackFormat.of((Integer)$$1.getFirst(), (Integer)$$1.get(1)) : PackFormat.of((Integer)$$1.getFirst(), $$0), $$1 -> $$1.minor != $$0 ? List.of(Integer.valueOf($$1.major()), Integer.valueOf($$1.minor())) : List.of(Integer.valueOf($$1.major())));
    }

    public static <ResultType, HolderType extends IntermediaryFormatHolder> DataResult<List<ResultType>> validateHolderList(List<HolderType> $$0, int $$1, BiFunction<HolderType, InclusiveRange<PackFormat>, ResultType> $$2) {
        int $$3 = $$0.stream().map(IntermediaryFormatHolder::format).mapToInt(IntermediaryFormat::effectiveMinMajorVersion).min().orElse(Integer.MAX_VALUE);
        ArrayList<ResultType> $$4 = new ArrayList<ResultType>($$0.size());
        for (IntermediaryFormatHolder $$5 : $$0) {
            IntermediaryFormat $$6 = $$5.format();
            if ($$6.min().isEmpty() && $$6.max().isEmpty() && $$6.supported().isEmpty()) {
                LOGGER.warn("Unknown or broken overlay entry {}", (Object)$$5);
                continue;
            }
            DataResult<InclusiveRange<PackFormat>> $$7 = $$6.validate($$1, false, $$3 <= $$1, "Overlay \"" + String.valueOf($$5) + "\"", "formats");
            if ($$7.isSuccess()) {
                $$4.add($$2.apply($$5, (InclusiveRange)$$7.getOrThrow()));
                continue;
            }
            return DataResult.error(() -> ((DataResult.Error)((DataResult.Error)$$7.error().get())).message());
        }
        return DataResult.success(List.copyOf($$4));
    }

    @VisibleForTesting
    public static int lastPreMinorVersion(PackType $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case PackType.CLIENT_RESOURCES -> 64;
            case PackType.SERVER_DATA -> 81;
        };
    }

    public static MapCodec<InclusiveRange<PackFormat>> packCodec(PackType $$0) {
        int $$12 = PackFormat.lastPreMinorVersion($$0);
        return IntermediaryFormat.PACK_CODEC.flatXmap($$1 -> $$1.validate($$12, true, false, "Pack", "supported_formats"), $$1 -> DataResult.success((Object)IntermediaryFormat.fromRange($$1, $$12)));
    }

    public static PackFormat of(int $$0, int $$1) {
        return new PackFormat($$0, $$1);
    }

    public static PackFormat of(int $$0) {
        return new PackFormat($$0, 0);
    }

    public InclusiveRange<PackFormat> minorRange() {
        return new InclusiveRange<PackFormat>(this, PackFormat.of(this.major, Integer.MAX_VALUE));
    }

    @Override
    public int compareTo(PackFormat $$0) {
        int $$1 = Integer.compare(this.major(), $$0.major());
        if ($$1 != 0) {
            return $$1;
        }
        return Integer.compare(this.minor(), $$0.minor());
    }

    @Override
    public String toString() {
        if (this.minor == Integer.MAX_VALUE) {
            return String.format(Locale.ROOT, "%d.*", this.major());
        }
        return String.format(Locale.ROOT, "%d.%d", this.major(), this.minor());
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((PackFormat)object);
    }

    public static interface IntermediaryFormatHolder {
        public IntermediaryFormat format();
    }

    public record IntermediaryFormat(Optional<PackFormat> min, Optional<PackFormat> max, Optional<Integer> format, Optional<InclusiveRange<Integer>> supported) {
        static final MapCodec<IntermediaryFormat> PACK_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(IntermediaryFormat::min), (App)TOP_CODEC.optionalFieldOf("max_format").forGetter(IntermediaryFormat::max), (App)Codec.INT.optionalFieldOf("pack_format").forGetter(IntermediaryFormat::format), (App)InclusiveRange.codec(Codec.INT).optionalFieldOf("supported_formats").forGetter(IntermediaryFormat::supported)).apply((Applicative)$$0, IntermediaryFormat::new));
        public static final MapCodec<IntermediaryFormat> OVERLAY_CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(IntermediaryFormat::min), (App)TOP_CODEC.optionalFieldOf("max_format").forGetter(IntermediaryFormat::max), (App)InclusiveRange.codec(Codec.INT).optionalFieldOf("formats").forGetter(IntermediaryFormat::supported)).apply((Applicative)$$02, ($$0, $$1, $$2) -> new IntermediaryFormat((Optional<PackFormat>)$$0, (Optional<PackFormat>)$$1, $$0.map(PackFormat::major), (Optional<InclusiveRange<Integer>>)$$2)));

        public static IntermediaryFormat fromRange(InclusiveRange<PackFormat> $$0, int $$1) {
            InclusiveRange<Integer> $$2 = $$0.map(PackFormat::major);
            return new IntermediaryFormat(Optional.of($$0.minInclusive()), Optional.of($$0.maxInclusive()), $$2.isValueInRange($$1) ? Optional.of($$2.minInclusive()) : Optional.empty(), $$2.isValueInRange($$1) ? Optional.of(new InclusiveRange<Integer>($$2.minInclusive(), $$2.maxInclusive())) : Optional.empty());
        }

        public int effectiveMinMajorVersion() {
            if (this.min.isPresent()) {
                if (this.supported.isPresent()) {
                    return Math.min(this.min.get().major(), this.supported.get().minInclusive());
                }
                return this.min.get().major();
            }
            if (this.supported.isPresent()) {
                return this.supported.get().minInclusive();
            }
            return Integer.MAX_VALUE;
        }

        public DataResult<InclusiveRange<PackFormat>> validate(int $$0, boolean $$1, boolean $$2, String $$3, String $$4) {
            if (this.min.isPresent() != this.max.isPresent()) {
                return DataResult.error(() -> $$3 + " missing field, must declare both min_format and max_format");
            }
            if ($$2 && this.supported.isEmpty()) {
                return DataResult.error(() -> $$3 + " missing required field " + $$4 + ", must be present in all overlays for any overlays to work across game versions");
            }
            if (this.min.isPresent()) {
                return this.validateNewFormat($$0, $$1, $$2, $$3, $$4);
            }
            if (this.supported.isPresent()) {
                return this.validateOldFormat($$0, $$1, $$3, $$4);
            }
            if ($$1 && this.format.isPresent()) {
                int $$5 = this.format.get();
                if ($$5 > $$0) {
                    return DataResult.error(() -> $$3 + " declares support for version newer than " + $$0 + ", but is missing mandatory fields min_format and max_format");
                }
                return DataResult.success(new InclusiveRange<PackFormat>(PackFormat.of($$5)));
            }
            return DataResult.error(() -> $$3 + " could not be parsed, missing format version information");
        }

        private DataResult<InclusiveRange<PackFormat>> validateNewFormat(int $$0, boolean $$1, boolean $$2, String $$3, String $$4) {
            int $$5 = this.min.get().major();
            int $$6 = this.max.get().major();
            if (this.min.get().compareTo(this.max.get()) > 0) {
                return DataResult.error(() -> $$3 + " min_format (" + String.valueOf(this.min.get()) + ") is greater than max_format (" + String.valueOf(this.max.get()) + ")");
            }
            if ($$5 > $$0 && !$$2) {
                String $$7;
                if (this.supported.isPresent()) {
                    return DataResult.error(() -> $$3 + " key " + $$4 + " is deprecated starting from pack format " + ($$0 + 1) + ". Remove " + $$4 + " from your pack.mcmeta.");
                }
                if ($$1 && this.format.isPresent() && ($$7 = this.validatePackFormatForRange($$5, $$6)) != null) {
                    return DataResult.error(() -> $$7);
                }
            } else {
                if (this.supported.isPresent()) {
                    InclusiveRange<Integer> $$8 = this.supported.get();
                    if ($$8.minInclusive() != $$5) {
                        return DataResult.error(() -> $$3 + " version declaration mismatch between " + $$4 + " (from " + String.valueOf($$8.minInclusive()) + ") and min_format (" + String.valueOf(this.min.get()) + ")");
                    }
                    if ($$8.maxInclusive() != $$6 && $$8.maxInclusive() != $$0) {
                        return DataResult.error(() -> $$3 + " version declaration mismatch between " + $$4 + " (up to " + String.valueOf($$8.maxInclusive()) + ") and max_format (" + String.valueOf(this.max.get()) + ")");
                    }
                } else {
                    return DataResult.error(() -> $$3 + " declares support for format " + $$5 + ", but game versions supporting formats 17 to " + $$0 + " require a " + $$4 + " field. Add \"" + $$4 + "\": [" + $$5 + ", " + $$0 + "] or require a version greater or equal to " + ($$0 + 1) + ".0.");
                }
                if ($$1) {
                    if (this.format.isPresent()) {
                        String $$9 = this.validatePackFormatForRange($$5, $$6);
                        if ($$9 != null) {
                            return DataResult.error(() -> $$9);
                        }
                    } else {
                        return DataResult.error(() -> $$3 + " declares support for formats up to " + $$0 + ", but game versions supporting formats 17 to " + $$0 + " require a pack_format field. Add \"pack_format\": " + $$5 + " or require a version greater or equal to " + ($$0 + 1) + ".0.");
                    }
                }
            }
            return DataResult.success(new InclusiveRange<PackFormat>(this.min.get(), this.max.get()));
        }

        private DataResult<InclusiveRange<PackFormat>> validateOldFormat(int $$0, boolean $$1, String $$2, String $$3) {
            InclusiveRange<Integer> $$4 = this.supported.get();
            int $$5 = $$4.minInclusive();
            int $$6 = $$4.maxInclusive();
            if ($$6 > $$0) {
                return DataResult.error(() -> $$2 + " declares support for version newer than " + $$0 + ", but is missing mandatory fields min_format and max_format");
            }
            if ($$1) {
                if (this.format.isPresent()) {
                    String $$7 = this.validatePackFormatForRange($$5, $$6);
                    if ($$7 != null) {
                        return DataResult.error(() -> $$7);
                    }
                } else {
                    return DataResult.error(() -> $$2 + " declares support for formats up to " + $$0 + ", but game versions supporting formats 17 to " + $$0 + " require a pack_format field. Add \"pack_format\": " + $$5 + " or require a version greater or equal to " + ($$0 + 1) + ".0.");
                }
            }
            return DataResult.success(new InclusiveRange<Integer>($$5, $$6).map(PackFormat::of));
        }

        private @Nullable String validatePackFormatForRange(int $$0, int $$1) {
            int $$2 = this.format.get();
            if ($$2 < $$0 || $$2 > $$1) {
                return "Pack declared support for versions " + $$0 + " to " + $$1 + " but declared main format is " + $$2;
            }
            if ($$2 < 15) {
                return "Multi-version packs cannot support minimum version of less than 15, since this will leave versions in range unable to load pack.";
            }
            return null;
        }
    }
}

