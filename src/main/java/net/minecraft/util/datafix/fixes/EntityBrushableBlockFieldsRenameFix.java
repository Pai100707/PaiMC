package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityBrushableBlockFieldsRenameFix extends NamedEntityFix {
   public EntityBrushableBlockFieldsRenameFix(Schema $$0) {
      super($$0, false, "EntityBrushableBlockFieldsRenameFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.renameField("loot_table", "LootTable").renameField("loot_table_seed", "LootTableSeed");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
