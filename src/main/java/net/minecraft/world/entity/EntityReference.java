package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EntityReference<StoredEntityType extends UniquelyIdentifyable> {
   private static final Codec<? extends net.minecraft.world.entity.EntityReference<?>> CODEC = UUIDUtil.CODEC
      .xmap(net.minecraft.world.entity.EntityReference::new, net.minecraft.world.entity.EntityReference::getUUID);
   private static final StreamCodec<ByteBuf, ? extends net.minecraft.world.entity.EntityReference<?>> STREAM_CODEC = UUIDUtil.STREAM_CODEC
      .map(net.minecraft.world.entity.EntityReference::new, net.minecraft.world.entity.EntityReference::getUUID);
   private Either<UUID, StoredEntityType> entity;

   public static <Type extends UniquelyIdentifyable> Codec<net.minecraft.world.entity.EntityReference<Type>> codec() {
      return (Codec<net.minecraft.world.entity.EntityReference<Type>>)CODEC;
   }

   public static <Type extends UniquelyIdentifyable> StreamCodec<ByteBuf, net.minecraft.world.entity.EntityReference<Type>> streamCodec() {
      return (StreamCodec<ByteBuf, net.minecraft.world.entity.EntityReference<Type>>)STREAM_CODEC;
   }

   private EntityReference(StoredEntityType $$0) {
      this.entity = Either.right($$0);
   }

   private EntityReference(UUID $$0) {
      this.entity = Either.left($$0);
   }

   
   public static <T extends UniquelyIdentifyable> net.minecraft.world.entity.EntityReference<T> of(T $$0) {
      return $$0 != null ? new net.minecraft.world.entity.EntityReference<>($$0) : null;
   }

   public static <T extends UniquelyIdentifyable> net.minecraft.world.entity.EntityReference<T> of(UUID $$0) {
      return new net.minecraft.world.entity.EntityReference<>($$0);
   }

   public UUID getUUID() {
      return (UUID)this.entity.map($$0 -> $$0, UniquelyIdentifyable::getUUID);
   }

   
   public StoredEntityType getEntity(UUIDLookup<? extends UniquelyIdentifyable> $$0, Class<StoredEntityType> $$1) {
      Optional<StoredEntityType> $$2 = this.entity.right();
      if ($$2.isPresent()) {
         StoredEntityType $$3 = $$2.get();
         if (!$$3.isRemoved()) {
            return $$3;
         }

         this.entity = Either.left($$3.getUUID());
      }

      Optional<UUID> $$4 = this.entity.left();
      if ($$4.isPresent()) {
         StoredEntityType $$5 = this.resolve($$0.lookup($$4.get()), $$1);
         if ($$5 != null && !$$5.isRemoved()) {
            this.entity = Either.right($$5);
            return $$5;
         }
      }

      return null;
   }

   
   public StoredEntityType getEntity(Level $$0, Class<StoredEntityType> $$1) {
      return Player.class.isAssignableFrom($$1) ? this.getEntity($$0::getPlayerInAnyDimension, $$1) : this.getEntity($$0::getEntityInAnyDimension, $$1);
   }

   
   private StoredEntityType resolve(UniquelyIdentifyable $$0, Class<StoredEntityType> $$1) {
      return $$0 != null && $$1.isAssignableFrom($$0.getClass()) ? $$1.cast($$0) : null;
   }

   public boolean matches(StoredEntityType $$0) {
      return this.getUUID().equals($$0.getUUID());
   }

   public void store(ValueOutput $$0, String $$1) {
      $$0.store($$1, UUIDUtil.CODEC, this.getUUID());
   }

   public static void store(net.minecraft.world.entity.EntityReference<?> $$0, ValueOutput $$1, String $$2) {
      if ($$0 != null) {
         $$0.store($$1, $$2);
      }
   }

   
   public static <StoredEntityType extends UniquelyIdentifyable> StoredEntityType get(
      net.minecraft.world.entity.EntityReference<StoredEntityType> $$0, Level $$1, Class<StoredEntityType> $$2
   ) {
      return $$0 != null ? $$0.getEntity($$1, $$2) : null;
   }

   
   public static net.minecraft.world.entity.Entity getEntity(
      net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.Entity> $$0, Level $$1
   ) {
      return get($$0, $$1, net.minecraft.world.entity.Entity.class);
   }

   
   public static net.minecraft.world.entity.LivingEntity getLivingEntity(
      net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> $$0, Level $$1
   ) {
      return get($$0, $$1, net.minecraft.world.entity.LivingEntity.class);
   }

   
   public static Player getPlayer(net.minecraft.world.entity.EntityReference<Player> $$0, Level $$1) {
      return get($$0, $$1, Player.class);
   }

   
   public static <StoredEntityType extends UniquelyIdentifyable> net.minecraft.world.entity.EntityReference<StoredEntityType> read(ValueInput $$0, String $$1) {
      return (net.minecraft.world.entity.EntityReference<StoredEntityType>)$$0.read($$1, codec()).orElse(null);
   }

   
   public static <StoredEntityType extends UniquelyIdentifyable> net.minecraft.world.entity.EntityReference<StoredEntityType> readWithOldOwnerConversion(
      ValueInput $$0, String $$1, Level $$2
   ) {
      Optional<UUID> $$3 = $$0.read($$1, UUIDUtil.CODEC);
      return $$3.isPresent()
         ? of($$3.get())
         : $$0.getString($$1)
            .map($$1x -> OldUsersConverter.convertMobOwnerIfNecessary($$2.getServer(), $$1x))
            .map(net.minecraft.world.entity.EntityReference::new)
            .orElse(null);
   }

   @Override
   public boolean equals(Object $$0) {
      return $$0 == this ? true : $$0 instanceof net.minecraft.world.entity.EntityReference<?> $$1 && this.getUUID().equals($$1.getUUID());
   }

   @Override
   public int hashCode() {
      return this.getUUID().hashCode();
   }
}
