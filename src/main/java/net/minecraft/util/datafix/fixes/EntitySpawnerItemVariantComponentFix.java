package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntitySpawnerItemVariantComponentFix extends DataFix {
   public EntitySpawnerItemVariantComponentFix(Schema $$0) {
      super($$0, false);
   }

   public final TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> $$1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> $$2 = $$0.findField("components");
      return this.fixTypeEverywhereTyped(
         "ItemStack bucket_entity_data variants to separate components",
         $$0,
         $$2x -> {
            String $$3 = $$2x.getOptional($$1).<String>map(Pair::getSecond).orElse("");

            return switch ($$3) {
               case "minecraft:salmon_bucket" -> $$2x.updateTyped($$2, EntitySpawnerItemVariantComponentFix::fixSalmonBucket);
               case "minecraft:axolotl_bucket" -> $$2x.updateTyped($$2, EntitySpawnerItemVariantComponentFix::fixAxolotlBucket);
               case "minecraft:tropical_fish_bucket" -> $$2x.updateTyped($$2, EntitySpawnerItemVariantComponentFix::fixTropicalFishBucket);
               case "minecraft:painting" -> $$2x.updateTyped(
                  $$2, $$0xx -> net.minecraft.util.Util.writeAndReadTypedOrThrow($$0xx, $$0xx.getType(), EntitySpawnerItemVariantComponentFix::fixPainting)
               );
               default -> $$2x;
            };
         }
      );
   }

   private static String getBaseColor(int $$0) {
      return ExtraDataFixUtils.dyeColorIdToName($$0 >> 16 & 0xFF);
   }

   private static String getPatternColor(int $$0) {
      return ExtraDataFixUtils.dyeColorIdToName($$0 >> 24 & 0xFF);
   }

   private static String getPattern(int $$0) {
      return switch ($$0 & 65535) {
         case 1 -> "flopper";
         case 256 -> "sunstreak";
         case 257 -> "stripey";
         case 512 -> "snooper";
         case 513 -> "glitter";
         case 768 -> "dasher";
         case 769 -> "blockfish";
         case 1024 -> "brinely";
         case 1025 -> "betty";
         case 1280 -> "spotty";
         case 1281 -> "clayfish";
         default -> "kob";
      };
   }

   private static <T> Dynamic<T> fixTropicalFishBucket(Dynamic<T> $$0, Dynamic<T> $$1) {
      Optional<Number> $$2 = $$1.get("BucketVariantTag").asNumber().result();
      if ($$2.isEmpty()) {
         return $$0;
      } else {
         int $$3 = $$2.get().intValue();
         String $$4 = getPattern($$3);
         String $$5 = getBaseColor($$3);
         String $$6 = getPatternColor($$3);
         return $$0.update("minecraft:bucket_entity_data", $$0x -> $$0x.remove("BucketVariantTag"))
            .set("minecraft:tropical_fish/pattern", $$0.createString($$4))
            .set("minecraft:tropical_fish/base_color", $$0.createString($$5))
            .set("minecraft:tropical_fish/pattern_color", $$0.createString($$6));
      }
   }

   private static <T> Dynamic<T> fixAxolotlBucket(Dynamic<T> $$0, Dynamic<T> $$1) {
      Optional<Number> $$2 = $$1.get("Variant").asNumber().result();
      if ($$2.isEmpty()) {
         return $$0;
      } else {
         String $$3 = switch ($$2.get().intValue()) {
            case 1 -> "wild";
            case 2 -> "gold";
            case 3 -> "cyan";
            case 4 -> "blue";
            default -> "lucy";
         };
         return $$0.update("minecraft:bucket_entity_data", $$0x -> $$0x.remove("Variant")).set("minecraft:axolotl/variant", $$0.createString($$3));
      }
   }

   private static <T> Dynamic<T> fixSalmonBucket(Dynamic<T> $$0, Dynamic<T> $$1) {
      Optional<Dynamic<T>> $$2 = $$1.get("type").result();
      return $$2.isEmpty() ? $$0 : $$0.update("minecraft:bucket_entity_data", $$0x -> $$0x.remove("type")).set("minecraft:salmon/size", $$2.get());
   }

   private static <T> Dynamic<T> fixPainting(Dynamic<T> $$0) {
      Optional<Dynamic<T>> $$1 = $$0.get("minecraft:entity_data").result();
      if ($$1.isEmpty()) {
         return $$0;
      } else if ($$1.get().get("id").asString().result().filter($$0x -> $$0x.equals("minecraft:painting")).isEmpty()) {
         return $$0;
      } else {
         Optional<Dynamic<T>> $$2 = $$1.get().get("variant").result();
         Dynamic<T> $$3 = $$1.get().remove("variant");
         if ($$3.remove("id").equals($$3.emptyMap())) {
            $$0 = $$0.remove("minecraft:entity_data");
         } else {
            $$0 = $$0.set("minecraft:entity_data", $$3);
         }

         if ($$2.isPresent()) {
            $$0 = $$0.set("minecraft:painting/variant", $$2.get());
         }

         return $$0;
      }
   }

   @FunctionalInterface
   interface Fixer extends Function<Typed<?>, Typed<?>> {
      default Typed<?> apply(Typed<?> $$0) {
         return $$0.update(DSL.remainderFinder(), this::fixRemainder);
      }

      default <T> Dynamic<T> fixRemainder(Dynamic<T> $$0) {
         return $$0.get("minecraft:bucket_entity_data").result().map($$1 -> this.fixRemainder($$0, (Dynamic<T>)$$1)).orElse($$0);
      }

      <T> Dynamic<T> fixRemainder(Dynamic<T> var1, Dynamic<T> var2);
   }
}
