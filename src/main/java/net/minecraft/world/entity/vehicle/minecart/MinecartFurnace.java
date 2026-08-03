package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MinecartFurnace extends AbstractMinecart {
   private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
   private static final int FUEL_TICKS_PER_ITEM = 3600;
   private static final int MAX_FUEL_TICKS = 32000;
   private static final short DEFAULT_FUEL = 0;
   private static final Vec3 DEFAULT_PUSH = Vec3.ZERO;
   private int fuel = 0;
   public Vec3 push = DEFAULT_PUSH;

   public MinecartFurnace(net.minecraft.world.entity.EntityType<? extends MinecartFurnace> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean isFurnace() {
      return true;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_ID_FUEL, false);
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.fuel > 0) {
            this.fuel--;
         }

         if (this.fuel <= 0) {
            this.push = Vec3.ZERO;
         }

         this.setHasFuel(this.fuel > 0);
      }

      if (this.hasFuel() && this.random.nextInt(4) == 0) {
         this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
      }
   }

   @Override
   protected double getMaxSpeed(ServerLevel $$0) {
      return this.isInWater() ? super.getMaxSpeed($$0) * 0.75 : super.getMaxSpeed($$0) * 0.5;
   }

   @Override
   protected Item getDropItem() {
      return Items.FURNACE_MINECART;
   }

   @Override
   public ItemStack getPickResult() {
      return new ItemStack(Items.FURNACE_MINECART);
   }

   @Override
   protected Vec3 applyNaturalSlowdown(Vec3 $$0) {
      Vec3 $$1;
      if (this.push.lengthSqr() > 1.0E-7) {
         this.push = this.calculateNewPushAlong($$0);
         $$1 = $$0.multiply(0.8, 0.0, 0.8).add(this.push);
         if (this.isInWater()) {
            $$1 = $$1.scale(0.1);
         }
      } else {
         $$1 = $$0.multiply(0.98, 0.0, 0.98);
      }

      return super.applyNaturalSlowdown($$1);
   }

   private Vec3 calculateNewPushAlong(Vec3 $$0) {
      double $$1 = 1.0E-4;
      double $$2 = 0.001;
      return this.push.horizontalDistanceSqr() > 1.0E-4 && $$0.horizontalDistanceSqr() > 0.001
         ? this.push.projectedOn($$0).normalize().scale(this.push.length())
         : this.push;
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if (this.addFuel($$0.position(), $$2)) {
         $$2.consume(1, $$0);
      }

      return InteractionResult.SUCCESS;
   }

   public boolean addFuel(Vec3 $$0, ItemStack $$1) {
      if ($$1.is(ItemTags.FURNACE_MINECART_FUEL) && this.fuel + 3600 <= 32000) {
         this.fuel += 3600;
         if (this.fuel > 0) {
            this.push = this.position().subtract($$0).horizontal();
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putDouble("PushX", this.push.x);
      $$0.putDouble("PushZ", this.push.z);
      $$0.putShort("Fuel", (short)this.fuel);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      double $$1 = $$0.getDoubleOr("PushX", DEFAULT_PUSH.x);
      double $$2 = $$0.getDoubleOr("PushZ", DEFAULT_PUSH.z);
      this.push = new Vec3($$1, 0.0, $$2);
      this.fuel = $$0.getShortOr("Fuel", (short)0);
   }

   protected boolean hasFuel() {
      return (Boolean)this.entityData.get(DATA_ID_FUEL);
   }

   protected void setHasFuel(boolean $$0) {
      this.entityData.set(DATA_ID_FUEL, $$0);
   }

   @Override
   public BlockState getDefaultDisplayBlockState() {
      return (BlockState)((BlockState)Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH))
         .setValue(FurnaceBlock.LIT, this.hasFuel());
   }
}
