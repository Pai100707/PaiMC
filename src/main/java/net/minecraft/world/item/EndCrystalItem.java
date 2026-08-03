package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class EndCrystalItem extends net.minecraft.world.item.Item {
   public EndCrystalItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockState $$3 = $$1.getBlockState($$2);
      if (!$$3.is(Blocks.OBSIDIAN) && !$$3.is(Blocks.BEDROCK)) {
         return InteractionResult.FAIL;
      } else {
         BlockPos $$4 = $$2.above();
         if (!$$1.isEmptyBlock($$4)) {
            return InteractionResult.FAIL;
         } else {
            double $$5 = $$4.getX();
            double $$6 = $$4.getY();
            double $$7 = $$4.getZ();
            List<Entity> $$8 = $$1.getEntities(null, new AABB($$5, $$6, $$7, $$5 + 1.0, $$6 + 2.0, $$7 + 1.0));
            if (!$$8.isEmpty()) {
               return InteractionResult.FAIL;
            } else {
               if ($$1 instanceof ServerLevel) {
                  EndCrystal $$9 = new EndCrystal($$1, $$5 + 0.5, $$6, $$7 + 0.5);
                  $$9.setShowBottom(false);
                  $$1.addFreshEntity($$9);
                  $$1.gameEvent($$0.getPlayer(), GameEvent.ENTITY_PLACE, $$4);
                  EndDragonFight $$10 = ((ServerLevel)$$1).getDragonFight();
                  if ($$10 != null) {
                     $$10.tryRespawn();
                  }
               }

               $$0.getItemInHand().shrink(1);
               return InteractionResult.SUCCESS;
            }
         }
      }
   }
}
