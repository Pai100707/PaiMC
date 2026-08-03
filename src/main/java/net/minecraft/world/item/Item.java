package net.minecraft.world.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Item implements FeatureElement, ItemLike {
   public static final Codec<Holder<net.minecraft.world.item.Item>> CODEC = BuiltInRegistries.ITEM
      .holderByNameCodec()
      .validate(
         $$0 -> $$0.is(net.minecraft.world.item.Items.AIR.builtInRegistryHolder())
            ? DataResult.error(() -> "Item must not be minecraft:air")
            : DataResult.success($$0)
      );
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<net.minecraft.world.item.Item>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Map<Block, net.minecraft.world.item.Item> BY_BLOCK = Maps.newHashMap();
   public static final Identifier BASE_ATTACK_DAMAGE_ID = Identifier.withDefaultNamespace("base_attack_damage");
   public static final Identifier BASE_ATTACK_SPEED_ID = Identifier.withDefaultNamespace("base_attack_speed");
   public static final int DEFAULT_MAX_STACK_SIZE = 64;
   public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
   public static final int MAX_BAR_WIDTH = 13;
   protected static final int APPROXIMATELY_INFINITE_USE_DURATION = 72000;
   private final Reference<net.minecraft.world.item.Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
   private final DataComponentMap components;
   @Nullable
   private final net.minecraft.world.item.Item craftingRemainingItem;
   protected final String descriptionId;
   private final FeatureFlagSet requiredFeatures;

   public static int getId(net.minecraft.world.item.Item $$0) {
      return $$0 == null ? 0 : BuiltInRegistries.ITEM.getId($$0);
   }

   public static net.minecraft.world.item.Item byId(int $$0) {
      return (net.minecraft.world.item.Item)BuiltInRegistries.ITEM.byId($$0);
   }

   @Deprecated
   public static net.minecraft.world.item.Item byBlock(Block $$0) {
      return BY_BLOCK.getOrDefault($$0, net.minecraft.world.item.Items.AIR);
   }

   public Item(net.minecraft.world.item.Item.Properties $$0) {
      this.descriptionId = $$0.effectiveDescriptionId();
      this.components = $$0.buildAndValidateComponents(Component.translatable(this.descriptionId), $$0.effectiveModel());
      this.craftingRemainingItem = $$0.craftingRemainingItem;
      this.requiredFeatures = $$0.requiredFeatures;
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         String $$1 = this.getClass().getSimpleName();
         if (!$$1.endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", $$1);
         }
      }
   }

   @Deprecated
   public Reference<net.minecraft.world.item.Item> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   public DataComponentMap components() {
      return this.components;
   }

   public int getDefaultMaxStackSize() {
      return (Integer)this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
   }

   public void onUseTick(Level $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2, int $$3) {
   }

   public void onDestroyed(ItemEntity $$0) {
   }

   public boolean canDestroyBlock(net.minecraft.world.item.ItemStack $$0, BlockState $$1, Level $$2, BlockPos $$3, LivingEntity $$4) {
      Tool $$5 = (Tool)$$0.get(DataComponents.TOOL);
      return $$5 != null && !$$5.canDestroyBlocksInCreative() ? !($$4 instanceof Player $$6 && $$6.getAbilities().instabuild) : true;
   }

   public net.minecraft.world.item.Item asItem() {
      return this;
   }

   public InteractionResult useOn(UseOnContext $$0) {
      return InteractionResult.PASS;
   }

   public float getDestroySpeed(net.minecraft.world.item.ItemStack $$0, BlockState $$1) {
      Tool $$2 = (Tool)$$0.get(DataComponents.TOOL);
      return $$2 != null ? $$2.getMiningSpeed($$1) : 1.0F;
   }

   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      Consumable $$4 = (Consumable)$$3.get(DataComponents.CONSUMABLE);
      if ($$4 != null) {
         return $$4.startConsuming($$1, $$3, $$2);
      } else {
         Equippable $$5 = (Equippable)$$3.get(DataComponents.EQUIPPABLE);
         if ($$5 != null && $$5.swappable()) {
            return $$5.swapWithEquipmentSlot($$3, $$1);
         } else if ($$3.has(DataComponents.BLOCKS_ATTACKS)) {
            $$1.startUsingItem($$2);
            return InteractionResult.CONSUME;
         } else {
            KineticWeapon $$6 = (KineticWeapon)$$3.get(DataComponents.KINETIC_WEAPON);
            if ($$6 != null) {
               $$1.startUsingItem($$2);
               $$6.makeSound($$1);
               return InteractionResult.CONSUME;
            } else {
               return InteractionResult.PASS;
            }
         }
      }
   }

   public net.minecraft.world.item.ItemStack finishUsingItem(net.minecraft.world.item.ItemStack $$0, Level $$1, LivingEntity $$2) {
      Consumable $$3 = (Consumable)$$0.get(DataComponents.CONSUMABLE);
      return $$3 != null ? $$3.onConsume($$1, $$2, $$0) : $$0;
   }

   public boolean isBarVisible(net.minecraft.world.item.ItemStack $$0) {
      return $$0.isDamaged();
   }

   public int getBarWidth(net.minecraft.world.item.ItemStack $$0) {
      return Mth.clamp(Math.round(13.0F - $$0.getDamageValue() * 13.0F / $$0.getMaxDamage()), 0, 13);
   }

   public int getBarColor(net.minecraft.world.item.ItemStack $$0) {
      int $$1 = $$0.getMaxDamage();
      float $$2 = Math.max(0.0F, ((float)$$1 - $$0.getDamageValue()) / $$1);
      return Mth.hsvToRgb($$2 / 3.0F, 1.0F, 1.0F);
   }

   public boolean overrideStackedOnOther(net.minecraft.world.item.ItemStack $$0, Slot $$1, ClickAction $$2, Player $$3) {
      return false;
   }

   public boolean overrideOtherStackedOnMe(
      net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1, Slot $$2, ClickAction $$3, Player $$4, SlotAccess $$5
   ) {
      return false;
   }

   public float getAttackDamageBonus(Entity $$0, float $$1, DamageSource $$2) {
      return 0.0F;
   }

   @Deprecated
   @Nullable
   public DamageSource getItemDamageSource(LivingEntity $$0) {
      return null;
   }

   public void hurtEnemy(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1, LivingEntity $$2) {
   }

   public void postHurtEnemy(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1, LivingEntity $$2) {
   }

   public boolean mineBlock(net.minecraft.world.item.ItemStack $$0, Level $$1, BlockState $$2, BlockPos $$3, LivingEntity $$4) {
      Tool $$5 = (Tool)$$0.get(DataComponents.TOOL);
      if ($$5 == null) {
         return false;
      } else {
         if (!$$1.isClientSide() && $$2.getDestroySpeed($$1, $$3) != 0.0F && $$5.damagePerBlock() > 0) {
            $$0.hurtAndBreak($$5.damagePerBlock(), $$4, EquipmentSlot.MAINHAND);
         }

         return true;
      }
   }

   public boolean isCorrectToolForDrops(net.minecraft.world.item.ItemStack $$0, BlockState $$1) {
      Tool $$2 = (Tool)$$0.get(DataComponents.TOOL);
      return $$2 != null && $$2.isCorrectForDrops($$1);
   }

   public InteractionResult interactLivingEntity(net.minecraft.world.item.ItemStack $$0, Player $$1, LivingEntity $$2, InteractionHand $$3) {
      return InteractionResult.PASS;
   }

   @Override
   public String toString() {
      return BuiltInRegistries.ITEM.wrapAsHolder(this).getRegisteredName();
   }

   public final net.minecraft.world.item.ItemStack getCraftingRemainder() {
      return this.craftingRemainingItem == null ? net.minecraft.world.item.ItemStack.EMPTY : new net.minecraft.world.item.ItemStack(this.craftingRemainingItem);
   }

   public void inventoryTick(net.minecraft.world.item.ItemStack $$0, ServerLevel $$1, Entity $$2, @Nullable EquipmentSlot $$3) {
   }

   public void onCraftedBy(net.minecraft.world.item.ItemStack $$0, Player $$1) {
      this.onCraftedPostProcess($$0, $$1.level());
   }

   public void onCraftedPostProcess(net.minecraft.world.item.ItemStack $$0, Level $$1) {
   }

   public net.minecraft.world.item.ItemUseAnimation getUseAnimation(net.minecraft.world.item.ItemStack $$0) {
      Consumable $$1 = (Consumable)$$0.get(DataComponents.CONSUMABLE);
      if ($$1 != null) {
         return $$1.animation();
      } else if ($$0.has(DataComponents.BLOCKS_ATTACKS)) {
         return net.minecraft.world.item.ItemUseAnimation.BLOCK;
      } else {
         return $$0.has(DataComponents.KINETIC_WEAPON) ? net.minecraft.world.item.ItemUseAnimation.SPEAR : net.minecraft.world.item.ItemUseAnimation.NONE;
      }
   }

   public int getUseDuration(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1) {
      Consumable $$2 = (Consumable)$$0.get(DataComponents.CONSUMABLE);
      if ($$2 != null) {
         return $$2.consumeTicks();
      } else {
         return !$$0.has(DataComponents.BLOCKS_ATTACKS) && !$$0.has(DataComponents.KINETIC_WEAPON) ? 0 : 72000;
      }
   }

   public boolean releaseUsing(net.minecraft.world.item.ItemStack $$0, Level $$1, LivingEntity $$2, int $$3) {
      return false;
   }

   @Deprecated
   public void appendHoverText(
      net.minecraft.world.item.ItemStack $$0,
      net.minecraft.world.item.Item.TooltipContext $$1,
      TooltipDisplay $$2,
      Consumer<Component> $$3,
      net.minecraft.world.item.TooltipFlag $$4
   ) {
   }

   public Optional<TooltipComponent> getTooltipImage(net.minecraft.world.item.ItemStack $$0) {
      return Optional.empty();
   }

   @VisibleForTesting
   public final String getDescriptionId() {
      return this.descriptionId;
   }

   public final Component getName() {
      return (Component)this.components.getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
   }

   public Component getName(net.minecraft.world.item.ItemStack $$0) {
      return (Component)$$0.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
   }

   public boolean isFoil(net.minecraft.world.item.ItemStack $$0) {
      return $$0.isEnchanted();
   }

   protected static BlockHitResult getPlayerPOVHitResult(Level $$0, Player $$1, Fluid $$2) {
      Vec3 $$3 = $$1.getEyePosition();
      Vec3 $$4 = $$3.add($$1.calculateViewVector($$1.getXRot(), $$1.getYRot()).scale($$1.blockInteractionRange()));
      return $$0.clip(new ClipContext($$3, $$4, net.minecraft.world.level.ClipContext.Block.OUTLINE, $$2, $$1));
   }

   public boolean useOnRelease(net.minecraft.world.item.ItemStack $$0) {
      return false;
   }

   public net.minecraft.world.item.ItemStack getDefaultInstance() {
      return new net.minecraft.world.item.ItemStack(this);
   }

   public boolean canFitInsideContainerItems() {
      return true;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   public boolean shouldPrintOpWarning(net.minecraft.world.item.ItemStack $$0, @Nullable Player $$1) {
      return false;
   }

   public static class Properties {
      private static final DependantName<net.minecraft.world.item.Item, String> BLOCK_DESCRIPTION_ID = $$0 -> Util.makeDescriptionId("block", $$0.identifier());
      private static final DependantName<net.minecraft.world.item.Item, String> ITEM_DESCRIPTION_ID = $$0 -> Util.makeDescriptionId("item", $$0.identifier());
      private final Builder components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
      @Nullable
      net.minecraft.world.item.Item craftingRemainingItem;
      FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
      @Nullable
      private ResourceKey<net.minecraft.world.item.Item> id;
      private DependantName<net.minecraft.world.item.Item, String> descriptionId = ITEM_DESCRIPTION_ID;
      private final DependantName<net.minecraft.world.item.Item, Identifier> model = ResourceKey::identifier;

      public net.minecraft.world.item.Item.Properties food(FoodProperties $$0) {
         return this.food($$0, Consumables.DEFAULT_FOOD);
      }

      public net.minecraft.world.item.Item.Properties food(FoodProperties $$0, Consumable $$1) {
         return this.component(DataComponents.FOOD, $$0).component(DataComponents.CONSUMABLE, $$1);
      }

      public net.minecraft.world.item.Item.Properties usingConvertsTo(net.minecraft.world.item.Item $$0) {
         return this.component(DataComponents.USE_REMAINDER, new UseRemainder(new net.minecraft.world.item.ItemStack($$0)));
      }

      public net.minecraft.world.item.Item.Properties useCooldown(float $$0) {
         return this.component(DataComponents.USE_COOLDOWN, new UseCooldown($$0));
      }

      public net.minecraft.world.item.Item.Properties stacksTo(int $$0) {
         return this.component(DataComponents.MAX_STACK_SIZE, $$0);
      }

      public net.minecraft.world.item.Item.Properties durability(int $$0) {
         this.component(DataComponents.MAX_DAMAGE, $$0);
         this.component(DataComponents.MAX_STACK_SIZE, 1);
         this.component(DataComponents.DAMAGE, 0);
         return this;
      }

      public net.minecraft.world.item.Item.Properties craftRemainder(net.minecraft.world.item.Item $$0) {
         this.craftingRemainingItem = $$0;
         return this;
      }

      public net.minecraft.world.item.Item.Properties rarity(net.minecraft.world.item.Rarity $$0) {
         return this.component(DataComponents.RARITY, $$0);
      }

      public net.minecraft.world.item.Item.Properties fireResistant() {
         return this.component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeTags.IS_FIRE));
      }

      public net.minecraft.world.item.Item.Properties jukeboxPlayable(ResourceKey<net.minecraft.world.item.JukeboxSong> $$0) {
         return this.component(DataComponents.JUKEBOX_PLAYABLE, new net.minecraft.world.item.JukeboxPlayable(new net.minecraft.world.item.EitherHolder<>($$0)));
      }

      public net.minecraft.world.item.Item.Properties enchantable(int $$0) {
         return this.component(DataComponents.ENCHANTABLE, new Enchantable($$0));
      }

      public net.minecraft.world.item.Item.Properties repairable(net.minecraft.world.item.Item $$0) {
         return this.component(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(new Holder[]{$$0.builtInRegistryHolder()})));
      }

      public net.minecraft.world.item.Item.Properties repairable(TagKey<net.minecraft.world.item.Item> $$0) {
         HolderGetter<net.minecraft.world.item.Item> $$1 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM);
         return this.component(DataComponents.REPAIRABLE, new Repairable($$1.getOrThrow($$0)));
      }

      public net.minecraft.world.item.Item.Properties equippable(EquipmentSlot $$0) {
         return this.component(DataComponents.EQUIPPABLE, Equippable.builder($$0).build());
      }

      public net.minecraft.world.item.Item.Properties equippableUnswappable(EquipmentSlot $$0) {
         return this.component(DataComponents.EQUIPPABLE, Equippable.builder($$0).setSwappable(false).build());
      }

      public net.minecraft.world.item.Item.Properties tool(net.minecraft.world.item.ToolMaterial $$0, TagKey<Block> $$1, float $$2, float $$3, float $$4) {
         return $$0.applyToolProperties(this, $$1, $$2, $$3, $$4);
      }

      public net.minecraft.world.item.Item.Properties pickaxe(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2) {
         return this.tool($$0, BlockTags.MINEABLE_WITH_PICKAXE, $$1, $$2, 0.0F);
      }

      public net.minecraft.world.item.Item.Properties axe(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2) {
         return this.tool($$0, BlockTags.MINEABLE_WITH_AXE, $$1, $$2, 5.0F);
      }

      public net.minecraft.world.item.Item.Properties hoe(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2) {
         return this.tool($$0, BlockTags.MINEABLE_WITH_HOE, $$1, $$2, 0.0F);
      }

      public net.minecraft.world.item.Item.Properties shovel(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2) {
         return this.tool($$0, BlockTags.MINEABLE_WITH_SHOVEL, $$1, $$2, 0.0F);
      }

      public net.minecraft.world.item.Item.Properties sword(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2) {
         return $$0.applySwordProperties(this, $$1, $$2);
      }

      public net.minecraft.world.item.Item.Properties spear(
         net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6, float $$7, float $$8, float $$9
      ) {
         return this.durability($$0.durability())
            .repairable($$0.repairItems())
            .enchantable($$0.enchantmentValue())
            .component(DataComponents.DAMAGE_TYPE, new net.minecraft.world.item.EitherHolder(DamageTypes.SPEAR))
            .component(
               DataComponents.KINETIC_WEAPON,
               new KineticWeapon(
                  10,
                  (int)($$3 * 20.0F),
                  KineticWeapon.Condition.ofAttackerSpeed((int)($$4 * 20.0F), $$5),
                  KineticWeapon.Condition.ofAttackerSpeed((int)($$6 * 20.0F), $$7),
                  KineticWeapon.Condition.ofRelativeSpeed((int)($$8 * 20.0F), $$9),
                  0.38F,
                  $$2,
                  Optional.of($$0 == net.minecraft.world.item.ToolMaterial.WOOD ? SoundEvents.SPEAR_WOOD_USE : SoundEvents.SPEAR_USE),
                  Optional.of($$0 == net.minecraft.world.item.ToolMaterial.WOOD ? SoundEvents.SPEAR_WOOD_HIT : SoundEvents.SPEAR_HIT)
               )
            )
            .component(
               DataComponents.PIERCING_WEAPON,
               new PiercingWeapon(
                  true,
                  false,
                  Optional.of($$0 == net.minecraft.world.item.ToolMaterial.WOOD ? SoundEvents.SPEAR_WOOD_ATTACK : SoundEvents.SPEAR_ATTACK),
                  Optional.of($$0 == net.minecraft.world.item.ToolMaterial.WOOD ? SoundEvents.SPEAR_WOOD_HIT : SoundEvents.SPEAR_HIT)
               )
            )
            .component(DataComponents.ATTACK_RANGE, new AttackRange(2.0F, 4.5F, 2.0F, 6.5F, 0.125F, 0.5F))
            .component(DataComponents.MINIMUM_ATTACK_CHARGE, 1.0F)
            .component(DataComponents.SWING_ANIMATION, new SwingAnimation(net.minecraft.world.item.SwingAnimationType.STAB, (int)($$1 * 20.0F)))
            .attributes(
               ItemAttributeModifiers.builder()
                  .add(
                     Attributes.ATTACK_DAMAGE,
                     new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, 0.0F + $$0.attackDamageBonus(), Operation.ADD_VALUE),
                     EquipmentSlotGroup.MAINHAND
                  )
                  .add(
                     Attributes.ATTACK_SPEED,
                     new AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID, 1.0F / $$1 - 4.0, Operation.ADD_VALUE),
                     EquipmentSlotGroup.MAINHAND
                  )
                  .build()
            )
            .component(DataComponents.USE_EFFECTS, new UseEffects(true, false, 1.0F))
            .component(DataComponents.WEAPON, new Weapon(1));
      }

      public net.minecraft.world.item.Item.Properties spawnEgg(EntityType<?> $$0) {
         return this.component(DataComponents.ENTITY_DATA, TypedEntityData.of($$0, new CompoundTag()));
      }

      public net.minecraft.world.item.Item.Properties humanoidArmor(ArmorMaterial $$0, ArmorType $$1) {
         return this.durability($$1.getDurability($$0.durability()))
            .attributes($$0.createAttributes($$1))
            .enchantable($$0.enchantmentValue())
            .component(DataComponents.EQUIPPABLE, Equippable.builder($$1.getSlot()).setEquipSound($$0.equipSound()).setAsset($$0.assetId()).build())
            .repairable($$0.repairIngredient());
      }

      public net.minecraft.world.item.Item.Properties wolfArmor(ArmorMaterial $$0) {
         return this.durability(ArmorType.BODY.getDurability($$0.durability()))
            .attributes($$0.createAttributes(ArmorType.BODY))
            .repairable($$0.repairIngredient())
            .component(
               DataComponents.EQUIPPABLE,
               Equippable.builder(EquipmentSlot.BODY)
                  .setEquipSound($$0.equipSound())
                  .setAsset($$0.assetId())
                  .setAllowedEntities(HolderSet.direct(new Holder[]{EntityType.WOLF.builtInRegistryHolder()}))
                  .setCanBeSheared(true)
                  .setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARMOR_UNEQUIP_WOLF))
                  .build()
            )
            .component(DataComponents.BREAK_SOUND, SoundEvents.WOLF_ARMOR_BREAK)
            .stacksTo(1);
      }

      public net.minecraft.world.item.Item.Properties horseArmor(ArmorMaterial $$0) {
         HolderGetter<EntityType<?>> $$1 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
         return this.attributes($$0.createAttributes(ArmorType.BODY))
            .component(
               DataComponents.EQUIPPABLE,
               Equippable.builder(EquipmentSlot.BODY)
                  .setEquipSound(SoundEvents.HORSE_ARMOR)
                  .setAsset($$0.assetId())
                  .setAllowedEntities($$1.getOrThrow(EntityTypeTags.CAN_WEAR_HORSE_ARMOR))
                  .setDamageOnHurt(false)
                  .setCanBeSheared(true)
                  .setShearingSound(SoundEvents.HORSE_ARMOR_UNEQUIP)
                  .build()
            )
            .stacksTo(1);
      }

      public net.minecraft.world.item.Item.Properties nautilusArmor(ArmorMaterial $$0) {
         HolderGetter<EntityType<?>> $$1 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
         return this.attributes($$0.createAttributes(ArmorType.BODY))
            .component(
               DataComponents.EQUIPPABLE,
               Equippable.builder(EquipmentSlot.BODY)
                  .setEquipSound(SoundEvents.ARMOR_EQUIP_NAUTILUS)
                  .setAsset($$0.assetId())
                  .setAllowedEntities($$1.getOrThrow(EntityTypeTags.CAN_WEAR_NAUTILUS_ARMOR))
                  .setDamageOnHurt(false)
                  .setEquipOnInteract(true)
                  .setCanBeSheared(true)
                  .setShearingSound(SoundEvents.ARMOR_UNEQUIP_NAUTILUS)
                  .build()
            )
            .stacksTo(1);
      }

      public net.minecraft.world.item.Item.Properties trimMaterial(ResourceKey<TrimMaterial> $$0) {
         return this.component(DataComponents.PROVIDES_TRIM_MATERIAL, new ProvidesTrimMaterial($$0));
      }

      public net.minecraft.world.item.Item.Properties requiredFeatures(FeatureFlag... $$0) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset($$0);
         return this;
      }

      public net.minecraft.world.item.Item.Properties setId(ResourceKey<net.minecraft.world.item.Item> $$0) {
         this.id = $$0;
         return this;
      }

      public net.minecraft.world.item.Item.Properties overrideDescription(String $$0) {
         this.descriptionId = DependantName.fixed($$0);
         return this;
      }

      public net.minecraft.world.item.Item.Properties useBlockDescriptionPrefix() {
         this.descriptionId = BLOCK_DESCRIPTION_ID;
         return this;
      }

      public net.minecraft.world.item.Item.Properties useItemDescriptionPrefix() {
         this.descriptionId = ITEM_DESCRIPTION_ID;
         return this;
      }

      protected String effectiveDescriptionId() {
         return (String)this.descriptionId.get(Objects.requireNonNull(this.id, "Item id not set"));
      }

      public Identifier effectiveModel() {
         return (Identifier)this.model.get(Objects.requireNonNull(this.id, "Item id not set"));
      }

      public <T> net.minecraft.world.item.Item.Properties component(DataComponentType<T> $$0, T $$1) {
         this.components.set($$0, $$1);
         return this;
      }

      public net.minecraft.world.item.Item.Properties attributes(ItemAttributeModifiers $$0) {
         return this.component(DataComponents.ATTRIBUTE_MODIFIERS, $$0);
      }

      DataComponentMap buildAndValidateComponents(Component $$0, Identifier $$1) {
         DataComponentMap $$2 = this.components.set(DataComponents.ITEM_NAME, $$0).set(DataComponents.ITEM_MODEL, $$1).build();
         if ($$2.has(DataComponents.DAMAGE) && (Integer)$$2.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
            throw new IllegalStateException("Item cannot have both durability and be stackable");
         } else {
            return $$2;
         }
      }
   }

   public interface TooltipContext {
      net.minecraft.world.item.Item.TooltipContext EMPTY = new net.minecraft.world.item.Item.TooltipContext() {
         @Nullable
         @Override
         public Provider registries() {
            return null;
         }

         @Override
         public float tickRate() {
            return 20.0F;
         }

         @Nullable
         @Override
         public MapItemSavedData mapData(MapId $$0) {
            return null;
         }

         @Override
         public boolean isPeaceful() {
            return false;
         }
      };

      @Nullable
      Provider registries();

      float tickRate();

      @Nullable
      MapItemSavedData mapData(MapId var1);

      boolean isPeaceful();

      static net.minecraft.world.item.Item.TooltipContext of(@Nullable final Level $$0) {
         return $$0 == null ? EMPTY : new net.minecraft.world.item.Item.TooltipContext() {
            @Override
            public Provider registries() {
               return $$0.registryAccess();
            }

            @Override
            public float tickRate() {
               return $$0.tickRateManager().tickrate();
            }

            @Override
            public MapItemSavedData mapData(MapId $$0x) {
               return $$0.getMapData($$0);
            }

            @Override
            public boolean isPeaceful() {
               return $$0.getDifficulty() == Difficulty.PEACEFUL;
            }
         };
      }

      static net.minecraft.world.item.Item.TooltipContext of(final Provider $$0) {
         return new net.minecraft.world.item.Item.TooltipContext() {
            @Override
            public Provider registries() {
               return $$0;
            }

            @Override
            public float tickRate() {
               return 20.0F;
            }

            @Nullable
            @Override
            public MapItemSavedData mapData(MapId $$0x) {
               return null;
            }

            @Override
            public boolean isPeaceful() {
               return false;
            }
         };
      }
   }
}
