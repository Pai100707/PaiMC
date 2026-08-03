package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.timeline.Timeline;

public interface TimelineTags {
   net.minecraft.tags.TagKey<Timeline> UNIVERSAL = create("universal");
   net.minecraft.tags.TagKey<Timeline> IN_OVERWORLD = create("in_overworld");
   net.minecraft.tags.TagKey<Timeline> IN_NETHER = create("in_nether");
   net.minecraft.tags.TagKey<Timeline> IN_END = create("in_end");

   private static net.minecraft.tags.TagKey<Timeline> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.TIMELINE, Identifier.withDefaultNamespace($$0));
   }
}
