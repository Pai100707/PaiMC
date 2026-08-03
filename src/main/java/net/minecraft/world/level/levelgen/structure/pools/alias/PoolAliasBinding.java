package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.WeightedList.Builder;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public interface PoolAliasBinding {
   Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

   void forEachResolved(RandomSource var1, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> var2);

   Stream<ResourceKey<StructureTemplatePool>> allTargets();

   static DirectPoolAlias direct(String $$0, String $$1) {
      return direct(Pools.createKey($$0), Pools.createKey($$1));
   }

   static DirectPoolAlias direct(ResourceKey<StructureTemplatePool> $$0, ResourceKey<StructureTemplatePool> $$1) {
      return new DirectPoolAlias($$0, $$1);
   }

   static RandomPoolAlias random(String $$0, WeightedList<String> $$1) {
      Builder<ResourceKey<StructureTemplatePool>> $$2 = WeightedList.builder();
      $$1.unwrap().forEach($$1x -> $$2.add(Pools.createKey((String)$$1x.value()), $$1x.weight()));
      return random(Pools.createKey($$0), $$2.build());
   }

   static RandomPoolAlias random(ResourceKey<StructureTemplatePool> $$0, WeightedList<ResourceKey<StructureTemplatePool>> $$1) {
      return new RandomPoolAlias($$0, $$1);
   }

   static RandomGroupPoolAlias randomGroup(WeightedList<List<PoolAliasBinding>> $$0) {
      return new RandomGroupPoolAlias($$0);
   }

   MapCodec<? extends PoolAliasBinding> codec();
}
