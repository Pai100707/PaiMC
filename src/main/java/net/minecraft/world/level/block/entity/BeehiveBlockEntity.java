package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.debug.DebugHiveInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource.Registration;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class BeehiveBlockEntity extends BlockEntity {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String TAG_FLOWER_POS = "flower_pos";
   private static final String BEES = "bees";
   static final List<String> IGNORED_BEE_TAGS = Arrays.asList(
      "Air",
      "drop_chances",
      "equipment",
      "Brain",
      "CanPickUpLoot",
      "DeathTime",
      "fall_distance",
      "FallFlying",
      "Fire",
      "HurtByTimestamp",
      "HurtTime",
      "LeftHanded",
      "Motion",
      "NoGravity",
      "OnGround",
      "PortalCooldown",
      "Pos",
      "Rotation",
      "sleeping_pos",
      "CannotEnterHiveTicks",
      "TicksSincePollination",
      "CropsGrownSincePollination",
      "hive_pos",
      "Passengers",
      "leash",
      "UUID"
   );
   public static final int MAX_OCCUPANTS = 3;
   private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
   private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
   public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
   private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
   @Nullable
   private BlockPos savedFlowerPos;

   public BeehiveBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.BEEHIVE, $$0, $$1);
   }

   @Override
   public void setChanged() {
      if (this.isFireNearby()) {
         this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
      }

      super.setChanged();
   }

   public boolean isFireNearby() {
      if (this.level == null) {
         return false;
      } else {
         for (BlockPos $$0 : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
            if (this.level.getBlockState($$0).getBlock() instanceof FireBlock) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isEmpty() {
      return this.stored.isEmpty();
   }

   public boolean isFull() {
      return this.stored.size() == 3;
   }

   public void emptyAllLivingFromHive(@Nullable Player $$0, BlockState $$1, BeehiveBlockEntity.BeeReleaseStatus $$2) {
      List<Entity> $$3 = this.releaseAllOccupants($$1, $$2);
      if ($$0 != null) {
         for (Entity $$4 : $$3) {
            if ($$4 instanceof Bee $$5 && $$0.position().distanceToSqr($$4.position()) <= 16.0) {
               if (!this.isSedated()) {
                  $$5.setTarget($$0);
               } else {
                  $$5.setStayOutOfHiveCountdown(400);
               }
            }
         }
      }
   }

   private List<Entity> releaseAllOccupants(BlockState $$0, BeehiveBlockEntity.BeeReleaseStatus $$1) {
      List<Entity> $$2 = Lists.newArrayList();
      this.stored.removeIf($$3 -> releaseOccupant(this.level, this.worldPosition, $$0, $$3.toOccupant(), $$2, $$1, this.savedFlowerPos));
      if (!$$2.isEmpty()) {
         super.setChanged();
      }

      return $$2;
   }

   @VisibleForDebug
   public int getOccupantCount() {
      return this.stored.size();
   }

   public static int getHoneyLevel(BlockState $$0) {
      return $$0.getValue(BeehiveBlock.HONEY_LEVEL);
   }

   @VisibleForDebug
   public boolean isSedated() {
      return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
   }

   public void addOccupant(Bee $$0) {
      if (this.stored.size() < 3) {
         $$0.stopRiding();
         $$0.ejectPassengers();
         $$0.dropLeash();
         this.storeBee(BeehiveBlockEntity.Occupant.of($$0));
         if (this.level != null) {
            if ($$0.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
               this.savedFlowerPos = $$0.getSavedFlowerPos();
            }

            BlockPos $$1 = this.getBlockPos();
            this.level.playSound(null, (double)$$1.getX(), (double)$$1.getY(), (double)$$1.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, $$1, GameEvent.Context.of($$0, this.getBlockState()));
         }

         $$0.discard();
         super.setChanged();
      }
   }

   public void storeBee(BeehiveBlockEntity.Occupant $$0) {
      this.stored.add(new BeehiveBlockEntity.BeeData($$0));
   }

   private static boolean releaseOccupant(
      net.minecraft.world.level.Level $$0,
      BlockPos $$1,
      BlockState $$2,
      BeehiveBlockEntity.Occupant $$3,
      @Nullable List<Entity> $$4,
      BeehiveBlockEntity.BeeReleaseStatus $$5,
      @Nullable BlockPos $$6
   ) {
      if ((Boolean)$$0.environmentAttributes().getValue(EnvironmentAttributes.BEES_STAY_IN_HIVE, $$1) && $$5 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
         return false;
      } else {
         Direction $$7 = $$2.getValue(BeehiveBlock.FACING);
         BlockPos $$8 = $$1.relative($$7);
         boolean $$9 = !$$0.getBlockState($$8).getCollisionShape($$0, $$8).isEmpty();
         if ($$9 && $$5 != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
         } else {
            Entity $$10 = $$3.createEntity($$0, $$1);
            if ($$10 != null) {
               if ($$10 instanceof Bee $$11) {
                  if ($$6 != null && !$$11.hasSavedFlowerPos() && $$0.random.nextFloat() < 0.9F) {
                     $$11.setSavedFlowerPos($$6);
                  }

                  if ($$5 == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                     $$11.dropOffNectar();
                     if ($$2.is(BlockTags.BEEHIVES, $$0x -> $$0x.hasProperty(BeehiveBlock.HONEY_LEVEL))) {
                        int $$12 = getHoneyLevel($$2);
                        if ($$12 < 5) {
                           int $$13 = $$0.random.nextInt(100) == 0 ? 2 : 1;
                           if ($$12 + $$13 > 5) {
                              $$13--;
                           }

                           $$0.setBlockAndUpdate($$1, $$2.setValue(BeehiveBlock.HONEY_LEVEL, $$12 + $$13));
                        }
                     }
                  }

                  if ($$4 != null) {
                     $$4.add($$11);
                  }

                  float $$14 = $$10.getBbWidth();
                  double $$15 = $$9 ? 0.0 : 0.55 + $$14 / 2.0F;
                  double $$16 = $$1.getX() + 0.5 + $$15 * $$7.getStepX();
                  double $$17 = $$1.getY() + 0.5 - $$10.getBbHeight() / 2.0F;
                  double $$18 = $$1.getZ() + 0.5 + $$15 * $$7.getStepZ();
                  $$10.snapTo($$16, $$17, $$18, $$10.getYRot(), $$10.getXRot());
               }

               $$0.playSound(null, $$1, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
               $$0.gameEvent(GameEvent.BLOCK_CHANGE, $$1, GameEvent.Context.of($$10, $$0.getBlockState($$1)));
               return $$0.addFreshEntity($$10);
            } else {
               return false;
            }
         }
      }
   }

   private boolean hasSavedFlowerPos() {
      return this.savedFlowerPos != null;
   }

   private static void tickOccupants(
      net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, List<BeehiveBlockEntity.BeeData> $$3, @Nullable BlockPos $$4
   ) {
      boolean $$5 = false;
      Iterator<BeehiveBlockEntity.BeeData> $$6 = $$3.iterator();

      while ($$6.hasNext()) {
         BeehiveBlockEntity.BeeData $$7 = $$6.next();
         if ($$7.tick()) {
            BeehiveBlockEntity.BeeReleaseStatus $$8 = $$7.hasNectar()
               ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
               : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
            if (releaseOccupant($$0, $$1, $$2, $$7.toOccupant(), null, $$8, $$4)) {
               $$5 = true;
               $$6.remove();
            }
         }
      }

      if ($$5) {
         setChanged($$0, $$1, $$2);
      }
   }

   public static void serverTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, BeehiveBlockEntity $$3) {
      tickOccupants($$0, $$1, $$2, $$3.stored, $$3.savedFlowerPos);
      if (!$$3.stored.isEmpty() && $$0.getRandom().nextDouble() < 0.005) {
         double $$4 = $$1.getX() + 0.5;
         double $$5 = $$1.getY();
         double $$6 = $$1.getZ() + 0.5;
         $$0.playSound(null, $$4, $$5, $$6, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.stored.clear();
      $$0.<List>read("bees", BeehiveBlockEntity.Occupant.LIST_CODEC).orElse(List.of()).forEach(this::storeBee);
      this.savedFlowerPos = $$0.<BlockPos>read("flower_pos", BlockPos.CODEC).orElse(null);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.store("bees", BeehiveBlockEntity.Occupant.LIST_CODEC, this.getBees());
      $$0.storeNullable("flower_pos", BlockPos.CODEC, this.savedFlowerPos);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.stored.clear();
      List<BeehiveBlockEntity.Occupant> $$1 = ((Bees)$$0.getOrDefault(DataComponents.BEES, Bees.EMPTY)).bees();
      $$1.forEach(this::storeBee);
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.BEES, new Bees(this.getBees()));
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      super.removeComponentsFromTag($$0);
      $$0.discard("bees");
   }

   private List<BeehiveBlockEntity.Occupant> getBees() {
      return this.stored.stream().map(BeehiveBlockEntity.BeeData::toOccupant).toList();
   }

   @Override
   public void registerDebugValues(ServerLevel $$0, Registration $$1) {
      $$1.register(DebugSubscriptions.BEE_HIVES, () -> DebugHiveInfo.pack(this));
   }

   static class BeeData {
      private final BeehiveBlockEntity.Occupant occupant;
      private int ticksInHive;

      BeeData(BeehiveBlockEntity.Occupant $$0) {
         this.occupant = $$0;
         this.ticksInHive = $$0.ticksInHive();
      }

      public boolean tick() {
         return this.ticksInHive++ > this.occupant.minTicksInHive;
      }

      public BeehiveBlockEntity.Occupant toOccupant() {
         return new BeehiveBlockEntity.Occupant(this.occupant.entityData, this.ticksInHive, this.occupant.minTicksInHive);
      }

      public boolean hasNectar() {
         return this.occupant.entityData.getUnsafe().getBooleanOr("HasNectar", false);
      }
   }

   public static enum BeeReleaseStatus {
      HONEY_DELIVERED,
      BEE_RELEASED,
      EMERGENCY;
   }

   public record Occupant(TypedEntityData<EntityType<?>> entityData, int ticksInHive, int minTicksInHive) {
      public static final Codec<BeehiveBlockEntity.Occupant> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               TypedEntityData.codec(EntityType.CODEC).fieldOf("entity_data").forGetter(BeehiveBlockEntity.Occupant::entityData),
               Codec.INT.fieldOf("ticks_in_hive").forGetter(BeehiveBlockEntity.Occupant::ticksInHive),
               Codec.INT.fieldOf("min_ticks_in_hive").forGetter(BeehiveBlockEntity.Occupant::minTicksInHive)
            )
            .apply($$0, BeehiveBlockEntity.Occupant::new)
      );
      public static final Codec<List<BeehiveBlockEntity.Occupant>> LIST_CODEC = CODEC.listOf();
      public static final StreamCodec<RegistryFriendlyByteBuf, BeehiveBlockEntity.Occupant> STREAM_CODEC = StreamCodec.composite(
         TypedEntityData.streamCodec(EntityType.STREAM_CODEC),
         BeehiveBlockEntity.Occupant::entityData,
         ByteBufCodecs.VAR_INT,
         BeehiveBlockEntity.Occupant::ticksInHive,
         ByteBufCodecs.VAR_INT,
         BeehiveBlockEntity.Occupant::minTicksInHive,
         BeehiveBlockEntity.Occupant::new
      );

      public static BeehiveBlockEntity.Occupant of(Entity $$0) {
         ScopedCollector $$1 = new ScopedCollector($$0.problemPath(), BeehiveBlockEntity.LOGGER);

         BeehiveBlockEntity.Occupant var5;
         try {
            TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0.registryAccess());
            $$0.save($$2);
            BeehiveBlockEntity.IGNORED_BEE_TAGS.forEach($$2::discard);
            CompoundTag $$3 = $$2.buildResult();
            boolean $$4 = $$3.getBooleanOr("HasNectar", false);
            var5 = new BeehiveBlockEntity.Occupant(TypedEntityData.of($$0.getType(), $$3), 0, $$4 ? 2400 : 600);
         } catch (Throwable var7) {
            try {
               $$1.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         $$1.close();
         return var5;
      }

      public static BeehiveBlockEntity.Occupant create(int $$0) {
         return new BeehiveBlockEntity.Occupant(TypedEntityData.of(EntityType.BEE, new CompoundTag()), $$0, 600);
      }

      @Nullable
      public Entity createEntity(net.minecraft.world.level.Level $$0, BlockPos $$1) {
         CompoundTag $$2 = this.entityData.copyTagWithoutId();
         BeehiveBlockEntity.IGNORED_BEE_TAGS.forEach($$2::remove);
         Entity $$3 = EntityType.loadEntityRecursive((EntityType)this.entityData.type(), $$2, $$0, EntitySpawnReason.LOAD, EntityProcessor.NOP);
         if ($$3 != null && $$3.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
            $$3.setNoGravity(true);
            if ($$3 instanceof Bee $$4) {
               $$4.setHivePos($$1);
               setBeeReleaseData(this.ticksInHive, $$4);
            }

            return $$3;
         } else {
            return null;
         }
      }

      private static void setBeeReleaseData(int $$0, Bee $$1) {
         int $$2 = $$1.getAge();
         if ($$2 < 0) {
            $$1.setAge(Math.min(0, $$2 + $$0));
         } else if ($$2 > 0) {
            $$1.setAge(Math.max(0, $$2 - $$0));
         }

         $$1.setInLoveTime(Math.max(0, $$1.getInLoveTime() - $$0));
      }
   }
}
