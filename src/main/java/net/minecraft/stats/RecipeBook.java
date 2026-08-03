package net.minecraft.stats;

import net.minecraft.world.inventory.RecipeBookType;

public class RecipeBook {
   protected final net.minecraft.stats.RecipeBookSettings bookSettings = new net.minecraft.stats.RecipeBookSettings();

   public boolean isOpen(RecipeBookType $$0) {
      return this.bookSettings.isOpen($$0);
   }

   public void setOpen(RecipeBookType $$0, boolean $$1) {
      this.bookSettings.setOpen($$0, $$1);
   }

   public boolean isFiltering(RecipeBookType $$0) {
      return this.bookSettings.isFiltering($$0);
   }

   public void setFiltering(RecipeBookType $$0, boolean $$1) {
      this.bookSettings.setFiltering($$0, $$1);
   }

   public void setBookSettings(net.minecraft.stats.RecipeBookSettings $$0) {
      this.bookSettings.replaceFrom($$0);
   }

   public net.minecraft.stats.RecipeBookSettings getBookSettings() {
      return this.bookSettings;
   }

   public void setBookSetting(RecipeBookType $$0, boolean $$1, boolean $$2) {
      this.bookSettings.setOpen($$0, $$1);
      this.bookSettings.setFiltering($$0, $$2);
   }
}
