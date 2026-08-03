package net.minecraft.world.item.alchemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Ingredient;

public class PotionBrewing {
   public static final int BREWING_TIME_SECONDS = 20;
   public static final PotionBrewing EMPTY = new PotionBrewing(List.of(), List.of(), List.of());
   private final List<Ingredient> containers;
   private final List<PotionBrewing.Mix<Potion>> potionMixes;
   private final List<PotionBrewing.Mix<net.minecraft.world.item.Item>> containerMixes;

   PotionBrewing(List<Ingredient> $$0, List<PotionBrewing.Mix<Potion>> $$1, List<PotionBrewing.Mix<net.minecraft.world.item.Item>> $$2) {
      this.containers = $$0;
      this.potionMixes = $$1;
      this.containerMixes = $$2;
   }

   public boolean isIngredient(net.minecraft.world.item.ItemStack $$0) {
      return this.isContainerIngredient($$0) || this.isPotionIngredient($$0);
   }

   private boolean isContainer(net.minecraft.world.item.ItemStack $$0) {
      for (Ingredient $$1 : this.containers) {
         if ($$1.test($$0)) {
            return true;
         }
      }

      return false;
   }

   public boolean isContainerIngredient(net.minecraft.world.item.ItemStack $$0) {
      for (PotionBrewing.Mix<net.minecraft.world.item.Item> $$1 : this.containerMixes) {
         if ($$1.ingredient.test($$0)) {
            return true;
         }
      }

      return false;
   }

   public boolean isPotionIngredient(net.minecraft.world.item.ItemStack $$0) {
      for (PotionBrewing.Mix<Potion> $$1 : this.potionMixes) {
         if ($$1.ingredient.test($$0)) {
            return true;
         }
      }

      return false;
   }

