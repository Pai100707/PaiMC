package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;

public record RaiderPredicate(boolean hasRaid, boolean isCaptain) implements EntitySubPredicate {
   public static final MapCodec<RaiderPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.BOOL.optionalFieldOf("has_raid", false).forGetter(RaiderPredicate::hasRaid),
            Codec.BOOL.optionalFieldOf("is_captain", false).forGetter(RaiderPredicate::isCaptain)
         )
         .apply($$0, RaiderPredicate::new)
   );
   public static final RaiderPredicate CAPTAIN_WITHOUT_RAID = new RaiderPredicate(false, true);

   @Override
   public MapCodec<RaiderPredicate> codec() {
      return EntitySubPredicates.RAIDER;
   }

   @Override
   public boolean matches(Entity $$0, ServerLevel $$1, Vec3 $$2) {
      return !($$0 instanceof Raider $$3) ? false : $$3.hasRaid() == this.hasRaid && $$3.isCaptain() == this.isCaptain;
   }
}
