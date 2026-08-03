package net.minecraft.world.inventory;

public abstract class DataSlot {
   private int prevValue;

   public static net.minecraft.world.inventory.DataSlot forContainer(final net.minecraft.world.inventory.ContainerData $$0, final int $$1) {
      return new net.minecraft.world.inventory.DataSlot() {
         @Override
         public int get() {
            return $$0.get($$1);
         }

         @Override
         public void set(int $$0x) {
            $$0.set($$1, $$0);
         }
      };
   }

   public static net.minecraft.world.inventory.DataSlot shared(final int[] $$0, final int $$1) {
      return new net.minecraft.world.inventory.DataSlot() {
         @Override
         public int get() {
            return $$0[$$1];
         }

         @Override
         public void set(int $$0x) {
            $$0[$$1] = $$0;
         }
      };
   }

   public static net.minecraft.world.inventory.DataSlot standalone() {
      return new net.minecraft.world.inventory.DataSlot() {
         private int value;

         @Override
         public int get() {
            return this.value;
         }

         @Override
         public void set(int $$0) {
            this.value = $$0;
         }
      };
   }

   public abstract int get();

   public abstract void set(int var1);

   public boolean checkAndClearUpdateFlag() {
      int $$0 = this.get();
      boolean $$1 = $$0 != this.prevValue;
      this.prevValue = $$0;
      return $$1;
   }
}
