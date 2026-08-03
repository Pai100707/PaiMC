package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends net.minecraft.world.item.Item {
   public PotionItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public net.minecraft.world.item.ItemStack getDefaultInstance() {
      net.minecraft.world.item.ItemStack $$0 = super.getDefaultInstance();
      $$0.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
      return $$0;
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      Player $$3 = $$0.getPlayer();
      net.minecraft.world.item.ItemStack $$4 = $$0.getItemInHand();
      PotionContents $$5 = (PotionContents)$$4.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      BlockState $$6 = $$1.getBlockState($$2);
      if ($$0.getClickedFace() != Direction.DOWN && $$6.is(BlockTags.CONVERTABLE_TO_MUD) && $$5.is(Potions.WATER)) {
         $$1.playSound(null, $$2, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$3.setItemInHand(
            $$0.getHand(),
            net.minecraft.world.item.ItemUtils.createFilledResult($$4, $$3, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE))
         );
         if (!$$1.isClientSide()) {
            ServerLevel $$7 = (ServerLevel)$$1;

            for (int $$8 = 0; $$8 < 5; $$8++) {
               $$7.sendParticles(
                  ParticleTypes.SPLASH, $$2.getX() + $$1.random.nextDouble(), $$2.getY() + 1, $$2.getZ() + $$1.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0
               );
            }
         }

         $$1.playSound(null, $$2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$1.gameEvent(null, GameEvent.FLUID_PLACE, $$2);
         $$1.setBlockAndUpdate($$2, Blocks.MUD.defaultBlockState());
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public Component getName(net.minecraft.world.item.ItemStack $$0) {
      PotionContents $$1 = (PotionContents)$$0.get(DataComponents.POTION_CONTENTS);
      return $$1 != null ? $$1.getName(this.descriptionId + ".effect.") : super.getName($$0);
   }
}
