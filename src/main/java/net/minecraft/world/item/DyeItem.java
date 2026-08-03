package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class DyeItem extends net.minecraft.world.item.Item implements net.minecraft.world.item.SignApplicator {
   private static final Map<net.minecraft.world.item.DyeColor, net.minecraft.world.item.DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(
      net.minecraft.world.item.DyeColor.class
   );
   private final net.minecraft.world.item.DyeColor dyeColor;

   public DyeItem(net.minecraft.world.item.DyeColor $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$1);
      this.dyeColor = $$0;
      ITEM_BY_COLOR.put($$0, this);
   }

   @Override
   public InteractionResult interactLivingEntity(net.minecraft.world.item.ItemStack $$0, Player $$1, LivingEntity $$2, InteractionHand $$3) {
      if ($$2 instanceof Sheep $$4 && $$4.isAlive() && !$$4.isSheared() && $$4.getColor() != this.dyeColor) {
         $$4.level().playSound($$1, $$4, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
         if (!$$1.level().isClientSide()) {
            $$4.setColor(this.dyeColor);
            $$0.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public net.minecraft.world.item.DyeColor getDyeColor() {
      return this.dyeColor;
   }

   public static net.minecraft.world.item.DyeItem byColor(net.minecraft.world.item.DyeColor $$0) {
      return ITEM_BY_COLOR.get($$0);
   }

   @Override
   public boolean tryApplyToSign(Level $$0, SignBlockEntity $$1, boolean $$2, Player $$3) {
      if ($$1.updateText($$0x -> $$0x.setColor(this.getDyeColor()), $$2)) {
         $$0.playSound(null, $$1.getBlockPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
