package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2551 extends NamespacedSchema {
   public V2551(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         false,
         References.WORLD_GEN_SETTINGS,
         () -> DSL.fields(
            "dimensions",
            DSL.compoundList(
               DSL.constType(namespacedString()),
               DSL.fields(
                  "generator",
                  DSL.taggedChoiceLazy(
                     "type",
                     DSL.string(),
                     ImmutableMap.of(
                        "minecraft:debug",
                        DSL::remainder,
                        "minecraft:flat",
                        (Supplier<TypeTemplate>)() -> DSL.optionalFields(
                           "settings",
                           DSL.optionalFields("biome", References.BIOME.in($$0), "layers", DSL.list(DSL.optionalFields("block", References.BLOCK_NAME.in($$0))))
                        ),
                        "minecraft:noise",
                        (Supplier<TypeTemplate>)() -> DSL.optionalFields(
                           "biome_source",
                           DSL.taggedChoiceLazy(
                              "type",
                              DSL.string(),
                              ImmutableMap.of(
                                 "minecraft:fixed",
                                 (Supplier<TypeTemplate>)() -> DSL.fields("biome", References.BIOME.in($$0)),
                                 "minecraft:multi_noise",
                                 (Supplier<TypeTemplate>)() -> DSL.list(DSL.fields("biome", References.BIOME.in($$0))),
                                 "minecraft:checkerboard",
                                 (Supplier<TypeTemplate>)() -> DSL.fields("biomes", DSL.list(References.BIOME.in($$0))),
                                 "minecraft:vanilla_layered",
                                 DSL::remainder,
                                 "minecraft:the_end",
                                 DSL::remainder
                              )
                           ),
                           "settings",
                           DSL.or(
                              DSL.constType(DSL.string()),
                              DSL.optionalFields("default_block", References.BLOCK_NAME.in($$0), "default_fluid", References.BLOCK_NAME.in($$0))
                           )
                        )
                     )
                  )
               )
            )
         )
      );
   }
}
