package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
   public static final MapCodec<EntityPositionSource> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid),
            Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter($$0x -> $$0x.yOffset)
         )
         .apply($$0, ($$0x, $$1) -> new EntityPositionSource(Either.right(Either.left($$0x)), $$1))
   );
   public static final StreamCodec<ByteBuf, EntityPositionSource> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      EntityPositionSource::getId,
      ByteBufCodecs.FLOAT,
      $$0 -> $$0.yOffset,
      ($$0, $$1) -> new EntityPositionSource(Either.right(Either.right($$0)), $$1)
   );
   private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
   private final float yOffset;

   public EntityPositionSource(Entity $$0, float $$1) {
      this(Either.left($$0), $$1);
   }

   private EntityPositionSource(Either<Entity, Either<UUID, Integer>> $$0, float $$1) {
      this.entityOrUuidOrId = $$0;
      this.yOffset = $$1;
   }

   @Override
   public Optional<Vec3> getPosition(net.minecraft.world.level.Level $$0) {
      if (this.entityOrUuidOrId.left().isEmpty()) {
         this.resolveEntity($$0);
      }

      return this.entityOrUuidOrId.left().map($$0x -> $$0x.position().add(0.0, this.yOffset, 0.0));
   }

   private void resolveEntity(net.minecraft.world.level.Level $$0) {
      ((Optional)this.entityOrUuidOrId
            .map(Optional::of, $$1 -> Optional.ofNullable((Entity)$$1.map($$1x -> $$0 instanceof ServerLevel $$2 ? $$2.getEntity($$1x) : null, $$0::getEntity))))
         .ifPresent($$0x -> this.entityOrUuidOrId = Either.left($$0x));
   }

   public UUID getUuid() {
      return (UUID)this.entityOrUuidOrId.map(Entity::getUUID, $$0 -> (UUID)$$0.map(Function.identity(), $$0x -> {
         throw new RuntimeException("Unable to get entityId from uuid");
      }));
   }

   private int getId() {
      return (Integer)this.entityOrUuidOrId.map(Entity::getId, $$0 -> (Integer)$$0.map($$0x -> {
         throw new IllegalStateException("Unable to get entityId from uuid");
      }, Function.identity()));
   }

   @Override
   public PositionSourceType<EntityPositionSource> getType() {
      return PositionSourceType.ENTITY;
   }

   public static class Type implements PositionSourceType<EntityPositionSource> {
      @Override
      public MapCodec<EntityPositionSource> codec() {
         return EntityPositionSource.CODEC;
      }

      @Override
      public StreamCodec<ByteBuf, EntityPositionSource> streamCodec() {
         return EntityPositionSource.STREAM_CODEC;
      }
   }
}
