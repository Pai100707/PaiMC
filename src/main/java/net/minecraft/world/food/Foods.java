package net.minecraft.world.food;

public class Foods {
   public static final net.minecraft.world.food.FoodProperties APPLE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(4)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties BAKED_POTATO = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(5)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties BEEF = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(3)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties BEETROOT = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(1)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties BEETROOT_SOUP = stew(6).build();
   public static final net.minecraft.world.food.FoodProperties BREAD = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(5)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties CARROT = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(3)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties CHICKEN = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties CHORUS_FRUIT = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(4)
      .saturationModifier(0.3F)
      .alwaysEdible()
      .build();
   public static final net.minecraft.world.food.FoodProperties COD = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_BEEF = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(8)
      .saturationModifier(0.8F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_CHICKEN = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(6)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_COD = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(5)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_MUTTON = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(6)
      .saturationModifier(0.8F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_PORKCHOP = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(8)
      .saturationModifier(0.8F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_RABBIT = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(5)
      .saturationModifier(0.6F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKED_SALMON = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(6)
      .saturationModifier(0.8F)
      .build();
   public static final net.minecraft.world.food.FoodProperties COOKIE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties DRIED_KELP = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(1)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties ENCHANTED_GOLDEN_APPLE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(4)
      .saturationModifier(1.2F)
      .alwaysEdible()
      .build();
   public static final net.minecraft.world.food.FoodProperties GOLDEN_APPLE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(4)
      .saturationModifier(1.2F)
      .alwaysEdible()
      .build();
   public static final net.minecraft.world.food.FoodProperties GOLDEN_CARROT = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(6)
      .saturationModifier(1.2F)
      .build();
   public static final net.minecraft.world.food.FoodProperties HONEY_BOTTLE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(6)
      .saturationModifier(0.1F)
      .alwaysEdible()
      .build();
   public static final net.minecraft.world.food.FoodProperties MELON_SLICE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties MUSHROOM_STEW = stew(6).build();
   public static final net.minecraft.world.food.FoodProperties MUTTON = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties POISONOUS_POTATO = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties PORKCHOP = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(3)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties POTATO = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(1)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties PUFFERFISH = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(1)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties PUMPKIN_PIE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(8)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties RABBIT = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(3)
      .saturationModifier(0.3F)
      .build();
   public static final net.minecraft.world.food.FoodProperties RABBIT_STEW = stew(10).build();
   public static final net.minecraft.world.food.FoodProperties ROTTEN_FLESH = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(4)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties SALMON = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties SPIDER_EYE = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.8F)
      .build();
   public static final net.minecraft.world.food.FoodProperties SUSPICIOUS_STEW = stew(6).alwaysEdible().build();
   public static final net.minecraft.world.food.FoodProperties SWEET_BERRIES = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties GLOW_BERRIES = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(2)
      .saturationModifier(0.1F)
      .build();
   public static final net.minecraft.world.food.FoodProperties TROPICAL_FISH = new net.minecraft.world.food.FoodProperties.Builder()
      .nutrition(1)
      .saturationModifier(0.1F)
      .build();

   private static net.minecraft.world.food.FoodProperties.Builder stew(int $$0) {
      return new net.minecraft.world.food.FoodProperties.Builder().nutrition($$0).saturationModifier(0.6F);
   }
}
