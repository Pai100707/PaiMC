package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;

public record Enchantment(Component description, Enchantment.EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {
   public static final int MAX_LEVEL = 255;
   public static final Codec<Enchantment> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description),
            Enchantment.EnchantmentDefinition.CODEC.forGetter(Enchantment::definition),
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT)
               .optionalFieldOf("exclusive_set", HolderSet.direct(new Holder[0]))
               .forGetter(Enchantment::exclusiveSet),
            EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY).forGetter(Enchantment::effects)
         )
         .apply($$0, Enchantment::new)
   );
   public static final Codec<Holder<Enchantment>> CODEC = RegistryFixedCodec.create(Registries.ENCHANTMENT);
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Enchantment>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT);

   public static Enchantment.Cost constantCost(int $$0) {
      return new Enchantment.Cost($$0, 0);
   }

   public static Enchantment.Cost dynamicCost(int $$0, int $$1) {
      return new Enchantment.Cost($$0, $$1);
   }

   public static Enchantment.EnchantmentDefinition definition(
      HolderSet<net.minecraft.world.item.Item> $$0,
      HolderSet<net.minecraft.world.item.Item> $$1,
      int $$2,
      int $$3,
      Enchantment.Cost $$4,
      Enchantment.Cost $$5,
      int $$6,
      EquipmentSlotGroup... $$7
   ) {
      return new Enchantment.EnchantmentDefinition($$0, Optional.of($$1), $$2, $$3, $$4, $$5, $$6, List.of($$7));
   }

   public static Enchantment.EnchantmentDefinition definition(
      HolderSet<net.minecraft.world.item.Item> $$0, int $$1, int $$2, Enchantment.Cost $$3, Enchantment.Cost $$4, int $$5, EquipmentSlotGroup... $$6
   ) {
      return new Enchantment.EnchantmentDefinition($$0, Optional.empty(), $$1, $$2, $$3, $$4, $$5, List.of($$6));
   }

   public Map<EquipmentSlot, net.minecraft.world.item.ItemStack> getSlotItems(LivingEntity $$0) {
      Map<EquipmentSlot, net.minecraft.world.item.ItemStack> $$1 = Maps.newEnumMap(EquipmentSlot.class);

      for (EquipmentSlot $$2 : EquipmentSlot.VALUES) {
         if (this.matchingSlot($$2)) {
            net.minecraft.world.item.ItemStack $$3 = $$0.getItemBySlot($$2);
            if (!$$3.isEmpty()) {
               $$1.put($$2, $$3);
            }
         }
      }

      return $$1;
   }

   public HolderSet<net.minecraft.world.item.Item> getSupportedItems() {
      return this.definition.supportedItems();
   }

   public boolean matchingSlot(EquipmentSlot $$0) {
      return this.definition.slots().stream().anyMatch($$1 -> $$1.test($$0));
   }

   public boolean isPrimaryItem(net.minecraft.world.item.ItemStack $$0) {
      return this.isSupportedItem($$0) && (this.definition.primaryItems.isEmpty() || $$0.is(this.definition.primaryItems.get()));
   }

   public boolean isSupportedItem(net.minecraft.world.item.ItemStack $$0) {
      return $$0.is(this.definition.supportedItems);
   }

   public int getWeight() {
      return this.definition.weight();
   }

   public int getAnvilCost() {
      return this.definition.anvilCost();
   }

   public int getMinLevel() {
      return 1;
   }

   public int getMaxLevel() {
      return this.definition.maxLevel();
   }

   public int getMinCost(int $$0) {
      return this.definition.minCost().calculate($$0);
   }

   public int getMaxCost(int $$0) {
      return this.definition.maxCost().calculate($$0);
   }

   @Override
   public String toString() {
      return "Enchantment " + this.description.getString();
   }

   public static boolean areCompatible(Holder<Enchantment> $$0, Holder<Enchantment> $$1) {
      return !$$0.equals($$1) && !((Enchantment)$$0.value()).exclusiveSet.contains($$1) && !((Enchantment)$$1.value()).exclusiveSet.contains($$0);
   }

   public static Component getFullname(Holder<Enchantment> $$0, int $$1) {
      MutableComponent $$2 = ((Enchantment)$$0.value()).description.copy();
      if ($$0.is(EnchantmentTags.CURSE)) {
         $$2 = ComponentUtils.mergeStyles($$2, Style.EMPTY.withColor(ChatFormatting.RED));
      } else {
         $$2 = ComponentUtils.mergeStyles($$2, Style.EMPTY.withColor(ChatFormatting.GRAY));
      }

      if ($$1 != 1 || ((Enchantment)$$0.value()).getMaxLevel() != 1) {
         $$2.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + $$1));
      }

      return $$2;
   }

   public boolean canEnchant(net.minecraft.world.item.ItemStack $$0) {
      return this.definition.supportedItems().contains($$0.getItemHolder());
   }

   public <T> List<T> getEffects(DataComponentType<List<T>> $$0) {
      return (List<T>)this.effects.getOrDefault($$0, List.of());
   }

   public boolean isImmuneToDamage(ServerLevel $$0, int $$1, Entity $$2, DamageSource $$3) {
      LootContext $$4 = damageContext($$0, $$1, $$2, $$3);

      for (ConditionalEffect<DamageImmunity> $$5 : this.getEffects(EnchantmentEffectComponents.DAMAGE_IMMUNITY)) {
         if ($$5.matches($$4)) {
            return true;
         }
      }

      return false;
   }

   public void modifyDamageProtection(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, DamageSource $$4, MutableFloat $$5) {
      LootContext $$6 = damageContext($$0, $$1, $$3, $$4);

      for (ConditionalEffect<EnchantmentValueEffect> $$7 : this.getEffects(EnchantmentEffectComponents.DAMAGE_PROTECTION)) {
         if ($$7.matches($$6)) {
            $$5.setValue($$7.effect().process($$1, $$3.getRandom(), $$5.floatValue()));
         }
      }
   }

   public void modifyDurabilityChange(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, MutableFloat $$3) {
      this.modifyItemFilteredCount(EnchantmentEffectComponents.ITEM_DAMAGE, $$0, $$1, $$2, $$3);
   }

   public void modifyAmmoCount(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, MutableFloat $$3) {
      this.modifyItemFilteredCount(EnchantmentEffectComponents.AMMO_USE, $$0, $$1, $$2, $$3);
   }

   public void modifyPiercingCount(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, MutableFloat $$3) {
      this.modifyItemFilteredCount(EnchantmentEffectComponents.PROJECTILE_PIERCING, $$0, $$1, $$2, $$3);
   }

   public void modifyBlockExperience(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, MutableFloat $$3) {
      this.modifyItemFilteredCount(EnchantmentEffectComponents.BLOCK_EXPERIENCE, $$0, $$1, $$2, $$3);
   }

   public void modifyMobExperience(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, MutableFloat $$4) {
      this.modifyEntityFilteredValue(EnchantmentEffectComponents.MOB_EXPERIENCE, $$0, $$1, $$2, $$3, $$4);
   }

   public void modifyDurabilityToRepairFromXp(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, MutableFloat $$3) {
      this.modifyItemFilteredCount(EnchantmentEffectComponents.REPAIR_WITH_XP, $$0, $$1, $$2, $$3);
   }

   public void modifyTridentReturnToOwnerAcceleration(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, MutableFloat $$4) {
      this.modifyEntityFilteredValue(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, $$0, $$1, $$2, $$3, $$4);
   }

   public void modifyTridentSpinAttackStrength(RandomSource $$0, int $$1, MutableFloat $$2) {
      this.modifyUnfilteredValue(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, $$0, $$1, $$2);
   }

   public void modifyFishingTimeReduction(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, MutableFloat $$4) {
      this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_TIME_REDUCTION, $$0, $$1, $$2, $$3, $$4);
   }

   public void modifyFishingLuckBonus(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, MutableFloat $$4) {
      this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_LUCK_BONUS, $$0, $$1, $$2, $$3, $$4);
   }

   public void modifyDamage(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, DamageSource $$4, MutableFloat $$5) {
      this.modifyDamageFilteredValue(EnchantmentEffectComponents.DAMAGE, $$0, $$1, $$2, $$3, $$4, $$5);
   }

   public void modifyFallBasedDamage(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, DamageSource $$4, MutableFloat $$5) {
      this.modifyDamageFilteredValue(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, $$0, $$1, $$2, $$3, $$4, $$5);
   }

   public void modifyKnockback(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, DamageSource $$4, MutableFloat $$5) {
      this.modifyDamageFilteredValue(EnchantmentEffectComponents.KNOCKBACK, $$0, $$1, $$2, $$3, $$4, $$5);
   }

   public void modifyArmorEffectivness(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, DamageSource $$4, MutableFloat $$5) {
      this.modifyDamageFilteredValue(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, $$0, $$1, $$2, $$3, $$4, $$5);
   }

   public void doPostAttack(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, EnchantmentTarget $$3, Entity $$4, DamageSource $$5) {
      for (TargetedConditionalEffect<EnchantmentEntityEffect> $$6 : this.getEffects(EnchantmentEffectComponents.POST_ATTACK)) {
         if ($$3 == $$6.enchanted()) {
            doPostAttack($$6, $$0, $$1, $$2, $$4, $$5);
         }
      }
   }

   public static void doPostAttack(
      TargetedConditionalEffect<EnchantmentEntityEffect> $$0, ServerLevel $$1, int $$2, EnchantedItemInUse $$3, Entity $$4, DamageSource $$5
   ) {
      if ($$0.matches(damageContext($$1, $$2, $$4, $$5))) {
         Entity $$6 = switch ($$0.affected()) {
            case ATTACKER -> $$5.getEntity();
            case DAMAGING_ENTITY -> $$5.getDirectEntity();
            case VICTIM -> $$4;
         };
         if ($$6 != null) {
            $$0.effect().apply($$1, $$2, $$3, $$6, $$6.position());
         }
      }
   }

   public void doLunge(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3) {
      applyEffects(
         this.getEffects(EnchantmentEffectComponents.POST_PIERCING_ATTACK),
         entityContext($$0, $$1, $$3, $$3.position()),
         $$4 -> $$4.apply($$0, $$1, $$2, $$3, $$3.position())
      );
   }

   public void modifyProjectileCount(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, MutableFloat $$4) {
      this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_COUNT, $$0, $$1, $$2, $$3, $$4);
   }

   public void modifyProjectileSpread(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2, Entity $$3, MutableFloat $$4) {
      this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_SPREAD, $$0, $$1, $$2, $$3, $$4);
   }

   public void modifyCrossbowChargeTime(RandomSource $$0, int $$1, MutableFloat $$2) {
      this.modifyUnfilteredValue(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, $$0, $$1, $$2);
   }

   public void modifyUnfilteredValue(DataComponentType<EnchantmentValueEffect> $$0, RandomSource $$1, int $$2, MutableFloat $$3) {
      EnchantmentValueEffect $$4 = (EnchantmentValueEffect)this.effects.get($$0);
      if ($$4 != null) {
         $$3.setValue($$4.process($$2, $$1, $$3.floatValue()));
      }
   }

   public void tick(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3) {
      applyEffects(
         this.getEffects(EnchantmentEffectComponents.TICK), entityContext($$0, $$1, $$3, $$3.position()), $$4 -> $$4.apply($$0, $$1, $$2, $$3, $$3.position())
      );
   }

   public void onProjectileSpawned(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3) {
      applyEffects(
         this.getEffects(EnchantmentEffectComponents.PROJECTILE_SPAWNED),
         entityContext($$0, $$1, $$3, $$3.position()),
         $$4 -> $$4.apply($$0, $$1, $$2, $$3, $$3.position())
      );
   }

   public void onHitBlock(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, Entity $$3, Vec3 $$4, BlockState $$5) {
      applyEffects(
         this.getEffects(EnchantmentEffectComponents.HIT_BLOCK), blockHitContext($$0, $$1, $$3, $$4, $$5), $$5x -> $$5x.apply($$0, $$1, $$2, $$3, $$4)
      );
   }

   private void modifyItemFilteredCount(
      DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> $$0,
      ServerLevel $$1,
      int $$2,
      net.minecraft.world.item.ItemStack $$3,
      MutableFloat $$4
   ) {
      applyEffects(this.getEffects($$0), itemContext($$1, $$2, $$3), $$3x -> $$4.setValue($$3x.process($$2, $$1.getRandom(), $$4.floatValue())));
   }

   private void modifyEntityFilteredValue(
      DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> $$0,
      ServerLevel $$1,
      int $$2,
      net.minecraft.world.item.ItemStack $$3,
      Entity $$4,
      MutableFloat $$5
   ) {
      applyEffects(
         this.getEffects($$0), entityContext($$1, $$2, $$4, $$4.position()), $$3x -> $$5.setValue($$3x.process($$2, $$4.getRandom(), $$5.floatValue()))
      );
   }

   private void modifyDamageFilteredValue(
      DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> $$0,
      ServerLevel $$1,
      int $$2,
      net.minecraft.world.item.ItemStack $$3,
      Entity $$4,
      DamageSource $$5,
      MutableFloat $$6
   ) {
      applyEffects(this.getEffects($$0), damageContext($$1, $$2, $$4, $$5), $$3x -> $$6.setValue($$3x.process($$2, $$4.getRandom(), $$6.floatValue())));
   }

   public static LootContext damageContext(ServerLevel $$0, int $$1, Entity $$2, DamageSource $$3) {
      LootParams $$4 = new net.minecraft.world.level.storage.loot.LootParams.Builder($$0)
         .withParameter(LootContextParams.THIS_ENTITY, $$2)
         .withParameter(LootContextParams.ENCHANTMENT_LEVEL, $$1)
         .withParameter(LootContextParams.ORIGIN, $$2.position())
         .withParameter(LootContextParams.DAMAGE_SOURCE, $$3)
         .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, $$3.getEntity())
         .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, $$3.getDirectEntity())
         .create(LootContextParamSets.ENCHANTED_DAMAGE);
      return new net.minecraft.world.level.storage.loot.LootContext.Builder($$4).create(Optional.empty());
   }

   private static LootContext itemContext(ServerLevel $$0, int $$1, net.minecraft.world.item.ItemStack $$2) {
      LootParams $$3 = new net.minecraft.world.level.storage.loot.LootParams.Builder($$0)
         .withParameter(LootContextParams.TOOL, $$2)
         .withParameter(LootContextParams.ENCHANTMENT_LEVEL, $$1)
         .create(LootContextParamSets.ENCHANTED_ITEM);
      return new net.minecraft.world.level.storage.loot.LootContext.Builder($$3).create(Optional.empty());
   }

   private static LootContext locationContext(ServerLevel $$0, int $$1, Entity $$2, boolean $$3) {
      LootParams $$4 = new net.minecraft.world.level.storage.loot.LootParams.Builder($$0)
         .withParameter(LootContextParams.THIS_ENTITY, $$2)
         .withParameter(LootContextParams.ENCHANTMENT_LEVEL, $$1)
         .withParameter(LootContextParams.ORIGIN, $$2.position())
         .withParameter(LootContextParams.ENCHANTMENT_ACTIVE, $$3)
         .create(LootContextParamSets.ENCHANTED_LOCATION);
      return new net.minecraft.world.level.storage.loot.LootContext.Builder($$4).create(Optional.empty());
   }

   private static LootContext entityContext(ServerLevel $$0, int $$1, Entity $$2, Vec3 $$3) {
      LootParams $$4 = new net.minecraft.world.level.storage.loot.LootParams.Builder($$0)
         .withParameter(LootContextParams.THIS_ENTITY, $$2)
         .withParameter(LootContextParams.ENCHANTMENT_LEVEL, $$1)
         .withParameter(LootContextParams.ORIGIN, $$3)
         .create(LootContextParamSets.ENCHANTED_ENTITY);
      return new net.minecraft.world.level.storage.loot.LootContext.Builder($$4).create(Optional.empty());
   }

   private static LootContext blockHitContext(ServerLevel $$0, int $$1, Entity $$2, Vec3 $$3, BlockState $$4) {
      LootParams $$5 = new net.minecraft.world.level.storage.loot.LootParams.Builder($$0)
         .withParameter(LootContextParams.THIS_ENTITY, $$2)
         .withParameter(LootContextParams.ENCHANTMENT_LEVEL, $$1)
         .withParameter(LootContextParams.ORIGIN, $$3)
         .withParameter(LootContextParams.BLOCK_STATE, $$4)
         .create(LootContextParamSets.HIT_BLOCK);
      return new net.minecraft.world.level.storage.loot.LootContext.Builder($$5).create(Optional.empty());
   }

   private static <T> void applyEffects(List<ConditionalEffect<T>> $$0, LootContext $$1, Consumer<T> $$2) {
      for (ConditionalEffect<T> $$3 : $$0) {
         if ($$3.matches($$1)) {
            $$2.accept($$3.effect());
         }
      }
   }

   public void runLocationChangedEffects(ServerLevel $$0, int $$1, EnchantedItemInUse $$2, LivingEntity $$3) {
      EquipmentSlot $$4 = $$2.inSlot();
      if ($$4 != null) {
         Map<Enchantment, Set<EnchantmentLocationBasedEffect>> $$5 = $$3.activeLocationDependentEnchantments($$4);
         if (!this.matchingSlot($$4)) {
            Set<EnchantmentLocationBasedEffect> $$6 = $$5.remove(this);
            if ($$6 != null) {
               $$6.forEach($$3x -> $$3x.onDeactivated($$2, $$3, $$3.position(), $$1));
            }
         } else {
            Set<EnchantmentLocationBasedEffect> $$7 = $$5.get(this);

            for (ConditionalEffect<EnchantmentLocationBasedEffect> $$8 : this.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
               EnchantmentLocationBasedEffect $$9 = $$8.effect();
               boolean $$10 = $$7 != null && $$7.contains($$9);
               if ($$8.matches(locationContext($$0, $$1, $$3, $$10))) {
                  if (!$$10) {
                     if ($$7 == null) {
                        $$7 = new ObjectArraySet();
                        $$5.put(this, $$7);
                     }

                     $$7.add($$9);
                  }

                  $$9.onChangedBlock($$0, $$1, $$2, $$3, $$3.position(), !$$10);
               } else if ($$7 != null && $$7.remove($$9)) {
                  $$9.onDeactivated($$2, $$3, $$3.position(), $$1);
               }
            }

            if ($$7 != null && $$7.isEmpty()) {
               $$5.remove(this);
            }
         }
      }
   }

   public void stopLocationBasedEffects(int $$0, EnchantedItemInUse $$1, LivingEntity $$2) {
      EquipmentSlot $$3 = $$1.inSlot();
      if ($$3 != null) {
         Set<EnchantmentLocationBasedEffect> $$4 = (Set<EnchantmentLocationBasedEffect>)$$2.activeLocationDependentEnchantments($$3).remove(this);
         if ($$4 != null) {
            for (EnchantmentLocationBasedEffect $$5 : $$4) {
               $$5.onDeactivated($$1, $$2, $$2.position(), $$0);
            }
         }
      }
   }

   public static Enchantment.Builder enchantment(Enchantment.EnchantmentDefinition $$0) {
      return new Enchantment.Builder($$0);
   }

   public static class Builder {
      private final Enchantment.EnchantmentDefinition definition;
      private HolderSet<Enchantment> exclusiveSet = HolderSet.direct(new Holder[0]);
      private final Map<DataComponentType<?>, List<?>> effectLists = new HashMap<>();
      private final net.minecraft.core.component.DataComponentMap.Builder effectMapBuilder = DataComponentMap.builder();

      public Builder(Enchantment.EnchantmentDefinition $$0) {
         this.definition = $$0;
      }

      public Enchantment.Builder exclusiveWith(HolderSet<Enchantment> $$0) {
         this.exclusiveSet = $$0;
         return this;
      }

      public <E> Enchantment.Builder withEffect(
         DataComponentType<List<ConditionalEffect<E>>> $$0, E $$1, net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$2
      ) {
         this.<ConditionalEffect<E>>getEffectsList($$0).add(new ConditionalEffect<>($$1, Optional.of($$2.build())));
         return this;
      }

      public <E> Enchantment.Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> $$0, E $$1) {
         this.<ConditionalEffect<E>>getEffectsList($$0).add(new ConditionalEffect<>($$1, Optional.empty()));
         return this;
      }

      public <E> Enchantment.Builder withEffect(
         DataComponentType<List<TargetedConditionalEffect<E>>> $$0,
         EnchantmentTarget $$1,
         EnchantmentTarget $$2,
         E $$3,
         net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$4
      ) {
         this.<TargetedConditionalEffect<E>>getEffectsList($$0).add(new TargetedConditionalEffect<>($$1, $$2, $$3, Optional.of($$4.build())));
         return this;
      }

      public <E> Enchantment.Builder withEffect(DataComponentType<List<TargetedConditionalEffect<E>>> $$0, EnchantmentTarget $$1, EnchantmentTarget $$2, E $$3) {
         this.<TargetedConditionalEffect<E>>getEffectsList($$0).add(new TargetedConditionalEffect<>($$1, $$2, $$3, Optional.empty()));
         return this;
      }

      public Enchantment.Builder withEffect(DataComponentType<List<EnchantmentAttributeEffect>> $$0, EnchantmentAttributeEffect $$1) {
         this.<EnchantmentAttributeEffect>getEffectsList($$0).add($$1);
         return this;
      }

      public <E> Enchantment.Builder withSpecialEffect(DataComponentType<E> $$0, E $$1) {
         this.effectMapBuilder.set($$0, $$1);
         return this;
      }

      public Enchantment.Builder withEffect(DataComponentType<Unit> $$0) {
         this.effectMapBuilder.set($$0, Unit.INSTANCE);
         return this;
      }

      private <E> List<E> getEffectsList(DataComponentType<List<E>> $$0) {
         return (List<E>)this.effectLists.computeIfAbsent($$0, $$1 -> {
            ArrayList<E> $$2 = new ArrayList<>();
            this.effectMapBuilder.set($$0, $$2);
            return $$2;
         });
      }

      public Enchantment build(Identifier $$0) {
         return new Enchantment(
            Component.translatable(Util.makeDescriptionId("enchantment", $$0)), this.definition, this.exclusiveSet, this.effectMapBuilder.build()
         );
      }
   }

   public record Cost(int base, int perLevelAboveFirst) {
      public static final Codec<Enchantment.Cost> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.INT.fieldOf("base").forGetter(Enchantment.Cost::base),
               Codec.INT.fieldOf("per_level_above_first").forGetter(Enchantment.Cost::perLevelAboveFirst)
            )
            .apply($$0, Enchantment.Cost::new)
      );

      public int calculate(int $$0) {
         return this.base + this.perLevelAboveFirst * ($$0 - 1);
      }
   }

   public record EnchantmentDefinition(
      HolderSet<net.minecraft.world.item.Item> supportedItems,
      Optional<HolderSet<net.minecraft.world.item.Item>> primaryItems,
      int weight,
      int maxLevel,
      Enchantment.Cost minCost,
      Enchantment.Cost maxCost,
      int anvilCost,
      List<EquipmentSlotGroup> slots
   ) {
      public static final MapCodec<Enchantment.EnchantmentDefinition> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("supported_items").forGetter(Enchantment.EnchantmentDefinition::supportedItems),
               RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("primary_items").forGetter(Enchantment.EnchantmentDefinition::primaryItems),
               ExtraCodecs.intRange(1, 1024).fieldOf("weight").forGetter(Enchantment.EnchantmentDefinition::weight),
               ExtraCodecs.intRange(1, 255).fieldOf("max_level").forGetter(Enchantment.EnchantmentDefinition::maxLevel),
               Enchantment.Cost.CODEC.fieldOf("min_cost").forGetter(Enchantment.EnchantmentDefinition::minCost),
               Enchantment.Cost.CODEC.fieldOf("max_cost").forGetter(Enchantment.EnchantmentDefinition::maxCost),
               ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anvil_cost").forGetter(Enchantment.EnchantmentDefinition::anvilCost),
               EquipmentSlotGroup.CODEC.listOf().fieldOf("slots").forGetter(Enchantment.EnchantmentDefinition::slots)
            )
            .apply($$0, Enchantment.EnchantmentDefinition::new)
      );
   }
}
