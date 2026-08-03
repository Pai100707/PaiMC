package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jspecify.annotations.Nullable;

public class SmithingTrimRecipe implements SmithingRecipe {
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;
   final Holder<TrimPattern> pattern;
   @Nullable
   private PlacementInfo placementInfo;

   public SmithingTrimRecipe(Ingredient $$0, Ingredient $$1, Ingredient $$2, Holder<TrimPattern> $$3) {
      this.template = $$0;
      this.base = $$1;
      this.addition = $$2;
      this.pattern = $$3;
   }

   public net.minecraft.world.item.ItemStack assemble(SmithingRecipeInput $$0, Provider $$1) {
      return applyTrim($$1, $$0.base(), $$0.addition(), this.pattern);
   }

   public static net.minecraft.world.item.ItemStack applyTrim(
      Provider $$0, net.minecraft.world.item.ItemStack $$1, net.minecraft.world.item.ItemStack $$2, Holder<TrimPattern> $$3
   ) {
      Optional<Holder<TrimMaterial>> $$4 = TrimMaterials.getFromIngredient($$0, $$2);
      if ($$4.isPresent()) {
         ArmorTrim $$5 = (ArmorTrim)$$1.get(DataComponents.TRIM);
         ArmorTrim $$6 = new ArmorTrim($$4.get(), $$3);
         if (Objects.equals($$5, $$6)) {
            return net.minecraft.world.item.ItemStack.EMPTY;
         } else {
            net.minecraft.world.item.ItemStack $$7 = $$1.copyWithCount(1);
            $$7.set(DataComponents.TRIM, $$6);
            return $$7;
         }
      } else {
         return net.minecraft.world.item.ItemStack.EMPTY;
      }
   }

   @Override
   public Optional<Ingredient> templateIngredient() {
      return Optional.of(this.template);
   }

   @Override
   public Ingredient baseIngredient() {
      return this.base;
   }

   @Override
   public Optional<Ingredient> additionIngredient() {
      return Optional.of(this.addition);
   }

   @Override
   public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
      return RecipeSerializer.SMITHING_TRIM;
   }

   @Override
   public PlacementInfo placementInfo() {
      if (this.placementInfo == null) {
         this.placementInfo = PlacementInfo.create(List.of(this.template, this.base, this.addition));
      }

      return this.placementInfo;
   }

   @Override
   public List<RecipeDisplay> display() {
      SlotDisplay $$0 = this.base.display();
      SlotDisplay $$1 = this.addition.display();
      SlotDisplay $$2 = this.template.display();
      return List.of(
         new SmithingRecipeDisplay(
            $$2,
            $$0,
            $$1,
            new SlotDisplay.SmithingTrimDemoSlotDisplay($$0, $$1, this.pattern),
            new SlotDisplay.ItemSlotDisplay(net.minecraft.world.item.Items.SMITHING_TABLE)
         )
      );
   }

   public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
      private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Ingredient.CODEC.fieldOf("template").forGetter($$0x -> $$0x.template),
               Ingredient.CODEC.fieldOf("base").forGetter($$0x -> $$0x.base),
               Ingredient.CODEC.fieldOf("addition").forGetter($$0x -> $$0x.addition),
               TrimPattern.CODEC.fieldOf("pattern").forGetter($$0x -> $$0x.pattern)
            )
            .apply($$0, SmithingTrimRecipe::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.composite(
         Ingredient.CONTENTS_STREAM_CODEC,
         $$0 -> $$0.template,
         Ingredient.CONTENTS_STREAM_CODEC,
         $$0 -> $$0.base,
         Ingredient.CONTENTS_STREAM_CODEC,
         $$0 -> $$0.addition,
         TrimPattern.STREAM_CODEC,
         $$0 -> $$0.pattern,
         SmithingTrimRecipe::new
      );

      @Override
      public MapCodec<SmithingTrimRecipe> codec() {
         return CODEC;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
         return STREAM_CODEC;
      }
   }
}
