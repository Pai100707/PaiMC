package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.ChangeDimensionTrigger.TriggerInstance;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class VanillaTheEndAdvancements implements AdvancementSubProvider {
   @Override
   public void generate(Provider $$0, Consumer<AdvancementHolder> $$1) {
      HolderGetter<EntityType<?>> $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
      AdvancementHolder $$3 = Builder.advancement()
         .display(
            Blocks.END_STONE,
            Component.translatable("advancements.end.root.title"),
            Component.translatable("advancements.end.root.description"),
            Identifier.withDefaultNamespace("gui/advancements/backgrounds/end"),
            AdvancementType.TASK,
            false,
            false,
            false
         )
         .addCriterion("entered_end", TriggerInstance.changedDimensionTo(Level.END))
         .save($$1, "end/root");
      AdvancementHolder $$4 = Builder.advancement()
         .parent($$3)
         .display(
            Blocks.DRAGON_HEAD,
            Component.translatable("advancements.end.kill_dragon.title"),
            Component.translatable("advancements.end.kill_dragon.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "killed_dragon",
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.ENDER_DRAGON)
            )
         )
         .save($$1, "end/kill_dragon");
      AdvancementHolder $$5 = Builder.advancement()
         .parent($$4)
         .display(
            Items.ENDER_PEARL,
            Component.translatable("advancements.end.enter_end_gateway.title"),
            Component.translatable("advancements.end.enter_end_gateway.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("entered_end_gateway", net.minecraft.advancements.criterion.EnterBlockTrigger.TriggerInstance.entersBlock(Blocks.END_GATEWAY))
         .save($$1, "end/enter_end_gateway");
      Builder.advancement()
         .parent($$4)
         .display(
            Items.END_CRYSTAL,
            Component.translatable("advancements.end.respawn_dragon.title"),
            Component.translatable("advancements.end.respawn_dragon.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion(
            "summoned_dragon",
            net.minecraft.advancements.criterion.SummonedEntityTrigger.TriggerInstance.summonedEntity(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.ENDER_DRAGON)
            )
         )
         .save($$1, "end/respawn_dragon");
      AdvancementHolder $$6 = Builder.advancement()
         .parent($$5)
         .display(
            Blocks.PURPUR_BLOCK,
            Component.translatable("advancements.end.find_end_city.title"),
            Component.translatable("advancements.end.find_end_city.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "in_city",
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.located(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.inStructure(
                  $$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.END_CITY)
               )
            )
         )
         .save($$1, "end/find_end_city");
      Builder.advancement()
         .parent($$4)
         .display(
            Items.DRAGON_BREATH,
            Component.translatable("advancements.end.dragon_breath.title"),
            Component.translatable("advancements.end.dragon_breath.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion(
            "dragon_breath", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.DRAGON_BREATH})
         )
         .save($$1, "end/dragon_breath");
      Builder.advancement()
         .parent($$6)
         .display(
            Items.SHULKER_SHELL,
            Component.translatable("advancements.end.levitate.title"),
            Component.translatable("advancements.end.levitate.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .addCriterion(
            "levitated", net.minecraft.advancements.criterion.LevitationTrigger.TriggerInstance.levitated(DistancePredicate.vertical(Doubles.atLeast(50.0)))
         )
         .save($$1, "end/levitate");
      Builder.advancement()
         .parent($$6)
         .display(
            Items.ELYTRA,
            Component.translatable("advancements.end.elytra.title"),
            Component.translatable("advancements.end.elytra.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion("elytra", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.ELYTRA}))
         .save($$1, "end/elytra");
      Builder.advancement()
         .parent($$4)
         .display(
            Blocks.DRAGON_EGG,
            Component.translatable("advancements.end.dragon_egg.title"),
            Component.translatable("advancements.end.dragon_egg.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion("dragon_egg", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Blocks.DRAGON_EGG}))
         .save($$1, "end/dragon_egg");
   }
}
