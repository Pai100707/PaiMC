package net.minecraft.data.worldgen;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
   public static void bootstrap(BootstrapContext<StructureTemplatePool> $$0) {
      PlainVillagePools.bootstrap($$0);
      SnowyVillagePools.bootstrap($$0);
      SavannaVillagePools.bootstrap($$0);
      DesertVillagePools.bootstrap($$0);
      TaigaVillagePools.bootstrap($$0);
   }
}
