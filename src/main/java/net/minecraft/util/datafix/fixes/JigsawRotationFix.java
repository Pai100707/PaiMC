package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;

public class JigsawRotationFix extends AbstractBlockPropertyFix {
   private static final Map<String, String> RENAMES = ImmutableMap.builder()
      .put("down", "down_south")
      .put("up", "up_north")
      .put("north", "north_up")
      .put("south", "south_up")
      .put("west", "west_up")
      .put("east", "east_up")
      .build();

   public JigsawRotationFix(Schema $$0) {
      super($$0, "jigsaw_rotation_fix");
   }

   @Override
   protected boolean shouldFix(String $$0) {
      return $$0.equals("minecraft:jigsaw");
   }

   @Override
   protected <T> Dynamic<T> fixProperties(String $$0, Dynamic<T> $$1) {
      String $$2 = $$1.get("facing").asString("north");
      return $$1.remove("facing").set("orientation", $$1.createString(RENAMES.getOrDefault($$2, $$2)));
   }
}
