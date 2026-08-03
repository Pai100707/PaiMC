package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record LocationCheck(Optional<LocationPredicate> predicate, BlockPos offset) implements LootItemCondition {
   private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
            Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
            Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)
         )
         .apply($$0, BlockPos::new)
   );
   public static final MapCodec<LocationCheck> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheck::predicate), OFFSET_CODEC.forGetter(LocationCheck::offset))
         .apply($$0, LocationCheck::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.LOCATION_CHECK;
   }

   public boolean test(LootContext $$0) {
      Vec3 $$1 = $$0.getOptionalParameter(LootContextParams.ORIGIN);
      return $$1 != null
         && (
            this.predicate.isEmpty()
               || this.predicate.get().matches($$0.getLevel(), $$1.x() + this.offset.getX(), $$1.y() + this.offset.getY(), $$1.z() + this.offset.getZ())
         );
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.ORIGIN);
   }

   public static LootItemCondition.Builder checkLocation(net.minecraft.advancements.criterion.LocationPredicate.Builder $$0) {
      return () -> new LocationCheck(Optional.of($$0.build()), BlockPos.ZERO);
   }

   public static LootItemCondition.Builder checkLocation(net.minecraft.advancements.criterion.LocationPredicate.Builder $$0, BlockPos $$1) {
      return () -> new LocationCheck(Optional.of($$0.build()), $$1);
   }
}
