package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class BeehiveFieldRenameFix extends DataFix {
   public BeehiveFieldRenameFix(Schema $$0) {
      super($$0, true);
   }

   private Dynamic<?> fixBeehive(Dynamic<?> $$0) {
      return $$0.remove("Bees");
   }

   private Dynamic<?> fixBee(Dynamic<?> $$0) {
      $$0 = $$0.remove("EntityData");
      $$0 = $$0.renameField("TicksInHive", "ticks_in_hive");
      return $$0.renameField("MinOccupationTicks", "min_ticks_in_hive");
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:beehive");
      OpticFinder<?> $$1 = DSL.namedChoice("minecraft:beehive", $$0);
      ListType<?> $$2 = (ListType<?>)$$0.findFieldType("Bees");
      Type<?> $$3 = $$2.getElement();
      OpticFinder<?> $$4 = DSL.fieldFinder("Bees", $$2);
      OpticFinder<?> $$5 = DSL.typeFinder($$3);
      Type<?> $$6 = this.getInputSchema().getType(References.BLOCK_ENTITY);
      Type<?> $$7 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
      return this.fixTypeEverywhereTyped(
         "BeehiveFieldRenameFix",
         $$6,
         $$7,
         $$4x -> ExtraDataFixUtils.cast(
            $$7,
            $$4x.updateTyped(
               $$1,
               $$2xx -> $$2xx.update(DSL.remainderFinder(), this::fixBeehive)
                  .updateTyped($$4, $$1xxx -> $$1xxx.updateTyped($$5, $$0xxxx -> $$0xxxx.update(DSL.remainderFinder(), this::fixBee)))
            )
         )
      );
   }
}
