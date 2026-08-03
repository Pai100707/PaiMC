package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
   public static final int SECTION_WIDTH = 16;
   public static final int SECTION_HEIGHT = 16;
   public static final int SECTION_SIZE = 4096;
   public static final int BIOME_CONTAINER_BITS = 2;
   private short nonEmptyBlockCount;
   private short tickingBlockCount;
   private short tickingFluidCount;
   private final PalettedContainer<BlockState> states;
   private PalettedContainerRO<Holder<Biome>> biomes;

   private LevelChunkSection(LevelChunkSection $$0) {
      this.nonEmptyBlockCount = $$0.nonEmptyBlockCount;
      this.tickingBlockCount = $$0.tickingBlockCount;
      this.tickingFluidCount = $$0.tickingFluidCount;
      this.states = $$0.states.copy();
      this.biomes = $$0.biomes.copy();
   }

   public LevelChunkSection(PalettedContainer<BlockState> $$0, PalettedContainerRO<Holder<Biome>> $$1) {
      this.states = $$0;
      this.biomes = $$1;
      this.recalcBlockCounts();
   }

   public LevelChunkSection(PalettedContainerFactory $$0) {
      this.states = $$0.createForBlockStates();
      this.biomes = $$0.createForBiomes();
   }

   public BlockState getBlockState(int $$0, int $$1, int $$2) {
      return this.states.get($$0, $$1, $$2);
   }

   public FluidState getFluidState(int $$0, int $$1, int $$2) {
      return this.states.get($$0, $$1, $$2).getFluidState();
   }

   public void acquire() {
      this.states.acquire();
   }

   public void release() {
      this.states.release();
   }

   public BlockState setBlockState(int $$0, int $$1, int $$2, BlockState $$3) {
      return this.setBlockState($$0, $$1, $$2, $$3, true);
   }

   public BlockState setBlockState(int $$0, int $$1, int $$2, BlockState $$3, boolean $$4) {
      BlockState $$5;
      if ($$4) {
         $$5 = this.states.getAndSet($$0, $$1, $$2, $$3);
      } else {
         $$5 = this.states.getAndSetUnchecked($$0, $$1, $$2, $$3);
      }

      FluidState $$7 = $$5.getFluidState();
      FluidState $$8 = $$3.getFluidState();
      if (!$$5.isAir()) {
         this.nonEmptyBlockCount--;
         if ($$5.isRandomlyTicking()) {
            this.tickingBlockCount--;
         }
      }

      if (!$$7.isEmpty()) {
         this.tickingFluidCount--;
      }

      if (!$$3.isAir()) {
         this.nonEmptyBlockCount++;
         if ($$3.isRandomlyTicking()) {
            this.tickingBlockCount++;
         }
      }

      if (!$$8.isEmpty()) {
         this.tickingFluidCount++;
      }

      return $$5;
   }

   public boolean hasOnlyAir() {
      return this.nonEmptyBlockCount == 0;
   }

   public boolean isRandomlyTicking() {
      return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
   }

   public boolean isRandomlyTickingBlocks() {
      return this.tickingBlockCount > 0;
   }

   public boolean isRandomlyTickingFluids() {
      return this.tickingFluidCount > 0;
   }

   public void recalcBlockCounts() {
      class BlockCounter implements PalettedContainer.CountConsumer<BlockState> {
         public int nonEmptyBlockCount;
         public int tickingBlockCount;
         public int tickingFluidCount;

         public void accept(BlockState $$0, int $$1) {
            FluidState $$2 = $$0.getFluidState();
            if (!$$0.isAir()) {
               this.nonEmptyBlockCount += $$1;
               if ($$0.isRandomlyTicking()) {
                  this.tickingBlockCount += $$1;
               }
            }

            if (!$$2.isEmpty()) {
               this.nonEmptyBlockCount += $$1;
               if ($$2.isRandomlyTicking()) {
                  this.tickingFluidCount += $$1;
               }
            }
         }
      }

      BlockCounter $$0 = new BlockCounter();
      this.states.count($$0);
      this.nonEmptyBlockCount = (short)$$0.nonEmptyBlockCount;
      this.tickingBlockCount = (short)$$0.tickingBlockCount;
      this.tickingFluidCount = (short)$$0.tickingFluidCount;
   }

   public PalettedContainer<BlockState> getStates() {
      return this.states;
   }

   public PalettedContainerRO<Holder<Biome>> getBiomes() {
      return this.biomes;
   }

   public void read(FriendlyByteBuf $$0) {
      this.nonEmptyBlockCount = $$0.readShort();
      this.states.read($$0);
      PalettedContainer<Holder<Biome>> $$1 = this.biomes.recreate();
      $$1.read($$0);
      this.biomes = $$1;
   }

   public void readBiomes(FriendlyByteBuf $$0) {
      PalettedContainer<Holder<Biome>> $$1 = this.biomes.recreate();
      $$1.read($$0);
      this.biomes = $$1;
   }

   public void write(FriendlyByteBuf $$0) {
      $$0.writeShort(this.nonEmptyBlockCount);
      this.states.write($$0);
      this.biomes.write($$0);
   }

   public int getSerializedSize() {
      return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
   }

   public boolean maybeHas(Predicate<BlockState> $$0) {
      return this.states.maybeHas($$0);
   }

   public Holder<Biome> getNoiseBiome(int $$0, int $$1, int $$2) {
      return this.biomes.get($$0, $$1, $$2);
   }

   public void fillBiomesFromNoise(BiomeResolver $$0, Climate.Sampler $$1, int $$2, int $$3, int $$4) {
      PalettedContainer<Holder<Biome>> $$5 = this.biomes.recreate();
      int $$6 = 4;

      for (int $$7 = 0; $$7 < 4; $$7++) {
         for (int $$8 = 0; $$8 < 4; $$8++) {
            for (int $$9 = 0; $$9 < 4; $$9++) {
               $$5.getAndSetUnchecked($$7, $$8, $$9, $$0.getNoiseBiome($$2 + $$7, $$3 + $$8, $$4 + $$9, $$1));
            }
         }
      }

      this.biomes = $$5;
   }

   public LevelChunkSection copy() {
      return new LevelChunkSection(this);
   }
}
