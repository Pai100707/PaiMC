package net.minecraft.world.level.saveddata;

public abstract class SavedData {
   private boolean dirty;

   public void setDirty() {
      this.setDirty(true);
   }

   public void setDirty(boolean $$0) {
      this.dirty = $$0;
   }

   public boolean isDirty() {
      return this.dirty;
   }
}
