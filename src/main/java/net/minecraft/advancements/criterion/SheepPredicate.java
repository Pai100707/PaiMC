package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.phys.Vec3;

public record SheepPredicate(Optional<Boolean> sheared) implements EntitySubPredicate {
   public static final MapCodec<SheepPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.BOOL.optionalFieldOf("sheared").forGetter(SheepPredicate::sheared)).apply($$0, SheepPredicate::new)
   );

   @Override
   public MapCodec<SheepPredicate> codec() {
      return EntitySubPredicates.SHEEP;
   }

   @Override
   public boolean matches(Entity $$0, ServerLevel $$1, Vec3 $$2) {
      return $$0 instanceof Sheep $$3 ? !this.sheared.isPresent() || $$3.isSheared() == this.sheared.get() : false;
   }

   public static SheepPredicate hasWool() {
      return new SheepPredicate(Optional.of(false));
   }
}
