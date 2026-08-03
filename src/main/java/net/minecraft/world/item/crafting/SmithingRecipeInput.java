package net.minecraft.world.item.crafting;

public record SmithingRecipeInput(
   net.minecraft.world.item.ItemStack template, net.minecraft.world.item.ItemStack base, net.minecraft.world.item.ItemStack addition
) implements RecipeInput {
   @Override
   public net.minecraft.world.item.ItemStack getItem(int $$0) {
      return switch ($$0) {
         case 0 -> this.template;
         case 1 -> this.base;
         case 2 -> this.addition;
         default -> throw new IllegalArgumentException("Recipe does not contain slot " + $$0);
      };
   }

   @Override
   public int size() {
      return 3;
   }

   @Override
   public boolean isEmpty() {
      return this.template.isEmpty() && this.base.isEmpty() && this.addition.isEmpty();
   }
}
