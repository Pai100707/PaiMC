package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.ShowItem;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.NullOps;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public final class ItemStack implements DataComponentHolder {
   private static final List<Component> OP_NBT_WARNING = List.of(
      Component.translatable("item.op_warning.line1").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}),
      Component.translatable("item.op_warning.line2").withStyle(ChatFormatting.RED),
      Component.translatable("item.op_warning.line3").withStyle(ChatFormatting.RED)
   );
   private static final Component UNBREAKABLE_TOOLTIP = Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE);
   private static final Component INTANGIBLE_TOOLTIP = Component.translatable("item.intangible").withStyle(ChatFormatting.GRAY);
   public static final MapCodec<net.minecraft.world.item.ItemStack> MAP_CODEC = MapCodec.recursive(
      "ItemStack",
      $$0 -> RecordCodecBuilder.mapCodec(
         $$0x -> $$0x.group(
               net.minecraft.world.item.Item.CODEC.fieldOf("id").forGetter(net.minecraft.world.item.ItemStack::getItemHolder),
               ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(net.minecraft.world.item.ItemStack::getCount),
               DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter($$0xx -> $$0xx.components.asPatch())
            )
            .apply($$0x, net.minecraft.world.item.ItemStack::new)
      )
   );
   public static final Codec<net.minecraft.world.item.ItemStack> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
   public static final Codec<net.minecraft.world.item.ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(
      () -> RecordCodecBuilder.create(
         $$0 -> $$0.group(
               net.minecraft.world.item.Item.CODEC.fieldOf("id").forGetter(net.minecraft.world.item.ItemStack::getItemHolder),
               DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter($$0x -> $$0x.components.asPatch())
            )
            .apply($$0, ($$0x, $$1) -> new net.minecraft.world.item.ItemStack($$0x, 1, $$1))
      )
   );
   public static final Codec<net.minecraft.world.item.ItemStack> STRICT_CODEC = CODEC.validate(net.minecraft.world.item.ItemStack::validateStrict);
   public static final Codec<net.minecraft.world.item.ItemStack> STRICT_SINGLE_ITEM_CODEC = SINGLE_ITEM_CODEC.validate(
      net.minecraft.world.item.ItemStack::validateStrict
   );
   public static final Codec<net.minecraft.world.item.ItemStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
      .xmap($$0 -> $$0.orElse(net.minecraft.world.item.ItemStack.EMPTY), $$0 -> $$0.isEmpty() ? Optional.empty() : Optional.of($$0));
   public static final Codec<net.minecraft.world.item.ItemStack> SIMPLE_ITEM_CODEC = net.minecraft.world.item.Item.CODEC
      .xmap(net.minecraft.world.item.ItemStack::new, net.minecraft.world.item.ItemStack::getItemHolder);
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> OPTIONAL_STREAM_CODEC = createOptionalStreamCodec(
      DataComponentPatch.STREAM_CODEC
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> OPTIONAL_UNTRUSTED_STREAM_CODEC = createOptionalStreamCodec(
      DataComponentPatch.DELIMITED_STREAM_CODEC
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack>() {
      public net.minecraft.world.item.ItemStack decode(RegistryFriendlyByteBuf $$0) {
         net.minecraft.world.item.ItemStack $$1 = (net.minecraft.world.item.ItemStack)net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode($$0);
         if ($$1.isEmpty()) {
            throw new DecoderException("Empty ItemStack not allowed");
         } else {
            return $$1;
         }
      }

      public void encode(RegistryFriendlyByteBuf $$0, net.minecraft.world.item.ItemStack $$1) {
         if ($$1.isEmpty()) {
            throw new EncoderException("Empty ItemStack not allowed");
         } else {
            net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode($$0, $$1);
         }
      }
   };
   public static final StreamCodec<RegistryFriendlyByteBuf, List<net.minecraft.world.item.ItemStack>> OPTIONAL_LIST_STREAM_CODEC = OPTIONAL_STREAM_CODEC.apply(
      ByteBufCodecs.collection(NonNullList::createWithCapacity)
   );
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final net.minecraft.world.item.ItemStack EMPTY = new net.minecraft.world.item.ItemStack((Void)null);
   private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
   private int count;
   private int popTime;
   @Deprecated
   
   private final net.minecraft.world.item.Item item;
   final PatchedDataComponentMap components;
   
   private Entity entityRepresentation;

   public static DataResult<net.minecraft.world.item.ItemStack> validateStrict(net.minecraft.world.item.ItemStack $$0) {
      DataResult<Unit> $$1 = validateComponents($$0.getComponents());
      if ($$1.isError()) {
         return $$1.map($$1x -> $$0);
      } else {
         return $$0.getCount() > $$0.getMaxStackSize()
            ? DataResult.error(() -> "Item stack with stack size of " + $$0.getCount() + " was larger than maximum: " + $$0.getMaxStackSize())
            : DataResult.success($$0);
      }
   }

   private static StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> createOptionalStreamCodec(
      final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> $$0
   ) {
      return new StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack>() {
         public net.minecraft.world.item.ItemStack decode(RegistryFriendlyByteBuf $$0x) {
            int $$1 = $$0.readVarInt();
            if ($$1 <= 0) {
               return net.minecraft.world.item.ItemStack.EMPTY;
            } else {
               Holder<net.minecraft.world.item.Item> $$2 = (Holder<net.minecraft.world.item.Item>)net.minecraft.world.item.Item.STREAM_CODEC.decode($$0);
               DataComponentPatch $$3 = (DataComponentPatch)$$0.decode($$0);
               return new net.minecraft.world.item.ItemStack($$2, $$1, $$3);
            }
         }

         public void encode(RegistryFriendlyByteBuf $$0x, net.minecraft.world.item.ItemStack $$1) {
            if ($$1.isEmpty()) {
               $$0.writeVarInt(0);
            } else {
               $$0.writeVarInt($$1.getCount());
               net.minecraft.world.item.Item.STREAM_CODEC.encode($$0, $$1.getItemHolder());
               $$0.encode($$0, $$1.components.asPatch());
            }
         }
      };
   }

   public static StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> validatedStreamCodec(
      final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> $$0
   ) {
      return new StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack>() {
         public net.minecraft.world.item.ItemStack decode(RegistryFriendlyByteBuf $$0x) {
            net.minecraft.world.item.ItemStack $$1 = (net.minecraft.world.item.ItemStack)$$0.decode($$0);
            if (!$$1.isEmpty()) {
               RegistryOps<Unit> $$2 = $$0.registryAccess().createSerializationContext(NullOps.INSTANCE);
               net.minecraft.world.item.ItemStack.CODEC.encodeStart($$2, $$1).getOrThrow(DecoderException::new);
            }

            return $$1;
         }

         public void encode(RegistryFriendlyByteBuf $$0x, net.minecraft.world.item.ItemStack $$1) {
            $$0.encode($$0, $$1);
         }
      };
   }

   public Optional<TooltipComponent> getTooltipImage() {
      return this.getItem().getTooltipImage(this);
   }

   public DataComponentMap getComponents() {
      return (DataComponentMap)(!this.isEmpty() ? this.components : DataComponentMap.EMPTY);
   }

   public DataComponentMap getPrototype() {
      return !this.isEmpty() ? this.getItem().components() : DataComponentMap.EMPTY;
   }

   public DataComponentPatch getComponentsPatch() {
      return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
   }

   public DataComponentMap immutableComponents() {
      return !this.isEmpty() ? this.components.toImmutableMap() : DataComponentMap.EMPTY;
   }

   public boolean hasNonDefault(DataComponentType<?> $$0) {
      return !this.isEmpty() && this.components.hasNonDefault($$0);
   }

   public ItemStack(ItemLike $$0) {
      this($$0, 1);
   }

   public ItemStack(Holder<net.minecraft.world.item.Item> $$0) {
      this((ItemLike)$$0.value(), 1);
   }

   public ItemStack(Holder<net.minecraft.world.item.Item> $$0, int $$1, DataComponentPatch $$2) {
      this((ItemLike)$$0.value(), $$1, PatchedDataComponentMap.fromPatch(((net.minecraft.world.item.Item)$$0.value()).components(), $$2));
   }

   public ItemStack(Holder<net.minecraft.world.item.Item> $$0, int $$1) {
      this((ItemLike)$$0.value(), $$1);
   }

   public ItemStack(ItemLike $$0, int $$1) {
      this($$0, $$1, new PatchedDataComponentMap($$0.asItem().components()));
   }

   private ItemStack(ItemLike $$0, int $$1, PatchedDataComponentMap $$2) {
      this.item = $$0.asItem();
      this.count = $$1;
      this.components = $$2;
   }

   private ItemStack(Void $$0) {
      this.item = null;
      this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
   }

   public static DataResult<Unit> validateComponents(DataComponentMap $$0) {
      if ($$0.has(DataComponents.MAX_DAMAGE) && (Integer)$$0.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
         return DataResult.error(() -> "Item cannot be both damageable and stackable");
      } else {
         ItemContainerContents $$1 = (ItemContainerContents)$$0.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

         for (net.minecraft.world.item.ItemStack $$2 : $$1.nonEmptyItems()) {
            int $$3 = $$2.getCount();
            int $$4 = $$2.getMaxStackSize();
            if ($$3 > $$4) {
               return DataResult.error(() -> "Item stack with count of " + $$3 + " was larger than maximum: " + $$4);
            }
         }

         return DataResult.success(Unit.INSTANCE);
      }
   }

   public boolean isEmpty() {
      return this == EMPTY || this.item == net.minecraft.world.item.Items.AIR || this.count <= 0;
   }

   public boolean isItemEnabled(FeatureFlagSet $$0) {
      return this.isEmpty() || this.getItem().isEnabled($$0);
   }

   public net.minecraft.world.item.ItemStack split(int $$0) {
      int $$1 = Math.min($$0, this.getCount());
      net.minecraft.world.item.ItemStack $$2 = this.copyWithCount($$1);
      this.shrink($$1);
      return $$2;
   }

   public net.minecraft.world.item.ItemStack copyAndClear() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         net.minecraft.world.item.ItemStack $$0 = this.copy();
         this.setCount(0);
         return $$0;
      }
   }

   public net.minecraft.world.item.Item getItem() {
      return this.isEmpty() ? net.minecraft.world.item.Items.AIR : this.item;
   }

   public Holder<net.minecraft.world.item.Item> getItemHolder() {
      return this.getItem().builtInRegistryHolder();
   }

   public boolean is(TagKey<net.minecraft.world.item.Item> $$0) {
      return this.getItem().builtInRegistryHolder().is($$0);
   }

   public boolean is(net.minecraft.world.item.Item $$0) {
      return this.getItem() == $$0;
   }

   public boolean is(Predicate<Holder<net.minecraft.world.item.Item>> $$0) {
      return $$0.test(this.getItem().builtInRegistryHolder());
   }

   public boolean is(Holder<net.minecraft.world.item.Item> $$0) {
      return this.getItem().builtInRegistryHolder() == $$0;
   }

   public boolean is(HolderSet<net.minecraft.world.item.Item> $$0) {
      return $$0.contains(this.getItemHolder());
   }

   public Stream<TagKey<net.minecraft.world.item.Item>> getTags() {
      return this.getItem().builtInRegistryHolder().tags();
   }

   public InteractionResult useOn(UseOnContext $$0) {
      Player $$1 = $$0.getPlayer();
      BlockPos $$2 = $$0.getClickedPos();
      if ($$1 != null && !$$1.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new BlockInWorld($$0.getLevel(), $$2, false))) {
         return InteractionResult.PASS;
      } else {
         net.minecraft.world.item.Item $$3 = this.getItem();
         InteractionResult $$4 = $$3.useOn($$0);
         if ($$1 != null && $$4 instanceof Success $$5 && $$5.wasItemInteraction()) {
            $$1.awardStat(Stats.ITEM_USED.get($$3));
         }

         return $$4;
      }
   }

   public float getDestroySpeed(BlockState $$0) {
      return this.getItem().getDestroySpeed(this, $$0);
   }

   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = this.copy();
      boolean $$4 = this.getUseDuration($$1) <= 0;
      InteractionResult $$5 = this.getItem().use($$0, $$1, $$2);
      return (InteractionResult)($$4 && $$5 instanceof Success $$6
         ? $$6.heldItemTransformedTo(
            $$6.heldItemTransformedTo() == null
               ? this.applyAfterUseComponentSideEffects($$1, $$3)
               : $$6.heldItemTransformedTo().applyAfterUseComponentSideEffects($$1, $$3)
         )
         : $$5);
   }

   public net.minecraft.world.item.ItemStack finishUsingItem(Level $$0, LivingEntity $$1) {
      net.minecraft.world.item.ItemStack $$2 = this.copy();
      net.minecraft.world.item.ItemStack $$3 = this.getItem().finishUsingItem(this, $$0, $$1);
      return $$3.applyAfterUseComponentSideEffects($$1, $$2);
   }

   private net.minecraft.world.item.ItemStack applyAfterUseComponentSideEffects(LivingEntity $$0, net.minecraft.world.item.ItemStack $$1) {
      UseRemainder $$2 = (UseRemainder)$$1.get(DataComponents.USE_REMAINDER);
      UseCooldown $$3 = (UseCooldown)$$1.get(DataComponents.USE_COOLDOWN);
      int $$4 = $$1.getCount();
      net.minecraft.world.item.ItemStack $$5 = this;
      if ($$2 != null) {
         $$5 = $$2.convertIntoRemainder(this, $$4, $$0.hasInfiniteMaterials(), $$0::handleExtraItemsCreatedOnUse);
      }

      if ($$3 != null) {
         $$3.apply($$1, $$0);
      }

      return $$5;
   }

   public int getMaxStackSize() {
      return (Integer)this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
   }

   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
   }

   public boolean isDamageableItem() {
      return this.has(DataComponents.MAX_DAMAGE) && !this.has(DataComponents.UNBREAKABLE) && this.has(DataComponents.DAMAGE);
   }

   public boolean isDamaged() {
      return this.isDamageableItem() && this.getDamageValue() > 0;
   }

   public int getDamageValue() {
      return Mth.clamp((Integer)this.getOrDefault(DataComponents.DAMAGE, 0), 0, this.getMaxDamage());
   }

   public void setDamageValue(int $$0) {
      this.set(DataComponents.DAMAGE, Mth.clamp($$0, 0, this.getMaxDamage()));
   }

   public int getMaxDamage() {
      return (Integer)this.getOrDefault(DataComponents.MAX_DAMAGE, 0);
   }

   public boolean isBroken() {
      return this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage();
   }

   public boolean nextDamageWillBreak() {
      return this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage() - 1;
   }

   public void hurtAndBreak(int $$0, ServerLevel $$1, ServerPlayer $$2, Consumer<net.minecraft.world.item.Item> $$3) {
      int $$4 = this.processDurabilityChange($$0, $$1, $$2);
      if ($$4 != 0) {
         this.applyDamage(this.getDamageValue() + $$4, $$2, $$3);
      }
   }

   private int processDurabilityChange(int $$0, ServerLevel $$1, ServerPlayer $$2) {
      if (!this.isDamageableItem()) {
         return 0;
      } else if ($$2 != null && $$2.hasInfiniteMaterials()) {
         return 0;
      } else {
         return $$0 > 0 ? EnchantmentHelper.processDurabilityChange($$1, this, $$0) : $$0;
      }
   }

   private void applyDamage(int $$0, ServerPlayer $$1, Consumer<net.minecraft.world.item.Item> $$2) {
      if ($$1 != null) {
         CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger($$1, this, $$0);
      }

      this.setDamageValue($$0);
      if (this.isBroken()) {
         net.minecraft.world.item.Item $$3 = this.getItem();
         this.shrink(1);
         $$2.accept($$3);
      }
   }

   public void hurtWithoutBreaking(int $$0, Player $$1) {
      if ($$1 instanceof ServerPlayer $$2) {
         int $$3 = this.processDurabilityChange($$0, $$2.level(), $$2);
         if ($$3 == 0) {
            return;
         }

         int $$4 = Math.min(this.getDamageValue() + $$3, this.getMaxDamage() - 1);
         this.applyDamage($$4, $$2, $$0x -> {});
      }
   }

   public void hurtAndBreak(int $$0, LivingEntity $$1, InteractionHand $$2) {
      this.hurtAndBreak($$0, $$1, $$2.asEquipmentSlot());
   }

   public void hurtAndBreak(int $$0, LivingEntity $$1, EquipmentSlot $$2) {
      if ($$1.level() instanceof ServerLevel $$3) {
         this.hurtAndBreak($$0, $$3, $$1 instanceof ServerPlayer $$4 ? $$4 : null, $$2x -> $$1.onEquippedItemBroken($$2x, $$2));
      }
   }

   public net.minecraft.world.item.ItemStack hurtAndConvertOnBreak(int $$0, ItemLike $$1, LivingEntity $$2, EquipmentSlot $$3) {
      this.hurtAndBreak($$0, $$2, $$3);
      if (this.isEmpty()) {
         net.minecraft.world.item.ItemStack $$4 = this.transmuteCopyIgnoreEmpty($$1, 1);
         if ($$4.isDamageableItem()) {
            $$4.setDamageValue(0);
         }

         return $$4;
      } else {
         return this;
      }
   }

   public boolean isBarVisible() {
      return this.getItem().isBarVisible(this);
   }

   public int getBarWidth() {
      return this.getItem().getBarWidth(this);
   }

   public int getBarColor() {
      return this.getItem().getBarColor(this);
   }

   public boolean overrideStackedOnOther(Slot $$0, ClickAction $$1, Player $$2) {
      return this.getItem().overrideStackedOnOther(this, $$0, $$1, $$2);
   }

   public boolean overrideOtherStackedOnMe(net.minecraft.world.item.ItemStack $$0, Slot $$1, ClickAction $$2, Player $$3, SlotAccess $$4) {
      return this.getItem().overrideOtherStackedOnMe(this, $$0, $$1, $$2, $$3, $$4);
   }

   public boolean hurtEnemy(LivingEntity $$0, LivingEntity $$1) {
      net.minecraft.world.item.Item $$2 = this.getItem();
      $$2.hurtEnemy(this, $$0, $$1);
      if (this.has(DataComponents.WEAPON)) {
         if ($$1 instanceof Player $$3) {
            $$3.awardStat(Stats.ITEM_USED.get($$2));
         }

         return true;
      } else {
         return false;
      }
   }

   public void postHurtEnemy(LivingEntity $$0, LivingEntity $$1) {
      this.getItem().postHurtEnemy(this, $$0, $$1);
      Weapon $$2 = (Weapon)this.get(DataComponents.WEAPON);
      if ($$2 != null) {
         this.hurtAndBreak($$2.itemDamagePerAttack(), $$1, EquipmentSlot.MAINHAND);
      }
   }

   public void mineBlock(Level $$0, BlockState $$1, BlockPos $$2, Player $$3) {
      net.minecraft.world.item.Item $$4 = this.getItem();
      if ($$4.mineBlock(this, $$0, $$1, $$2, $$3)) {
         $$3.awardStat(Stats.ITEM_USED.get($$4));
      }
   }

   public boolean isCorrectToolForDrops(BlockState $$0) {
      return this.getItem().isCorrectToolForDrops(this, $$0);
   }

   public InteractionResult interactLivingEntity(Player $$0, LivingEntity $$1, InteractionHand $$2) {
      Equippable $$3 = (Equippable)this.get(DataComponents.EQUIPPABLE);
      if ($$3 != null && $$3.equipOnInteract()) {
         InteractionResult $$4 = $$3.equipOnTarget($$0, $$1, this);
         if ($$4 != InteractionResult.PASS) {
            return $$4;
         }
      }

      return this.getItem().interactLivingEntity(this, $$0, $$1, $$2);
   }

   public net.minecraft.world.item.ItemStack copy() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         net.minecraft.world.item.ItemStack $$0 = new net.minecraft.world.item.ItemStack(this.getItem(), this.count, this.components.copy());
         $$0.setPopTime(this.getPopTime());
         return $$0;
      }
   }

   public net.minecraft.world.item.ItemStack copyWithCount(int $$0) {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         net.minecraft.world.item.ItemStack $$1 = this.copy();
         $$1.setCount($$0);
         return $$1;
      }
   }

   public net.minecraft.world.item.ItemStack transmuteCopy(ItemLike $$0) {
      return this.transmuteCopy($$0, this.getCount());
   }

   public net.minecraft.world.item.ItemStack transmuteCopy(ItemLike $$0, int $$1) {
      return this.isEmpty() ? EMPTY : this.transmuteCopyIgnoreEmpty($$0, $$1);
   }

   private net.minecraft.world.item.ItemStack transmuteCopyIgnoreEmpty(ItemLike $$0, int $$1) {
      return new net.minecraft.world.item.ItemStack($$0.asItem().builtInRegistryHolder(), $$1, this.components.asPatch());
   }

   public static boolean matches(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      if ($$0 == $$1) {
         return true;
      } else {
         return $$0.getCount() != $$1.getCount() ? false : isSameItemSameComponents($$0, $$1);
      }
   }

   @Deprecated
   public static boolean listMatches(List<net.minecraft.world.item.ItemStack> $$0, List<net.minecraft.world.item.ItemStack> $$1) {
      if ($$0.size() != $$1.size()) {
         return false;
      } else {
         for (int $$2 = 0; $$2 < $$0.size(); $$2++) {
            if (!matches($$0.get($$2), $$1.get($$2))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isSameItem(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      return $$0.is($$1.getItem());
   }

   public static boolean isSameItemSameComponents(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      if (!$$0.is($$1.getItem())) {
         return false;
      } else {
         return $$0.isEmpty() && $$1.isEmpty() ? true : Objects.equals($$0.components, $$1.components);
      }
   }

   public static boolean matchesIgnoringComponents(
      net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1, Predicate<DataComponentType<?>> $$2
   ) {
      if ($$0 == $$1) {
         return true;
      } else if ($$0.getCount() != $$1.getCount()) {
         return false;
      } else if (!$$0.is($$1.getItem())) {
         return false;
      } else if ($$0.isEmpty() && $$1.isEmpty()) {
         return true;
      } else if ($$0.components.size() != $$1.components.size()) {
         return false;
      } else {
         for (DataComponentType<?> $$3 : $$0.components.keySet()) {
            Object $$4 = $$0.components.get($$3);
            Object $$5 = $$1.components.get($$3);
            if ($$4 == null || $$5 == null) {
               return false;
            }

            if (!Objects.equals($$4, $$5) && !$$2.test($$3)) {
               return false;
            }
         }

         return true;
      }
   }

   public static MapCodec<net.minecraft.world.item.ItemStack> lenientOptionalFieldOf(String $$0) {
      return CODEC.lenientOptionalFieldOf($$0).xmap($$0x -> $$0x.orElse(EMPTY), $$0x -> $$0x.isEmpty() ? Optional.empty() : Optional.of($$0x));
   }

   public static int hashItemAndComponents(net.minecraft.world.item.ItemStack $$0) {
      if ($$0 != null) {
         int $$1 = 31 + $$0.getItem().hashCode();
         return 31 * $$1 + $$0.getComponents().hashCode();
      } else {
         return 0;
      }
   }

   @Deprecated
   public static int hashStackList(List<net.minecraft.world.item.ItemStack> $$0) {
      int $$1 = 0;

      for (net.minecraft.world.item.ItemStack $$2 : $$0) {
         $$1 = $$1 * 31 + hashItemAndComponents($$2);
      }

      return $$1;
   }

   @Override
   public String toString() {
      return this.getCount() + " " + this.getItem();
   }

   public void inventoryTick(Level $$0, Entity $$1, EquipmentSlot $$2) {
      if (this.popTime > 0) {
         this.popTime--;
      }

      if ($$0 instanceof ServerLevel $$3) {
         this.getItem().inventoryTick(this, $$3, $$1, $$2);
      }
   }

   public void onCraftedBy(Player $$0, int $$1) {
      $$0.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), $$1);
      this.getItem().onCraftedBy(this, $$0);
   }

   public void onCraftedBySystem(Level $$0) {
      this.getItem().onCraftedPostProcess(this, $$0);
   }

   public int getUseDuration(LivingEntity $$0) {
      return this.getItem().getUseDuration(this, $$0);
   }

   public net.minecraft.world.item.ItemUseAnimation getUseAnimation() {
      return this.getItem().getUseAnimation(this);
   }

   public void releaseUsing(Level $$0, LivingEntity $$1, int $$2) {
      net.minecraft.world.item.ItemStack $$3 = this.copy();
      if (this.getItem().releaseUsing(this, $$0, $$1, $$2)) {
         net.minecraft.world.item.ItemStack $$4 = this.applyAfterUseComponentSideEffects($$1, $$3);
         if ($$4 != this) {
            $$1.setItemInHand($$1.getUsedItemHand(), $$4);
         }
      }
   }

   public void causeUseVibration(Entity $$0, Reference<GameEvent> $$1) {
      UseEffects $$2 = (UseEffects)this.get(DataComponents.USE_EFFECTS);
      if ($$2 != null && $$2.interactVibrations()) {
         $$0.gameEvent($$1);
      }
   }

   public boolean useOnRelease() {
      return this.getItem().useOnRelease(this);
   }

   
   public <T> T set(DataComponentType<T> $$0, T $$1) {
      return (T)this.components.set($$0, $$1);
   }

   
   public <T> T set(TypedDataComponent<T> $$0) {
      return (T)this.components.set($$0);
   }

   public <T> void copyFrom(DataComponentType<T> $$0, DataComponentGetter $$1) {
      this.set($$0, $$1.get($$0));
   }

   
   public <T, U> T update(DataComponentType<T> $$0, T $$1, U $$2, BiFunction<T, U, T> $$3) {
      return this.set($$0, $$3.apply((T)this.getOrDefault($$0, $$1), $$2));
   }

   
   public <T> T update(DataComponentType<T> $$0, T $$1, UnaryOperator<T> $$2) {
      T $$3 = (T)this.getOrDefault($$0, $$1);
      return this.set($$0, $$2.apply($$3));
   }

   
   public <T> T remove(DataComponentType<? extends T> $$0) {
      return (T)this.components.remove($$0);
   }

   public void applyComponentsAndValidate(DataComponentPatch $$0) {
      DataComponentPatch $$1 = this.components.asPatch();
      this.components.applyPatch($$0);
      Optional<Error<net.minecraft.world.item.ItemStack>> $$2 = validateStrict(this).error();
      if ($$2.isPresent()) {
         LOGGER.error("Failed to apply component patch '{}' to item: '{}'", $$0, $$2.get().message());
         this.components.restorePatch($$1);
      }
   }

   public void applyComponents(DataComponentPatch $$0) {
      this.components.applyPatch($$0);
   }

   public void applyComponents(DataComponentMap $$0) {
      this.components.setAll($$0);
   }

   public Component getHoverName() {
      Component $$0 = this.getCustomName();
      return $$0 != null ? $$0 : this.getItemName();
   }

   
   public Component getCustomName() {
      Component $$0 = (Component)this.get(DataComponents.CUSTOM_NAME);
      if ($$0 != null) {
         return $$0;
      } else {
         WrittenBookContent $$1 = (WrittenBookContent)this.get(DataComponents.WRITTEN_BOOK_CONTENT);
         if ($$1 != null) {
            String $$2 = (String)$$1.title().raw();
            if (!StringUtil.isBlank($$2)) {
               return Component.literal($$2);
            }
         }

         return null;
      }
   }

   public Component getItemName() {
      return this.getItem().getName(this);
   }

   public Component getStyledHoverName() {
      MutableComponent $$0 = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color());
      if (this.has(DataComponents.CUSTOM_NAME)) {
         $$0.withStyle(ChatFormatting.ITALIC);
      }

      return $$0;
   }

   public <T extends TooltipProvider> void addToTooltip(
      DataComponentType<T> $$0,
      net.minecraft.world.item.Item.TooltipContext $$1,
      TooltipDisplay $$2,
      Consumer<Component> $$3,
      net.minecraft.world.item.TooltipFlag $$4
   ) {
      T $$5 = (T)this.get($$0);
      if ($$5 != null && $$2.shows($$0)) {
         $$5.addToTooltip($$1, $$3, $$4, this.components);
      }
   }

   public List<Component> getTooltipLines(net.minecraft.world.item.Item.TooltipContext $$0, Player $$1, net.minecraft.world.item.TooltipFlag $$2) {
      TooltipDisplay $$3 = (TooltipDisplay)this.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
      if (!$$2.isCreative() && $$3.hideTooltip()) {
         boolean $$4 = this.getItem().shouldPrintOpWarning(this, $$1);
         return $$4 ? OP_NBT_WARNING : List.of();
      } else {
         List<Component> $$5 = Lists.newArrayList();
         $$5.add(this.getStyledHoverName());
         this.addDetailsToTooltip($$0, $$3, $$1, $$2, $$5::add);
         return $$5;
      }
   }

   public void addDetailsToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0,
      TooltipDisplay $$1,
      Player $$2,
      net.minecraft.world.item.TooltipFlag $$3,
      Consumer<Component> $$4
   ) {
      this.getItem().appendHoverText(this, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.TROPICAL_FISH_PATTERN, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.INSTRUMENT, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.MAP_ID, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.BEES, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.CONTAINER_LOOT, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.CONTAINER, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.BANNER_PATTERNS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.POT_DECORATIONS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.WRITTEN_BOOK_CONTENT, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.CHARGED_PROJECTILES, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.FIREWORKS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.FIREWORK_EXPLOSION, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.POTION_CONTENTS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.JUKEBOX_PLAYABLE, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.TRIM, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.STORED_ENCHANTMENTS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.ENCHANTMENTS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.DYED_COLOR, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.PROFILE, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.LORE, $$0, $$1, $$4, $$3);
      this.addAttributeTooltips($$4, $$1, $$2);
      this.addUnitComponentToTooltip(DataComponents.INTANGIBLE_PROJECTILE, INTANGIBLE_TOOLTIP, $$1, $$4);
      this.addUnitComponentToTooltip(DataComponents.UNBREAKABLE, UNBREAKABLE_TOOLTIP, $$1, $$4);
      this.addToTooltip(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.SUSPICIOUS_STEW_EFFECTS, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.BLOCK_STATE, $$0, $$1, $$4, $$3);
      this.addToTooltip(DataComponents.ENTITY_DATA, $$0, $$1, $$4, $$3);
      if ((this.is(net.minecraft.world.item.Items.SPAWNER) || this.is(net.minecraft.world.item.Items.TRIAL_SPAWNER))
         && $$1.shows(DataComponents.BLOCK_ENTITY_DATA)) {
         TypedEntityData<BlockEntityType<?>> $$5 = (TypedEntityData<BlockEntityType<?>>)this.get(DataComponents.BLOCK_ENTITY_DATA);
         Spawner.appendHoverText($$5, $$4, "SpawnData");
      }

      net.minecraft.world.item.AdventureModePredicate $$6 = (net.minecraft.world.item.AdventureModePredicate)this.get(DataComponents.CAN_BREAK);
      if ($$6 != null && $$1.shows(DataComponents.CAN_BREAK)) {
         $$4.accept(CommonComponents.EMPTY);
         $$4.accept(net.minecraft.world.item.AdventureModePredicate.CAN_BREAK_HEADER);
         $$6.addToTooltip($$4);
      }

      net.minecraft.world.item.AdventureModePredicate $$7 = (net.minecraft.world.item.AdventureModePredicate)this.get(DataComponents.CAN_PLACE_ON);
      if ($$7 != null && $$1.shows(DataComponents.CAN_PLACE_ON)) {
         $$4.accept(CommonComponents.EMPTY);
         $$4.accept(net.minecraft.world.item.AdventureModePredicate.CAN_PLACE_HEADER);
         $$7.addToTooltip($$4);
      }

      if ($$3.isAdvanced()) {
         if (this.isDamaged() && $$1.shows(DataComponents.DAMAGE)) {
            $$4.accept(Component.translatable("item.durability", new Object[]{this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()}));
         }

         $$4.accept(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
         int $$8 = this.components.size();
         if ($$8 > 0) {
            $$4.accept(Component.translatable("item.components", new Object[]{$$8}).withStyle(ChatFormatting.DARK_GRAY));
         }
      }

      if ($$2 != null && !this.getItem().isEnabled($$2.level().enabledFeatures())) {
         $$4.accept(DISABLED_ITEM_TOOLTIP);
      }

      boolean $$9 = this.getItem().shouldPrintOpWarning(this, $$2);
      if ($$9) {
         OP_NBT_WARNING.forEach($$4);
      }
   }

   private void addUnitComponentToTooltip(DataComponentType<?> $$0, Component $$1, TooltipDisplay $$2, Consumer<Component> $$3) {
      if (this.has($$0) && $$2.shows($$0)) {
         $$3.accept($$1);
      }
   }

   private void addAttributeTooltips(Consumer<Component> $$0, TooltipDisplay $$1, Player $$2) {
      if ($$1.shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
         for (EquipmentSlotGroup $$3 : EquipmentSlotGroup.values()) {
            MutableBoolean $$4 = new MutableBoolean(true);
            this.forEachModifier($$3, ($$4x, $$5, $$6) -> {
               if ($$6 != ItemAttributeModifiers.Display.hidden()) {
                  if ($$4.isTrue()) {
                     $$0.accept(CommonComponents.EMPTY);
                     $$0.accept(Component.translatable("item.modifiers." + $$3.getSerializedName()).withStyle(ChatFormatting.GRAY));
                     $$4.setFalse();
                  }

                  $$6.apply($$0, $$2, $$4x, $$5);
               }
            });
         }
      }
   }

   public boolean hasFoil() {
      Boolean $$0 = (Boolean)this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
      return $$0 != null ? $$0 : this.getItem().isFoil(this);
   }

   public net.minecraft.world.item.Rarity getRarity() {
      net.minecraft.world.item.Rarity $$0 = (net.minecraft.world.item.Rarity)this.getOrDefault(DataComponents.RARITY, net.minecraft.world.item.Rarity.COMMON);
      if (!this.isEnchanted()) {
         return $$0;
      } else {
         return switch ($$0) {
            case COMMON, UNCOMMON -> net.minecraft.world.item.Rarity.RARE;
            case RARE -> net.minecraft.world.item.Rarity.EPIC;
            default -> $$0;
         };
      }
   }

   public boolean isEnchantable() {
      if (!this.has(DataComponents.ENCHANTABLE)) {
         return false;
      } else {
         ItemEnchantments $$0 = (ItemEnchantments)this.get(DataComponents.ENCHANTMENTS);
         return $$0 != null && $$0.isEmpty();
      }
   }

   public void enchant(Holder<Enchantment> $$0, int $$1) {
      EnchantmentHelper.updateEnchantments(this, $$2 -> $$2.upgrade($$0, $$1));
   }

   public boolean isEnchanted() {
      return !((ItemEnchantments)this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)).isEmpty();
   }

   public ItemEnchantments getEnchantments() {
      return (ItemEnchantments)this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
   }

   public boolean isFramed() {
      return this.entityRepresentation instanceof ItemFrame;
   }

   public void setEntityRepresentation(Entity $$0) {
      if (!this.isEmpty()) {
         this.entityRepresentation = $$0;
      }
   }

   
   public ItemFrame getFrame() {
      return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
   }

   
   public Entity getEntityRepresentation() {
      return !this.isEmpty() ? this.entityRepresentation : null;
   }

   public void forEachModifier(EquipmentSlotGroup $$0, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> $$1) {
      ItemAttributeModifiers $$2 = (ItemAttributeModifiers)this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
      $$2.forEach($$0, $$1);
      EnchantmentHelper.forEachModifier(this, $$0, ($$1x, $$2x) -> $$1.accept($$1x, $$2x, ItemAttributeModifiers.Display.attributeModifiers()));
   }

   public void forEachModifier(EquipmentSlot $$0, BiConsumer<Holder<Attribute>, AttributeModifier> $$1) {
      ItemAttributeModifiers $$2 = (ItemAttributeModifiers)this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
      $$2.forEach($$0, $$1);
      EnchantmentHelper.forEachModifier(this, $$0, $$1);
   }

   public Component getDisplayName() {
      MutableComponent $$0 = Component.empty().append(this.getHoverName());
      if (this.has(DataComponents.CUSTOM_NAME)) {
         $$0.withStyle(ChatFormatting.ITALIC);
      }

      MutableComponent $$1 = ComponentUtils.wrapInSquareBrackets($$0);
      if (!this.isEmpty()) {
         $$1.withStyle(this.getRarity().color()).withStyle($$0x -> $$0x.withHoverEvent(new ShowItem(this)));
      }

      return $$1;
   }

   public SwingAnimation getSwingAnimation() {
      return (SwingAnimation)this.getOrDefault(DataComponents.SWING_ANIMATION, SwingAnimation.DEFAULT);
   }

   public boolean canPlaceOnBlockInAdventureMode(BlockInWorld $$0) {
      net.minecraft.world.item.AdventureModePredicate $$1 = (net.minecraft.world.item.AdventureModePredicate)this.get(DataComponents.CAN_PLACE_ON);
      return $$1 != null && $$1.test($$0);
   }

   public boolean canBreakBlockInAdventureMode(BlockInWorld $$0) {
      net.minecraft.world.item.AdventureModePredicate $$1 = (net.minecraft.world.item.AdventureModePredicate)this.get(DataComponents.CAN_BREAK);
      return $$1 != null && $$1.test($$0);
   }

   public int getPopTime() {
      return this.popTime;
   }

   public void setPopTime(int $$0) {
      this.popTime = $$0;
   }

   public int getCount() {
      return this.isEmpty() ? 0 : this.count;
   }

   public void setCount(int $$0) {
      this.count = $$0;
   }

   public void limitSize(int $$0) {
      if (!this.isEmpty() && this.getCount() > $$0) {
         this.setCount($$0);
      }
   }

   public void grow(int $$0) {
      this.setCount(this.getCount() + $$0);
   }

   public void shrink(int $$0) {
      this.grow(-$$0);
   }

   public void consume(int $$0, LivingEntity $$1) {
      if ($$1 == null || !$$1.hasInfiniteMaterials()) {
         this.shrink($$0);
      }
   }

   public net.minecraft.world.item.ItemStack consumeAndReturn(int $$0, LivingEntity $$1) {
      net.minecraft.world.item.ItemStack $$2 = this.copyWithCount($$0);
      this.consume($$0, $$1);
      return $$2;
   }

   public void onUseTick(Level $$0, LivingEntity $$1, int $$2) {
      Consumable $$3 = (Consumable)this.get(DataComponents.CONSUMABLE);
      if ($$3 != null && $$3.shouldEmitParticlesAndSounds($$2)) {
         $$3.emitParticlesAndSounds($$1.getRandom(), $$1, this, 5);
      }

      KineticWeapon $$4 = (KineticWeapon)this.get(DataComponents.KINETIC_WEAPON);
      if ($$4 != null && !$$0.isClientSide()) {
         $$4.damageEntities(this, $$2, $$1, $$1.getUsedItemHand().asEquipmentSlot());
      } else {
         this.getItem().onUseTick($$0, $$1, this, $$2);
      }
   }

   public void onDestroyed(ItemEntity $$0) {
      this.getItem().onDestroyed($$0);
   }

   public boolean canBeHurtBy(DamageSource $$0) {
      DamageResistant $$1 = (DamageResistant)this.get(DataComponents.DAMAGE_RESISTANT);
      return $$1 == null || !$$1.isResistantTo($$0);
   }

   public boolean isValidRepairItem(net.minecraft.world.item.ItemStack $$0) {
      Repairable $$1 = (Repairable)this.get(DataComponents.REPAIRABLE);
      return $$1 != null && $$1.isValidRepairItem($$0);
   }

   public boolean canDestroyBlock(BlockState $$0, Level $$1, BlockPos $$2, Player $$3) {
      return this.getItem().canDestroyBlock(this, $$0, $$1, $$2, $$3);
   }

   public DamageSource getDamageSource(LivingEntity $$0, Supplier<DamageSource> $$1) {
      return Optional.ofNullable((net.minecraft.world.item.EitherHolder)this.get(DataComponents.DAMAGE_TYPE))
         .flatMap($$1x -> $$1x.unwrap($$0.registryAccess()))
         .map($$1x -> new DamageSource($$1x, $$0))
         .or(() -> Optional.ofNullable(this.getItem().getItemDamageSource($$0)))
         .orElseGet($$1);
   }
}
