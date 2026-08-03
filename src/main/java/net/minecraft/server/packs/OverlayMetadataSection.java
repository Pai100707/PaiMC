package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayMetadataSection.OverlayEntry> overlays) {
   private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
   public static final MetadataSectionType<OverlayMetadataSection> CLIENT_TYPE = new MetadataSectionType<>(
      "overlays", codecForPackType(PackType.CLIENT_RESOURCES)
   );
   public static final MetadataSectionType<OverlayMetadataSection> SERVER_TYPE = new MetadataSectionType<>("overlays", codecForPackType(PackType.SERVER_DATA));

   private static DataResult<String> validateOverlayDir(String $$0) {
      return !DIR_VALIDATOR.matcher($$0).matches() ? DataResult.error(() -> $$0 + " is not accepted directory name") : DataResult.success($$0);
   }

   @VisibleForTesting
   public static Codec<OverlayMetadataSection> codecForPackType(PackType $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group(OverlayMetadataSection.OverlayEntry.listCodecForPackType($$0).fieldOf("entries").forGetter(OverlayMetadataSection::overlays))
            .apply($$1, OverlayMetadataSection::new)
      );
   }

   public static MetadataSectionType<OverlayMetadataSection> forPackType(PackType $$0) {
      return switch ($$0) {
         case CLIENT_RESOURCES -> CLIENT_TYPE;
         case SERVER_DATA -> SERVER_TYPE;
      };
   }

   public List<String> overlaysForVersion(PackFormat $$0) {
      return this.overlays.stream().filter($$1 -> $$1.isApplicable($$0)).map(OverlayMetadataSection.OverlayEntry::overlay).toList();
   }

   public record OverlayEntry(InclusiveRange<PackFormat> format, String overlay) {
      static Codec<List<OverlayMetadataSection.OverlayEntry>> listCodecForPackType(PackType $$0) {
         int $$1 = PackFormat.lastPreMinorVersion($$0);
         return OverlayMetadataSection.OverlayEntry.IntermediateEntry.CODEC
            .listOf()
            .flatXmap(
               $$1x -> PackFormat.validateHolderList($$1x, $$1, ($$0xx, $$1xx) -> new OverlayMetadataSection.OverlayEntry($$1xx, $$0xx.overlay())),
               $$1x -> DataResult.success(
                  $$1x.stream()
                     .map(
                        $$1xx -> new OverlayMetadataSection.OverlayEntry.IntermediateEntry(
                           PackFormat.IntermediaryFormat.fromRange($$1xx.format(), $$1), $$1xx.overlay()
                        )
                     )
                     .toList()
               )
            );
      }

      public boolean isApplicable(PackFormat $$0) {
         return this.format.isValueInRange($$0);
      }

      record IntermediateEntry(PackFormat.IntermediaryFormat format, String overlay) implements PackFormat.IntermediaryFormatHolder {
         static final Codec<OverlayMetadataSection.OverlayEntry.IntermediateEntry> CODEC = RecordCodecBuilder.create(
            $$0 -> $$0.group(
                  PackFormat.IntermediaryFormat.OVERLAY_CODEC.forGetter(OverlayMetadataSection.OverlayEntry.IntermediateEntry::format),
                  Codec.STRING
                     .validate(OverlayMetadataSection::validateOverlayDir)
                     .fieldOf("directory")
                     .forGetter(OverlayMetadataSection.OverlayEntry.IntermediateEntry::overlay)
               )
               .apply($$0, OverlayMetadataSection.OverlayEntry.IntermediateEntry::new)
         );

         @Override
         public String toString() {
            return this.overlay;
         }
      }
   }
}
