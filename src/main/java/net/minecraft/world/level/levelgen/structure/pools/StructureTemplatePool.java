package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.StringRepresentable.EnumCodec;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class StructureTemplatePool {
   private static final int SIZE_UNSET = Integer.MIN_VALUE;
   private static final MutableObject<Codec<Holder<StructureTemplatePool>>> CODEC_REFERENCE = new MutableObject();
   public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.lazyInitialized(CODEC_REFERENCE).fieldOf("fallback").forGetter(StructureTemplatePool::getFallback),
            Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.intRange(1, 150).fieldOf("weight"))
               .codec()
               .listOf()
               .fieldOf("elements")
               .forGetter($$0x -> $$0x.rawTemplates)
         )
         .apply($$0, StructureTemplatePool::new)
   );
   public static final Codec<Holder<StructureTemplatePool>> CODEC = (Codec<Holder<StructureTemplatePool>>)Util.make(
      RegistryFileCodec.create(Registries.TEMPLATE_POOL, DIRECT_CODEC), CODEC_REFERENCE::setValue
   );
   private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
   private final ObjectArrayList<StructurePoolElement> templates;
   private final Holder<StructureTemplatePool> fallback;
   private int maxSize = Integer.MIN_VALUE;

   public StructureTemplatePool(Holder<StructureTemplatePool> $$0, List<Pair<StructurePoolElement, Integer>> $$1) {
      this.rawTemplates = $$1;
      this.templates = new ObjectArrayList();

      for (Pair<StructurePoolElement, Integer> $$2 : $$1) {
         StructurePoolElement $$3 = (StructurePoolElement)$$2.getFirst();

         for (int $$4 = 0; $$4 < $$2.getSecond(); $$4++) {
            this.templates.add($$3);
         }
      }

      this.fallback = $$0;
   }

   public StructureTemplatePool(
      Holder<StructureTemplatePool> $$0,
      List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> $$1,
      StructureTemplatePool.Projection $$2
   ) {
      this.rawTemplates = Lists.newArrayList();
      this.templates = new ObjectArrayList();

      for (Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> $$3 : $$1) {
         StructurePoolElement $$4 = (StructurePoolElement)((Function)$$3.getFirst()).apply($$2);
         this.rawTemplates.add(Pair.of($$4, (Integer)$$3.getSecond()));

         for (int $$5 = 0; $$5 < $$3.getSecond(); $$5++) {
            this.templates.add($$4);
         }
      }

      this.fallback = $$0;
   }

   public int getMaxSize(StructureTemplateManager $$0) {
      if (this.maxSize == Integer.MIN_VALUE) {
         this.maxSize = this.templates
            .stream()
            .filter($$0x -> $$0x != EmptyPoolElement.INSTANCE)
            .mapToInt($$1 -> $$1.getBoundingBox($$0, BlockPos.ZERO, Rotation.NONE).getYSpan())
            .max()
            .orElse(0);
      }

      return this.maxSize;
   }

   @VisibleForTesting
   public List<Pair<StructurePoolElement, Integer>> getTemplates() {
      return this.rawTemplates;
   }

   public Holder<StructureTemplatePool> getFallback() {
      return this.fallback;
   }

   public StructurePoolElement getRandomTemplate(RandomSource $$0) {
      return (StructurePoolElement)(this.templates.isEmpty()
         ? EmptyPoolElement.INSTANCE
         : (StructurePoolElement)this.templates.get($$0.nextInt(this.templates.size())));
   }

   public List<StructurePoolElement> getShuffledTemplates(RandomSource $$0) {
      return Util.shuffledCopy(this.templates, $$0);
   }

   public int size() {
      return this.templates.size();
   }

   public static enum Projection implements StringRepresentable {
      TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
      RIGID("rigid", ImmutableList.of());

      public static final EnumCodec<StructureTemplatePool.Projection> CODEC = StringRepresentable.fromEnum(StructureTemplatePool.Projection::values);
      private final String name;
      private final ImmutableList<StructureProcessor> processors;

      private Projection(final String $$0, final ImmutableList<StructureProcessor> $$1) {
         this.name = $$0;
         this.processors = $$1;
      }

      public String getName() {
         return this.name;
      }

      public static StructureTemplatePool.Projection byName(String $$0) {
         return (StructureTemplatePool.Projection)CODEC.byName($$0);
      }

      public ImmutableList<StructureProcessor> getProcessors() {
         return this.processors;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
