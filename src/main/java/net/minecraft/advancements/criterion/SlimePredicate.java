package net.minecraft.advancements.criterion;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate {
   public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("size", MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size)).apply($$0, SlimePredicate::new)
   );

   public static SlimePredicate sized(MinMaxBounds.Ints $$0) {
      return new SlimePredicate($$0);
   }

   @Override
   public boolean matches(Entity $$0, ServerLevel $$1, @Nullable Vec3 $$2) {
      return $$0 instanceof Slime $$3 ? this.size.matches($$3.getSize()) : false;
   }

   @Override
   public MapCodec<SlimePredicate> codec() {
      return EntitySubPredicates.SLIME;
   }
}
