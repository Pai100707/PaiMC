package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EnchantingTableBlockEntity extends BlockEntity implements Nameable {
   private static final Component DEFAULT_NAME = Component.translatable("container.enchant");
   public int time;
   public float flip;
   public float oFlip;
   public float flipT;
   public float flipA;
   public float open;
   public float oOpen;
   public float rot;
   public float oRot;
   public float tRot;
   private static final RandomSource RANDOM = RandomSource.create();
   
   private Component name;

   public EnchantingTableBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.ENCHANTING_TABLE, $$0, $$1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.name = parseCustomNameSafe($$0, "CustomName");
   }

   public static void bookAnimationTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, EnchantingTableBlockEntity $$3) {
      $$3.oOpen = $$3.open;
      $$3.oRot = $$3.rot;
      Player $$4 = $$0.getNearestPlayer($$1.getX() + 0.5, $$1.getY() + 0.5, $$1.getZ() + 0.5, 3.0, false);
      if ($$4 != null) {
         double $$5 = $$4.getX() - ($$1.getX() + 0.5);
         double $$6 = $$4.getZ() - ($$1.getZ() + 0.5);
         $$3.tRot = (float)Mth.atan2($$6, $$5);
         $$3.open += 0.1F;
         if ($$3.open < 0.5F || RANDOM.nextInt(40) == 0) {
            float $$7 = $$3.flipT;

            do {
               $$3.flipT = $$3.flipT + (RANDOM.nextInt(4) - RANDOM.nextInt(4));
            } while ($$7 == $$3.flipT);
         }
      } else {
         $$3.tRot += 0.02F;
         $$3.open -= 0.1F;
      }

      while ($$3.rot >= (float) Math.PI) {
         $$3.rot -= (float) (Math.PI * 2);
      }

      while ($$3.rot < (float) -Math.PI) {
         $$3.rot += (float) (Math.PI * 2);
      }

      while ($$3.tRot >= (float) Math.PI) {
         $$3.tRot -= (float) (Math.PI * 2);
      }

      while ($$3.tRot < (float) -Math.PI) {
         $$3.tRot += (float) (Math.PI * 2);
      }

      float $$8 = $$3.tRot - $$3.rot;

      while ($$8 >= (float) Math.PI) {
         $$8 -= (float) (Math.PI * 2);
      }

      while ($$8 < (float) -Math.PI) {
         $$8 += (float) (Math.PI * 2);
      }

      $$3.rot += $$8 * 0.4F;
      $$3.open = Mth.clamp($$3.open, 0.0F, 1.0F);
      $$3.time++;
      $$3.oFlip = $$3.flip;
      float $$9 = ($$3.flipT - $$3.flip) * 0.4F;
      float $$10 = 0.2F;
      $$9 = Mth.clamp($$9, -0.2F, 0.2F);
      $$3.flipA = $$3.flipA + ($$9 - $$3.flipA) * 0.9F;
      $$3.flip = $$3.flip + $$3.flipA;
   }

   public Component getName() {
      return this.name != null ? this.name : DEFAULT_NAME;
   }

   public void setCustomName(Component $$0) {
      this.name = $$0;
   }

   
   public Component getCustomName() {
      return this.name;
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.name = (Component)$$0.get(DataComponents.CUSTOM_NAME);
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.CUSTOM_NAME, this.name);
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      $$0.discard("CustomName");
   }
}
