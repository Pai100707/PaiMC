/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record AdvancementRewards(int experience, List<ResourceKey<LootTable>> loot, List<ResourceKey<Recipe<?>>> recipes, Optional<CacheableFunction> function) {
    public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.INT.optionalFieldOf("experience", (Object)0).forGetter(AdvancementRewards::experience), (App)LootTable.KEY_CODEC.listOf().optionalFieldOf("loot", List.of()).forGetter(AdvancementRewards::loot), (App)Recipe.KEY_CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(AdvancementRewards::recipes), (App)CacheableFunction.CODEC.optionalFieldOf("function").forGetter(AdvancementRewards::function)).apply((Applicative)$$0, AdvancementRewards::new));
    public static final AdvancementRewards EMPTY = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

    public void grant(ServerPlayer $$0) {
        $$0.giveExperiencePoints(this.experience);
        ServerLevel $$12 = $$0.level();
        MinecraftServer $$22 = $$12.getServer();
        LootParams $$3 = new LootParams.Builder($$12).withParameter(LootContextParams.THIS_ENTITY, $$0).withParameter(LootContextParams.ORIGIN, $$0.position()).create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean $$4 = false;
        for (ResourceKey<LootTable> $$5 : this.loot) {
            for (ItemStack $$6 : $$22.reloadableRegistries().getLootTable($$5).getRandomItems($$3)) {
                if ($$0.addItem($$6)) {
                    $$12.playSound(null, $$0.getX(), $$0.getY(), $$0.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, (($$0.getRandom().nextFloat() - $$0.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                    $$4 = true;
                    continue;
                }
                ItemEntity $$7 = $$0.drop($$6, false);
                if ($$7 == null) continue;
                $$7.setNoPickUpDelay();
                $$7.setTarget($$0.getUUID());
            }
        }
        if ($$4) {
            $$0.containerMenu.broadcastChanges();
        }
        if (!this.recipes.isEmpty()) {
            $$0.awardRecipesByKey(this.recipes);
        }
        this.function.flatMap($$1 -> $$1.get($$22.getFunctions())).ifPresent($$2 -> $$22.getFunctions().execute((CommandFunction<CommandSourceStack>)$$2, $$0.createCommandSourceStack().withSuppressedOutput().withPermission(LevelBasedPermissionSet.GAMEMASTER)));
    }

    public static class Builder {
        private int experience;
        private final ImmutableList.Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceKey<Recipe<?>>> recipes = ImmutableList.builder();
        private Optional<Identifier> function = Optional.empty();

        public static Builder experience(int $$0) {
            return new Builder().addExperience($$0);
        }

        public Builder addExperience(int $$0) {
            this.experience += $$0;
            return this;
        }

        public static Builder loot(ResourceKey<LootTable> $$0) {
            return new Builder().addLootTable($$0);
        }

        public Builder addLootTable(ResourceKey<LootTable> $$0) {
            this.loot.add($$0);
            return this;
        }

        public static Builder recipe(ResourceKey<Recipe<?>> $$0) {
            return new Builder().addRecipe($$0);
        }

        public Builder addRecipe(ResourceKey<Recipe<?>> $$0) {
            this.recipes.add($$0);
            return this;
        }

        public static Builder function(Identifier $$0) {
            return new Builder().runs($$0);
        }

        public Builder runs(Identifier $$0) {
            this.function = Optional.of($$0);
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, (List<ResourceKey<LootTable>>)this.loot.build(), (List<ResourceKey<Recipe<?>>>)this.recipes.build(), this.function.map(CacheableFunction::new));
        }
    }
}

