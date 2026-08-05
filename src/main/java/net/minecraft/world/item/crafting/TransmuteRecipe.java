package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class TransmuteRecipe implements CraftingRecipe {
   final String group;
   final CraftingBookCategory category;
   final Ingredient input;
   final Ingredient material;
   final TransmuteResult result;
   
   private PlacementInfo placementInfo;

   public TransmuteRecipe(String $$0, CraftingBookCategory $$1, Ingredient $$2, Ingredient $$3, TransmuteResult $$4) {
      this.group = $$0;
      this.category = $$1;
      this.input = $$2;
      this.material = $$3;
      this.result = $$4;
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.ingredientCount() != 2) {
         return false;
      } else {
         boolean $$2 = false;
         boolean $$3 = false;

         for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
            net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
            if (!$$5.isEmpty()) {
               if (!$$2 && this.input.test($$5)) {
                  if (this.result.isResultUnchanged($$5)) {
                     return false;
                  }

                  $$2 = true;
               } else {
                  if ($$3 || !this.material.test($$5)) {
                     return false;
                  }

                  $$3 = true;
               }
            }
         }

         return $$2 && $$3;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      for (int $$2 = 0; $$2 < $$0.size(); $$2++) {
         net.minecraft.world.item.ItemStack $$3 = $$0.getItem($$2);
         if (!$$3.isEmpty() && this.input.test($$3)) {
            return this.result.apply($$3);
         }
      }

      return net.minecraft.world.item.ItemStack.EMPTY;
   }

   @Override
   public List<RecipeDisplay> display() {
      return List.of(
         new ShapelessCraftingRecipeDisplay(
            List.of(this.input.display(), this.material.display()),
            this.result.display(),
            new SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.CRAFTING_TABLE)
         )
      );
   }

   @Override
   public RecipeSerializer<TransmuteRecipe> getSerializer() {
      return RecipeSerializer.TRANSMUTE;
   }

   @Override
   public String group() {
      return this.group;
   }

   @Override
   public PlacementInfo placementInfo() {
      if (this.placementInfo == null) {
         this.placementInfo = PlacementInfo.create(List.of(this.input, this.material));
      }

      return this.placementInfo;
   }

   @Override
   public CraftingBookCategory category() {
      return this.category;
   }

   public static class Serializer implements RecipeSerializer<TransmuteRecipe> {
      private static final MapCodec<TransmuteRecipe> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.STRING.optionalFieldOf("group", "").forGetter($$0x -> $$0x.group),
               CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter($$0x -> $$0x.category),
               Ingredient.CODEC.fieldOf("input").forGetter($$0x -> $$0x.input),
               Ingredient.CODEC.fieldOf("material").forGetter($$0x -> $$0x.material),
               TransmuteResult.CODEC.fieldOf("result").forGetter($$0x -> $$0x.result)
            )
            .apply($$0, TransmuteRecipe::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         $$0 -> $$0.group,
         CraftingBookCategory.STREAM_CODEC,
         $$0 -> $$0.category,
         Ingredient.CONTENTS_STREAM_CODEC,
         $$0 -> $$0.input,
         Ingredient.CONTENTS_STREAM_CODEC,
         $$0 -> $$0.material,
         TransmuteResult.STREAM_CODEC,
         $$0 -> $$0.result,
         TransmuteRecipe::new
      );

      @Override
      public MapCodec<TransmuteRecipe> codec() {
         return CODEC;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, TransmuteRecipe> streamCodec() {
         return STREAM_CODEC;
      }
   }
}
