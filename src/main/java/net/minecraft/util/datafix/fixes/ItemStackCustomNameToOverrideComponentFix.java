package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.OptionalDynamic;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackCustomNameToOverrideComponentFix extends DataFix {
   private static final Set<String> MAP_NAMES = Set.of(
      "filled_map.buried_treasure",
      "filled_map.explorer_jungle",
      "filled_map.explorer_swamp",
      "filled_map.mansion",
      "filled_map.monument",
      "filled_map.trial_chambers",
      "filled_map.village_desert",
      "filled_map.village_plains",
      "filled_map.village_savanna",
      "filled_map.village_snowy",
      "filled_map.village_taiga"
   );

   public ItemStackCustomNameToOverrideComponentFix(Schema $$0) {
      super($$0, false);
   }

   public final TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> $$1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> $$2 = $$0.findField("components");
      return this.fixTypeEverywhereTyped(
         "ItemStack custom_name to item_name component fix",
         $$0,
         $$2x -> {
            Optional<Pair<String, String>> $$3 = $$2x.getOptional($$1);
            Optional<String> $$4 = $$3.map(Pair::getSecond);
            if ($$4.filter($$0xx -> $$0xx.equals("minecraft:white_banner")).isPresent()) {
               return $$2x.updateTyped($$2, ItemStackCustomNameToOverrideComponentFix::fixBanner);
            } else {
               return $$4.filter($$0xx -> $$0xx.equals("minecraft:filled_map")).isPresent()
                  ? $$2x.updateTyped($$2, ItemStackCustomNameToOverrideComponentFix::fixMap)
                  : $$2x;
            }
         }
      );
   }

   private static <T> Typed<T> fixMap(Typed<T> $$0) {
      return fixCustomName($$0, MAP_NAMES::contains);
   }

   private static <T> Typed<T> fixBanner(Typed<T> $$0) {
      return fixCustomName($$0, $$0x -> $$0x.equals("block.minecraft.ominous_banner"));
   }

   private static <T> Typed<T> fixCustomName(Typed<T> $$0, Predicate<String> $$1) {
      return net.minecraft.util.Util.writeAndReadTypedOrThrow($$0, $$0.getType(), $$1x -> {
         OptionalDynamic<?> $$2 = $$1x.get("minecraft:custom_name");
         Optional<String> $$3 = $$2.asString().result().flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter($$1);
         return $$3.isPresent() ? $$1x.renameField("minecraft:custom_name", "minecraft:item_name") : $$1x;
      });
   }
}
