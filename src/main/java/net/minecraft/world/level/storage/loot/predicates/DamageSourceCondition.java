package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record DamageSourceCondition(Optional<DamageSourcePredicate> predicate) implements LootItemCondition {
   public static final MapCodec<DamageSourceCondition> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(DamageSourcePredicate.CODEC.optionalFieldOf("predicate").forGetter(DamageSourceCondition::predicate))
         .apply($$0, DamageSourceCondition::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.ORIGIN, LootContextParams.DAMAGE_SOURCE);
   }

   public boolean test(LootContext $$0) {
      DamageSource $$1 = $$0.getOptionalParameter(LootContextParams.DAMAGE_SOURCE);
      Vec3 $$2 = $$0.getOptionalParameter(LootContextParams.ORIGIN);
      return $$2 != null && $$1 != null ? this.predicate.isEmpty() || this.predicate.get().matches($$0.getLevel(), $$2, $$1) : false;
   }

   public static LootItemCondition.Builder hasDamageSource(net.minecraft.advancements.criterion.DamageSourcePredicate.Builder $$0) {
      return () -> new DamageSourceCondition(Optional.of($$0.build()));
   }
}
