package net.minecraft.world.item.crafting.display;

import java.util.List;
import net.minecraft.core.Holder;

public interface DisplayContentsFactory<T> {
   public interface ForRemainders<T> extends DisplayContentsFactory<T> {
      T addRemainder(T var1, List<T> var2);
   }

   public interface ForStacks<T> extends DisplayContentsFactory<T> {
      default T forStack(Holder<net.minecraft.world.item.Item> $$0) {
         return this.forStack(new net.minecraft.world.item.ItemStack($$0));
      }

      default T forStack(net.minecraft.world.item.Item $$0) {
         return this.forStack(new net.minecraft.world.item.ItemStack($$0));
      }

      T forStack(net.minecraft.world.item.ItemStack var1);
   }
}
