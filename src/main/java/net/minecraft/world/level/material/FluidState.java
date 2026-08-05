package net.minecraft.world.level.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FluidState extends StateHolder<Fluid, FluidState> {
   public static final Codec<FluidState> CODEC = codec(BuiltInRegistries.FLUID.byNameCodec(), Fluid::defaultFluidState).stable();
   public static final int AMOUNT_MAX = 9;
   public static final int AMOUNT_FULL = 8;

   public FluidState(Fluid $$0, Reference2ObjectArrayMap<Property<?>, Comparable<?>> $$1, MapCodec<FluidState> $$2) {
      super($$0, $$1, $$2);
   }

   public Fluid getType() {
      return this.owner;
   }

   public boolean isSource() {
      return this.getType().isSource(this);
   }

   public boolean isSourceOfType(Fluid $$0) {
      return this.owner == $$0 && this.owner.isSource(this);
   }

   public boolean isEmpty() {
      return this.getType().isEmpty();
   }

   public float getHeight(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return this.getType().getHeight(this, $$0, $$1);
   }

   public float getOwnHeight() {
      return this.getType().getOwnHeight(this);
   }

   public int getAmount() {
      return this.getType().getAmount(this);
   }

   public boolean shouldRenderBackwardUpFace(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      for (int $$2 = -1; $$2 <= 1; $$2++) {
         for (int $$3 = -1; $$3 <= 1; $$3++) {
            BlockPos $$4 = $$1.offset($$2, 0, $$3);
            FluidState $$5 = $$0.getFluidState($$4);
            if (!$$5.getType().isSame(this.getType()) && !$$0.getBlockState($$4).isSolidRender()) {
               return true;
            }
         }
      }

      return false;
   }

   public void tick(ServerLevel $$0, BlockPos $$1, BlockState $$2) {
      this.getType().tick($$0, $$1, $$2, this);
   }

   public void animateTick(net.minecraft.world.level.Level $$0, BlockPos $$1, RandomSource $$2) {
      this.getType().animateTick($$0, $$1, this, $$2);
   }

   public boolean isRandomlyTicking() {
      return this.getType().isRandomlyTicking();
   }

   public void randomTick(ServerLevel $$0, BlockPos $$1, RandomSource $$2) {
      this.getType().randomTick($$0, $$1, this, $$2);
   }

   public Vec3 getFlow(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return this.getType().getFlow($$0, $$1, this);
   }

   public BlockState createLegacyBlock() {
      return this.getType().createLegacyBlock(this);
   }

   
   public ParticleOptions getDripParticle() {
      return this.getType().getDripParticle();
   }

   public boolean is(TagKey<Fluid> $$0) {
      return this.getType().builtInRegistryHolder().is($$0);
   }

   public boolean is(HolderSet<Fluid> $$0) {
      return $$0.contains(this.getType().builtInRegistryHolder());
   }

   public boolean is(Fluid $$0) {
      return this.getType() == $$0;
   }

   public float getExplosionResistance() {
      return this.getType().getExplosionResistance();
   }

   public boolean canBeReplacedWith(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Fluid $$2, Direction $$3) {
      return this.getType().canBeReplacedWith(this, $$0, $$1, $$2, $$3);
   }

   public VoxelShape getShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return this.getType().getShape(this, $$0, $$1);
   }

   
   public AABB getAABB(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return this.getType().getAABB(this, $$0, $$1);
   }

   public Holder<Fluid> holder() {
      return this.owner.builtInRegistryHolder();
   }

   public Stream<TagKey<Fluid>> getTags() {
      return this.owner.builtInRegistryHolder().tags();
   }

   public void entityInside(net.minecraft.world.level.Level $$0, BlockPos $$1, Entity $$2, InsideBlockEffectApplier $$3) {
      this.getType().entityInside($$0, $$1, $$2, $$3);
   }
}
