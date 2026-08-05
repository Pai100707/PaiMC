package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

public class SmithingTransformRecipe implements SmithingRecipe {
   final Optional<Ingredient> template;
   final Ingredient base;
   final Optional<Ingredient> addition;
   final TransmuteResult result;
   
   private PlacementInfo placementInfo;

   public SmithingTransformRecipe(Optional<Ingredient> $$0, Ingredient $$1, Optional<Ingredient> $$2, TransmuteResult $$3) {
      this.template = $$0;
      this.base = $$1;
      this.addition = $$2;
      this.result = $$3;
   }

   public net.minecraft.world.item.ItemStack assemble(SmithingRecipeInput $$0, Provider $$1) {
      return this.result.apply($$0.base());
   }

   @Override
   public Optional<Ingredient> templateIngredient() {
      return this.template;
   }

   @Override
   public Ingredient baseIngredient() {
      return this.base;
   }

   @Override
   public Optional<Ingredient> additionIngredient() {
      return this.addition;
   }

   @Override
   public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
      return RecipeSerializer.SMITHING_TRANSFORM;
   }

   @Override
   public PlacementInfo placementInfo() {
      if (this.placementInfo == null) {
         this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, Optional.of(this.base), this.addition));
      }

      return this.placementInfo;
   }

   @Override
   public List<RecipeDisplay> display() {
      return List.of(
         new SmithingRecipeDisplay(
            Ingredient.optionalIngredientToDisplay(this.template),
            this.base.display(),
            Ingredient.optionalIngredientToDisplay(this.addition),
            this.result.display(),
            new SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.SMITHING_TABLE)
         )
      );
   }

   public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
      private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Ingredient.CODEC.optionalFieldOf("template").forGetter($$0x -> $$0x.template),
               Ingredient.CODEC.fieldOf("base").forGetter($$0x -> $$0x.base),
               Ingredient.CODEC.optionalFieldOf("addition").forGetter($$0x -> $$0x.addition),
               TransmuteResult.CODEC.fieldOf("result").forGetter($$0x -> $$0x.result)
            )
            .apply($$0, SmithingTransformRecipe::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.composite(
         Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
         $$0 -> $$0.template,
         Ingredient.CONTENTS_STREAM_CODEC,
         $$0 -> $$0.base,
         Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
         $$0 -> $$0.addition,
         TransmuteResult.STREAM_CODEC,
         $$0 -> $$0.result,
         SmithingTransformRecipe::new
      );

      @Override
      public MapCodec<SmithingTransformRecipe> codec() {
         return CODEC;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
         return STREAM_CODEC;
      }
   }
}
