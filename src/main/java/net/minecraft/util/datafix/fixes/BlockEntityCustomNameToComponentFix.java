package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockEntityCustomNameToComponentFix extends DataFix {
   private static final Set<String> NAMEABLE_BLOCK_ENTITIES = Set.of(
      "minecraft:beacon",
      "minecraft:banner",
      "minecraft:brewing_stand",
      "minecraft:chest",
      "minecraft:trapped_chest",
      "minecraft:dispenser",
      "minecraft:dropper",
      "minecraft:enchanting_table",
      "minecraft:furnace",
      "minecraft:hopper",
      "minecraft:shulker_box"
   );

   public BlockEntityCustomNameToComponentFix(Schema $$0) {
      super($$0, true);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<String> $$0 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
      Type<?> $$1 = this.getInputSchema().getType(References.BLOCK_ENTITY);
      Type<?> $$2 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
      Type<?> $$3 = ExtraDataFixUtils.patchSubType($$1, $$1, $$2);
      return this.fixTypeEverywhereTyped(
         "BlockEntityCustomNameToComponentFix",
         $$1,
         $$2,
         $$3x -> {
            Optional<String> $$4 = $$3x.getOptional($$0);
            return $$4.isPresent() && !NAMEABLE_BLOCK_ENTITIES.contains($$4.get())
               ? ExtraDataFixUtils.cast($$2, $$3x)
               : net.minecraft.util.Util.writeAndReadTypedOrThrow(ExtraDataFixUtils.cast($$3, $$3x), $$2, BlockEntityCustomNameToComponentFix::fixTagCustomName);
         }
      );
   }

   public static <T> Dynamic<T> fixTagCustomName(Dynamic<T> $$0) {
      String $$1 = $$0.get("CustomName").asString("");
      return $$1.isEmpty() ? $$0.remove("CustomName") : $$0.set("CustomName", LegacyComponentDataFixUtils.createPlainTextComponent($$0.getOps(), $$1));
   }
}
