package net.minecraft.world.inventory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class MenuType<T extends net.minecraft.world.inventory.AbstractContainerMenu> implements FeatureElement {
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> GENERIC_9x1 = register(
      "generic_9x1", net.minecraft.world.inventory.ChestMenu::oneRow
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> GENERIC_9x2 = register(
      "generic_9x2", net.minecraft.world.inventory.ChestMenu::twoRows
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> GENERIC_9x3 = register(
      "generic_9x3", net.minecraft.world.inventory.ChestMenu::threeRows
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> GENERIC_9x4 = register(
      "generic_9x4", net.minecraft.world.inventory.ChestMenu::fourRows
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> GENERIC_9x5 = register(
      "generic_9x5", net.minecraft.world.inventory.ChestMenu::fiveRows
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> GENERIC_9x6 = register(
      "generic_9x6", net.minecraft.world.inventory.ChestMenu::sixRows
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.DispenserMenu> GENERIC_3x3 = register(
      "generic_3x3", net.minecraft.world.inventory.DispenserMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.CrafterMenu> CRAFTER_3x3 = register(
      "crafter_3x3", net.minecraft.world.inventory.CrafterMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.AnvilMenu> ANVIL = register(
      "anvil", net.minecraft.world.inventory.AnvilMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.BeaconMenu> BEACON = register(
      "beacon", net.minecraft.world.inventory.BeaconMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.BlastFurnaceMenu> BLAST_FURNACE = register(
      "blast_furnace", net.minecraft.world.inventory.BlastFurnaceMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.BrewingStandMenu> BREWING_STAND = register(
      "brewing_stand", net.minecraft.world.inventory.BrewingStandMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.CraftingMenu> CRAFTING = register(
      "crafting", net.minecraft.world.inventory.CraftingMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.EnchantmentMenu> ENCHANTMENT = register(
      "enchantment", net.minecraft.world.inventory.EnchantmentMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.FurnaceMenu> FURNACE = register(
      "furnace", net.minecraft.world.inventory.FurnaceMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.GrindstoneMenu> GRINDSTONE = register(
      "grindstone", net.minecraft.world.inventory.GrindstoneMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.HopperMenu> HOPPER = register(
      "hopper", net.minecraft.world.inventory.HopperMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.LecternMenu> LECTERN = register(
      "lectern", ($$0, $$1) -> new net.minecraft.world.inventory.LecternMenu($$0)
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.LoomMenu> LOOM = register(
      "loom", net.minecraft.world.inventory.LoomMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.MerchantMenu> MERCHANT = register(
      "merchant", net.minecraft.world.inventory.MerchantMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ShulkerBoxMenu> SHULKER_BOX = register(
      "shulker_box", net.minecraft.world.inventory.ShulkerBoxMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.SmithingMenu> SMITHING = register(
      "smithing", net.minecraft.world.inventory.SmithingMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.SmokerMenu> SMOKER = register(
      "smoker", net.minecraft.world.inventory.SmokerMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.CartographyTableMenu> CARTOGRAPHY_TABLE = register(
      "cartography_table", net.minecraft.world.inventory.CartographyTableMenu::new
   );
   public static final net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.StonecutterMenu> STONECUTTER = register(
      "stonecutter", net.minecraft.world.inventory.StonecutterMenu::new
   );
   private final FeatureFlagSet requiredFeatures;
   private final net.minecraft.world.inventory.MenuType.MenuSupplier<T> constructor;

   private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> net.minecraft.world.inventory.MenuType<T> register(
      String $$0, net.minecraft.world.inventory.MenuType.MenuSupplier<T> $$1
   ) {
      return (net.minecraft.world.inventory.MenuType<T>)Registry.register(
         BuiltInRegistries.MENU, $$0, new net.minecraft.world.inventory.MenuType<>($$1, FeatureFlags.VANILLA_SET)
      );
   }

   private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> net.minecraft.world.inventory.MenuType<T> register(
      String $$0, net.minecraft.world.inventory.MenuType.MenuSupplier<T> $$1, FeatureFlag... $$2
   ) {
      return (net.minecraft.world.inventory.MenuType<T>)Registry.register(
         BuiltInRegistries.MENU, $$0, new net.minecraft.world.inventory.MenuType<>($$1, FeatureFlags.REGISTRY.subset($$2))
      );
   }

   private MenuType(net.minecraft.world.inventory.MenuType.MenuSupplier<T> $$0, FeatureFlagSet $$1) {
      this.constructor = $$0;
      this.requiredFeatures = $$1;
   }

   public T create(int $$0, Inventory $$1) {
      return this.constructor.create($$0, $$1);
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   interface MenuSupplier<T extends net.minecraft.world.inventory.AbstractContainerMenu> {
      T create(int var1, Inventory var2);
   }
}
