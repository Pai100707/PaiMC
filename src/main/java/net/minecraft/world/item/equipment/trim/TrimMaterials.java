package net.minecraft.world.item.equipment.trim;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ProvidesTrimMaterial;

public class TrimMaterials {
   public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
   public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
   public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
   public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
   public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
   public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
   public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
   public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
   public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
   public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");
   public static final ResourceKey<TrimMaterial> RESIN = registryKey("resin");

   public static void bootstrap(BootstrapContext<TrimMaterial> $$0) {
      register($$0, QUARTZ, Style.EMPTY.withColor(14931140), MaterialAssetGroup.QUARTZ);
      register($$0, IRON, Style.EMPTY.withColor(15527148), MaterialAssetGroup.IRON);
      register($$0, NETHERITE, Style.EMPTY.withColor(6445145), MaterialAssetGroup.NETHERITE);
      register($$0, REDSTONE, Style.EMPTY.withColor(9901575), MaterialAssetGroup.REDSTONE);
      register($$0, COPPER, Style.EMPTY.withColor(11823181), MaterialAssetGroup.COPPER);
      register($$0, GOLD, Style.EMPTY.withColor(14594349), MaterialAssetGroup.GOLD);
      register($$0, EMERALD, Style.EMPTY.withColor(1155126), MaterialAssetGroup.EMERALD);
      register($$0, DIAMOND, Style.EMPTY.withColor(7269586), MaterialAssetGroup.DIAMOND);
      register($$0, LAPIS, Style.EMPTY.withColor(4288151), MaterialAssetGroup.LAPIS);
      register($$0, AMETHYST, Style.EMPTY.withColor(10116294), MaterialAssetGroup.AMETHYST);
      register($$0, RESIN, Style.EMPTY.withColor(16545810), MaterialAssetGroup.RESIN);
   }

   public static Optional<Holder<TrimMaterial>> getFromIngredient(Provider $$0, net.minecraft.world.item.ItemStack $$1) {
      ProvidesTrimMaterial $$2 = (ProvidesTrimMaterial)$$1.get(DataComponents.PROVIDES_TRIM_MATERIAL);
      return $$2 != null ? $$2.unwrap($$0) : Optional.empty();
   }

   private static void register(BootstrapContext<TrimMaterial> $$0, ResourceKey<TrimMaterial> $$1, Style $$2, MaterialAssetGroup $$3) {
      Component $$4 = Component.translatable(Util.makeDescriptionId("trim_material", $$1.identifier())).withStyle($$2);
      $$0.register($$1, new TrimMaterial($$3, $$4));
   }

   private static ResourceKey<TrimMaterial> registryKey(String $$0) {
      return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.withDefaultNamespace($$0));
   }
}
