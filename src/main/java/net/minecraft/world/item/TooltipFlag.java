package net.minecraft.world.item;

public interface TooltipFlag {
   net.minecraft.world.item.TooltipFlag.Default NORMAL = new net.minecraft.world.item.TooltipFlag.Default(false, false);
   net.minecraft.world.item.TooltipFlag.Default ADVANCED = new net.minecraft.world.item.TooltipFlag.Default(true, false);

   boolean isAdvanced();

   boolean isCreative();

   public record Default(boolean advanced, boolean creative) implements net.minecraft.world.item.TooltipFlag {
      @Override
      public boolean isAdvanced() {
         return this.advanced;
      }

      @Override
      public boolean isCreative() {
         return this.creative;
      }

      public net.minecraft.world.item.TooltipFlag.Default asCreative() {
         return new net.minecraft.world.item.TooltipFlag.Default(this.advanced, true);
      }
   }
}
