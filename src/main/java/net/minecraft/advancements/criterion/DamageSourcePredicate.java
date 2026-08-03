package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public record DamageSourcePredicate(
   List<TagPredicate<DamageType>> tags, Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity, Optional<Boolean> isDirect
) {
   public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            TagPredicate.codec(Registries.DAMAGE_TYPE).listOf().optionalFieldOf("tags", List.of()).forGetter(DamageSourcePredicate::tags),
            EntityPredicate.CODEC.optionalFieldOf("direct_entity").forGetter(DamageSourcePredicate::directEntity),
            EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamageSourcePredicate::sourceEntity),
            Codec.BOOL.optionalFieldOf("is_direct").forGetter(DamageSourcePredicate::isDirect)
         )
         .apply($$0, DamageSourcePredicate::new)
   );

   public boolean matches(ServerPlayer $$0, DamageSource $$1) {
      return this.matches($$0.level(), $$0.position(), $$1);
   }

   public boolean matches(ServerLevel $$0, Vec3 $$1, DamageSource $$2) {
      for (TagPredicate<DamageType> $$3 : this.tags) {
         if (!$$3.matches($$2.typeHolder())) {
            return false;
         }
      }

      if (this.directEntity.isPresent() && !this.directEntity.get().matches($$0, $$1, $$2.getDirectEntity())) {
         return false;
      } else {
         return this.sourceEntity.isPresent() && !this.sourceEntity.get().matches($$0, $$1, $$2.getEntity())
            ? false
            : !this.isDirect.isPresent() || this.isDirect.get() == $$2.isDirect();
      }
   }

   public static class Builder {
      private final com.google.common.collect.ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
      private Optional<EntityPredicate> directEntity = Optional.empty();
      private Optional<EntityPredicate> sourceEntity = Optional.empty();
      private Optional<Boolean> isDirect = Optional.empty();

      public static DamageSourcePredicate.Builder damageType() {
         return new DamageSourcePredicate.Builder();
      }

      public DamageSourcePredicate.Builder tag(TagPredicate<DamageType> $$0) {
         this.tags.add($$0);
         return this;
      }

      public DamageSourcePredicate.Builder direct(EntityPredicate.Builder $$0) {
         this.directEntity = Optional.of($$0.build());
         return this;
      }

      public DamageSourcePredicate.Builder source(EntityPredicate.Builder $$0) {
         this.sourceEntity = Optional.of($$0.build());
         return this;
      }

      public DamageSourcePredicate.Builder isDirect(boolean $$0) {
         this.isDirect = Optional.of($$0);
         return this;
      }

      public DamageSourcePredicate build() {
         return new DamageSourcePredicate(this.tags.build(), this.directEntity, this.sourceEntity, this.isDirect);
      }
   }
}
