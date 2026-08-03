package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class BottleItem extends net.minecraft.world.item.Item {
   public BottleItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      List<AreaEffectCloud> $$3 = $$0.getEntitiesOfClass(
         AreaEffectCloud.class, $$1.getBoundingBox().inflate(2.0), $$0x -> $$0x.isAlive() && $$0x.getOwner() instanceof EnderDragon
      );
      net.minecraft.world.item.ItemStack $$4 = $$1.getItemInHand($$2);
      if (!$$3.isEmpty()) {
         AreaEffectCloud $$5 = $$3.get(0);
         $$5.setRadius($$5.getRadius() - 0.5F);
         $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
         $$0.gameEvent($$1, GameEvent.FLUID_PICKUP, $$1.position());
         if ($$1 instanceof ServerPlayer $$6) {
            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger($$6, $$4, $$5);
         }

         return InteractionResult.SUCCESS
            .heldItemTransformedTo(this.turnBottleIntoItem($$4, $$1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH)));
      } else {
         BlockHitResult $$7 = getPlayerPOVHitResult($$0, $$1, Fluid.SOURCE_ONLY);
         if ($$7.getType() == Type.MISS) {
            return InteractionResult.PASS;
         } else {
            if ($$7.getType() == Type.BLOCK) {
               BlockPos $$8 = $$7.getBlockPos();
               if (!$$0.mayInteract($$1, $$8)) {
                  return InteractionResult.PASS;
               }

               if ($$0.getFluidState($$8).is(FluidTags.WATER)) {
                  $$0.playSound($$1, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                  $$0.gameEvent($$1, GameEvent.FLUID_PICKUP, $$8);
                  return InteractionResult.SUCCESS
                     .heldItemTransformedTo(
                        this.turnBottleIntoItem($$4, $$1, PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, Potions.WATER))
                     );
               }
            }

            return InteractionResult.PASS;
         }
      }
   }

   protected net.minecraft.world.item.ItemStack turnBottleIntoItem(net.minecraft.world.item.ItemStack $$0, Player $$1, net.minecraft.world.item.ItemStack $$2) {
      $$1.awardStat(Stats.ITEM_USED.get(this));
      return net.minecraft.world.item.ItemUtils.createFilledResult($$0, $$1, $$2);
   }
}
