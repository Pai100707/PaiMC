package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record LootItemEntityPropertyCondition(Optional<EntityPredicate> predicate, LootContext.EntityTarget entityTarget) implements LootItemCondition {
   public static final MapCodec<LootItemEntityPropertyCondition> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(LootItemEntityPropertyCondition::predicate),
            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(LootItemEntityPropertyCondition::entityTarget)
         )
         .apply($$0, LootItemEntityPropertyCondition::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.ENTITY_PROPERTIES;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(LootContextParams.ORIGIN, this.entityTarget.contextParam());
   }

   public boolean test(LootContext $$0) {
      Entity $$1 = $$0.getOptionalParameter(this.entityTarget.contextParam());
      Vec3 $$2 = $$0.getOptionalParameter(LootContextParams.ORIGIN);
      return this.predicate.isEmpty() || this.predicate.get().matches($$0.getLevel(), $$2, $$1);
   }

   public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget $$0) {
      return hasProperties($$0, net.minecraft.advancements.criterion.EntityPredicate.Builder.entity());
   }

   public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget $$0, net.minecraft.advancements.criterion.EntityPredicate.Builder $$1) {
      return () -> new LootItemEntityPropertyCondition(Optional.of($$1.build()), $$0);
   }

   public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget $$0, EntityPredicate $$1) {
      return () -> new LootItemEntityPropertyCondition(Optional.of($$1), $$0);
   }
}
