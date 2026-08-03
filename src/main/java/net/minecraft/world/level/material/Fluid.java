package net.minecraft.world.level.material;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class Fluid {
   public static final IdMapper<FluidState> FLUID_STATE_REGISTRY = new IdMapper();
   protected final StateDefinition<Fluid, FluidState> stateDefinition;
   private FluidState defaultFluidState;
   private final Reference<Fluid> builtInRegistryHolder = BuiltInRegistries.FLUID.createIntrusiveHolder(this);

   protected Fluid() {
      StateDefinition.Builder<Fluid, FluidState> $$0 = new StateDefinition.Builder<>(this);
      this.createFluidStateDefinition($$0);
      this.stateDefinition = $$0.create(Fluid::defaultFluidState, FluidState::new);
      this.registerDefaultState(this.stateDefinition.any());
   }

   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> $$0) {
   }

   public StateDefinition<Fluid, FluidState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(FluidState $$0) {
      this.defaultFluidState = $$0;
   }

   public final FluidState defaultFluidState() {
      return this.defaultFluidState;
   }

   public abstract Item getBucket();

   protected void animateTick(net.minecraft.world.level.Level $$0, BlockPos $$1, FluidState $$2, RandomSource $$3) {
   }

   protected void tick(ServerLevel $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
   }

   protected void randomTick(ServerLevel $$0, BlockPos $$1, FluidState $$2, RandomSource $$3) {
   }

   protected void entityInside(net.minecraft.world.level.Level $$0, BlockPos $$1, Entity $$2, InsideBlockEffectApplier $$3) {
   }

   @Nullable
   protected ParticleOptions getDripParticle() {
      return null;
   }

   protected abstract boolean canBeReplacedWith(FluidState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3, Fluid var4, Direction var5);

   protected abstract Vec3 getFlow(net.minecraft.world.level.BlockGetter var1, BlockPos var2, FluidState var3);

   public abstract int getTickDelay(net.minecraft.world.level.LevelReader var1);

   protected boolean isRandomlyTicking() {
      return false;
   }

   protected boolean isEmpty() {
      return false;
   }

   protected abstract float getExplosionResistance();

   public abstract float getHeight(FluidState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3);

   public abstract float getOwnHeight(FluidState var1);

   protected abstract BlockState createLegacyBlock(FluidState var1);

   public abstract boolean isSource(FluidState var1);

   public abstract int getAmount(FluidState var1);

   public boolean isSame(Fluid $$0) {
      return $$0 == this;
   }

   @Deprecated
   public boolean is(TagKey<Fluid> $$0) {
      return this.builtInRegistryHolder.is($$0);
   }

   public abstract VoxelShape getShape(FluidState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3);

   @Nullable
   public AABB getAABB(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      if (this.isEmpty()) {
         return null;
      } else {
         float $$3 = $$0.getHeight($$1, $$2);
         return new AABB($$2.getX(), $$2.getY(), $$2.getZ(), $$2.getX() + 1.0, $$2.getY() + $$3, $$2.getZ() + 1.0);
      }
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.empty();
   }

   @Deprecated
   public Reference<Fluid> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }
}
