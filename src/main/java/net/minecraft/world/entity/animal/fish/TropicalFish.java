package net.minecraft.world.entity.animal.fish;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TropicalFish extends AbstractSchoolingFish {
   public static final TropicalFish.Variant DEFAULT_VARIANT = new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.WHITE, DyeColor.WHITE);
   private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
   public static final List<TropicalFish.Variant> COMMON_VARIANTS = List.of(
      new TropicalFish.Variant(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY),
      new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
      new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE),
      new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
      new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY),
      new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
      new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE),
      new TropicalFish.Variant(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
      new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED),
      new TropicalFish.Variant(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
      new TropicalFish.Variant(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY),
      new TropicalFish.Variant(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
      new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK),
      new TropicalFish.Variant(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
      new TropicalFish.Variant(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE),
      new TropicalFish.Variant(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
      new TropicalFish.Variant(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE),
      new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
      new TropicalFish.Variant(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE),
      new TropicalFish.Variant(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
      new TropicalFish.Variant(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW),
      new TropicalFish.Variant(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)
   );
   private boolean isSchool = true;

   public TropicalFish(net.minecraft.world.entity.EntityType<? extends TropicalFish> $$0, Level $$1) {
      super($$0, $$1);
   }

   public static String getPredefinedName(int $$0) {
      return "entity.minecraft.tropical_fish.predefined." + $$0;
   }

   static int packVariant(TropicalFish.Pattern $$0, DyeColor $$1, DyeColor $$2) {
      return $$0.getPackedId() & 65535 | ($$1.getId() & 0xFF) << 16 | ($$2.getId() & 0xFF) << 24;
   }

   public static DyeColor getBaseColor(int $$0) {
      return DyeColor.byId($$0 >> 16 & 0xFF);
   }

   public static DyeColor getPatternColor(int $$0) {
      return DyeColor.byId($$0 >> 24 & 0xFF);
   }

   public static TropicalFish.Pattern getPattern(int $$0) {
      return TropicalFish.Pattern.byId($$0 & 65535);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_ID_TYPE_VARIANT, DEFAULT_VARIANT.getPackedId());
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("Variant", TropicalFish.Variant.CODEC, new TropicalFish.Variant(this.getPackedVariant()));
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      TropicalFish.Variant $$1 = $$0.read("Variant", TropicalFish.Variant.CODEC).orElse(DEFAULT_VARIANT);
      this.setPackedVariant($$1.getPackedId());
   }

   private void setPackedVariant(int $$0) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, $$0);
   }

   @Override
   public boolean isMaxGroupSizeReached(int $$0) {
      return !this.isSchool;
   }

   private int getPackedVariant() {
      return (Integer)this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   public DyeColor getBaseColor() {
      return getBaseColor(this.getPackedVariant());
   }

   public DyeColor getPatternColor() {
      return getPatternColor(this.getPackedVariant());
   }

   public TropicalFish.Pattern getPattern() {
      return getPattern(this.getPackedVariant());
   }

   private void setPattern(TropicalFish.Pattern $$0) {
      int $$1 = this.getPackedVariant();
      DyeColor $$2 = getBaseColor($$1);
      DyeColor $$3 = getPatternColor($$1);
      this.setPackedVariant(packVariant($$0, $$2, $$3));
   }

   private void setBaseColor(DyeColor $$0) {
      int $$1 = this.getPackedVariant();
      TropicalFish.Pattern $$2 = getPattern($$1);
      DyeColor $$3 = getPatternColor($$1);
      this.setPackedVariant(packVariant($$2, $$0, $$3));
   }

   private void setPatternColor(DyeColor $$0) {
      int $$1 = this.getPackedVariant();
      TropicalFish.Pattern $$2 = getPattern($$1);
      DyeColor $$3 = getBaseColor($$1);
      this.setPackedVariant(packVariant($$2, $$3, $$0));
   }

   @Nullable
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      if ($$0 == DataComponents.TROPICAL_FISH_PATTERN) {
         return castComponentValue((DataComponentType<T>)$$0, this.getPattern());
      } else if ($$0 == DataComponents.TROPICAL_FISH_BASE_COLOR) {
         return castComponentValue((DataComponentType<T>)$$0, this.getBaseColor());
      } else {
         return $$0 == DataComponents.TROPICAL_FISH_PATTERN_COLOR ? castComponentValue((DataComponentType<T>)$$0, this.getPatternColor()) : super.get($$0);
      }
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.TROPICAL_FISH_PATTERN);
      this.applyImplicitComponentIfPresent($$0, DataComponents.TROPICAL_FISH_BASE_COLOR);
      this.applyImplicitComponentIfPresent($$0, DataComponents.TROPICAL_FISH_PATTERN_COLOR);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.TROPICAL_FISH_PATTERN) {
         this.setPattern(castComponentValue(DataComponents.TROPICAL_FISH_PATTERN, $$1));
         return true;
      } else if ($$0 == DataComponents.TROPICAL_FISH_BASE_COLOR) {
         this.setBaseColor(castComponentValue(DataComponents.TROPICAL_FISH_BASE_COLOR, $$1));
         return true;
      } else if ($$0 == DataComponents.TROPICAL_FISH_PATTERN_COLOR) {
         this.setPatternColor(castComponentValue(DataComponents.TROPICAL_FISH_PATTERN_COLOR, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   @Override
   public void saveToBucketTag(ItemStack $$0) {
      super.saveToBucketTag($$0);
      $$0.copyFrom(DataComponents.TROPICAL_FISH_PATTERN, this);
      $$0.copyFrom(DataComponents.TROPICAL_FISH_BASE_COLOR, this);
      $$0.copyFrom(DataComponents.TROPICAL_FISH_PATTERN_COLOR, this);
   }

   @Override
   public ItemStack getBucketItemStack() {
      return new ItemStack(Items.TROPICAL_FISH_BUCKET);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.TROPICAL_FISH_AMBIENT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.TROPICAL_FISH_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.TROPICAL_FISH_HURT;
   }

   @Override
   protected SoundEvent getFlopSound() {
      return SoundEvents.TROPICAL_FISH_FLOP;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
      RandomSource $$4 = $$0.getRandom();
      TropicalFish.Variant $$6;
      if ($$3 instanceof TropicalFish.TropicalFishGroupData $$5) {
         $$6 = $$5.variant;
      } else if ($$4.nextFloat() < 0.9) {
         $$6 = (TropicalFish.Variant)Util.getRandom(COMMON_VARIANTS, $$4);
         $$3 = new TropicalFish.TropicalFishGroupData(this, $$6);
      } else {
         this.isSchool = false;
         TropicalFish.Pattern[] $$8 = TropicalFish.Pattern.values();
         DyeColor[] $$9 = DyeColor.values();
         TropicalFish.Pattern $$10 = (TropicalFish.Pattern)Util.getRandom($$8, $$4);
         DyeColor $$11 = (DyeColor)Util.getRandom($$9, $$4);
         DyeColor $$12 = (DyeColor)Util.getRandom($$9, $$4);
         $$6 = new TropicalFish.Variant($$10, $$11, $$12);
      }

      this.setPackedVariant($$6.getPackedId());
      return $$3;
   }

   public static boolean checkTropicalFishSpawnRules(
      net.minecraft.world.entity.EntityType<TropicalFish> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$1.getFluidState($$3.below()).is(FluidTags.WATER)
         && $$1.getBlockState($$3.above()).is(Blocks.WATER)
         && (
            $$1.getBiome($$3).is(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterAnimal.checkSurfaceWaterAnimalSpawnRules($$0, $$1, $$2, $$3, $$4)
         );
   }

   public static enum Base {
      SMALL(0),
      LARGE(1);

      final int id;

      private Base(final int $$0) {
         this.id = $$0;
      }
   }

   public static enum Pattern implements StringRepresentable, TooltipProvider {
      KOB("kob", TropicalFish.Base.SMALL, 0),
      SUNSTREAK("sunstreak", TropicalFish.Base.SMALL, 1),
      SNOOPER("snooper", TropicalFish.Base.SMALL, 2),
      DASHER("dasher", TropicalFish.Base.SMALL, 3),
      BRINELY("brinely", TropicalFish.Base.SMALL, 4),
      SPOTTY("spotty", TropicalFish.Base.SMALL, 5),
      FLOPPER("flopper", TropicalFish.Base.LARGE, 0),
      STRIPEY("stripey", TropicalFish.Base.LARGE, 1),
      GLITTER("glitter", TropicalFish.Base.LARGE, 2),
      BLOCKFISH("blockfish", TropicalFish.Base.LARGE, 3),
      BETTY("betty", TropicalFish.Base.LARGE, 4),
      CLAYFISH("clayfish", TropicalFish.Base.LARGE, 5);

      public static final Codec<TropicalFish.Pattern> CODEC = StringRepresentable.fromEnum(TropicalFish.Pattern::values);
      private static final IntFunction<TropicalFish.Pattern> BY_ID = ByIdMap.sparse(TropicalFish.Pattern::getPackedId, values(), KOB);
      public static final StreamCodec<ByteBuf, TropicalFish.Pattern> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TropicalFish.Pattern::getPackedId);
      private final String name;
      private final Component displayName;
      private final TropicalFish.Base base;
      private final int packedId;

      private Pattern(final String $$0, final TropicalFish.Base $$1, final int $$2) {
         this.name = $$0;
         this.base = $$1;
         this.packedId = $$1.id | $$2 << 8;
         this.displayName = Component.translatable("entity.minecraft.tropical_fish.type." + this.name);
      }

      public static TropicalFish.Pattern byId(int $$0) {
         return BY_ID.apply($$0);
      }

      public TropicalFish.Base base() {
         return this.base;
      }

      public int getPackedId() {
         return this.packedId;
      }

      public String getSerializedName() {
         return this.name;
      }

      public Component displayName() {
         return this.displayName;
      }

      public void addToTooltip(TooltipContext $$0, Consumer<Component> $$1, TooltipFlag $$2, DataComponentGetter $$3) {
         DyeColor $$4 = (DyeColor)$$3.getOrDefault(DataComponents.TROPICAL_FISH_BASE_COLOR, TropicalFish.DEFAULT_VARIANT.baseColor());
         DyeColor $$5 = (DyeColor)$$3.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN_COLOR, TropicalFish.DEFAULT_VARIANT.patternColor());
         ChatFormatting[] $$6 = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
         int $$7 = TropicalFish.COMMON_VARIANTS.indexOf(new TropicalFish.Variant(this, $$4, $$5));
         if ($$7 != -1) {
            $$1.accept(Component.translatable(TropicalFish.getPredefinedName($$7)).withStyle($$6));
         } else {
            $$1.accept(this.displayName.plainCopy().withStyle($$6));
            MutableComponent $$8 = Component.translatable("color.minecraft." + $$4.getName());
            if ($$4 != $$5) {
               $$8.append(", ").append(Component.translatable("color.minecraft." + $$5.getName()));
            }

            $$8.withStyle($$6);
            $$1.accept($$8);
         }
      }
   }

   static class TropicalFishGroupData extends AbstractSchoolingFish.SchoolSpawnGroupData {
      final TropicalFish.Variant variant;

      TropicalFishGroupData(TropicalFish $$0, TropicalFish.Variant $$1) {
         super($$0);
         this.variant = $$1;
      }
   }

   public record Variant(TropicalFish.Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
      public static final Codec<TropicalFish.Variant> CODEC = Codec.INT.xmap(TropicalFish.Variant::new, TropicalFish.Variant::getPackedId);

      public Variant(int $$0) {
         this(TropicalFish.getPattern($$0), TropicalFish.getBaseColor($$0), TropicalFish.getPatternColor($$0));
      }

      public int getPackedId() {
         return TropicalFish.packVariant(this.pattern, this.baseColor, this.patternColor);
      }
   }
}
