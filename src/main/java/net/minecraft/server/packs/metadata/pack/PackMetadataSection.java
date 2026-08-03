package net.minecraft.server.packs.metadata.pack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record PackMetadataSection(Component description, InclusiveRange<PackFormat> supportedFormats) {
   private static final Codec<PackMetadataSection> FALLBACK_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(ComponentSerialization.CODEC.fieldOf("description").forGetter(PackMetadataSection::description))
         .apply($$0, $$0x -> new PackMetadataSection($$0x, new InclusiveRange(PackFormat.of(Integer.MAX_VALUE))))
   );
   public static final MetadataSectionType<PackMetadataSection> CLIENT_TYPE = new MetadataSectionType<>("pack", codecForPackType(PackType.CLIENT_RESOURCES));
   public static final MetadataSectionType<PackMetadataSection> SERVER_TYPE = new MetadataSectionType<>("pack", codecForPackType(PackType.SERVER_DATA));
   public static final MetadataSectionType<PackMetadataSection> FALLBACK_TYPE = new MetadataSectionType<>("pack", FALLBACK_CODEC);

   private static Codec<PackMetadataSection> codecForPackType(PackType $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group(
               ComponentSerialization.CODEC.fieldOf("description").forGetter(PackMetadataSection::description),
               PackFormat.packCodec($$0).forGetter(PackMetadataSection::supportedFormats)
            )
            .apply($$1, PackMetadataSection::new)
      );
   }

   public static MetadataSectionType<PackMetadataSection> forPackType(PackType $$0) {
      return switch ($$0) {
         case CLIENT_RESOURCES -> CLIENT_TYPE;
         case SERVER_DATA -> SERVER_TYPE;
      };
   }
}
