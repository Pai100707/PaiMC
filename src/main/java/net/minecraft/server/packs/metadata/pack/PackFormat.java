package net.minecraft.server.packs.metadata.pack;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import org.slf4j.Logger;

public record PackFormat(int major, int minor) implements Comparable<PackFormat> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<PackFormat> BOTTOM_CODEC = fullCodec(0);
   public static final Codec<PackFormat> TOP_CODEC = fullCodec(Integer.MAX_VALUE);

   private static Codec<PackFormat> fullCodec(int $$0) {
      return ExtraCodecs.compactListCodec(ExtraCodecs.NON_NEGATIVE_INT, ExtraCodecs.NON_NEGATIVE_INT.listOf(1, 256))
         .xmap(
            $$1 -> $$1.size() > 1 ? of((Integer)$$1.getFirst(), (Integer)$$1.get(1)) : of((Integer)$$1.getFirst(), $$0),
            $$1 -> $$1.minor != $$0 ? List.of($$1.major(), $$1.minor()) : List.of($$1.major())
         );
   }

   public static <ResultType, HolderType extends PackFormat.IntermediaryFormatHolder> DataResult<List<ResultType>> validateHolderList(
      List<HolderType> $$0, int $$1, BiFunction<HolderType, InclusiveRange<PackFormat>, ResultType> $$2
   ) {
      int $$3 = $$0.stream()
         .map(PackFormat.IntermediaryFormatHolder::format)
         .mapToInt(PackFormat.IntermediaryFormat::effectiveMinMajorVersion)
         .min()
         .orElse(Integer.MAX_VALUE);
      List<ResultType> $$4 = new ArrayList<>($$0.size());

      for (HolderType $$5 : $$0) {
         PackFormat.IntermediaryFormat $$6 = $$5.format();
         if ($$6.min().isEmpty() && $$6.max().isEmpty() && $$6.supported().isEmpty()) {
            LOGGER.warn("Unknown or broken overlay entry {}", $$5);
         } else {
            DataResult<InclusiveRange<PackFormat>> $$7 = $$6.validate($$1, false, $$3 <= $$1, "Overlay \"" + $$5 + "\"", "formats");
            if (!$$7.isSuccess()) {
               return DataResult.error(((Error)$$7.error().get())::message);
            }

            $$4.add($$2.apply($$5, (InclusiveRange<PackFormat>)$$7.getOrThrow()));
         }
      }

      return DataResult.success(List.copyOf($$4));
   }

   @VisibleForTesting
   public static int lastPreMinorVersion(PackType $$0) {
      return switch ($$0) {
         case CLIENT_RESOURCES -> 64;
         case SERVER_DATA -> 81;
      };
   }

   public static MapCodec<InclusiveRange<PackFormat>> packCodec(PackType $$0) {
      int $$1 = lastPreMinorVersion($$0);
      return PackFormat.IntermediaryFormat.PACK_CODEC
         .flatXmap(
            $$1x -> $$1x.validate($$1, true, false, "Pack", "supported_formats"),
            $$1x -> DataResult.success(PackFormat.IntermediaryFormat.fromRange($$1x, $$1))
         );
   }

   public static PackFormat of(int $$0, int $$1) {
      return new PackFormat($$0, $$1);
   }

   public static PackFormat of(int $$0) {
      return new PackFormat($$0, 0);
   }

   public InclusiveRange<PackFormat> minorRange() {
      return new InclusiveRange(this, of(this.major, Integer.MAX_VALUE));
   }

   public int compareTo(PackFormat $$0) {
      int $$1 = Integer.compare(this.major(), $$0.major());
      return $$1 != 0 ? $$1 : Integer.compare(this.minor(), $$0.minor());
   }

   @Override
   public String toString() {
      return this.minor == Integer.MAX_VALUE
         ? String.format(Locale.ROOT, "%d.*", this.major())
         : String.format(Locale.ROOT, "%d.%d", this.major(), this.minor());
   }

   public record IntermediaryFormat(Optional<PackFormat> min, Optional<PackFormat> max, Optional<Integer> format, Optional<InclusiveRange<Integer>> supported) {
      static final MapCodec<PackFormat.IntermediaryFormat> PACK_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               PackFormat.BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(PackFormat.IntermediaryFormat::min),
               PackFormat.TOP_CODEC.optionalFieldOf("max_format").forGetter(PackFormat.IntermediaryFormat::max),
               Codec.INT.optionalFieldOf("pack_format").forGetter(PackFormat.IntermediaryFormat::format),
               InclusiveRange.codec(Codec.INT).optionalFieldOf("supported_formats").forGetter(PackFormat.IntermediaryFormat::supported)
            )
            .apply($$0, PackFormat.IntermediaryFormat::new)
      );
      public static final MapCodec<PackFormat.IntermediaryFormat> OVERLAY_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               PackFormat.BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(PackFormat.IntermediaryFormat::min),
               PackFormat.TOP_CODEC.optionalFieldOf("max_format").forGetter(PackFormat.IntermediaryFormat::max),
               InclusiveRange.codec(Codec.INT).optionalFieldOf("formats").forGetter(PackFormat.IntermediaryFormat::supported)
            )
            .apply($$0, ($$0x, $$1, $$2) -> new PackFormat.IntermediaryFormat($$0x, $$1, $$0x.map(PackFormat::major), $$2))
      );

      public static PackFormat.IntermediaryFormat fromRange(InclusiveRange<PackFormat> $$0, int $$1) {
         InclusiveRange<Integer> $$2 = $$0.map(PackFormat::major);
         return new PackFormat.IntermediaryFormat(
            Optional.of((PackFormat)$$0.minInclusive()),
            Optional.of((PackFormat)$$0.maxInclusive()),
            $$2.isValueInRange($$1) ? Optional.of((Integer)$$2.minInclusive()) : Optional.empty(),
            $$2.isValueInRange($$1) ? Optional.of(new InclusiveRange((Integer)$$2.minInclusive(), (Integer)$$2.maxInclusive())) : Optional.empty()
         );
      }

      public int effectiveMinMajorVersion() {
         if (this.min.isPresent()) {
            return this.supported.isPresent() ? Math.min(this.min.get().major(), (Integer)this.supported.get().minInclusive()) : this.min.get().major();
         } else {
            return this.supported.isPresent() ? (Integer)this.supported.get().minInclusive() : Integer.MAX_VALUE;
         }
      }

      public DataResult<InclusiveRange<PackFormat>> validate(int $$0, boolean $$1, boolean $$2, String $$3, String $$4) {
         if (this.min.isPresent() != this.max.isPresent()) {
            return DataResult.error(() -> $$3 + " missing field, must declare both min_format and max_format");
         } else if ($$2 && this.supported.isEmpty()) {
            return DataResult.error(
               () -> $$3 + " missing required field " + $$4 + ", must be present in all overlays for any overlays to work across game versions"
            );
         } else if (this.min.isPresent()) {
            return this.validateNewFormat($$0, $$1, $$2, $$3, $$4);
         } else if (this.supported.isPresent()) {
            return this.validateOldFormat($$0, $$1, $$3, $$4);
         } else if ($$1 && this.format.isPresent()) {
            int $$5 = this.format.get();
            return $$5 > $$0
               ? DataResult.error(() -> $$3 + " declares support for version newer than " + $$0 + ", but is missing mandatory fields min_format and max_format")
               : DataResult.success(new InclusiveRange(PackFormat.of($$5)));
         } else {
            return DataResult.error(() -> $$3 + " could not be parsed, missing format version information");
         }
      }

      private DataResult<InclusiveRange<PackFormat>> validateNewFormat(int $$0, boolean $$1, boolean $$2, String $$3, String $$4) {
         int $$5 = this.min.get().major();
         int $$6 = this.max.get().major();
         if (this.min.get().compareTo(this.max.get()) > 0) {
            return DataResult.error(() -> $$3 + " min_format (" + this.min.get() + ") is greater than max_format (" + this.max.get() + ")");
         } else {
            if ($$5 > $$0 && !$$2) {
               if (this.supported.isPresent()) {
                  return DataResult.error(
                     () -> $$3 + " key " + $$4 + " is deprecated starting from pack format " + ($$0 + 1) + ". Remove " + $$4 + " from your pack.mcmeta."
                  );
               }

               if ($$1 && this.format.isPresent()) {
                  String $$7 = this.validatePackFormatForRange($$5, $$6);
                  if ($$7 != null) {
                     return DataResult.error(() -> $$7);
                  }
               }
            } else {
               if (!this.supported.isPresent()) {
                  return DataResult.error(
                     () -> $$3
                        + " declares support for format "
                        + $$5
                        + ", but game versions supporting formats 17 to "
                        + $$0
                        + " require a "
                        + $$4
                        + " field. Add \""
                        + $$4
                        + "\": ["
                        + $$5
                        + ", "
                        + $$0
                        + "] or require a version greater or equal to "
                        + ($$0 + 1)
                        + ".0."
                  );
               }

               InclusiveRange<Integer> $$8 = this.supported.get();
               if ((Integer)$$8.minInclusive() != $$5) {
                  return DataResult.error(
                     () -> $$3 + " version declaration mismatch between " + $$4 + " (from " + $$8.minInclusive() + ") and min_format (" + this.min.get() + ")"
                  );
               }

               if ((Integer)$$8.maxInclusive() != $$6 && (Integer)$$8.maxInclusive() != $$0) {
                  return DataResult.error(
                     () -> $$3 + " version declaration mismatch between " + $$4 + " (up to " + $$8.maxInclusive() + ") and max_format (" + this.max.get() + ")"
                  );
               }

               if ($$1) {
                  if (!this.format.isPresent()) {
                     return DataResult.error(
                        () -> $$3
                           + " declares support for formats up to "
                           + $$0
                           + ", but game versions supporting formats 17 to "
                           + $$0
                           + " require a pack_format field. Add \"pack_format\": "
                           + $$5
                           + " or require a version greater or equal to "
                           + ($$0 + 1)
                           + ".0."
                     );
                  }

                  String $$9 = this.validatePackFormatForRange($$5, $$6);
                  if ($$9 != null) {
                     return DataResult.error(() -> $$9);
                  }
               }
            }

            return DataResult.success(new InclusiveRange(this.min.get(), this.max.get()));
         }
      }

      private DataResult<InclusiveRange<PackFormat>> validateOldFormat(int $$0, boolean $$1, String $$2, String $$3) {
         InclusiveRange<Integer> $$4 = this.supported.get();
         int $$5 = (Integer)$$4.minInclusive();
         int $$6 = (Integer)$$4.maxInclusive();
         if ($$6 > $$0) {
            return DataResult.error(
               () -> $$2 + " declares support for version newer than " + $$0 + ", but is missing mandatory fields min_format and max_format"
            );
         } else {
            if ($$1) {
               if (!this.format.isPresent()) {
                  return DataResult.error(
                     () -> $$2
                        + " declares support for formats up to "
                        + $$0
                        + ", but game versions supporting formats 17 to "
                        + $$0
                        + " require a pack_format field. Add \"pack_format\": "
                        + $$5
                        + " or require a version greater or equal to "
                        + ($$0 + 1)
                        + ".0."
                  );
               }

               String $$7 = this.validatePackFormatForRange($$5, $$6);
               if ($$7 != null) {
                  return DataResult.error(() -> $$7);
               }
            }

            return DataResult.success(new InclusiveRange($$5, $$6).map(PackFormat::of));
         }
      }

      
      private String validatePackFormatForRange(int $$0, int $$1) {
         int $$2 = this.format.get();
         if ($$2 < $$0 || $$2 > $$1) {
            return "Pack declared support for versions " + $$0 + " to " + $$1 + " but declared main format is " + $$2;
         } else {
            return $$2 < 15
               ? "Multi-version packs cannot support minimum version of less than 15, since this will leave versions in range unable to load pack."
               : null;
         }
      }
   }

   public interface IntermediaryFormatHolder {
      PackFormat.IntermediaryFormat format();
   }
}
