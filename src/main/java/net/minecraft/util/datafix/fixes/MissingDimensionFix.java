package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MissingDimensionFix extends DataFix {
   public MissingDimensionFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected static <A> Type<Pair<A, Dynamic<?>>> fields(String $$0, Type<A> $$1) {
      return DSL.and(DSL.field($$0, $$1), DSL.remainderType());
   }

   protected static <A> Type<Pair<Either<A, com.mojang.datafixers.util.Unit>, Dynamic<?>>> optionalFields(String $$0, Type<A> $$1) {
      return DSL.and(DSL.optional(DSL.field($$0, $$1)), DSL.remainderType());
   }

   protected static <A1, A2> Type<Pair<Either<A1, com.mojang.datafixers.util.Unit>, Pair<Either<A2, com.mojang.datafixers.util.Unit>, Dynamic<?>>>> optionalFields(
      String $$0, Type<A1> $$1, String $$2, Type<A2> $$3
   ) {
      return DSL.and(DSL.optional(DSL.field($$0, $$1)), DSL.optional(DSL.field($$2, $$3)), DSL.remainderType());
   }

   protected TypeRewriteRule makeRule() {
      Schema $$0 = this.getInputSchema();
      Type<?> $$1 = DSL.taggedChoiceType(
         "type",
         DSL.string(),
         ImmutableMap.of(
            "minecraft:debug",
            DSL.remainderType(),
            "minecraft:flat",
            flatType($$0),
            "minecraft:noise",
            optionalFields(
               "biome_source",
               DSL.taggedChoiceType(
                  "type",
                  DSL.string(),
                  ImmutableMap.of(
                     "minecraft:fixed",
                     fields("biome", $$0.getType(References.BIOME)),
                     "minecraft:multi_noise",
                     DSL.list(fields("biome", $$0.getType(References.BIOME))),
                     "minecraft:checkerboard",
                     fields("biomes", DSL.list($$0.getType(References.BIOME))),
                     "minecraft:vanilla_layered",
                     DSL.remainderType(),
                     "minecraft:the_end",
                     DSL.remainderType()
                  )
               ),
               "settings",
               DSL.or(DSL.string(), optionalFields("default_block", $$0.getType(References.BLOCK_NAME), "default_fluid", $$0.getType(References.BLOCK_NAME)))
            )
         )
      );
      CompoundListType<String, ?> $$2 = DSL.compoundList(NamespacedSchema.namespacedString(), fields("generator", $$1));
      Type<?> $$3 = DSL.and($$2, DSL.remainderType());
      Type<?> $$4 = $$0.getType(References.WORLD_GEN_SETTINGS);
      FieldFinder<?> $$5 = new FieldFinder("dimensions", $$3);
      if (!$$4.findFieldType("dimensions").equals($$3)) {
         throw new IllegalStateException();
      } else {
         OpticFinder<? extends List<? extends Pair<String, ?>>> $$6 = $$2.finder();
         return this.fixTypeEverywhereTyped("MissingDimensionFix", $$4, $$3x -> $$3x.updateTyped($$5, $$3xx -> $$3xx.updateTyped($$6, $$2xxx -> {
            if (!($$2xxx.getValue() instanceof List)) {
               throw new IllegalStateException("List exptected");
            } else if (((List)$$2xxx.getValue()).isEmpty()) {
               Dynamic<?> $$3xxx = (Dynamic<?>)$$3x.get(DSL.remainderFinder());
               Dynamic<?> $$4x = this.recreateSettings($$3xxx);
               return (Typed)DataFixUtils.orElse($$2.readTyped($$4x).result().map(Pair::getFirst), $$2xxx);
            } else {
               return $$2xxx;
            }
         })));
      }
   }

   protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, com.mojang.datafixers.util.Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, com.mojang.datafixers.util.Unit>, Dynamic<?>>>, com.mojang.datafixers.util.Unit>, Dynamic<?>>>, com.mojang.datafixers.util.Unit>, Dynamic<?>>> flatType(
      Schema $$0
   ) {
      return optionalFields(
         "settings", optionalFields("biome", $$0.getType(References.BIOME), "layers", DSL.list(optionalFields("block", $$0.getType(References.BLOCK_NAME))))
      );
   }

   private <T> Dynamic<T> recreateSettings(Dynamic<T> $$0) {
      long $$1 = $$0.get("seed").asLong(0L);
      return new Dynamic($$0.getOps(), WorldGenSettingsFix.vanillaLevels($$0, $$1, WorldGenSettingsFix.defaultOverworld($$0, $$1), false));
   }
}
