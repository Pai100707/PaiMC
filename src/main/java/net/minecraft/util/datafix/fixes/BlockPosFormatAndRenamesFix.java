package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class BlockPosFormatAndRenamesFix extends DataFix {
   private static final List<String> PATROLLING_MOBS = List.of(
      "minecraft:witch", "minecraft:ravager", "minecraft:pillager", "minecraft:illusioner", "minecraft:evoker", "minecraft:vindicator"
   );

   public BlockPosFormatAndRenamesFix(Schema $$0) {
      super($$0, true);
   }

   private Typed<?> fixFields(Typed<?> $$0, Map<String, String> $$1) {
      return $$0.update(DSL.remainderFinder(), $$1x -> {
         for (Entry<String, String> $$2 : $$1.entrySet()) {
            $$1x = $$1x.renameAndFixField($$2.getKey(), $$2.getValue(), ExtraDataFixUtils::fixBlockPos);
         }

         return $$1x;
      });
   }

   private <T> Dynamic<T> fixMapSavedData(Dynamic<T> $$0) {
      return $$0.update("frames", $$0x -> $$0x.createList($$0x.asStream().map($$0xx -> {
         $$0xx = $$0xx.renameAndFixField("Pos", "pos", ExtraDataFixUtils::fixBlockPos);
         $$0xx = $$0xx.renameField("Rotation", "rotation");
         return $$0xx.renameField("EntityId", "entity_id");
      }))).update("banners", $$0x -> $$0x.createList($$0x.asStream().map($$0xx -> {
         $$0xx = $$0xx.renameField("Pos", "pos");
         $$0xx = $$0xx.renameField("Color", "color");
         return $$0xx.renameField("Name", "name");
      })));
   }

   public TypeRewriteRule makeRule() {
      List<TypeRewriteRule> $$0 = new ArrayList<>();
      this.addEntityRules($$0);
      this.addBlockEntityRules($$0);
      $$0.add(
         this.writeFixAndRead(
            "BlockPos format for map frames",
            this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA),
            this.getOutputSchema().getType(References.SAVED_DATA_MAP_DATA),
            $$0x -> $$0x.update("data", this::fixMapSavedData)
         )
      );
      Type<?> $$1 = this.getInputSchema().getType(References.ITEM_STACK);
      $$0.add(
         this.fixTypeEverywhereTyped(
            "BlockPos format for compass target",
            $$1,
            ItemStackTagFix.createFixer(
               $$1,
               "minecraft:compass"::equals,
               $$0x -> $$0x.update(DSL.remainderFinder(), $$0xx -> $$0xx.update("LodestonePos", ExtraDataFixUtils::fixBlockPos))
            )
         )
      );
      return TypeRewriteRule.seq($$0);
   }

   private void addEntityRules(List<TypeRewriteRule> $$0) {
      $$0.add(this.createEntityFixer(References.ENTITY, "minecraft:bee", Map.of("HivePos", "hive_pos", "FlowerPos", "flower_pos")));
      $$0.add(this.createEntityFixer(References.ENTITY, "minecraft:end_crystal", Map.of("BeamTarget", "beam_target")));
      $$0.add(this.createEntityFixer(References.ENTITY, "minecraft:wandering_trader", Map.of("WanderTarget", "wander_target")));

      for (String $$1 : PATROLLING_MOBS) {
         $$0.add(this.createEntityFixer(References.ENTITY, $$1, Map.of("PatrolTarget", "patrol_target")));
      }

      $$0.add(
         this.fixTypeEverywhereTyped(
            "BlockPos format in Leash for mobs",
            this.getInputSchema().getType(References.ENTITY),
            $$0x -> $$0x.update(DSL.remainderFinder(), $$0xx -> $$0xx.renameAndFixField("Leash", "leash", ExtraDataFixUtils::fixBlockPos))
         )
      );
   }

   private void addBlockEntityRules(List<TypeRewriteRule> $$0) {
      $$0.add(this.createEntityFixer(References.BLOCK_ENTITY, "minecraft:beehive", Map.of("FlowerPos", "flower_pos")));
      $$0.add(this.createEntityFixer(References.BLOCK_ENTITY, "minecraft:end_gateway", Map.of("ExitPortal", "exit_portal")));
   }

   private TypeRewriteRule createEntityFixer(TypeReference $$0, String $$1, Map<String, String> $$2) {
      String $$3 = "BlockPos format in " + $$2.keySet() + " for " + $$1 + " (" + $$0.typeName() + ")";
      OpticFinder<?> $$4 = DSL.namedChoice($$1, this.getInputSchema().getChoiceType($$0, $$1));
      return this.fixTypeEverywhereTyped($$3, this.getInputSchema().getType($$0), $$2x -> $$2x.updateTyped($$4, $$1xx -> this.fixFields($$1xx, $$2)));
   }
}
