package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketItem extends net.minecraft.world.item.Item implements net.minecraft.world.item.ProjectileItem {
   public static final byte[] CRAFTABLE_DURATIONS = new byte[]{1, 2, 3};
   public static final double ROCKET_PLACEMENT_OFFSET = 0.15;

   public FireworkRocketItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      Player $$2 = $$0.getPlayer();
      if ($$2 != null && $$2.isFallFlying()) {
         return InteractionResult.PASS;
      } else {
         if ($$1 instanceof ServerLevel $$3) {
            net.minecraft.world.item.ItemStack $$4 = $$0.getItemInHand();
            Vec3 $$5 = $$0.getClickLocation();
            Direction $$6 = $$0.getClickedFace();
            Projectile.spawnProjectile(
               new FireworkRocketEntity($$1, $$0.getPlayer(), $$5.x + $$6.getStepX() * 0.15, $$5.y + $$6.getStepY() * 0.15, $$5.z + $$6.getStepZ() * 0.15, $$4),
               $$3,
               $$4
            );
            $$4.shrink(1);
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      if ($$1.isFallFlying()) {
         net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
         if ($$0 instanceof ServerLevel $$4) {
            if ($$1.dropAllLeashConnections(null)) {
               $$0.playSound(null, $$1, SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            Projectile.spawnProjectile(new FireworkRocketEntity($$0, $$3, $$1), $$4, $$3);
            $$3.consume(1, $$1);
            $$1.awardStat(Stats.ITEM_USED.get(this));
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public Projectile asProjectile(Level $$0, Position $$1, net.minecraft.world.item.ItemStack $$2, Direction $$3) {
      return new FireworkRocketEntity($$0, $$2.copyWithCount(1), $$1.x(), $$1.y(), $$1.z(), true);
   }

   @Override
   public net.minecraft.world.item.ProjectileItem.DispenseConfig createDispenseConfig() {
      return net.minecraft.world.item.ProjectileItem.DispenseConfig.builder()
         .positionFunction(net.minecraft.world.item.FireworkRocketItem::getEntityJustOutsideOfBlockPos)
         .uncertainty(1.0F)
         .power(0.5F)
         .overrideDispenseEvent(1004)
         .build();
   }

   private static Vec3 getEntityJustOutsideOfBlockPos(BlockSource $$0, Direction $$1) {
      return $$0.center().add($$1.getStepX() * 0.5000099999997474, $$1.getStepY() * 0.5000099999997474, $$1.getStepZ() * 0.5000099999997474);
   }
}
