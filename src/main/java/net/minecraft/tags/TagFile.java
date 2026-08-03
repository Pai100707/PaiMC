package net.minecraft.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record TagFile(List<net.minecraft.tags.TagEntry> entries, boolean replace) {
   public static final Codec<net.minecraft.tags.TagFile> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.tags.TagEntry.CODEC.listOf().fieldOf("values").forGetter(net.minecraft.tags.TagFile::entries),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(net.minecraft.tags.TagFile::replace)
         )
         .apply($$0, net.minecraft.tags.TagFile::new)
   );
}
