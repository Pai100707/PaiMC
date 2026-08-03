package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;

public class EntityHealthFix extends DataFix {
   private static final Set<String> ENTITIES = Sets.newHashSet(
      new String[]{
         "ArmorStand",
         "Bat",
         "Blaze",
         "CaveSpider",
         "Chicken",
         "Cow",
         "Creeper",
         "EnderDragon",
         "Enderman",
         "Endermite",
         "EntityHorse",
         "Ghast",
         "Giant",
         "Guardian",
         "LavaSlime",
         "MushroomCow",
         "Ozelot",
         "Pig",
         "PigZombie",
         "Rabbit",
         "Sheep",
         "Shulker",
         "Silverfish",
         "Skeleton",
         "Slime",
         "SnowMan",
         "Spider",
         "Squid",
         "Villager",
         "VillagerGolem",
         "Witch",
         "WitherBoss",
         "Wolf",
         "Zombie"
      }
   );

   public EntityHealthFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      Optional<Number> $$1 = $$0.get("HealF").asNumber().result();
      Optional<Number> $$2 = $$0.get("Health").asNumber().result();
      float $$3;
      if ($$1.isPresent()) {
         $$3 = $$1.get().floatValue();
         $$0 = $$0.remove("HealF");
      } else {
         if (!$$2.isPresent()) {
            return $$0;
         }

         $$3 = $$2.get().floatValue();
      }

      return $$0.set("Health", $$0.createFloat($$3));
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "EntityHealthFix", this.getInputSchema().getType(References.ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), this::fixTag)
      );
   }
}
