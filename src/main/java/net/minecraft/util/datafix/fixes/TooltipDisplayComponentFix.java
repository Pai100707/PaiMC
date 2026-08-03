package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public class TooltipDisplayComponentFix extends DataFix {
   private static final List<String> CONVERTED_ADDITIONAL_TOOLTIP_TYPES = List.of(
      "minecraft:banner_patterns",
      "minecraft:bees",
      "minecraft:block_entity_data",
      "minecraft:block_state",
      "minecraft:bundle_contents",
      "minecraft:charged_projectiles",
      "minecraft:container",
      "minecraft:container_loot",
      "minecraft:firework_explosion",
      "minecraft:fireworks",
      "minecraft:instrument",
      "minecraft:map_id",
      "minecraft:painting/variant",
      "minecraft:pot_decorations",
      "minecraft:potion_contents",
      "minecraft:tropical_fish/pattern",
      "minecraft:written_book_content"
   );

   public TooltipDisplayComponentFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.DATA_COMPONENTS);
      Type<?> $$1 = this.getOutputSchema().getType(References.DATA_COMPONENTS);
      OpticFinder<?> $$2 = $$0.findField("minecraft:can_place_on");
      OpticFinder<?> $$3 = $$0.findField("minecraft:can_break");
      Type<?> $$4 = $$1.findFieldType("minecraft:can_place_on");
      Type<?> $$5 = $$1.findFieldType("minecraft:can_break");
      return this.fixTypeEverywhereTyped("TooltipDisplayComponentFix", $$0, $$1, $$4x -> fix($$4x, $$2, $$3, $$4, $$5));
   }

   private static Typed<?> fix(Typed<?> $$0, OpticFinder<?> $$1, OpticFinder<?> $$2, Type<?> $$3, Type<?> $$4) {
      Set<String> $$5 = new HashSet<>();
      $$0 = fixAdventureModePredicate($$0, $$1, $$3, "minecraft:can_place_on", $$5);
      $$0 = fixAdventureModePredicate($$0, $$2, $$4, "minecraft:can_break", $$5);
      return $$0.update(
         DSL.remainderFinder(),
         $$1x -> {
            $$1x = fixSimpleComponent($$1x, "minecraft:trim", $$5);
            $$1x = fixSimpleComponent($$1x, "minecraft:unbreakable", $$5);
            $$1x = fixComponentAndUnwrap($$1x, "minecraft:dyed_color", "rgb", $$5);
            $$1x = fixComponentAndUnwrap($$1x, "minecraft:attribute_modifiers", "modifiers", $$5);
            $$1x = fixComponentAndUnwrap($$1x, "minecraft:enchantments", "levels", $$5);
            $$1x = fixComponentAndUnwrap($$1x, "minecraft:stored_enchantments", "levels", $$5);
            $$1x = fixComponentAndUnwrap($$1x, "minecraft:jukebox_playable", "song", $$5);
            boolean $$2x = $$1x.get("minecraft:hide_tooltip").result().isPresent();
            $$1x = $$1x.remove("minecraft:hide_tooltip");
            boolean $$3x = $$1x.get("minecraft:hide_additional_tooltip").result().isPresent();
            $$1x = $$1x.remove("minecraft:hide_additional_tooltip");
            if ($$3x) {
               for (String $$4x : CONVERTED_ADDITIONAL_TOOLTIP_TYPES) {
                  if ($$1x.get($$4x).result().isPresent()) {
                     $$5.add($$4x);
                  }
               }
            }

            return $$5.isEmpty() && !$$2x
               ? $$1x
               : $$1x.set(
                  "minecraft:tooltip_display",
                  $$1x.createMap(
                     Map.of(
                        $$1x.createString("hide_tooltip"),
                        $$1x.createBoolean($$2x),
                        $$1x.createString("hidden_components"),
                        $$1x.createList($$5.stream().map($$1x::createString))
                     )
                  )
               );
         }
      );
   }

   private static Dynamic<?> fixSimpleComponent(Dynamic<?> $$0, String $$1, Set<String> $$2) {
      return fixRemainderComponent($$0, $$1, $$2, UnaryOperator.identity());
   }

   private static Dynamic<?> fixComponentAndUnwrap(Dynamic<?> $$0, String $$1, String $$2, Set<String> $$3) {
      return fixRemainderComponent($$0, $$1, $$3, $$1x -> (Dynamic)DataFixUtils.orElse($$1x.get($$2).result(), $$1x));
   }

   private static Dynamic<?> fixRemainderComponent(Dynamic<?> $$0, String $$1, Set<String> $$2, UnaryOperator<Dynamic<?>> $$3) {
      return $$0.update($$1, $$3x -> {
         boolean $$4 = $$3x.get("show_in_tooltip").asBoolean(true);
         if (!$$4) {
            $$2.add($$1);
         }

         return $$3.apply($$3x.remove("show_in_tooltip"));
      });
   }

   private static Typed<?> fixAdventureModePredicate(Typed<?> $$0, OpticFinder<?> $$1, Type<?> $$2, String $$3, Set<String> $$4) {
      return $$0.updateTyped($$1, $$2, $$3x -> net.minecraft.util.Util.writeAndReadTypedOrThrow($$3x, $$2, $$2xx -> {
         OptionalDynamic<?> $$3xx = $$2xx.get("predicates");
         if ($$3xx.result().isEmpty()) {
            return $$2xx;
         } else {
            boolean $$4x = $$2xx.get("show_in_tooltip").asBoolean(true);
            if (!$$4x) {
               $$4.add($$3);
            }

            return (Dynamic)$$3xx.result().get();
         }
      }));
   }
}
