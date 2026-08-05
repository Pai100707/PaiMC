/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
    public static final StreamCodec<FriendlyByteBuf, RecipeBookSettings> STREAM_CODEC = StreamCodec.composite(TypeSettings.STREAM_CODEC, $$0 -> $$0.crafting, TypeSettings.STREAM_CODEC, $$0 -> $$0.furnace, TypeSettings.STREAM_CODEC, $$0 -> $$0.blastFurnace, TypeSettings.STREAM_CODEC, $$0 -> $$0.smoker, RecipeBookSettings::new);
    public static final MapCodec<RecipeBookSettings> MAP_CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)TypeSettings.CRAFTING_MAP_CODEC.forGetter($$0 -> $$0.crafting), (App)TypeSettings.FURNACE_MAP_CODEC.forGetter($$0 -> $$0.furnace), (App)TypeSettings.BLAST_FURNACE_MAP_CODEC.forGetter($$0 -> $$0.blastFurnace), (App)TypeSettings.SMOKER_MAP_CODEC.forGetter($$0 -> $$0.smoker)).apply((Applicative)$$02, RecipeBookSettings::new));
    private TypeSettings crafting;
    private TypeSettings furnace;
    private TypeSettings blastFurnace;
    private TypeSettings smoker;

    public RecipeBookSettings() {
        this(TypeSettings.DEFAULT, TypeSettings.DEFAULT, TypeSettings.DEFAULT, TypeSettings.DEFAULT);
    }

    private RecipeBookSettings(TypeSettings $$0, TypeSettings $$1, TypeSettings $$2, TypeSettings $$3) {
        this.crafting = $$0;
        this.furnace = $$1;
        this.blastFurnace = $$2;
        this.smoker = $$3;
    }

    @VisibleForTesting
    public TypeSettings getSettings(RecipeBookType $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case RecipeBookType.CRAFTING -> this.crafting;
            case RecipeBookType.FURNACE -> this.furnace;
            case RecipeBookType.BLAST_FURNACE -> this.blastFurnace;
            case RecipeBookType.SMOKER -> this.smoker;
        };
    }

    private void updateSettings(RecipeBookType $$0, UnaryOperator<TypeSettings> $$1) {
        switch ($$0) {
            case CRAFTING: {
                this.crafting = (TypeSettings)$$1.apply(this.crafting);
                break;
            }
            case FURNACE: {
                this.furnace = (TypeSettings)$$1.apply(this.furnace);
                break;
            }
            case BLAST_FURNACE: {
                this.blastFurnace = (TypeSettings)$$1.apply(this.blastFurnace);
                break;
            }
            case SMOKER: {
                this.smoker = (TypeSettings)$$1.apply(this.smoker);
            }
        }
    }

    public boolean isOpen(RecipeBookType $$0) {
        return this.getSettings((RecipeBookType)$$0).open;
    }

    public void setOpen(RecipeBookType $$0, boolean $$12) {
        this.updateSettings($$0, $$1 -> $$1.setOpen($$12));
    }

    public boolean isFiltering(RecipeBookType $$0) {
        return this.getSettings((RecipeBookType)$$0).filtering;
    }

    public void setFiltering(RecipeBookType $$0, boolean $$12) {
        this.updateSettings($$0, $$1 -> $$1.setFiltering($$12));
    }

    public RecipeBookSettings copy() {
        return new RecipeBookSettings(this.crafting, this.furnace, this.blastFurnace, this.smoker);
    }

    public void replaceFrom(RecipeBookSettings $$0) {
        this.crafting = $$0.crafting;
        this.furnace = $$0.furnace;
        this.blastFurnace = $$0.blastFurnace;
        this.smoker = $$0.smoker;
    }

    public static final class TypeSettings
    extends Record {
        final boolean open;
        final boolean filtering;
        public static final TypeSettings DEFAULT = new TypeSettings(false, false);
        public static final MapCodec<TypeSettings> CRAFTING_MAP_CODEC = TypeSettings.codec("isGuiOpen", "isFilteringCraftable");
        public static final MapCodec<TypeSettings> FURNACE_MAP_CODEC = TypeSettings.codec("isFurnaceGuiOpen", "isFurnaceFilteringCraftable");
        public static final MapCodec<TypeSettings> BLAST_FURNACE_MAP_CODEC = TypeSettings.codec("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable");
        public static final MapCodec<TypeSettings> SMOKER_MAP_CODEC = TypeSettings.codec("isSmokerGuiOpen", "isSmokerFilteringCraftable");
        public static final StreamCodec<ByteBuf, TypeSettings> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TypeSettings::open, ByteBufCodecs.BOOL, TypeSettings::filtering, TypeSettings::new);

        public TypeSettings(boolean $$0, boolean $$1) {
            this.open = $$0;
            this.filtering = $$1;
        }

        @Override
        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + "]";
        }

        public TypeSettings setOpen(boolean $$0) {
            return new TypeSettings($$0, this.filtering);
        }

        public TypeSettings setFiltering(boolean $$0) {
            return new TypeSettings(this.open, $$0);
        }

        private static MapCodec<TypeSettings> codec(String $$0, String $$1) {
            return RecordCodecBuilder.mapCodec($$2 -> $$2.group((App)Codec.BOOL.optionalFieldOf($$0, (Object)false).forGetter(TypeSettings::open), (App)Codec.BOOL.optionalFieldOf($$1, (Object)false).forGetter(TypeSettings::filtering)).apply((Applicative)$$2, TypeSettings::new));
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TypeSettings.class, "open;filtering", "open", "filtering"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TypeSettings.class, "open;filtering", "open", "filtering"}, this, $$0);
        }

        public boolean open() {
            return this.open;
        }

        public boolean filtering() {
            return this.filtering;
        }
    }
}

