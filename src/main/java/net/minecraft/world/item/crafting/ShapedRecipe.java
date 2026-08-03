package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ShapedRecipe implements CraftingRecipe {
   final ShapedRecipePattern pattern;
   final net.minecraft.world.item.ItemStack result;
   final String group;
   final CraftingBookCategory category;
   final boolean showNotification;
   @Nullable
   private PlacementInfo placementInfo;

   public ShapedRecipe(String $$0, CraftingBookCategory $$1, ShapedRecipePattern $$2, net.minecraft.world.item.ItemStack $$3, boolean $$4) {
      this.group = $$0;
      this.category = $$1;
      this.pattern = $$2;
      this.result = $$3;
      this.showNotification = $$4;
   }

   public ShapedRecipe(String $$0, CraftingBookCategory $$1, ShapedRecipePattern $$2, net.minecraft.world.item.ItemStack $$3) {
      this($$0, $$1, $$2, $$3, true);
   }

   @Override
   public RecipeSerializer<? extends ShapedRecipe> getSerializer() {
      return RecipeSerializer.SHAPED_RECIPE;
   }

   @Override
   public String group() {
      return this.group;
   }

   @Override
   public CraftingBookCategory category() {
      return this.category;
   }

   @VisibleForTesting
   public List<Optional<Ingredient>> getIngredients() {
      return this.pattern.ingredients();
   }

   @Override
   public PlacementInfo placementInfo() {
      if (this.placementInfo == null) {
         this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
      }

      return this.placementInfo;
   }

   @Override
   public boolean showNotification() {
      return this.showNotification;
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      return this.pattern.matches($$0);
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      return this.result.copy();
   }

   public int getWidth() {
      return this.pattern.width();
   }

   public int getHeight() {
      return this.pattern.height();
   }

   @Override
   public List<RecipeDisplay> display() {
      return List.of(
         new ShapedCraftingRecipeDisplay(
            this.pattern.width(),
            this.pattern.height(),
            this.pattern.ingredients().stream().map($$0 -> $$0.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
            new SlotDisplay.ItemStackSlotDisplay(this.result),
            new SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.CRAFTING_TABLE)
         )
      );
   }

   public static class Serializer implements RecipeSerializer<ShapedRecipe> {
      public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.STRING.optionalFieldOf("group", "").forGetter($$0x -> $$0x.group),
               CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter($$0x -> $$0x.category),
               ShapedRecipePattern.MAP_CODEC.forGetter($$0x -> $$0x.pattern),
               net.minecraft.world.item.ItemStack.STRICT_CODEC.fieldOf("result").forGetter($$0x -> $$0x.result),
               Codec.BOOL.optionalFieldOf("show_notification", true).forGetter($$0x -> $$0x.showNotification)
            )
            .apply($$0, ShapedRecipe::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> STREAM_CODEC = StreamCodec.of(
         ShapedRecipe.Serializer::toNetwork, ShapedRecipe.Serializer::fromNetwork
      );

      @Override
      public MapCodec<ShapedRecipe> codec() {
         return CODEC;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
         return STREAM_CODEC;
      }

      private static ShapedRecipe fromNetwork(RegistryFriendlyByteBuf $$0) {
         String $$1 = $$0.readUtf();
         CraftingBookCategory $$2 = (CraftingBookCategory)$$0.readEnum(CraftingBookCategory.class);
         ShapedRecipePattern $$3 = (ShapedRecipePattern)ShapedRecipePattern.STREAM_CODEC.decode($$0);
         net.minecraft.world.item.ItemStack $$4 = (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.STREAM_CODEC.decode($$0);
         boolean $$5 = $$0.readBoolean();
         return new ShapedRecipe($$1, $$2, $$3, $$4, $$5);
      }

      private static void toNetwork(RegistryFriendlyByteBuf $$0, ShapedRecipe $$1) {
         $$0.writeUtf($$1.group);
         $$0.writeEnum($$1.category);
         ShapedRecipePattern.STREAM_CODEC.encode($$0, $$1.pattern);
         net.minecraft.world.item.ItemStack.STREAM_CODEC.encode($$0, $$1.result);
         $$0.writeBoolean($$1.showNotification);
      }
   }
}
