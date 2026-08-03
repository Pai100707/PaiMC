package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class PoolAliasBindings {
   public static MapCodec<? extends PoolAliasBinding> bootstrap(Registry<MapCodec<? extends PoolAliasBinding>> $$0) {
      Registry.register($$0, "random", RandomPoolAlias.CODEC);
      Registry.register($$0, "random_group", RandomGroupPoolAlias.CODEC);
      return (MapCodec<? extends PoolAliasBinding>)Registry.register($$0, "direct", DirectPoolAlias.CODEC);
   }

   public static void registerTargetsAsPools(BootstrapContext<StructureTemplatePool> $$0, Holder<StructureTemplatePool> $$1, List<PoolAliasBinding> $$2) {
      $$2.stream()
         .flatMap(PoolAliasBinding::allTargets)
         .map($$0x -> $$0x.identifier().getPath())
         .forEach(
            $$2x -> Pools.register(
               $$0, $$2x, new StructureTemplatePool($$1, List.of(Pair.of(StructurePoolElement.single($$2x), 1)), StructureTemplatePool.Projection.RIGID)
            )
         );
   }
}
