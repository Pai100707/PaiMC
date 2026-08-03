package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record ExplodeEffect(
   boolean attributeToUser,
   Optional<Holder<DamageType>> damageType,
   Optional<LevelBasedValue> knockbackMultiplier,
   Optional<HolderSet<Block>> immuneBlocks,
   Vec3 offset,
   LevelBasedValue radius,
   boolean createFire,
   ExplosionInteraction blockInteraction,
   ParticleOptions smallParticle,
   ParticleOptions largeParticle,
   WeightedList<ExplosionParticleInfo> blockParticles,
   Holder<SoundEvent> sound
) implements EnchantmentEntityEffect {
   public static final MapCodec<ExplodeEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.BOOL.optionalFieldOf("attribute_to_user", false).forGetter(ExplodeEffect::attributeToUser),
            DamageType.CODEC.optionalFieldOf("damage_type").forGetter(ExplodeEffect::damageType),
            LevelBasedValue.CODEC.optionalFieldOf("knockback_multiplier").forGetter(ExplodeEffect::knockbackMultiplier),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("immune_blocks").forGetter(ExplodeEffect::immuneBlocks),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(ExplodeEffect::offset),
            LevelBasedValue.CODEC.fieldOf("radius").forGetter(ExplodeEffect::radius),
            Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(ExplodeEffect::createFire),
            ExplosionInteraction.CODEC.fieldOf("block_interaction").forGetter(ExplodeEffect::blockInteraction),
            ParticleTypes.CODEC.fieldOf("small_particle").forGetter(ExplodeEffect::smallParticle),
            ParticleTypes.CODEC.fieldOf("large_particle").forGetter(ExplodeEffect::largeParticle),
            WeightedList.codec(ExplosionParticleInfo.CODEC).optionalFieldOf("block_particles", WeightedList.of()).forGetter(ExplodeEffect::blockParticles),
            SoundEvent.CODEC.fieldOf("sound").forGetter(ExplodeEffect::sound)
         )
         .apply($$0, ExplodeEffect::new)
   );

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      Vec3 $$5 = $$4.add(this.offset);
      $$0.explode(
         this.attributeToUser ? $$3 : null,
         this.getDamageSource($$3, $$5),
         new SimpleExplosionDamageCalculator(
            this.blockInteraction != ExplosionInteraction.NONE,
            this.damageType.isPresent(),
            this.knockbackMultiplier.map($$1x -> $$1x.calculate($$1)),
            this.immuneBlocks
         ),
         $$5.x(),
         $$5.y(),
         $$5.z(),
         Math.max(this.radius.calculate($$1), 0.0F),
         this.createFire,
         this.blockInteraction,
         this.smallParticle,
         this.largeParticle,
         this.blockParticles,
         this.sound
      );
   }

   @Nullable
   private DamageSource getDamageSource(Entity $$0, Vec3 $$1) {
      if (this.damageType.isEmpty()) {
         return null;
      } else {
         return this.attributeToUser ? new DamageSource(this.damageType.get(), $$0) : new DamageSource(this.damageType.get(), $$1);
      }
   }

   @Override
   public MapCodec<ExplodeEffect> codec() {
      return CODEC;
   }
}
