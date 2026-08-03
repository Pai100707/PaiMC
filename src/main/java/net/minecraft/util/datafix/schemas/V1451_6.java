package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.fixes.References;

public class V1451_6 extends NamespacedSchema {
   public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
   protected static final HookFunction UNPACK_OBJECTIVE_ID = new HookFunction() {
      public <T> T apply(DynamicOps<T> $$0, T $$1) {
         Dynamic<T> $$2 = new Dynamic($$0, $$1);
         return (T)((Dynamic)DataFixUtils.orElse(
               $$2.get("CriteriaName")
                  .asString()
                  .result()
                  .map($$0x -> {
                     int $$1x = $$0x.indexOf(58);
                     if ($$1x < 0) {
                        return Pair.of("_special", $$0x);
                     } else {
                        try {
                           Identifier $$2x = Identifier.bySeparator($$0x.substring(0, $$1x), '.');
                           Identifier $$3 = Identifier.bySeparator($$0x.substring($$1x + 1), '.');
                           return Pair.of($$2x.toString(), $$3.toString());
                        } catch (Exception var4) {
                           return Pair.of("_special", $$0x);
                        }
                     }
                  })
                  .map(
                     $$1x -> $$2.set(
                        "CriteriaType",
                        $$2.createMap(
                           ImmutableMap.of(
                              $$2.createString("type"),
                              $$2.createString((String)$$1x.getFirst()),
                              $$2.createString("id"),
                              $$2.createString((String)$$1x.getSecond())
                           )
                        )
                     )
                  ),
               $$2
            ))
            .getValue();
      }
   };
   protected static final HookFunction REPACK_OBJECTIVE_ID = new HookFunction() {
      public <T> T apply(DynamicOps<T> $$0, T $$1) {
         Dynamic<T> $$2 = new Dynamic($$0, $$1);
         Optional<Dynamic<T>> $$3 = $$2.get("CriteriaType")
            .get()
            .result()
            .flatMap(
               $$1x -> {
                  Optional<String> $$2x = $$1x.get("type").asString().result();
                  Optional<String> $$3x = $$1x.get("id").asString().result();
                  if ($$2x.isPresent() && $$3x.isPresent()) {
                     String $$4 = $$2x.get();
                     return $$4.equals("_special")
                        ? Optional.of($$2.createString($$3x.get()))
                        : Optional.of($$1x.createString(V1451_6.packNamespacedWithDot($$4) + ":" + V1451_6.packNamespacedWithDot($$3x.get())));
                  } else {
                     return Optional.empty();
                  }
               }
            );
         return (T)((Dynamic)DataFixUtils.orElse($$3.map($$1x -> $$2.set("CriteriaName", $$1x).remove("CriteriaType")), $$2)).getValue();
      }
   };

   public V1451_6(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      Supplier<TypeTemplate> $$3 = () -> DSL.compoundList(References.ITEM_NAME.in($$0), DSL.constType(DSL.intType()));
      $$0.registerType(
         false,
         References.STATS,
         () -> DSL.optionalFields(
            "stats",
            DSL.optionalFields(
               new Pair[]{
                  Pair.of("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in($$0), DSL.constType(DSL.intType()))),
                  Pair.of("minecraft:crafted", $$3.get()),
                  Pair.of("minecraft:used", $$3.get()),
                  Pair.of("minecraft:broken", $$3.get()),
                  Pair.of("minecraft:picked_up", $$3.get()),
                  Pair.of("minecraft:dropped", $$3.get()),
                  Pair.of("minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.intType()))),
                  Pair.of("minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.intType()))),
                  Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType())))
               }
            )
         )
      );
      Map<String, Supplier<TypeTemplate>> $$4 = createCriterionTypes($$0);
      $$0.registerType(
         false,
         References.OBJECTIVE,
         () -> DSL.hook(
            DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), $$4), "DisplayName", References.TEXT_COMPONENT.in($$0)),
            UNPACK_OBJECTIVE_ID,
            REPACK_OBJECTIVE_ID
         )
      );
   }

   protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema $$0) {
      Supplier<TypeTemplate> $$1 = () -> DSL.optionalFields("id", References.ITEM_NAME.in($$0));
      Supplier<TypeTemplate> $$2 = () -> DSL.optionalFields("id", References.BLOCK_NAME.in($$0));
      Supplier<TypeTemplate> $$3 = () -> DSL.optionalFields("id", References.ENTITY_NAME.in($$0));
      Map<String, Supplier<TypeTemplate>> $$4 = Maps.newHashMap();
      $$4.put("minecraft:mined", $$2);
      $$4.put("minecraft:crafted", $$1);
      $$4.put("minecraft:used", $$1);
      $$4.put("minecraft:broken", $$1);
      $$4.put("minecraft:picked_up", $$1);
      $$4.put("minecraft:dropped", $$1);
      $$4.put("minecraft:killed", $$3);
      $$4.put("minecraft:killed_by", $$3);
      $$4.put("minecraft:custom", () -> DSL.optionalFields("id", DSL.constType(namespacedString())));
      $$4.put("_special", () -> DSL.optionalFields("id", DSL.constType(DSL.string())));
      return $$4;
   }

   public static String packNamespacedWithDot(String $$0) {
      Identifier $$1 = Identifier.tryParse($$0);
      return $$1 != null ? $$1.getNamespace() + "." + $$1.getPath() : $$0;
   }
}
