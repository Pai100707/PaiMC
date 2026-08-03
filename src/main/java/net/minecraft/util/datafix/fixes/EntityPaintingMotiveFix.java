package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityPaintingMotiveFix extends NamedEntityFix {
   private static final Map<String, String> MAP = (Map<String, String>)DataFixUtils.make(Maps.newHashMap(), $$0 -> {
      $$0.put("donkeykong", "donkey_kong");
      $$0.put("burningskull", "burning_skull");
      $$0.put("skullandroses", "skull_and_roses");
   });

   public EntityPaintingMotiveFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "EntityPaintingMotiveFix", References.ENTITY, "minecraft:painting");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      Optional<String> $$1 = $$0.get("Motive").asString().result();
      if ($$1.isPresent()) {
         String $$2 = $$1.get().toLowerCase(Locale.ROOT);
         return $$0.set("Motive", $$0.createString(NamespacedSchema.ensureNamespaced(MAP.getOrDefault($$2, $$2))));
      } else {
         return $$0;
      }
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
