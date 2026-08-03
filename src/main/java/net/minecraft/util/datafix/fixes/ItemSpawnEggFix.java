package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemSpawnEggFix extends DataFix {
   private static final String[] ID_TO_ENTITY = (String[])DataFixUtils.make(new String[256], $$0 -> {
      $$0[1] = "Item";
      $$0[2] = "XPOrb";
      $$0[7] = "ThrownEgg";
      $$0[8] = "LeashKnot";
      $$0[9] = "Painting";
      $$0[10] = "Arrow";
      $$0[11] = "Snowball";
      $$0[12] = "Fireball";
      $$0[13] = "SmallFireball";
      $$0[14] = "ThrownEnderpearl";
      $$0[15] = "EyeOfEnderSignal";
      $$0[16] = "ThrownPotion";
      $$0[17] = "ThrownExpBottle";
      $$0[18] = "ItemFrame";
      $$0[19] = "WitherSkull";
      $$0[20] = "PrimedTnt";
      $$0[21] = "FallingSand";
      $$0[22] = "FireworksRocketEntity";
      $$0[23] = "TippedArrow";
      $$0[24] = "SpectralArrow";
      $$0[25] = "ShulkerBullet";
      $$0[26] = "DragonFireball";
      $$0[30] = "ArmorStand";
      $$0[41] = "Boat";
      $$0[42] = "MinecartRideable";
      $$0[43] = "MinecartChest";
      $$0[44] = "MinecartFurnace";
      $$0[45] = "MinecartTNT";
      $$0[46] = "MinecartHopper";
      $$0[47] = "MinecartSpawner";
      $$0[40] = "MinecartCommandBlock";
      $$0[50] = "Creeper";
      $$0[51] = "Skeleton";
      $$0[52] = "Spider";
      $$0[53] = "Giant";
      $$0[54] = "Zombie";
      $$0[55] = "Slime";
      $$0[56] = "Ghast";
      $$0[57] = "PigZombie";
      $$0[58] = "Enderman";
      $$0[59] = "CaveSpider";
      $$0[60] = "Silverfish";
      $$0[61] = "Blaze";
      $$0[62] = "LavaSlime";
      $$0[63] = "EnderDragon";
      $$0[64] = "WitherBoss";
      $$0[65] = "Bat";
      $$0[66] = "Witch";
      $$0[67] = "Endermite";
      $$0[68] = "Guardian";
      $$0[69] = "Shulker";
      $$0[90] = "Pig";
      $$0[91] = "Sheep";
      $$0[92] = "Cow";
      $$0[93] = "Chicken";
      $$0[94] = "Squid";
      $$0[95] = "Wolf";
      $$0[96] = "MushroomCow";
      $$0[97] = "SnowMan";
      $$0[98] = "Ozelot";
      $$0[99] = "VillagerGolem";
      $$0[100] = "EntityHorse";
      $$0[101] = "Rabbit";
      $$0[120] = "Villager";
      $$0[200] = "EnderCrystal";
   });

   public ItemSpawnEggFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      Schema $$0 = this.getInputSchema();
      Type<?> $$1 = $$0.getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> $$2 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<String> $$3 = DSL.fieldFinder("id", DSL.string());
      OpticFinder<?> $$4 = $$1.findField("tag");
      OpticFinder<?> $$5 = $$4.type().findField("EntityTag");
      OpticFinder<?> $$6 = DSL.typeFinder($$0.getTypeRaw(References.ENTITY));
      return this.fixTypeEverywhereTyped("ItemSpawnEggFix", $$1, $$5x -> {
         Optional<Pair<String, String>> $$6x = $$5x.getOptional($$2);
         if ($$6x.isPresent() && Objects.equals($$6x.get().getSecond(), "minecraft:spawn_egg")) {
            Dynamic<?> $$7 = (Dynamic<?>)$$5x.get(DSL.remainderFinder());
            short $$8 = $$7.get("Damage").asShort((short)0);
            Optional<? extends Typed<?>> $$9 = $$5x.getOptionalTyped($$4);
            Optional<? extends Typed<?>> $$10 = $$9.flatMap($$1xx -> $$1xx.getOptionalTyped($$5));
            Optional<? extends Typed<?>> $$11 = $$10.flatMap($$1xx -> $$1xx.getOptionalTyped($$6));
            Optional<String> $$12 = $$11.flatMap($$1xx -> $$1xx.getOptional($$3));
            Typed<?> $$13 = $$5x;
            String $$14 = ID_TO_ENTITY[$$8 & 255];
            if ($$14 != null && ($$12.isEmpty() || !Objects.equals($$12.get(), $$14))) {
               Typed<?> $$15 = $$5x.getOrCreateTyped($$4);
               Dynamic<?> $$16 = (Dynamic<?>)DataFixUtils.orElse($$15.getOptionalTyped($$5).map($$0xx -> (Dynamic)$$0xx.write().getOrThrow()), $$7.emptyMap());
               $$16 = $$16.set("id", $$16.createString($$14));
               $$13 = $$5x.set($$4, ExtraDataFixUtils.readAndSet($$15, $$5, $$16));
            }

            if ($$8 != 0) {
               $$7 = $$7.set("Damage", $$7.createShort((short)0));
               $$13 = $$13.set(DSL.remainderFinder(), $$7);
            }

            return $$13;
         } else {
            return $$5x;
         }
      });
   }
}
