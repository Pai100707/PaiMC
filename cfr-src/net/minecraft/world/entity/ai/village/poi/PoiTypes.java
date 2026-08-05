/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 */
package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class PoiTypes {
    public static final ResourceKey<PoiType> ARMORER = PoiTypes.createKey("armorer");
    public static final ResourceKey<PoiType> BUTCHER = PoiTypes.createKey("butcher");
    public static final ResourceKey<PoiType> CARTOGRAPHER = PoiTypes.createKey("cartographer");
    public static final ResourceKey<PoiType> CLERIC = PoiTypes.createKey("cleric");
    public static final ResourceKey<PoiType> FARMER = PoiTypes.createKey("farmer");
    public static final ResourceKey<PoiType> FISHERMAN = PoiTypes.createKey("fisherman");
    public static final ResourceKey<PoiType> FLETCHER = PoiTypes.createKey("fletcher");
    public static final ResourceKey<PoiType> LEATHERWORKER = PoiTypes.createKey("leatherworker");
    public static final ResourceKey<PoiType> LIBRARIAN = PoiTypes.createKey("librarian");
    public static final ResourceKey<PoiType> MASON = PoiTypes.createKey("mason");
    public static final ResourceKey<PoiType> SHEPHERD = PoiTypes.createKey("shepherd");
    public static final ResourceKey<PoiType> TOOLSMITH = PoiTypes.createKey("toolsmith");
    public static final ResourceKey<PoiType> WEAPONSMITH = PoiTypes.createKey("weaponsmith");
    public static final ResourceKey<PoiType> HOME = PoiTypes.createKey("home");
    public static final ResourceKey<PoiType> MEETING = PoiTypes.createKey("meeting");
    public static final ResourceKey<PoiType> BEEHIVE = PoiTypes.createKey("beehive");
    public static final ResourceKey<PoiType> BEE_NEST = PoiTypes.createKey("bee_nest");
    public static final ResourceKey<PoiType> NETHER_PORTAL = PoiTypes.createKey("nether_portal");
    public static final ResourceKey<PoiType> LODESTONE = PoiTypes.createKey("lodestone");
    public static final ResourceKey<PoiType> LIGHTNING_ROD = PoiTypes.createKey("lightning_rod");
    public static final ResourceKey<PoiType> TEST_INSTANCE = PoiTypes.createKey("test_instance");
    private static final Set<BlockState> BEDS = (Set)ImmutableList.of((Object)Blocks.RED_BED, (Object)Blocks.BLACK_BED, (Object)Blocks.BLUE_BED, (Object)Blocks.BROWN_BED, (Object)Blocks.CYAN_BED, (Object)Blocks.GRAY_BED, (Object)Blocks.GREEN_BED, (Object)Blocks.LIGHT_BLUE_BED, (Object)Blocks.LIGHT_GRAY_BED, (Object)Blocks.LIME_BED, (Object)Blocks.MAGENTA_BED, (Object)Blocks.ORANGE_BED, (Object[])new Block[]{Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED}).stream().flatMap($$0 -> $$0.getStateDefinition().getPossibleStates().stream()).filter($$0 -> $$0.getValue(BedBlock.PART) == BedPart.HEAD).collect(ImmutableSet.toImmutableSet());
    private static final Set<BlockState> CAULDRONS = (Set)ImmutableList.of((Object)Blocks.CAULDRON, (Object)Blocks.LAVA_CAULDRON, (Object)Blocks.WATER_CAULDRON, (Object)Blocks.POWDER_SNOW_CAULDRON).stream().flatMap($$0 -> $$0.getStateDefinition().getPossibleStates().stream()).collect(ImmutableSet.toImmutableSet());
    private static final Set<BlockState> LIGHTNING_RODS = (Set)ImmutableList.of((Object)Blocks.LIGHTNING_ROD, (Object)Blocks.EXPOSED_LIGHTNING_ROD, (Object)Blocks.WEATHERED_LIGHTNING_ROD, (Object)Blocks.OXIDIZED_LIGHTNING_ROD, (Object)Blocks.WAXED_LIGHTNING_ROD, (Object)Blocks.WAXED_EXPOSED_LIGHTNING_ROD, (Object)Blocks.WAXED_WEATHERED_LIGHTNING_ROD, (Object)Blocks.WAXED_OXIDIZED_LIGHTNING_ROD).stream().flatMap($$0 -> $$0.getStateDefinition().getPossibleStates().stream()).collect(ImmutableSet.toImmutableSet());
    private static final Map<BlockState, Holder<PoiType>> TYPE_BY_STATE = Maps.newHashMap();

    private static Set<BlockState> getBlockStates(Block $$0) {
        return ImmutableSet.copyOf($$0.getStateDefinition().getPossibleStates());
    }

    private static ResourceKey<PoiType> createKey(String $$0) {
        return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Identifier.withDefaultNamespace($$0));
    }

    private static PoiType register(Registry<PoiType> $$0, ResourceKey<PoiType> $$1, Set<BlockState> $$2, int $$3, int $$4) {
        PoiType $$5 = new PoiType($$2, $$3, $$4);
        Registry.register($$0, $$1, $$5);
        PoiTypes.registerBlockStates($$0.getOrThrow($$1), $$2);
        return $$5;
    }

    private static void registerBlockStates(Holder<PoiType> $$0, Set<BlockState> $$12) {
        $$12.forEach($$1 -> {
            Holder<PoiType> $$2 = TYPE_BY_STATE.put((BlockState)$$1, $$0);
            if ($$2 != null) {
                throw Util.pauseInIde(new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", $$1)));
            }
        });
    }

    public static Optional<Holder<PoiType>> forState(BlockState $$0) {
        return Optional.ofNullable(TYPE_BY_STATE.get($$0));
    }

    public static boolean hasPoi(BlockState $$0) {
        return TYPE_BY_STATE.containsKey($$0);
    }

    public static PoiType bootstrap(Registry<PoiType> $$0) {
        PoiTypes.register($$0, ARMORER, PoiTypes.getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
        PoiTypes.register($$0, BUTCHER, PoiTypes.getBlockStates(Blocks.SMOKER), 1, 1);
        PoiTypes.register($$0, CARTOGRAPHER, PoiTypes.getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
        PoiTypes.register($$0, CLERIC, PoiTypes.getBlockStates(Blocks.BREWING_STAND), 1, 1);
        PoiTypes.register($$0, FARMER, PoiTypes.getBlockStates(Blocks.COMPOSTER), 1, 1);
        PoiTypes.register($$0, FISHERMAN, PoiTypes.getBlockStates(Blocks.BARREL), 1, 1);
        PoiTypes.register($$0, FLETCHER, PoiTypes.getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
        PoiTypes.register($$0, LEATHERWORKER, CAULDRONS, 1, 1);
        PoiTypes.register($$0, LIBRARIAN, PoiTypes.getBlockStates(Blocks.LECTERN), 1, 1);
        PoiTypes.register($$0, MASON, PoiTypes.getBlockStates(Blocks.STONECUTTER), 1, 1);
        PoiTypes.register($$0, SHEPHERD, PoiTypes.getBlockStates(Blocks.LOOM), 1, 1);
        PoiTypes.register($$0, TOOLSMITH, PoiTypes.getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
        PoiTypes.register($$0, WEAPONSMITH, PoiTypes.getBlockStates(Blocks.GRINDSTONE), 1, 1);
        PoiTypes.register($$0, HOME, BEDS, 1, 1);
        PoiTypes.register($$0, MEETING, PoiTypes.getBlockStates(Blocks.BELL), 32, 6);
        PoiTypes.register($$0, BEEHIVE, PoiTypes.getBlockStates(Blocks.BEEHIVE), 0, 1);
        PoiTypes.register($$0, BEE_NEST, PoiTypes.getBlockStates(Blocks.BEE_NEST), 0, 1);
        PoiTypes.register($$0, NETHER_PORTAL, PoiTypes.getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
        PoiTypes.register($$0, LODESTONE, PoiTypes.getBlockStates(Blocks.LODESTONE), 0, 1);
        PoiTypes.register($$0, TEST_INSTANCE, PoiTypes.getBlockStates(Blocks.TEST_INSTANCE_BLOCK), 0, 1);
        return PoiTypes.register($$0, LIGHTNING_ROD, LIGHTNING_RODS, 0, 1);
    }
}

