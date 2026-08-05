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
import net.minecraft.data.worldgen.BastionBridgePools;
import net.minecraft.data.worldgen.BastionHoglinStablePools;
import net.minecraft.data.worldgen.BastionHousingUnitsPools;
import net.minecraft.data.worldgen.BastionSharedPools;
import net.minecraft.data.worldgen.BastionTreasureRoomPools;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionPieces {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("bastion/starts");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> $$0) {
        HolderGetter<StructureProcessorList> $$1 = $$0.lookup(Registries.PROCESSOR_LIST);
        Holder.Reference<StructureProcessorList> $$2 = $$1.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        HolderGetter<StructureTemplatePool> $$3 = $$0.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> $$4 = $$3.getOrThrow(Pools.EMPTY);
        $$0.register(START, new StructureTemplatePool($$4, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/air_base", $$2), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/hoglin_stable/air_base", $$2), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/treasure/big_air_full", $$2), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_base", $$2), (Object)1)), StructureTemplatePool.Projection.RIGID));
        BastionHousingUnitsPools.bootstrap($$0);
        BastionHoglinStablePools.bootstrap($$0);
        BastionTreasureRoomPools.bootstrap($$0);
        BastionBridgePools.bootstrap($$0);
        BastionSharedPools.bootstrap($$0);
    }
}

