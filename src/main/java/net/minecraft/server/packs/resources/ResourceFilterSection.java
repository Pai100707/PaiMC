package net.minecraft.server.packs.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.IdentifierPattern;

public class ResourceFilterSection {
   private static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(Codec.list(IdentifierPattern.CODEC).fieldOf("block").forGetter($$0x -> $$0x.blockList)).apply($$0, ResourceFilterSection::new)
   );
   public static final MetadataSectionType<ResourceFilterSection> TYPE = new MetadataSectionType<>("filter", CODEC);
   private final List<IdentifierPattern> blockList;

   public ResourceFilterSection(List<IdentifierPattern> $$0) {
      this.blockList = List.copyOf($$0);
   }

   public boolean isNamespaceFiltered(String $$0) {
      return this.blockList.stream().anyMatch($$1 -> $$1.namespacePredicate().test($$0));
   }

   public boolean isPathFiltered(String $$0) {
      return this.blockList.stream().anyMatch($$1 -> $$1.pathPredicate().test($$0));
   }
}
