/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.ChangeDimensionTrigger;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.advancements.criterion.LevitationTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class VanillaTheEndAdvancements
implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider $$0, Consumer<AdvancementHolder> $$1) {
        HolderGetter $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
        AdvancementHolder $$3 = Advancement.Builder.advancement().display(Blocks.END_STONE, (Component)Component.translatable("advancements.end.root.title"), (Component)Component.translatable("advancements.end.root.description"), Identifier.withDefaultNamespace("gui/advancements/backgrounds/end"), AdvancementType.TASK, false, false, false).addCriterion("entered_end", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END)).save($$1, "end/root");
        AdvancementHolder $$4 = Advancement.Builder.advancement().parent($$3).display(Blocks.DRAGON_HEAD, (Component)Component.translatable("advancements.end.kill_dragon.title"), (Component)Component.translatable("advancements.end.kill_dragon.description"), null, AdvancementType.TASK, true, true, false).addCriterion("killed_dragon", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of($$2, EntityType.ENDER_DRAGON))).save($$1, "end/kill_dragon");
        AdvancementHolder $$5 = Advancement.Builder.advancement().parent($$4).display(Items.ENDER_PEARL, (Component)Component.translatable("advancements.end.enter_end_gateway.title"), (Component)Component.translatable("advancements.end.enter_end_gateway.description"), null, AdvancementType.TASK, true, true, false).addCriterion("entered_end_gateway", EnterBlockTrigger.TriggerInstance.entersBlock(Blocks.END_GATEWAY)).save($$1, "end/enter_end_gateway");
        Advancement.Builder.advancement().parent($$4).display(Items.END_CRYSTAL, (Component)Component.translatable("advancements.end.respawn_dragon.title"), (Component)Component.translatable("advancements.end.respawn_dragon.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("summoned_dragon", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of($$2, EntityType.ENDER_DRAGON))).save($$1, "end/respawn_dragon");
        AdvancementHolder $$6 = Advancement.Builder.advancement().parent($$5).display(Blocks.PURPUR_BLOCK, (Component)Component.translatable("advancements.end.find_end_city.title"), (Component)Component.translatable("advancements.end.find_end_city.description"), null, AdvancementType.TASK, true, true, false).addCriterion("in_city", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure($$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.END_CITY)))).save($$1, "end/find_end_city");
        Advancement.Builder.advancement().parent($$4).display(Items.DRAGON_BREATH, (Component)Component.translatable("advancements.end.dragon_breath.title"), (Component)Component.translatable("advancements.end.dragon_breath.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("dragon_breath", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DRAGON_BREATH)).save($$1, "end/dragon_breath");
        Advancement.Builder.advancement().parent($$6).display(Items.SHULKER_SHELL, (Component)Component.translatable("advancements.end.levitate.title"), (Component)Component.translatable("advancements.end.levitate.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("levitated", LevitationTrigger.TriggerInstance.levitated(DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(50.0)))).save($$1, "end/levitate");
        Advancement.Builder.advancement().parent($$6).display(Items.ELYTRA, (Component)Component.translatable("advancements.end.elytra.title"), (Component)Component.translatable("advancements.end.elytra.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("elytra", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ELYTRA)).save($$1, "end/elytra");
        Advancement.Builder.advancement().parent($$4).display(Blocks.DRAGON_EGG, (Component)Component.translatable("advancements.end.dragon_egg.title"), (Component)Component.translatable("advancements.end.dragon_egg.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("dragon_egg", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.DRAGON_EGG)).save($$1, "end/dragon_egg");
    }
}

