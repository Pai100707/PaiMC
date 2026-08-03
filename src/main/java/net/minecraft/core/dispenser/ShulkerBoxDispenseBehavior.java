package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

public class ShulkerBoxDispenseBehavior extends OptionalDispenseItemBehavior {
   private static final Logger LOGGER = LogUtils.getLogger();

   @Override
   protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
      this.setSuccess(false);
      Item $$2 = $$1.getItem();
      if ($$2 instanceof BlockItem) {
         net.minecraft.core.Direction $$3 = (net.minecraft.core.Direction)$$0.state().getValue(DispenserBlock.FACING);
         net.minecraft.core.BlockPos $$4 = $$0.pos().relative($$3);
         net.minecraft.core.Direction $$5 = $$0.level().isEmptyBlock($$4.below()) ? $$3 : net.minecraft.core.Direction.UP;

         try {
            this.setSuccess(((BlockItem)$$2).place(new DirectionalPlaceContext($$0.level(), $$4, $$3, $$1, $$5)).consumesAction());
         } catch (Exception var8) {
            LOGGER.error("Error trying to place shulker box at {}", $$4, var8);
         }
      }

      return $$1;
   }
}
