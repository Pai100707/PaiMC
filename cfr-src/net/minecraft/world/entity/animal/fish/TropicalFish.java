/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TropicalFish
extends AbstractSchoolingFish {
    public static final Variant DEFAULT_VARIANT = new Variant(Pattern.KOB, DyeColor.WHITE, DyeColor.WHITE);
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
    public static final List<Variant> COMMON_VARIANTS = List.of(new Variant(Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), new Variant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), new Variant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), new Variant(Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), new Variant(Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), new Variant(Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), new Variant(Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), new Variant(Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), new Variant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), new Variant(Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), new Variant(Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), new Variant(Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), new Variant(Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), new Variant(Pattern.KOB, DyeColor.RED, DyeColor.WHITE), new Variant(Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), new Variant(Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), new Variant(Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));
    private boolean isSchool = true;

    public TropicalFish(EntityType<? extends TropicalFish> $$0, Level $$1) {
        super((EntityType<? extends AbstractSchoolingFish>)$$0, $$1);
    }

    public static String getPredefinedName(int $$0) {
        return "entity.minecraft.tropical_fish.predefined." + $$0;
    }

    static int packVariant(Pattern $$0, DyeColor $$1, DyeColor $$2) {
        return $$0.getPackedId() & 0xFFFF | ($$1.getId() & 0xFF) << 16 | ($$2.getId() & 0xFF) << 24;
    }

    public static DyeColor getBaseColor(int $$0) {
        return DyeColor.byId($$0 >> 16 & 0xFF);
    }

    public static DyeColor getPatternColor(int $$0) {
        return DyeColor.byId($$0 >> 24 & 0xFF);
    }

    public static Pattern getPattern(int $$0) {
        return Pattern.byId($$0 & 0xFFFF);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_TYPE_VARIANT, DEFAULT_VARIANT.getPackedId());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("Variant", Variant.CODEC, new Variant(this.getPackedVariant()));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        Variant $$1 = $$0.read("Variant", Variant.CODEC).orElse(DEFAULT_VARIANT);
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
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    public DyeColor getBaseColor() {
        return TropicalFish.getBaseColor(this.getPackedVariant());
    }

    public DyeColor getPatternColor() {
        return TropicalFish.getPatternColor(this.getPackedVariant());
    }

    public Pattern getPattern() {
        return TropicalFish.getPattern(this.getPackedVariant());
    }

    private void setPattern(Pattern $$0) {
        int $$1 = this.getPackedVariant();
        DyeColor $$2 = TropicalFish.getBaseColor($$1);
        DyeColor $$3 = TropicalFish.getPatternColor($$1);
        this.setPackedVariant(TropicalFish.packVariant($$0, $$2, $$3));
    }

    private void setBaseColor(DyeColor $$0) {
        int $$1 = this.getPackedVariant();
        Pattern $$2 = TropicalFish.getPattern($$1);
        DyeColor $$3 = TropicalFish.getPatternColor($$1);
        this.setPackedVariant(TropicalFish.packVariant($$2, $$0, $$3));
    }

    private void setPatternColor(DyeColor $$0) {
        int $$1 = this.getPackedVariant();
        Pattern $$2 = TropicalFish.getPattern($$1);
        DyeColor $$3 = TropicalFish.getBaseColor($$1);
        this.setPackedVariant(TropicalFish.packVariant($$2, $$3, $$0));
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.TROPICAL_FISH_PATTERN) {
            return TropicalFish.castComponentValue($$0, this.getPattern());
        }
        if ($$0 == DataComponents.TROPICAL_FISH_BASE_COLOR) {
            return TropicalFish.castComponentValue($$0, this.getBaseColor());
        }
        if ($$0 == DataComponents.TROPICAL_FISH_PATTERN_COLOR) {
            return TropicalFish.castComponentValue($$0, this.getPatternColor());
        }
        return super.get($$0);
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
            this.setPattern(TropicalFish.castComponentValue(DataComponents.TROPICAL_FISH_PATTERN, $$1));
            return true;
        }
        if ($$0 == DataComponents.TROPICAL_FISH_BASE_COLOR) {
            this.setBaseColor(TropicalFish.castComponentValue(DataComponents.TROPICAL_FISH_BASE_COLOR, $$1));
            return true;
        }
        if ($$0 == DataComponents.TROPICAL_FISH_PATTERN_COLOR) {
            this.setPatternColor(TropicalFish.castComponentValue(DataComponents.TROPICAL_FISH_PATTERN_COLOR, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
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

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        Variant $$13;
        $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
        RandomSource $$4 = $$0.getRandom();
        if ($$3 instanceof TropicalFishGroupData) {
            TropicalFishGroupData $$5 = (TropicalFishGroupData)$$3;
            Variant $$6 = $$5.variant;
        } else if ((double)$$4.nextFloat() < 0.9) {
            Variant $$7 = Util.getRandom(COMMON_VARIANTS, $$4);
            $$3 = new TropicalFishGroupData(this, $$7);
        } else {
            this.isSchool = false;
            Pattern[] $$8 = Pattern.values();
            DyeColor[] $$9 = DyeColor.values();
            Pattern $$10 = Util.getRandom($$8, $$4);
            DyeColor $$11 = Util.getRandom($$9, $$4);
            DyeColor $$12 = Util.getRandom($$9, $$4);
            $$13 = new Variant($$10, $$11, $$12);
        }
        this.setPackedVariant($$13.getPackedId());
        return $$3;
    }

    public static boolean checkTropicalFishSpawnRules(EntityType<TropicalFish> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getFluidState($$3.below()).is(FluidTags.WATER) && $$1.getBlockState($$3.above()).is(Blocks.WATER) && ($$1.getBiome($$3).is(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT) || WaterAnimal.checkSurfaceWaterAnimalSpawnRules($$0, $$1, $$2, $$3, $$4));
    }

    public static enum Pattern implements StringRepresentable,
    TooltipProvider
    {
        KOB("kob", Base.SMALL, 0),
        SUNSTREAK("sunstreak", Base.SMALL, 1),
        SNOOPER("snooper", Base.SMALL, 2),
        DASHER("dasher", Base.SMALL, 3),
        BRINELY("brinely", Base.SMALL, 4),
        SPOTTY("spotty", Base.SMALL, 5),
        FLOPPER("flopper", Base.LARGE, 0),
        STRIPEY("stripey", Base.LARGE, 1),
        GLITTER("glitter", Base.LARGE, 2),
        BLOCKFISH("blockfish", Base.LARGE, 3),
        BETTY("betty", Base.LARGE, 4),
        CLAYFISH("clayfish", Base.LARGE, 5);

        public static final Codec<Pattern> CODEC;
        private static final IntFunction<Pattern> BY_ID;
        public static final StreamCodec<ByteBuf, Pattern> STREAM_CODEC;
        private final String name;
        private final Component displayName;
        private final Base base;
        private final int packedId;

        private Pattern(String $$0, Base $$1, int $$2) {
            this.name = $$0;
            this.base = $$1;
            this.packedId = $$1.id | $$2 << 8;
            this.displayName = Component.translatable("entity.minecraft.tropical_fish.type." + this.name);
        }

        public static Pattern byId(int $$0) {
            return BY_ID.apply($$0);
        }

        public Base base() {
            return this.base;
        }

        public int getPackedId() {
            return this.packedId;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Component displayName() {
            return this.displayName;
        }

        @Override
        public void addToTooltip(Item.TooltipContext $$0, Consumer<Component> $$1, TooltipFlag $$2, DataComponentGetter $$3) {
            DyeColor $$4 = $$3.getOrDefault(DataComponents.TROPICAL_FISH_BASE_COLOR, DEFAULT_VARIANT.baseColor());
            DyeColor $$5 = $$3.getOrDefault(DataComponents.TROPICAL_FISH_PATTERN_COLOR, DEFAULT_VARIANT.patternColor());
            ChatFormatting[] $$6 = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
            int $$7 = COMMON_VARIANTS.indexOf(new Variant(this, $$4, $$5));
            if ($$7 != -1) {
                $$1.accept(Component.translatable(TropicalFish.getPredefinedName($$7)).withStyle($$6));
                return;
            }
            $$1.accept(this.displayName.plainCopy().withStyle($$6));
            MutableComponent $$8 = Component.translatable("color.minecraft." + $$4.getName());
            if ($$4 != $$5) {
                $$8.append(", ").append(Component.translatable("color.minecraft." + $$5.getName()));
            }
            $$8.withStyle($$6);
            $$1.accept($$8);
        }

        static {
            CODEC = StringRepresentable.fromEnum(Pattern::values);
            BY_ID = ByIdMap.sparse(Pattern::getPackedId, Pattern.values(), KOB);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Pattern::getPackedId);
        }
    }

    public record Variant(Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
        public static final Codec<Variant> CODEC = Codec.INT.xmap(Variant::new, Variant::getPackedId);

        public Variant(int $$0) {
            this(TropicalFish.getPattern($$0), TropicalFish.getBaseColor($$0), TropicalFish.getPatternColor($$0));
        }

        public int getPackedId() {
            return TropicalFish.packVariant(this.pattern, this.baseColor, this.patternColor);
        }
    }

    static class TropicalFishGroupData
    extends AbstractSchoolingFish.SchoolSpawnGroupData {
        final Variant variant;

        TropicalFishGroupData(TropicalFish $$0, Variant $$1) {
            super($$0);
            this.variant = $$1;
        }
    }

    public static enum Base {
        SMALL(0),
        LARGE(1);

        final int id;

        private Base(int $$0) {
            this.id = $$0;
        }
    }
}

