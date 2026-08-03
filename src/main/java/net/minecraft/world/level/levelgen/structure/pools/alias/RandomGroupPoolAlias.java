package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public record RandomGroupPoolAlias(WeightedList<List<PoolAliasBinding>> groups) implements PoolAliasBinding {
   static MapCodec<RandomGroupPoolAlias> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WeightedList.nonEmptyCodec(Codec.list(PoolAliasBinding.CODEC)).fieldOf("groups").forGetter(RandomGroupPoolAlias::groups))
         .apply($$0, RandomGroupPoolAlias::new)
   );

   @Override
   public void forEachResolved(RandomSource $$0, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> $$1) {
      this.groups.getRandom($$0).ifPresent($$2 -> $$2.forEach($$2x -> $$2x.forEachResolved($$0, $$1)));
   }

   @Override
   public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
      return this.groups.unwrap().stream().flatMap($$0 -> ((List)$$0.value()).stream()).flatMap(PoolAliasBinding::allTargets);
   }

   @Override
   public MapCodec<RandomGroupPoolAlias> codec() {
      return CODEC;
   }
}
