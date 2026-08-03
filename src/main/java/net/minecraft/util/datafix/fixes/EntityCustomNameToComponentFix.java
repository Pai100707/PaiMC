package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityCustomNameToComponentFix extends DataFix {
   public EntityCustomNameToComponentFix(Schema $$0) {
      super($$0, true);
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ENTITY);
      Type<?> $$1 = this.getOutputSchema().getType(References.ENTITY);
      OpticFinder<String> $$2 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
      OpticFinder<String> $$3 = $$0.findField("CustomName");
      Type<?> $$4 = $$1.findFieldType("CustomName");
      return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", $$0, $$1, $$4x -> fixEntity($$4x, $$1, $$2, $$3, $$4));
   }

   private static <T> Typed<?> fixEntity(Typed<?> $$0, Type<?> $$1, OpticFinder<String> $$2, OpticFinder<String> $$3, Type<T> $$4) {
      Optional<String> $$5 = $$0.getOptional($$3);
      if ($$5.isEmpty()) {
         return ExtraDataFixUtils.cast($$1, (Typed<T>)$$0);
      } else if ($$5.get().isEmpty()) {
         return net.minecraft.util.Util.writeAndReadTypedOrThrow($$0, $$1, $$0x -> $$0x.remove("CustomName"));
      } else {
         String $$6 = $$0.getOptional($$2).orElse("");
         Dynamic<?> $$7 = fixCustomName($$0.getOps(), $$5.get(), $$6);
         return $$0.set($$3, net.minecraft.util.Util.readTypedOrThrow($$4, $$7));
      }
   }

   private static <T> Dynamic<T> fixCustomName(DynamicOps<T> $$0, String $$1, String $$2) {
      return "minecraft:commandblock_minecart".equals($$2)
         ? new Dynamic($$0, $$0.createString($$1))
         : LegacyComponentDataFixUtils.createPlainTextComponent($$0, $$1);
   }
}
