package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

@FunctionalInterface
public interface PoolAliasLookup {
   PoolAliasLookup EMPTY = $$0 -> $$0;

   ResourceKey<StructureTemplatePool> lookup(ResourceKey<StructureTemplatePool> var1);

   static PoolAliasLookup create(List<PoolAliasBinding> $$0, BlockPos $$1, long $$2) {
      if ($$0.isEmpty()) {
         return EMPTY;
      } else {
         RandomSource $$3 = RandomSource.create($$2).forkPositional().at($$1);
         Builder<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> $$4 = ImmutableMap.builder();
         $$0.forEach($$2x -> $$2x.forEachResolved($$3, $$4::put));
         Map<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> $$5 = $$4.build();
         return $$1x -> Objects.requireNonNull($$5.getOrDefault($$1x, $$1x), () -> "alias " + $$1x.identifier() + " was mapped to null value");
      }
   }
}
