package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record VibrationInfo(
   Holder<GameEvent> gameEvent, float distance, Vec3 pos, UUID uuid, UUID projectileOwnerUuid, Entity entity
) {
   public static final Codec<VibrationInfo> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            GameEvent.CODEC.fieldOf("game_event").forGetter(VibrationInfo::gameEvent),
            Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("distance").forGetter(VibrationInfo::distance),
            Vec3.CODEC.fieldOf("pos").forGetter(VibrationInfo::pos),
            UUIDUtil.CODEC.lenientOptionalFieldOf("source").forGetter($$0x -> Optional.ofNullable($$0x.uuid())),
            UUIDUtil.CODEC.lenientOptionalFieldOf("projectile_owner").forGetter($$0x -> Optional.ofNullable($$0x.projectileOwnerUuid()))
         )
         .apply($$0, ($$0x, $$1, $$2, $$3, $$4) -> new VibrationInfo($$0x, $$1, $$2, (UUID)$$3.orElse(null), (UUID)$$4.orElse(null)))
   );

   public VibrationInfo(Holder<GameEvent> $$0, float $$1, Vec3 $$2, UUID $$3, UUID $$4) {
      this($$0, $$1, $$2, $$3, $$4, null);
   }

   public VibrationInfo(Holder<GameEvent> $$0, float $$1, Vec3 $$2, Entity $$3) {
      this($$0, $$1, $$2, $$3 == null ? null : $$3.getUUID(), getProjectileOwner($$3), $$3);
   }

   
   private static UUID getProjectileOwner(Entity $$0) {
      return $$0 instanceof Projectile $$1 && $$1.getOwner() != null ? $$1.getOwner().getUUID() : null;
   }

   public Optional<Entity> getEntity(ServerLevel $$0) {
      return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map($$0::getEntity));
   }

   public Optional<Entity> getProjectileOwner(ServerLevel $$0) {
      return this.getEntity($$0)
         .filter($$0x -> $$0x instanceof Projectile)
         .map($$0x -> (Projectile)$$0x)
         .<Entity>map(Projectile::getOwner)
         .or(() -> Optional.ofNullable(this.projectileOwnerUuid).map($$0::getEntity));
   }
}
