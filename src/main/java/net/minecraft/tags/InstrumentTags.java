package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Instrument;

public interface InstrumentTags {
   net.minecraft.tags.TagKey<Instrument> REGULAR_GOAT_HORNS = create("regular_goat_horns");
   net.minecraft.tags.TagKey<Instrument> SCREAMING_GOAT_HORNS = create("screaming_goat_horns");
   net.minecraft.tags.TagKey<Instrument> GOAT_HORNS = create("goat_horns");

   private static net.minecraft.tags.TagKey<Instrument> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.INSTRUMENT, Identifier.withDefaultNamespace($$0));
   }
}
