package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Set;

public class WallPropertyFix extends DataFix {
   private static final Set<String> WALL_BLOCKS = ImmutableSet.of(
      "minecraft:andesite_wall",
      "minecraft:brick_wall",
      "minecraft:cobblestone_wall",
      "minecraft:diorite_wall",
      "minecraft:end_stone_brick_wall",
      "minecraft:granite_wall",
      new String[]{
         "minecraft:mossy_cobblestone_wall",
         "minecraft:mossy_stone_brick_wall",
         "minecraft:nether_brick_wall",
         "minecraft:prismarine_wall",
         "minecraft:red_nether_brick_wall",
         "minecraft:red_sandstone_wall",
         "minecraft:sandstone_wall",
         "minecraft:stone_brick_wall"
      }
   );

   public WallPropertyFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "WallPropertyFix",
         this.getInputSchema().getType(References.BLOCK_STATE),
         $$0 -> $$0.update(DSL.remainderFinder(), WallPropertyFix::upgradeBlockStateTag)
      );
   }

   private static String mapProperty(String $$0) {
      return "true".equals($$0) ? "low" : "none";
   }

   private static <T> Dynamic<T> fixWallProperty(Dynamic<T> $$0, String $$1) {
      return $$0.update($$1, $$0x -> (Dynamic)DataFixUtils.orElse($$0x.asString().result().map(WallPropertyFix::mapProperty).map($$0x::createString), $$0x));
   }

   private static <T> Dynamic<T> upgradeBlockStateTag(Dynamic<T> $$0) {
      boolean $$1 = $$0.get("Name").asString().result().filter(WALL_BLOCKS::contains).isPresent();
      return !$$1 ? $$0 : $$0.update("Properties", $$0x -> {
         Dynamic<?> $$1x = fixWallProperty($$0x, "east");
         $$1x = fixWallProperty((Dynamic<T>)$$1x, "west");
         $$1x = fixWallProperty((Dynamic<T>)$$1x, "north");
         return fixWallProperty((Dynamic<T>)$$1x, "south");
      });
   }
}
