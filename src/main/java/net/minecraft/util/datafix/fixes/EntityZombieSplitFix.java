package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;

public class EntityZombieSplitFix extends EntityRenameFix {
   private final Supplier<Type<?>> zombieVillagerType = Suppliers.memoize(() -> this.getOutputSchema().getChoiceType(References.ENTITY, "ZombieVillager"));

   public EntityZombieSplitFix(Schema $$0) {
      super("EntityZombieSplitFix", $$0, true);
   }

   @Override
   protected Pair<String, Typed<?>> fix(String $$0, Typed<?> $$1) {
      if (!$$0.equals("Zombie")) {
         return Pair.of($$0, $$1);
      } else {
         Dynamic<?> $$2 = (Dynamic<?>)$$1.getOptional(DSL.remainderFinder()).orElseThrow();
         int $$3 = $$2.get("ZombieType").asInt(0);
         String $$4;
         Typed<?> $$5;
         switch ($$3) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
               $$4 = "ZombieVillager";
               $$5 = this.changeSchemaToZombieVillager($$1, $$3 - 1);
               break;
            case 6:
               $$4 = "Husk";
               $$5 = $$1;
               break;
            default:
               $$4 = "Zombie";
               $$5 = $$1;
         }

         return Pair.of($$4, $$5.update(DSL.remainderFinder(), $$0x -> $$0x.remove("ZombieType")));
      }
   }

   private Typed<?> changeSchemaToZombieVillager(Typed<?> $$0, int $$1) {
      return net.minecraft.util.Util.writeAndReadTypedOrThrow($$0, this.zombieVillagerType.get(), $$1x -> $$1x.set("Profession", $$1x.createInt($$1)));
   }
}