   public boolean isBrewablePotion(Holder<Potion> $$0) {
      for (PotionBrewing.Mix<Potion> $$1 : this.potionMixes) {
         if ($$1.to.is($$0)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasMix(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      return !this.isContainer($$0) ? false : this.hasContainerMix($$0, $$1) || this.hasPotionMix($$0, $$1);
   }

   public boolean hasContainerMix(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      for (PotionBrewing.Mix<net.minecraft.world.item.Item> $$2 : this.containerMixes) {
         if ($$0.is($$2.from) && $$2.ingredient.test($$1)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasPotionMix(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      Optional<Holder<Potion>> $$2 = ((PotionContents)$$0.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).potion();
      if ($$2.isEmpty()) {
         return false;
      } else {
         for (PotionBrewing.Mix<Potion> $$3 : this.potionMixes) {
            if ($$3.from.is($$2.get()) && $$3.ingredient.test($$1)) {
               return true;
            }
         }

         return false;
      }
   }

   public net.minecraft.world.item.ItemStack mix(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      if ($$1.isEmpty()) {
         return $$1;
      } else {
         Optional<Holder<Potion>> $$2 = ((PotionContents)$$1.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).potion();
         if ($$2.isEmpty()) {
            return $$1;
         } else {
            for (PotionBrewing.Mix<net.minecraft.world.item.Item> $$3 : this.containerMixes) {
               if ($$1.is($$3.from) && $$3.ingredient.test($$0)) {
                  return PotionContents.createItemStack((net.minecraft.world.item.Item)$$3.to.value(), $$2.get());
               }
            }

            for (PotionBrewing.Mix<Potion> $$4 : this.potionMixes) {
               if ($$4.from.is($$2.get()) && $$4.ingredient.test($$0)) {
                  return PotionContents.createItemStack($$1.getItem(), $$4.to);
               }
            }

            return $$1;
         }
      }
   }

   public static PotionBrewing bootstrap(FeatureFlagSet $$0) {
      PotionBrewing.Builder $$1 = new PotionBrewing.Builder($$0);
      addVanillaMixes($$1);
      return $$1.build();
   }

   public static void addVanillaMixes(PotionBrewing.Builder $$0) {
      $$0.addContainer(net.minecraft.world.item.Items.POTION);
      $$0.addContainer(net.minecraft.world.item.Items.SPLASH_POTION);
      $$0.addContainer(net.minecraft.world.item.Items.LINGERING_POTION);
      $$0.addContainerRecipe(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.Items.GUNPOWDER, net.minecraft.world.item.Items.SPLASH_POTION);
      $$0.addContainerRecipe(
         net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.Items.DRAGON_BREATH, net.minecraft.world.item.Items.LINGERING_POTION
      );
      $$0.addMix(Potions.WATER, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.THICK);
      $$0.addMix(Potions.WATER, net.minecraft.world.item.Items.REDSTONE, Potions.MUNDANE);
      $$0.addMix(Potions.WATER, net.minecraft.world.item.Items.NETHER_WART, Potions.AWKWARD);
      $$0.addStartMix(net.minecraft.world.item.Items.BREEZE_ROD, Potions.WIND_CHARGED);
      $$0.addStartMix(net.minecraft.world.item.Items.SLIME_BLOCK, Potions.OOZING);
      $$0.addStartMix(net.minecraft.world.item.Items.STONE, Potions.INFESTED);
      $$0.addStartMix(net.minecraft.world.item.Items.COBWEB, Potions.WEAVING);
      $$0.addMix(Potions.AWKWARD, net.minecraft.world.item.Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
      $$0.addMix(Potions.NIGHT_VISION, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_NIGHT_VISION);
      $$0.addMix(Potions.NIGHT_VISION, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
      $$0.addMix(Potions.LONG_NIGHT_VISION, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
      $$0.addMix(Potions.INVISIBILITY, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_INVISIBILITY);
      $$0.addStartMix(net.minecraft.world.item.Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
      $$0.addMix(Potions.FIRE_RESISTANCE, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
      $$0.addStartMix(net.minecraft.world.item.Items.RABBIT_FOOT, Potions.LEAPING);
      $$0.addMix(Potions.LEAPING, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_LEAPING);
      $$0.addMix(Potions.LEAPING, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
      $$0.addMix(Potions.LEAPING, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
      $$0.addMix(Potions.LONG_LEAPING, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
      $$0.addMix(Potions.SLOWNESS, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_SLOWNESS);
      $$0.addMix(Potions.SLOWNESS, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
      $$0.addMix(Potions.AWKWARD, net.minecraft.world.item.Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
      $$0.addMix(Potions.TURTLE_MASTER, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
      $$0.addMix(Potions.TURTLE_MASTER, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
      $$0.addMix(Potions.SWIFTNESS, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
      $$0.addMix(Potions.LONG_SWIFTNESS, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
      $$0.addStartMix(net.minecraft.world.item.Items.SUGAR, Potions.SWIFTNESS);
      $$0.addMix(Potions.SWIFTNESS, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_SWIFTNESS);
      $$0.addMix(Potions.SWIFTNESS, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
      $$0.addMix(Potions.AWKWARD, net.minecraft.world.item.Items.PUFFERFISH, Potions.WATER_BREATHING);
      $$0.addMix(Potions.WATER_BREATHING, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_WATER_BREATHING);
      $$0.addStartMix(net.minecraft.world.item.Items.GLISTERING_MELON_SLICE, Potions.HEALING);
      $$0.addMix(Potions.HEALING, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
      $$0.addMix(Potions.HEALING, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
      $$0.addMix(Potions.STRONG_HEALING, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
      $$0.addMix(Potions.HARMING, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
      $$0.addMix(Potions.POISON, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
      $$0.addMix(Potions.LONG_POISON, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
      $$0.addMix(Potions.STRONG_POISON, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
      $$0.addStartMix(net.minecraft.world.item.Items.SPIDER_EYE, Potions.POISON);
      $$0.addMix(Potions.POISON, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_POISON);
      $$0.addMix(Potions.POISON, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
      $$0.addStartMix(net.minecraft.world.item.Items.GHAST_TEAR, Potions.REGENERATION);
      $$0.addMix(Potions.REGENERATION, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_REGENERATION);
      $$0.addMix(Potions.REGENERATION, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
      $$0.addStartMix(net.minecraft.world.item.Items.BLAZE_POWDER, Potions.STRENGTH);
      $$0.addMix(Potions.STRENGTH, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_STRENGTH);
      $$0.addMix(Potions.STRENGTH, net.minecraft.world.item.Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
      $$0.addMix(Potions.WATER, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
      $$0.addMix(Potions.WEAKNESS, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_WEAKNESS);
      $$0.addMix(Potions.AWKWARD, net.minecraft.world.item.Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
      $$0.addMix(Potions.SLOW_FALLING, net.minecraft.world.item.Items.REDSTONE, Potions.LONG_SLOW_FALLING);
   }

   public static class Builder {
      private final List<Ingredient> containers = new ArrayList<>();
      private final List<PotionBrewing.Mix<Potion>> potionMixes = new ArrayList<>();
      private final List<PotionBrewing.Mix<net.minecraft.world.item.Item>> containerMixes = new ArrayList<>();
      private final FeatureFlagSet enabledFeatures;

      public Builder(FeatureFlagSet $$0) {
         this.enabledFeatures = $$0;
      }

      private static void expectPotion(net.minecraft.world.item.Item $$0) {
         if (!($$0 instanceof net.minecraft.world.item.PotionItem)) {
            throw new IllegalArgumentException("Expected a potion, got: " + BuiltInRegistries.ITEM.getKey($$0));
         }
      }

      public void addContainerRecipe(net.minecraft.world.item.Item $$0, net.minecraft.world.item.Item $$1, net.minecraft.world.item.Item $$2) {
         if ($$0.isEnabled(this.enabledFeatures) && $$1.isEnabled(this.enabledFeatures) && $$2.isEnabled(this.enabledFeatures)) {
            expectPotion($$0);
            expectPotion($$2);
            this.containerMixes.add(new PotionBrewing.Mix<>($$0.builtInRegistryHolder(), Ingredient.of($$1), $$2.builtInRegistryHolder()));
         }
      }

      public void addContainer(net.minecraft.world.item.Item $$0) {
         if ($$0.isEnabled(this.enabledFeatures)) {
            expectPotion($$0);
            this.containers.add(Ingredient.of($$0));
         }
      }

      public void addMix(Holder<Potion> $$0, net.minecraft.world.item.Item $$1, Holder<Potion> $$2) {
         if (((Potion)$$0.value()).isEnabled(this.enabledFeatures)
            && $$1.isEnabled(this.enabledFeatures)
            && ((Potion)$$2.value()).isEnabled(this.enabledFeatures)) {
            this.potionMixes.add(new PotionBrewing.Mix<>($$0, Ingredient.of($$1), $$2));
         }
      }

      public void addStartMix(net.minecraft.world.item.Item $$0, Holder<Potion> $$1) {
         if (((Potion)$$1.value()).isEnabled(this.enabledFeatures)) {
            this.addMix(Potions.WATER, $$0, Potions.MUNDANE);
            this.addMix(Potions.AWKWARD, $$0, $$1);
         }
      }

      public PotionBrewing build() {
         return new PotionBrewing(List.copyOf(this.containers), List.copyOf(this.potionMixes), List.copyOf(this.containerMixes));
      }
   }

   record Mix<T>(Holder<T> from, Ingredient ingredient, Holder<T> to) {
   }
}
