package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public class SpawnEggItem extends net.minecraft.world.item.Item {
   private static final Map<EntityType<?>, net.minecraft.world.item.SpawnEggItem> BY_ID = Maps.newIdentityHashMap();

   public SpawnEggItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
      TypedEntityData<EntityType<?>> $$1 = (TypedEntityData<EntityType<?>>)this.components().get(DataComponents.ENTITY_DATA);
      if ($$1 != null) {
         BY_ID.put($$1.type(), this);
      }
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      if (!($$1 instanceof ServerLevel $$2)) {
         return InteractionResult.SUCCESS;
      } else {
         net.minecraft.world.item.ItemStack $$4 = $$0.getItemInHand();
         BlockPos $$5 = $$0.getClickedPos();
         Direction $$6 = $$0.getClickedFace();
         BlockState $$7 = $$1.getBlockState($$5);
         if ($$1.getBlockEntity($$5) instanceof Spawner $$8) {
            EntityType<?> $$9 = this.getType($$4);
            if ($$9 == null) {
               return InteractionResult.FAIL;
            } else if (!$$2.isSpawnerBlockEnabled()) {
               if ($$0.getPlayer() instanceof ServerPlayer $$10) {
                  $$10.sendSystemMessage(Component.translatable("advMode.notEnabled.spawner"));
               }

               return InteractionResult.FAIL;
            } else {
               $$8.setEntityId($$9, $$1.getRandom());
               $$1.sendBlockUpdated($$5, $$7, $$7, 3);
               $$1.gameEvent($$0.getPlayer(), GameEvent.BLOCK_CHANGE, $$5);
               $$4.shrink(1);
               return InteractionResult.SUCCESS;
            }
         } else {
            BlockPos $$11;
            if ($$7.getCollisionShape($$1, $$5).isEmpty()) {
               $$11 = $$5;
            } else {
               $$11 = $$5.relative($$6);
            }

            return this.spawnMob($$0.getPlayer(), $$4, $$1, $$11, true, !Objects.equals($$5, $$11) && $$6 == Direction.UP);
         }
      }
   }

   private InteractionResult spawnMob(@Nullable LivingEntity $$0, net.minecraft.world.item.ItemStack $$1, Level $$2, BlockPos $$3, boolean $$4, boolean $$5) {
      EntityType<?> $$6 = this.getType($$1);
      if ($$6 == null) {
         return InteractionResult.FAIL;
      } else if (!$$6.isAllowedInPeaceful() && $$2.getDifficulty() == Difficulty.PEACEFUL) {
         return InteractionResult.FAIL;
      } else {
         if ($$6.spawn((ServerLevel)$$2, $$1, $$0, $$3, EntitySpawnReason.SPAWN_ITEM_USE, $$4, $$5) != null) {
            $$1.consume(1, $$0);
            $$2.gameEvent($$0, GameEvent.ENTITY_PLACE, $$3);
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      BlockHitResult $$4 = getPlayerPOVHitResult($$0, $$1, Fluid.SOURCE_ONLY);
      if ($$4.getType() != Type.BLOCK) {
         return InteractionResult.PASS;
      } else if ($$0 instanceof ServerLevel $$5) {
         BlockPos $$7 = $$4.getBlockPos();
         if (!($$0.getBlockState($$7).getBlock() instanceof LiquidBlock)) {
            return InteractionResult.PASS;
         } else if ($$0.mayInteract($$1, $$7) && $$1.mayUseItemAt($$7, $$4.getDirection(), $$3)) {
            InteractionResult $$8 = this.spawnMob($$1, $$3, $$0, $$7, false, false);
            if ($$8 == InteractionResult.SUCCESS) {
               $$1.awardStat(Stats.ITEM_USED.get(this));
            }

            return $$8;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   public boolean spawnsEntity(net.minecraft.world.item.ItemStack $$0, EntityType<?> $$1) {
      return Objects.equals(this.getType($$0), $$1);
   }

   @Nullable
   public static net.minecraft.world.item.SpawnEggItem byId(@Nullable EntityType<?> $$0) {
      return BY_ID.get($$0);
   }

   public static Iterable<net.minecraft.world.item.SpawnEggItem> eggs() {
      return Iterables.unmodifiableIterable(BY_ID.values());
   }

   @Nullable
   public EntityType<?> getType(net.minecraft.world.item.ItemStack $$0) {
      TypedEntityData<EntityType<?>> $$1 = (TypedEntityData<EntityType<?>>)$$0.get(DataComponents.ENTITY_DATA);
      return $$1 != null ? $$1.type() : null;
   }

   @Override
   public FeatureFlagSet requiredFeatures() {
      return Optional.ofNullable((TypedEntityData)this.components().get(DataComponents.ENTITY_DATA))
         .map(TypedEntityData::type)
         .<FeatureFlagSet>map(EntityType::requiredFeatures)
         .orElseGet(FeatureFlagSet::of);
   }

   public Optional<Mob> spawnOffspringFromSpawnEgg(
      Player $$0, Mob $$1, EntityType<? extends Mob> $$2, ServerLevel $$3, Vec3 $$4, net.minecraft.world.item.ItemStack $$5
   ) {
      if (!this.spawnsEntity($$5, $$2)) {
         return Optional.empty();
      } else {
         Mob $$6;
         if ($$1 instanceof AgeableMob) {
            $$6 = ((AgeableMob)$$1).getBreedOffspring($$3, (AgeableMob)$$1);
         } else {
            $$6 = (Mob)$$2.create($$3, EntitySpawnReason.SPAWN_ITEM_USE);
         }

         if ($$6 == null) {
            return Optional.empty();
         } else {
            $$6.setBaby(true);
            if (!$$6.isBaby()) {
               return Optional.empty();
            } else {
               $$6.snapTo($$4.x(), $$4.y(), $$4.z(), 0.0F, 0.0F);
               $$6.applyComponentsFromItemStack($$5);
               $$3.addFreshEntityWithPassengers($$6);
               $$5.consume(1, $$0);
               return Optional.of($$6);
            }
         }
      }
   }

   @Override
   public boolean shouldPrintOpWarning(net.minecraft.world.item.ItemStack $$0, @Nullable Player $$1) {
      if ($$1 != null && $$1.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         TypedEntityData<EntityType<?>> $$2 = (TypedEntityData<EntityType<?>>)$$0.get(DataComponents.ENTITY_DATA);
         if ($$2 != null) {
            return $$2.type().onlyOpCanSetNbt();
         }
      }

      return false;
   }
}
