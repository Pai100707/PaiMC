package net.minecraft.world.entity.npc.villager;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public record VillagerProfession(
   Component name,
   Predicate<Holder<PoiType>> heldJobSite,
   Predicate<Holder<PoiType>> acquirableJobSite,
   ImmutableSet<Item> requestedItems,
   ImmutableSet<Block> secondaryPoi,
   @Nullable SoundEvent workSound
) {
   public static final Predicate<Holder<PoiType>> ALL_ACQUIRABLE_JOBS = $$0 -> $$0.is(PoiTypeTags.ACQUIRABLE_JOB_SITE);
   public static final ResourceKey<VillagerProfession> NONE = createKey("none");
   public static final ResourceKey<VillagerProfession> ARMORER = createKey("armorer");
   public static final ResourceKey<VillagerProfession> BUTCHER = createKey("butcher");
   public static final ResourceKey<VillagerProfession> CARTOGRAPHER = createKey("cartographer");
   public static final ResourceKey<VillagerProfession> CLERIC = createKey("cleric");
   public static final ResourceKey<VillagerProfession> FARMER = createKey("farmer");
   public static final ResourceKey<VillagerProfession> FISHERMAN = createKey("fisherman");
   public static final ResourceKey<VillagerProfession> FLETCHER = createKey("fletcher");
   public static final ResourceKey<VillagerProfession> LEATHERWORKER = createKey("leatherworker");
   public static final ResourceKey<VillagerProfession> LIBRARIAN = createKey("librarian");
   public static final ResourceKey<VillagerProfession> MASON = createKey("mason");
   public static final ResourceKey<VillagerProfession> NITWIT = createKey("nitwit");
   public static final ResourceKey<VillagerProfession> SHEPHERD = createKey("shepherd");
   public static final ResourceKey<VillagerProfession> TOOLSMITH = createKey("toolsmith");
   public static final ResourceKey<VillagerProfession> WEAPONSMITH = createKey("weaponsmith");

   private static ResourceKey<VillagerProfession> createKey(String $$0) {
      return ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.withDefaultNamespace($$0));
   }

   private static VillagerProfession register(
      Registry<VillagerProfession> $$0, ResourceKey<VillagerProfession> $$1, ResourceKey<PoiType> $$2, @Nullable SoundEvent $$3
   ) {
      return register($$0, $$1, $$1x -> $$1x.is($$2), $$1x -> $$1x.is($$2), $$3);
   }

   private static VillagerProfession register(
      Registry<VillagerProfession> $$0,
      ResourceKey<VillagerProfession> $$1,
      Predicate<Holder<PoiType>> $$2,
      Predicate<Holder<PoiType>> $$3,
      @Nullable SoundEvent $$4
   ) {
      return register($$0, $$1, $$2, $$3, ImmutableSet.of(), ImmutableSet.of(), $$4);
   }

   private static VillagerProfession register(
      Registry<VillagerProfession> $$0,
      ResourceKey<VillagerProfession> $$1,
      ResourceKey<PoiType> $$2,
      ImmutableSet<Item> $$3,
      ImmutableSet<Block> $$4,
      @Nullable SoundEvent $$5
   ) {
      return register($$0, $$1, $$1x -> $$1x.is($$2), $$1x -> $$1x.is($$2), $$3, $$4, $$5);
   }

   private static VillagerProfession register(
      Registry<VillagerProfession> $$0,
      ResourceKey<VillagerProfession> $$1,
      Predicate<Holder<PoiType>> $$2,
      Predicate<Holder<PoiType>> $$3,
      ImmutableSet<Item> $$4,
      ImmutableSet<Block> $$5,
      @Nullable SoundEvent $$6
   ) {
      return (VillagerProfession)Registry.register(
         $$0,
         $$1,
         new VillagerProfession(
            Component.translatable("entity." + $$1.identifier().getNamespace() + ".villager." + $$1.identifier().getPath()), $$2, $$3, $$4, $$5, $$6
         )
      );
   }

   public static VillagerProfession bootstrap(Registry<VillagerProfession> $$0) {
      register($$0, NONE, PoiType.NONE, ALL_ACQUIRABLE_JOBS, null);
      register($$0, ARMORER, PoiTypes.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
      register($$0, BUTCHER, PoiTypes.BUTCHER, SoundEvents.VILLAGER_WORK_BUTCHER);
      register($$0, CARTOGRAPHER, PoiTypes.CARTOGRAPHER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER);
      register($$0, CLERIC, PoiTypes.CLERIC, SoundEvents.VILLAGER_WORK_CLERIC);
      register(
         $$0,
         FARMER,
         PoiTypes.FARMER,
         ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL),
         ImmutableSet.of(Blocks.FARMLAND),
         SoundEvents.VILLAGER_WORK_FARMER
      );
      register($$0, FISHERMAN, PoiTypes.FISHERMAN, SoundEvents.VILLAGER_WORK_FISHERMAN);
      register($$0, FLETCHER, PoiTypes.FLETCHER, SoundEvents.VILLAGER_WORK_FLETCHER);
      register($$0, LEATHERWORKER, PoiTypes.LEATHERWORKER, SoundEvents.VILLAGER_WORK_LEATHERWORKER);
      register($$0, LIBRARIAN, PoiTypes.LIBRARIAN, SoundEvents.VILLAGER_WORK_LIBRARIAN);
      register($$0, MASON, PoiTypes.MASON, SoundEvents.VILLAGER_WORK_MASON);
      register($$0, NITWIT, PoiType.NONE, PoiType.NONE, null);
      register($$0, SHEPHERD, PoiTypes.SHEPHERD, SoundEvents.VILLAGER_WORK_SHEPHERD);
      register($$0, TOOLSMITH, PoiTypes.TOOLSMITH, SoundEvents.VILLAGER_WORK_TOOLSMITH);
      return register($$0, WEAPONSMITH, PoiTypes.WEAPONSMITH, SoundEvents.VILLAGER_WORK_WEAPONSMITH);
   }
}
