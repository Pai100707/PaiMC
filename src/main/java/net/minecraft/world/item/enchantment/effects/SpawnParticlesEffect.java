package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public record SpawnParticlesEffect(
   ParticleOptions particle,
   SpawnParticlesEffect.PositionSource horizontalPosition,
   SpawnParticlesEffect.PositionSource verticalPosition,
   SpawnParticlesEffect.VelocitySource horizontalVelocity,
   SpawnParticlesEffect.VelocitySource verticalVelocity,
   FloatProvider speed
) implements EnchantmentEntityEffect {
   public static final MapCodec<SpawnParticlesEffect> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticlesEffect::particle),
            SpawnParticlesEffect.PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEffect::horizontalPosition),
            SpawnParticlesEffect.PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEffect::verticalPosition),
            SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEffect::horizontalVelocity),
            SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEffect::verticalVelocity),
            FloatProvider.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect::speed)
         )
         .apply($$0, SpawnParticlesEffect::new)
   );

   public static SpawnParticlesEffect.PositionSource offsetFromEntityPosition(float $$0) {
      return new SpawnParticlesEffect.PositionSource(SpawnParticlesEffect.PositionSourceType.ENTITY_POSITION, $$0, 1.0F);
   }

   public static SpawnParticlesEffect.PositionSource inBoundingBox() {
      return new SpawnParticlesEffect.PositionSource(SpawnParticlesEffect.PositionSourceType.BOUNDING_BOX, 0.0F, 1.0F);
   }

   public static SpawnParticlesEffect.VelocitySource movementScaled(float $$0) {
      return new SpawnParticlesEffect.VelocitySource($$0, ConstantFloat.ZERO);
   }

   public static SpawnParticlesEffect.VelocitySource fixedVelocity(FloatProvider $$0) {
      return new SpawnParticlesEffect.VelocitySource(0.0F, $$0);
   }

   @Override
   public void apply(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4) {
      RandomSource $$5 = $$3.getRandom();
      Vec3 $$6 = $$3.getKnownMovement();
      float $$7 = $$3.getBbWidth();
      float $$8 = $$3.getBbHeight();
      $$0.sendParticles(
         this.particle,
         this.horizontalPosition.getCoordinate($$4.x(), $$4.x(), $$7, $$5),
         this.verticalPosition.getCoordinate($$4.y(), $$4.y() + $$8 / 2.0F, $$8, $$5),
         this.horizontalPosition.getCoordinate($$4.z(), $$4.z(), $$7, $$5),
         0,
         this.horizontalVelocity.getVelocity($$6.x(), $$5),
         this.verticalVelocity.getVelocity($$6.y(), $$5),
         this.horizontalVelocity.getVelocity($$6.z(), $$5),
         this.speed.sample($$5)
      );
   }

   @Override
   public MapCodec<SpawnParticlesEffect> codec() {
      return CODEC;
   }

   public record PositionSource(SpawnParticlesEffect.PositionSourceType type, float offset, float scale) {
      public static final MapCodec<SpawnParticlesEffect.PositionSource> CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  SpawnParticlesEffect.PositionSourceType.CODEC.fieldOf("type").forGetter(SpawnParticlesEffect.PositionSource::type),
                  Codec.FLOAT.optionalFieldOf("offset", 0.0F).forGetter(SpawnParticlesEffect.PositionSource::offset),
                  ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1.0F).forGetter(SpawnParticlesEffect.PositionSource::scale)
               )
               .apply($$0, SpawnParticlesEffect.PositionSource::new)
         )
         .validate(
            $$0 -> $$0.type() == SpawnParticlesEffect.PositionSourceType.ENTITY_POSITION && $$0.scale() != 1.0F
               ? DataResult.error(() -> "Cannot scale an entity position coordinate source")
               : DataResult.success($$0)
         );

      public double getCoordinate(double $$0, double $$1, float $$2, RandomSource $$3) {
         return this.type.getCoordinate($$0, $$1, $$2 * this.scale, $$3) + this.offset;
      }
   }

   public static enum PositionSourceType implements StringRepresentable {
      ENTITY_POSITION("entity_position", ($$0, $$1, $$2, $$3) -> $$0),
      BOUNDING_BOX("in_bounding_box", ($$0, $$1, $$2, $$3) -> $$1 + ($$3.nextDouble() - 0.5) * $$2);

      public static final Codec<SpawnParticlesEffect.PositionSourceType> CODEC = StringRepresentable.fromEnum(SpawnParticlesEffect.PositionSourceType::values);
      private final String id;
      private final SpawnParticlesEffect.PositionSourceType.CoordinateSource source;

      private PositionSourceType(final String $$0, final SpawnParticlesEffect.PositionSourceType.CoordinateSource $$1) {
         this.id = $$0;
         this.source = $$1;
      }

      public double getCoordinate(double $$0, double $$1, float $$2, RandomSource $$3) {
         return this.source.getCoordinate($$0, $$1, $$2, $$3);
      }

      public String getSerializedName() {
         return this.id;
      }

      @FunctionalInterface
      interface CoordinateSource {
         double getCoordinate(double var1, double var3, float var5, RandomSource var6);
      }
   }

   public record VelocitySource(float movementScale, FloatProvider base) {
      public static final MapCodec<SpawnParticlesEffect.VelocitySource> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.optionalFieldOf("movement_scale", 0.0F).forGetter(SpawnParticlesEffect.VelocitySource::movementScale),
               FloatProvider.CODEC.optionalFieldOf("base", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect.VelocitySource::base)
            )
            .apply($$0, SpawnParticlesEffect.VelocitySource::new)
      );

      public double getVelocity(double $$0, RandomSource $$1) {
         return $$0 * this.movementScale + this.base.sample($$1);
      }
   }
}
