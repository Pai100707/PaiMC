package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

public abstract class SingleItemRecipe implements Recipe<SingleRecipeInput> {
   private final Ingredient input;
   private final net.minecraft.world.item.ItemStack result;
   private final String group;
   
   private PlacementInfo placementInfo;

   public SingleItemRecipe(String $$0, Ingredient $$1, net.minecraft.world.item.ItemStack $$2) {
      this.group = $$0;
      this.input = $$1;
      this.result = $$2;
   }

   @Override
   public abstract RecipeSerializer<? extends SingleItemRecipe> getSerializer();

   @Override
   public abstract RecipeType<? extends SingleItemRecipe> getType();

   public boolean matches(SingleRecipeInput $$0, Level $$1) {
      return this.input.test($$0.item());
   }

   @Override
   public String group() {
      return this.group;
   }

   public Ingredient input() {
      return this.input;
   }

   protected net.minecraft.world.item.ItemStack result() {
      return this.result;
   }

   @Override
   public PlacementInfo placementInfo() {
      if (this.placementInfo == null) {
         this.placementInfo = PlacementInfo.create(this.input);
      }

      return this.placementInfo;
   }

   public net.minecraft.world.item.ItemStack assemble(SingleRecipeInput $$0, Provider $$1) {
      return this.result.copy();
   }

   @FunctionalInterface
   public interface Factory<T extends SingleItemRecipe> {
      T create(String var1, Ingredient var2, net.minecraft.world.item.ItemStack var3);
   }

   public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
      private final MapCodec<T> codec;
      private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

      protected Serializer(SingleItemRecipe.Factory<T> $$0) {
         this.codec = RecordCodecBuilder.mapCodec(
            $$1 -> $$1.group(
                  Codec.STRING.optionalFieldOf("group", "").forGetter(SingleItemRecipe::group),
                  Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
                  net.minecraft.world.item.ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SingleItemRecipe::result)
               )
               .apply($$1, $$0::create)
         );
         this.streamCodec = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SingleItemRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC,
            SingleItemRecipe::input,
            net.minecraft.world.item.ItemStack.STREAM_CODEC,
            SingleItemRecipe::result,
            $$0::create
         );
      }

      @Override
      public MapCodec<T> codec() {
         return this.codec;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
         return this.streamCodec;
      }
   }
}
