package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemoveBlockEntityTagFix extends DataFix {
   private final Set<String> blockEntityIdsToDrop;

   public RemoveBlockEntityTagFix(Schema $$0, Set<String> $$1) {
      super($$0, true);
      this.blockEntityIdsToDrop = $$1;
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> $$1 = $$0.findField("tag");
      OpticFinder<?> $$2 = $$1.type().findField("BlockEntityTag");
      Type<?> $$3 = this.getInputSchema().getType(References.ENTITY);
      OpticFinder<?> $$4 = DSL.namedChoice("minecraft:falling_block", this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:falling_block"));
      OpticFinder<?> $$5 = $$4.type().findField("TileEntityData");
      Type<?> $$6 = this.getInputSchema().getType(References.STRUCTURE);
      OpticFinder<?> $$7 = $$6.findField("blocks");
      OpticFinder<?> $$8 = DSL.typeFinder(((ListType)$$7.type()).getElement());
      OpticFinder<?> $$9 = $$8.type().findField("nbt");
      OpticFinder<String> $$10 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(
            "ItemRemoveBlockEntityTagFix", $$0, $$3x -> $$3x.updateTyped($$1, $$2xx -> this.removeBlockEntity($$2xx, $$2, $$10, "BlockEntityTag"))
         ),
         new TypeRewriteRule[]{
            this.fixTypeEverywhereTyped(
               "FallingBlockEntityRemoveBlockEntityTagFix",
               $$3,
               $$3x -> $$3x.updateTyped($$4, $$2xx -> this.removeBlockEntity($$2xx, $$5, $$10, "TileEntityData"))
            ),
            this.fixTypeEverywhereTyped(
               "StructureRemoveBlockEntityTagFix",
               $$6,
               $$4x -> $$4x.updateTyped($$7, $$3xx -> $$3xx.updateTyped($$8, $$2xxx -> this.removeBlockEntity($$2xxx, $$9, $$10, "nbt")))
            ),
            this.convertUnchecked(
               "ItemRemoveBlockEntityTagFix - update block entity type",
               this.getInputSchema().getType(References.BLOCK_ENTITY),
               this.getOutputSchema().getType(References.BLOCK_ENTITY)
            )
         }
      );
   }

   private Typed<?> removeBlockEntity(Typed<?> $$0, OpticFinder<?> $$1, OpticFinder<String> $$2, String $$3) {
      Optional<? extends Typed<?>> $$4 = $$0.getOptionalTyped($$1);
      if ($$4.isEmpty()) {
         return $$0;
      } else {
         String $$5 = $$4.get().getOptional($$2).orElse("");
         return !this.blockEntityIdsToDrop.contains($$5) ? $$0 : net.minecraft.util.Util.writeAndReadTypedOrThrow($$0, $$0.getType(), $$1x -> $$1x.remove($$3));
      }
   }
}
