package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.EncoderCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.fish.TropicalFish.Pattern;
import net.minecraft.world.entity.animal.fox.Fox.Variant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;

public class DataComponents {
   static final EncoderCache ENCODER_CACHE = new EncoderCache(512);
   public static final DataComponentType<CustomData> CUSTOM_DATA = register("custom_data", $$0 -> $$0.persistent(CustomData.CODEC));
   public static final DataComponentType<Integer> MAX_STACK_SIZE = register(
      "max_stack_size", $$0 -> $$0.persistent(ExtraCodecs.intRange(1, 99)).networkSynchronized(ByteBufCodecs.VAR_INT)
   );
   public static final DataComponentType<Integer> MAX_DAMAGE = register(
      "max_damage", $$0 -> $$0.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
   );
   public static final DataComponentType<Integer> DAMAGE = register(
      "damage", $$0 -> $$0.persistent(ExtraCodecs.NON_NEGATIVE_INT).ignoreSwapAnimation().networkSynchronized(ByteBufCodecs.VAR_INT)
   );
   public static final DataComponentType<Unit> UNBREAKABLE = register("unbreakable", $$0 -> $$0.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));
   public static final DataComponentType<UseEffects> USE_EFFECTS = register(
      "use_effects", $$0 -> $$0.persistent(UseEffects.CODEC).networkSynchronized(UseEffects.STREAM_CODEC)
   );
   public static final DataComponentType<Component> CUSTOM_NAME = register(
      "custom_name", $$0 -> $$0.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Float> MINIMUM_ATTACK_CHARGE = register(
      "minimum_attack_charge", $$0 -> $$0.persistent(ExtraCodecs.floatRange(0.0F, 1.0F)).networkSynchronized(ByteBufCodecs.FLOAT)
   );
   public static final DataComponentType<EitherHolder<DamageType>> DAMAGE_TYPE = register(
      "damage_type",
      $$0 -> $$0.persistent(EitherHolder.codec(Registries.DAMAGE_TYPE, DamageType.CODEC))
         .networkSynchronized(EitherHolder.streamCodec(Registries.DAMAGE_TYPE, DamageType.STREAM_CODEC))
   );
   public static final DataComponentType<Component> ITEM_NAME = register(
      "item_name", $$0 -> $$0.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Identifier> ITEM_MODEL = register(
      "item_model", $$0 -> $$0.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<ItemLore> LORE = register(
      "lore", $$0 -> $$0.persistent(ItemLore.CODEC).networkSynchronized(ItemLore.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Rarity> RARITY = register("rarity", $$0 -> $$0.persistent(Rarity.CODEC).networkSynchronized(Rarity.STREAM_CODEC));
   public static final DataComponentType<ItemEnchantments> ENCHANTMENTS = register(
      "enchantments", $$0 -> $$0.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<AdventureModePredicate> CAN_PLACE_ON = register(
      "can_place_on", $$0 -> $$0.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<AdventureModePredicate> CAN_BREAK = register(
      "can_break", $$0 -> $$0.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<ItemAttributeModifiers> ATTRIBUTE_MODIFIERS = register(
      "attribute_modifiers", $$0 -> $$0.persistent(ItemAttributeModifiers.CODEC).networkSynchronized(ItemAttributeModifiers.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<CustomModelData> CUSTOM_MODEL_DATA = register(
      "custom_model_data", $$0 -> $$0.persistent(CustomModelData.CODEC).networkSynchronized(CustomModelData.STREAM_CODEC)
   );
   public static final DataComponentType<TooltipDisplay> TOOLTIP_DISPLAY = register(
      "tooltip_display", $$0 -> $$0.persistent(TooltipDisplay.CODEC).networkSynchronized(TooltipDisplay.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Integer> REPAIR_COST = register(
      "repair_cost", $$0 -> $$0.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
   );
   public static final DataComponentType<Unit> CREATIVE_SLOT_LOCK = register("creative_slot_lock", $$0 -> $$0.networkSynchronized(Unit.STREAM_CODEC));
   public static final DataComponentType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register(
      "enchantment_glint_override", $$0 -> $$0.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
   );
   public static final DataComponentType<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", $$0 -> $$0.persistent(Unit.CODEC));
   public static final DataComponentType<FoodProperties> FOOD = register(
      "food", $$0 -> $$0.persistent(FoodProperties.DIRECT_CODEC).networkSynchronized(FoodProperties.DIRECT_STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Consumable> CONSUMABLE = register(
      "consumable", $$0 -> $$0.persistent(Consumable.CODEC).networkSynchronized(Consumable.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<UseRemainder> USE_REMAINDER = register(
      "use_remainder", $$0 -> $$0.persistent(UseRemainder.CODEC).networkSynchronized(UseRemainder.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<UseCooldown> USE_COOLDOWN = register(
      "use_cooldown", $$0 -> $$0.persistent(UseCooldown.CODEC).networkSynchronized(UseCooldown.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<DamageResistant> DAMAGE_RESISTANT = register(
      "damage_resistant", $$0 -> $$0.persistent(DamageResistant.CODEC).networkSynchronized(DamageResistant.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Tool> TOOL = register("tool", $$0 -> $$0.persistent(Tool.CODEC).networkSynchronized(Tool.STREAM_CODEC).cacheEncoding());
   public static final DataComponentType<Weapon> WEAPON = register(
      "weapon", $$0 -> $$0.persistent(Weapon.CODEC).networkSynchronized(Weapon.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<AttackRange> ATTACK_RANGE = register(
      "attack_range", $$0 -> $$0.persistent(AttackRange.CODEC).networkSynchronized(AttackRange.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Enchantable> ENCHANTABLE = register(
      "enchantable", $$0 -> $$0.persistent(Enchantable.CODEC).networkSynchronized(Enchantable.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Equippable> EQUIPPABLE = register(
      "equippable", $$0 -> $$0.persistent(Equippable.CODEC).networkSynchronized(Equippable.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Repairable> REPAIRABLE = register(
      "repairable", $$0 -> $$0.persistent(Repairable.CODEC).networkSynchronized(Repairable.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Unit> GLIDER = register("glider", $$0 -> $$0.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));
   public static final DataComponentType<Identifier> TOOLTIP_STYLE = register(
      "tooltip_style", $$0 -> $$0.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<DeathProtection> DEATH_PROTECTION = register(
      "death_protection", $$0 -> $$0.persistent(DeathProtection.CODEC).networkSynchronized(DeathProtection.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<BlocksAttacks> BLOCKS_ATTACKS = register(
      "blocks_attacks", $$0 -> $$0.persistent(BlocksAttacks.CODEC).networkSynchronized(BlocksAttacks.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<PiercingWeapon> PIERCING_WEAPON = register(
      "piercing_weapon", $$0 -> $$0.persistent(PiercingWeapon.CODEC).networkSynchronized(PiercingWeapon.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<KineticWeapon> KINETIC_WEAPON = register(
      "kinetic_weapon", $$0 -> $$0.persistent(KineticWeapon.CODEC).networkSynchronized(KineticWeapon.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<SwingAnimation> SWING_ANIMATION = register(
      "swing_animation", $$0 -> $$0.persistent(SwingAnimation.CODEC).networkSynchronized(SwingAnimation.STREAM_CODEC)
   );
   public static final DataComponentType<ItemEnchantments> STORED_ENCHANTMENTS = register(
      "stored_enchantments", $$0 -> $$0.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<DyedItemColor> DYED_COLOR = register(
      "dyed_color", $$0 -> $$0.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC)
   );
   public static final DataComponentType<MapItemColor> MAP_COLOR = register(
      "map_color", $$0 -> $$0.persistent(MapItemColor.CODEC).networkSynchronized(MapItemColor.STREAM_CODEC)
   );
   public static final DataComponentType<MapId> MAP_ID = register("map_id", $$0 -> $$0.persistent(MapId.CODEC).networkSynchronized(MapId.STREAM_CODEC));
   public static final DataComponentType<MapDecorations> MAP_DECORATIONS = register(
      "map_decorations", $$0 -> $$0.persistent(MapDecorations.CODEC).cacheEncoding()
   );
   public static final DataComponentType<MapPostProcessing> MAP_POST_PROCESSING = register(
      "map_post_processing", $$0 -> $$0.networkSynchronized(MapPostProcessing.STREAM_CODEC)
   );
   public static final DataComponentType<ChargedProjectiles> CHARGED_PROJECTILES = register(
      "charged_projectiles", $$0 -> $$0.persistent(ChargedProjectiles.CODEC).networkSynchronized(ChargedProjectiles.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<BundleContents> BUNDLE_CONTENTS = register(
      "bundle_contents", $$0 -> $$0.persistent(BundleContents.CODEC).networkSynchronized(BundleContents.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<PotionContents> POTION_CONTENTS = register(
      "potion_contents", $$0 -> $$0.persistent(PotionContents.CODEC).networkSynchronized(PotionContents.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Float> POTION_DURATION_SCALE = register(
      "potion_duration_scale", $$0 -> $$0.persistent(ExtraCodecs.NON_NEGATIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT).cacheEncoding()
   );
   public static final DataComponentType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register(
      "suspicious_stew_effects", $$0 -> $$0.persistent(SuspiciousStewEffects.CODEC).networkSynchronized(SuspiciousStewEffects.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT = register(
      "writable_book_content", $$0 -> $$0.persistent(WritableBookContent.CODEC).networkSynchronized(WritableBookContent.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT = register(
      "written_book_content", $$0 -> $$0.persistent(WrittenBookContent.CODEC).networkSynchronized(WrittenBookContent.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<ArmorTrim> TRIM = register(
      "trim", $$0 -> $$0.persistent(ArmorTrim.CODEC).networkSynchronized(ArmorTrim.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<DebugStickState> DEBUG_STICK_STATE = register(
      "debug_stick_state", $$0 -> $$0.persistent(DebugStickState.CODEC).cacheEncoding()
   );
   public static final DataComponentType<TypedEntityData<EntityType<?>>> ENTITY_DATA = register(
      "entity_data", $$0 -> $$0.persistent(TypedEntityData.codec(EntityType.CODEC)).networkSynchronized(TypedEntityData.streamCodec(EntityType.STREAM_CODEC))
   );
   public static final DataComponentType<CustomData> BUCKET_ENTITY_DATA = register(
      "bucket_entity_data", $$0 -> $$0.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
   );
   public static final DataComponentType<TypedEntityData<BlockEntityType<?>>> BLOCK_ENTITY_DATA = register(
      "block_entity_data",
      $$0 -> $$0.persistent(TypedEntityData.codec(BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec()))
         .networkSynchronized(TypedEntityData.streamCodec(ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE)))
   );
   public static final DataComponentType<InstrumentComponent> INSTRUMENT = register(
      "instrument", $$0 -> $$0.persistent(InstrumentComponent.CODEC).networkSynchronized(InstrumentComponent.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<ProvidesTrimMaterial> PROVIDES_TRIM_MATERIAL = register(
      "provides_trim_material", $$0 -> $$0.persistent(ProvidesTrimMaterial.CODEC).networkSynchronized(ProvidesTrimMaterial.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<OminousBottleAmplifier> OMINOUS_BOTTLE_AMPLIFIER = register(
      "ominous_bottle_amplifier", $$0 -> $$0.persistent(OminousBottleAmplifier.CODEC).networkSynchronized(OminousBottleAmplifier.STREAM_CODEC)
   );
   public static final DataComponentType<JukeboxPlayable> JUKEBOX_PLAYABLE = register(
      "jukebox_playable", $$0 -> $$0.persistent(JukeboxPlayable.CODEC).networkSynchronized(JukeboxPlayable.STREAM_CODEC)
   );
   public static final DataComponentType<TagKey<BannerPattern>> PROVIDES_BANNER_PATTERNS = register(
      "provides_banner_patterns",
      $$0 -> $$0.persistent(TagKey.hashedCodec(Registries.BANNER_PATTERN)).networkSynchronized(TagKey.streamCodec(Registries.BANNER_PATTERN)).cacheEncoding()
   );
   public static final DataComponentType<List<ResourceKey<Recipe<?>>>> RECIPES = register(
      "recipes", $$0 -> $$0.persistent(Recipe.KEY_CODEC.listOf()).cacheEncoding()
   );
   public static final DataComponentType<LodestoneTracker> LODESTONE_TRACKER = register(
      "lodestone_tracker", $$0 -> $$0.persistent(LodestoneTracker.CODEC).networkSynchronized(LodestoneTracker.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<FireworkExplosion> FIREWORK_EXPLOSION = register(
      "firework_explosion", $$0 -> $$0.persistent(FireworkExplosion.CODEC).networkSynchronized(FireworkExplosion.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Fireworks> FIREWORKS = register(
      "fireworks", $$0 -> $$0.persistent(Fireworks.CODEC).networkSynchronized(Fireworks.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<ResolvableProfile> PROFILE = register(
      "profile", $$0 -> $$0.persistent(ResolvableProfile.CODEC).networkSynchronized(ResolvableProfile.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Identifier> NOTE_BLOCK_SOUND = register(
      "note_block_sound", $$0 -> $$0.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC)
   );
   public static final DataComponentType<BannerPatternLayers> BANNER_PATTERNS = register(
      "banner_patterns", $$0 -> $$0.persistent(BannerPatternLayers.CODEC).networkSynchronized(BannerPatternLayers.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<DyeColor> BASE_COLOR = register(
      "base_color", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentType<PotDecorations> POT_DECORATIONS = register(
      "pot_decorations", $$0 -> $$0.persistent(PotDecorations.CODEC).networkSynchronized(PotDecorations.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<ItemContainerContents> CONTAINER = register(
      "container", $$0 -> $$0.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<BlockItemStateProperties> BLOCK_STATE = register(
      "block_state", $$0 -> $$0.persistent(BlockItemStateProperties.CODEC).networkSynchronized(BlockItemStateProperties.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<Bees> BEES = register("bees", $$0 -> $$0.persistent(Bees.CODEC).networkSynchronized(Bees.STREAM_CODEC).cacheEncoding());
   public static final DataComponentType<LockCode> LOCK = register("lock", $$0 -> $$0.persistent(LockCode.CODEC));
   public static final DataComponentType<SeededContainerLoot> CONTAINER_LOOT = register("container_loot", $$0 -> $$0.persistent(SeededContainerLoot.CODEC));
   public static final DataComponentType<net.minecraft.core.Holder<SoundEvent>> BREAK_SOUND = register(
      "break_sound", $$0 -> $$0.persistent(SoundEvent.CODEC).networkSynchronized(SoundEvent.STREAM_CODEC).cacheEncoding()
   );
   public static final DataComponentType<net.minecraft.core.Holder<VillagerType>> VILLAGER_VARIANT = register(
      "villager/variant", $$0 -> $$0.persistent(VillagerType.CODEC).networkSynchronized(VillagerType.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.core.Holder<WolfVariant>> WOLF_VARIANT = register(
      "wolf/variant", $$0 -> $$0.persistent(WolfVariant.CODEC).networkSynchronized(WolfVariant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.core.Holder<WolfSoundVariant>> WOLF_SOUND_VARIANT = register(
      "wolf/sound_variant", $$0 -> $$0.persistent(WolfSoundVariant.CODEC).networkSynchronized(WolfSoundVariant.STREAM_CODEC)
   );
   public static final DataComponentType<DyeColor> WOLF_COLLAR = register(
      "wolf/collar", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentType<Variant> FOX_VARIANT = register(
      "fox/variant", $$0 -> $$0.persistent(Variant.CODEC).networkSynchronized(Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.fish.Salmon.Variant> SALMON_SIZE = register(
      "salmon/size",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.fish.Salmon.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.fish.Salmon.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.parrot.Parrot.Variant> PARROT_VARIANT = register(
      "parrot/variant",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.parrot.Parrot.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.parrot.Parrot.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<Pattern> TROPICAL_FISH_PATTERN = register(
      "tropical_fish/pattern", $$0 -> $$0.persistent(Pattern.CODEC).networkSynchronized(Pattern.STREAM_CODEC)
   );
   public static final DataComponentType<DyeColor> TROPICAL_FISH_BASE_COLOR = register(
      "tropical_fish/base_color", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentType<DyeColor> TROPICAL_FISH_PATTERN_COLOR = register(
      "tropical_fish/pattern_color", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.cow.MushroomCow.Variant> MOOSHROOM_VARIANT = register(
      "mooshroom/variant",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.cow.MushroomCow.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.cow.MushroomCow.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.rabbit.Rabbit.Variant> RABBIT_VARIANT = register(
      "rabbit/variant",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.rabbit.Rabbit.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.rabbit.Rabbit.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.core.Holder<PigVariant>> PIG_VARIANT = register(
      "pig/variant", $$0 -> $$0.persistent(PigVariant.CODEC).networkSynchronized(PigVariant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.core.Holder<CowVariant>> COW_VARIANT = register(
      "cow/variant", $$0 -> $$0.persistent(CowVariant.CODEC).networkSynchronized(CowVariant.STREAM_CODEC)
   );
   public static final DataComponentType<EitherHolder<ChickenVariant>> CHICKEN_VARIANT = register(
      "chicken/variant",
      $$0 -> $$0.persistent(EitherHolder.codec(Registries.CHICKEN_VARIANT, ChickenVariant.CODEC))
         .networkSynchronized(EitherHolder.streamCodec(Registries.CHICKEN_VARIANT, ChickenVariant.STREAM_CODEC))
   );
   public static final DataComponentType<EitherHolder<ZombieNautilusVariant>> ZOMBIE_NAUTILUS_VARIANT = register(
      "zombie_nautilus/variant",
      $$0 -> $$0.persistent(EitherHolder.codec(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.CODEC))
         .networkSynchronized(EitherHolder.streamCodec(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.STREAM_CODEC))
   );
   public static final DataComponentType<net.minecraft.core.Holder<FrogVariant>> FROG_VARIANT = register(
      "frog/variant", $$0 -> $$0.persistent(FrogVariant.CODEC).networkSynchronized(FrogVariant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.equine.Variant> HORSE_VARIANT = register(
      "horse/variant",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.equine.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.equine.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.core.Holder<PaintingVariant>> PAINTING_VARIANT = register(
      "painting/variant", $$0 -> $$0.persistent(PaintingVariant.CODEC).networkSynchronized(PaintingVariant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.equine.Llama.Variant> LLAMA_VARIANT = register(
      "llama/variant",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.equine.Llama.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.equine.Llama.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.world.entity.animal.axolotl.Axolotl.Variant> AXOLOTL_VARIANT = register(
      "axolotl/variant",
      $$0 -> $$0.persistent(net.minecraft.world.entity.animal.axolotl.Axolotl.Variant.CODEC)
         .networkSynchronized(net.minecraft.world.entity.animal.axolotl.Axolotl.Variant.STREAM_CODEC)
   );
   public static final DataComponentType<net.minecraft.core.Holder<CatVariant>> CAT_VARIANT = register(
      "cat/variant", $$0 -> $$0.persistent(CatVariant.CODEC).networkSynchronized(CatVariant.STREAM_CODEC)
   );
   public static final DataComponentType<DyeColor> CAT_COLLAR = register(
      "cat/collar", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentType<DyeColor> SHEEP_COLOR = register(
      "sheep/color", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentType<DyeColor> SHULKER_COLOR = register(
      "shulker/color", $$0 -> $$0.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
   );
   public static final DataComponentMap COMMON_ITEM_COMPONENTS = DataComponentMap.builder()
      .set(MAX_STACK_SIZE, 64)
      .set(LORE, ItemLore.EMPTY)
      .set(ENCHANTMENTS, ItemEnchantments.EMPTY)
      .set(REPAIR_COST, 0)
      .set(USE_EFFECTS, UseEffects.DEFAULT)
      .set(ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
      .set(RARITY, Rarity.COMMON)
      .set(BREAK_SOUND, SoundEvents.ITEM_BREAK)
      .set(TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
      .set(SWING_ANIMATION, SwingAnimation.DEFAULT)
      .build();

   public static DataComponentType<?> bootstrap(net.minecraft.core.Registry<DataComponentType<?>> $$0) {
      return CUSTOM_DATA;
   }

   private static <T> DataComponentType<T> register(String $$0, UnaryOperator<DataComponentType.Builder<T>> $$1) {
      return net.minecraft.core.Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, $$0, $$1.apply(DataComponentType.builder()).build());
   }
}
