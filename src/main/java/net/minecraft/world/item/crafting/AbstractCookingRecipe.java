package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public abstract class AbstractCookingRecipe extends SingleItemRecipe {
   private final CookingBookCategory category;
   private final float experience;
   private final int cookingTime;

   public AbstractCookingRecipe(String $$0, CookingBookCategory $$1, Ingredient $$2, net.minecraft.world.item.ItemStack $$3, float $$4, int $$5) {
      super($$0, $$2, $$3);
      this.category = $$1;
      this.experience = $$4;
      this.cookingTime = $$5;
   }

   @Override
   public abstract RecipeSerializer<? extends AbstractCookingRecipe> getSerializer();

   @Override
   public abstract RecipeType<? extends AbstractCookingRecipe> getType();

   public float experience() {
      return this.experience;
   }

   public int cookingTime() {
      return this.cookingTime;
   }

   public CookingBookCategory category() {
      return this.category;
   }

   protected abstract net.minecraft.world.item.Item furnaceIcon();

   @Override
   public List<RecipeDisplay> display() {
      return List.of(
         new FurnaceRecipeDisplay(
            this.input().display(),
            SlotDisplay.AnyFuel.INSTANCE,
            new SlotDisplay.ItemStackSlotDisplay(this.result()),
            new SlotDisplay.ItemSlotDisplay(this.furnaceIcon()),
            this.cookingTime,
            this.experience
         )
      );
   }

   @FunctionalInterface
   public interface Factory<T extends AbstractCookingRecipe> {
      T create(String var1, CookingBookCategory var2, Ingredient var3, net.minecraft.world.item.ItemStack var4, float var5, int var6);
   }

   public static class Serializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
      private final MapCodec<T> codec;
      private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

      public Serializer(AbstractCookingRecipe.Factory<T> $$0, int $$1) {
         this.codec = RecordCodecBuilder.mapCodec(
            $$2 -> $$2.group(
                  Codec.STRING.optionalFieldOf("group", "").forGetter(SingleItemRecipe::group),
                  CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(AbstractCookingRecipe::category),
                  Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
                  net.minecraft.world.item.ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(SingleItemRecipe::result),
                  Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(AbstractCookingRecipe::experience),
                  Codec.INT.fieldOf("cookingtime").orElse($$1).forGetter(AbstractCookingRecipe::cookingTime)
               )
               .apply($$2, $$0::create)
         );
         this.streamCodec = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SingleItemRecipe::group,
            CookingBookCategory.STREAM_CODEC,
            AbstractCookingRecipe::category,
            Ingredient.CONTENTS_STREAM_CODEC,
            SingleItemRecipe::input,
            net.minecraft.world.item.ItemStack.STREAM_CODEC,
            SingleItemRecipe::result,
            ByteBufCodecs.FLOAT,
            AbstractCookingRecipe::experience,
            ByteBufCodecs.INT,
            AbstractCookingRecipe::cookingTime,
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
