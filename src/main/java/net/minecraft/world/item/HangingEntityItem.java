package net.minecraft.world.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends net.minecraft.world.item.Item {
   private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
   private final EntityType<? extends HangingEntity> type;

   public HangingEntityItem(EntityType<? extends HangingEntity> $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$1);
      this.type = $$0;
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      BlockPos $$1 = $$0.getClickedPos();
      Direction $$2 = $$0.getClickedFace();
      BlockPos $$3 = $$1.relative($$2);
      Player $$4 = $$0.getPlayer();
      net.minecraft.world.item.ItemStack $$5 = $$0.getItemInHand();
      if ($$4 != null && !this.mayPlace($$4, $$2, $$5, $$3)) {
         return InteractionResult.FAIL;
      } else {
         Level $$6 = $$0.getLevel();
         HangingEntity $$8;
         if (this.type == EntityType.PAINTING) {
            Optional<Painting> $$7 = Painting.create($$6, $$3, $$2);
            if ($$7.isEmpty()) {
               return InteractionResult.CONSUME;
            }

            $$8 = (HangingEntity)$$7.get();
         } else if (this.type == EntityType.ITEM_FRAME) {
            $$8 = new ItemFrame($$6, $$3, $$2);
         } else {
            if (this.type != EntityType.GLOW_ITEM_FRAME) {
               return InteractionResult.SUCCESS;
            }

            $$8 = new GlowItemFrame($$6, $$3, $$2);
         }

         EntityType.createDefaultStackConfig($$6, $$5, $$4).accept($$8);
         if ($$8.survives()) {
            if (!$$6.isClientSide()) {
               $$8.playPlacementSound();
               $$6.gameEvent($$4, GameEvent.ENTITY_PLACE, $$8.position());
               $$6.addFreshEntity($$8);
            }

            $$5.shrink(1);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.CONSUME;
         }
      }
   }

   protected boolean mayPlace(Player $$0, Direction $$1, net.minecraft.world.item.ItemStack $$2, BlockPos $$3) {
      return !$$1.getAxis().isVertical() && $$0.mayUseItemAt($$3, $$1, $$2);
   }

   @Override
   public void appendHoverText(
      net.minecraft.world.item.ItemStack $$0,
      net.minecraft.world.item.Item.TooltipContext $$1,
      TooltipDisplay $$2,
      Consumer<Component> $$3,
      net.minecraft.world.item.TooltipFlag $$4
   ) {
      if (this.type == EntityType.PAINTING && $$2.shows(DataComponents.PAINTING_VARIANT)) {
         Holder<PaintingVariant> $$5 = (Holder<PaintingVariant>)$$0.get(DataComponents.PAINTING_VARIANT);
         if ($$5 != null) {
            ((PaintingVariant)$$5.value()).title().ifPresent($$3);
            ((PaintingVariant)$$5.value()).author().ifPresent($$3);
            $$3.accept(
               Component.translatable("painting.dimensions", new Object[]{((PaintingVariant)$$5.value()).width(), ((PaintingVariant)$$5.value()).height()})
            );
         } else if ($$4.isCreative()) {
            $$3.accept(TOOLTIP_RANDOM_VARIANT);
         }
      }
   }
}
