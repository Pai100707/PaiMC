package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public record FluidPredicate(Optional<HolderSet<Fluid>> fluids, Optional<StatePropertiesPredicate> properties) {
   public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).optionalFieldOf("fluids").forGetter(FluidPredicate::fluids),
            StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(FluidPredicate::properties)
         )
         .apply($$0, FluidPredicate::new)
   );

   public boolean matches(ServerLevel $$0, BlockPos $$1) {
      if (!$$0.isLoaded($$1)) {
         return false;
      } else {
         FluidState $$2 = $$0.getFluidState($$1);
         return this.fluids.isPresent() && !$$2.is(this.fluids.get()) ? false : !this.properties.isPresent() || this.properties.get().matches($$2);
      }
   }

   public static class Builder {
      private Optional<HolderSet<Fluid>> fluids = Optional.empty();
      private Optional<StatePropertiesPredicate> properties = Optional.empty();

      private Builder() {
      }

      public static FluidPredicate.Builder fluid() {
         return new FluidPredicate.Builder();
      }

      public FluidPredicate.Builder of(Fluid $$0) {
         this.fluids = Optional.of(HolderSet.direct(new Holder[]{$$0.builtInRegistryHolder()}));
         return this;
      }

      public FluidPredicate.Builder of(HolderSet<Fluid> $$0) {
         this.fluids = Optional.of($$0);
         return this;
      }

      public FluidPredicate.Builder setProperties(StatePropertiesPredicate $$0) {
         this.properties = Optional.of($$0);
         return this;
      }

      public FluidPredicate build() {
         return new FluidPredicate(this.fluids, this.properties);
      }
   }
}
