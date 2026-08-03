package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;

public class Pools {
   public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

   public static ResourceKey<StructureTemplatePool> createKey(Identifier $$0) {
      return ResourceKey.create(Registries.TEMPLATE_POOL, $$0);
   }

   public static ResourceKey<StructureTemplatePool> createKey(String $$0) {
      return createKey(Identifier.withDefaultNamespace($$0));
   }

   public static ResourceKey<StructureTemplatePool> parseKey(String $$0) {
      return createKey(Identifier.parse($$0));
   }

   public static void register(BootstrapContext<StructureTemplatePool> $$0, String $$1, StructureTemplatePool $$2) {
      $$0.register(createKey($$1), $$2);
   }

   public static void bootstrap(BootstrapContext<StructureTemplatePool> $$0) {
      HolderGetter<StructureTemplatePool> $$1 = $$0.lookup(Registries.TEMPLATE_POOL);
      Holder<StructureTemplatePool> $$2 = $$1.getOrThrow(EMPTY);
      $$0.register(EMPTY, new StructureTemplatePool($$2, ImmutableList.of(), Projection.RIGID));
      BastionPieces.bootstrap($$0);
      PillagerOutpostPools.bootstrap($$0);
      VillagePools.bootstrap($$0);
      AncientCityStructurePieces.bootstrap($$0);
      TrailRuinsStructurePools.bootstrap($$0);
      TrialChambersStructurePools.bootstrap($$0);
   }
}
