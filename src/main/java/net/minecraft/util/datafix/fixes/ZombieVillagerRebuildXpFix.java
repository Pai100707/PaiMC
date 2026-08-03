package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;

public class ZombieVillagerRebuildXpFix extends NamedEntityFix {
   public ZombieVillagerRebuildXpFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "Zombie Villager XP rebuild", References.ENTITY, "minecraft:zombie_villager");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), $$0x -> {
         Optional<Number> $$1 = $$0x.get("Xp").asNumber().result();
         if ($$1.isEmpty()) {
            int $$2 = $$0x.get("VillagerData").get("level").asInt(1);
            return $$0x.set("Xp", $$0x.createInt(VillagerRebuildLevelAndXpFix.getMinXpPerLevel($$2)));
         } else {
            return $$0x;
         }
      });
   }
}
