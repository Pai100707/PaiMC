package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class FossilFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<FossilFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Identifier.CODEC.listOf().fieldOf("fossil_structures").forGetter($$0x -> $$0x.fossilStructures),
            Identifier.CODEC.listOf().fieldOf("overlay_structures").forGetter($$0x -> $$0x.overlayStructures),
            StructureProcessorType.LIST_CODEC.fieldOf("fossil_processors").forGetter($$0x -> $$0x.fossilProcessors),
            StructureProcessorType.LIST_CODEC.fieldOf("overlay_processors").forGetter($$0x -> $$0x.overlayProcessors),
            Codec.intRange(0, 7).fieldOf("max_empty_corners_allowed").forGetter($$0x -> $$0x.maxEmptyCornersAllowed)
         )
         .apply($$0, FossilFeatureConfiguration::new)
   );
   public final List<Identifier> fossilStructures;
   public final List<Identifier> overlayStructures;
   public final Holder<StructureProcessorList> fossilProcessors;
   public final Holder<StructureProcessorList> overlayProcessors;
   public final int maxEmptyCornersAllowed;

   public FossilFeatureConfiguration(
      List<Identifier> $$0, List<Identifier> $$1, Holder<StructureProcessorList> $$2, Holder<StructureProcessorList> $$3, int $$4
   ) {
      if ($$0.isEmpty()) {
         throw new IllegalArgumentException("Fossil structure lists need at least one entry");
      } else if ($$0.size() != $$1.size()) {
         throw new IllegalArgumentException("Fossil structure lists must be equal lengths");
      } else {
         this.fossilStructures = $$0;
         this.overlayStructures = $$1;
         this.fossilProcessors = $$2;
         this.overlayProcessors = $$3;
         this.maxEmptyCornersAllowed = $$4;
      }
   }
}
