package net.minecraft.advancements.criterion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public record LightningBoltPredicate(MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate {
   public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            MinMaxBounds.Ints.CODEC.optionalFieldOf("blocks_set_on_fire", MinMaxBounds.Ints.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire),
            EntityPredicate.CODEC.optionalFieldOf("entity_struck").forGetter(LightningBoltPredicate::entityStruck)
         )
         .apply($$0, LightningBoltPredicate::new)
   );

   public static LightningBoltPredicate blockSetOnFire(MinMaxBounds.Ints $$0) {
      return new LightningBoltPredicate($$0, Optional.empty());
   }

   @Override
   public MapCodec<LightningBoltPredicate> codec() {
      return EntitySubPredicates.LIGHTNING;
   }

   @Override
   public boolean matches(Entity $$0, ServerLevel $$1, Vec3 $$2) {
      return !($$0 instanceof LightningBolt $$3)
         ? false
         : this.blocksSetOnFire.matches($$3.getBlocksSetOnFire())
            && (this.entityStruck.isEmpty() || $$3.getHitEntities().anyMatch($$2x -> this.entityStruck.get().matches($$1, $$2, $$2x)));
   }
}
