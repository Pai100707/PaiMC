package net.minecraft.world.entity.projectile.hurtingprojectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WindCharge extends AbstractWindCharge {
   private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
      true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
   );
   private static final float RADIUS = 1.2F;
   private static final float MIN_CAMERA_DISTANCE_SQUARED = Mth.square(3.5F);
   private int noDeflectTicks = 5;

   public WindCharge(net.minecraft.world.entity.EntityType<? extends AbstractWindCharge> $$0, Level $$1) {
      super($$0, $$1);
   }

   public WindCharge(Player $$0, Level $$1, double $$2, double $$3, double $$4) {
      super(net.minecraft.world.entity.EntityType.WIND_CHARGE, $$1, $$0, $$2, $$3, $$4);
   }

   public WindCharge(Level $$0, double $$1, double $$2, double $$3, Vec3 $$4) {
      super(net.minecraft.world.entity.EntityType.WIND_CHARGE, $$1, $$2, $$3, $$4, $$0);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.noDeflectTicks > 0) {
         this.noDeflectTicks--;
      }
   }

   @Override
   public boolean deflect(
      ProjectileDeflection $$0,
      @Nullable net.minecraft.world.entity.Entity $$1,
      @Nullable net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.Entity> $$2,
      boolean $$3
   ) {
      return this.noDeflectTicks > 0 ? false : super.deflect($$0, $$1, $$2, $$3);
   }

   @Override
   protected void explode(Vec3 $$0) {
      this.level()
         .explode(
            this,
            null,
            EXPLOSION_DAMAGE_CALCULATOR,
            $$0.x(),
            $$0.y(),
            $$0.z(),
            1.2F,
            false,
            ExplosionInteraction.TRIGGER,
            ParticleTypes.GUST_EMITTER_SMALL,
            ParticleTypes.GUST_EMITTER_LARGE,
            WeightedList.of(),
            SoundEvents.WIND_CHARGE_BURST
         );
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      return this.tickCount < 2 && $$0 < MIN_CAMERA_DISTANCE_SQUARED ? false : super.shouldRenderAtSqrDistance($$0);
   }
}
