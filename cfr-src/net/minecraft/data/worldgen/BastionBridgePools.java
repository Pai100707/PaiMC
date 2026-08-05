/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionBridgePools {
    public static void bootstrap(BootstrapContext<StructureTemplatePool> $$0) {
        HolderGetter<StructureProcessorList> $$1 = $$0.lookup(Registries.PROCESSOR_LIST);
        Holder.Reference<StructureProcessorList> $$2 = $$1.getOrThrow(ProcessorLists.ENTRANCE_REPLACEMENT);
        Holder.Reference<StructureProcessorList> $$3 = $$1.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        Holder.Reference<StructureProcessorList> $$4 = $$1.getOrThrow(ProcessorLists.BRIDGE);
        Holder.Reference<StructureProcessorList> $$5 = $$1.getOrThrow(ProcessorLists.RAMPART_DEGRADATION);
        HolderGetter<StructureTemplatePool> $$6 = $$0.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> $$7 = $$6.getOrThrow(Pools.EMPTY);
        Pools.register($$0, "bastion/bridge/starting_pieces", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance", $$2), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_face", $$3), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register($$0, "bastion/bridge/bridge_pieces", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/bridge_pieces/bridge", $$4), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register($$0, "bastion/bridge/legs", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/legs/leg_0", $$3), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/legs/leg_1", $$3), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register($$0, "bastion/bridge/walls", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/walls/wall_base_0", $$5), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/walls/wall_base_1", $$5), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register($$0, "bastion/bridge/ramparts", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/ramparts/rampart_0", $$5), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/ramparts/rampart_1", $$5), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register($$0, "bastion/bridge/rampart_plates", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/rampart_plates/plate_0", $$5), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register($$0, "bastion/bridge/connectors", new StructureTemplatePool($$7, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/connectors/back_bridge_top", $$3), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/connectors/back_bridge_bottom", $$3), (Object)1)), StructureTemplatePool.Projection.RIGID));
    }
}

