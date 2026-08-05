package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public record FishingHookPredicate(Optional<Boolean> inOpenWater) implements EntitySubPredicate {
   public static final FishingHookPredicate ANY = new FishingHookPredicate(Optional.empty());
   public static final MapCodec<FishingHookPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.BOOL.optionalFieldOf("in_open_water").forGetter(FishingHookPredicate::inOpenWater)).apply($$0, FishingHookPredicate::new)
   );

   public static FishingHookPredicate inOpenWater(boolean $$0) {
      return new FishingHookPredicate(Optional.of($$0));
   }

   @Override
   public MapCodec<FishingHookPredicate> codec() {
      return EntitySubPredicates.FISHING_HOOK;
   }

   @Override
   public boolean matches(Entity $$0, ServerLevel $$1, Vec3 $$2) {
      if (this.inOpenWater.isEmpty()) {
         return true;
      } else {
         return $$0 instanceof FishingHook $$3 ? this.inOpenWater.get() == $$3.isOpenWaterFishing() : false;
      }
   }
}
