/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat.contents.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource
{
    public static final MapCodec<BlockDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply((Applicative)$$0, BlockDataSource::new));

    public BlockDataSource(String $$0) {
        this($$0, BlockDataSource.compilePos($$0));
    }

    private static @Nullable Coordinates compilePos(String $$0) {
        try {
            return BlockPosArgument.blockPos().parse(new StringReader($$0));
        }
        catch (CommandSyntaxException $$1) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack $$0) {
        BlockEntity $$3;
        BlockPos $$2;
        ServerLevel $$1;
        if (this.compiledPos != null && ($$1 = $$0.getLevel()).isLoaded($$2 = this.compiledPos.getBlockPos($$0)) && ($$3 = $$1.getBlockEntity($$2)) != null) {
            return Stream.of($$3.saveWithFullMetadata($$0.registryAccess()));
        }
        return Stream.empty();
    }

    public MapCodec<BlockDataSource> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "block=" + this.posPattern;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof BlockDataSource)) return false;
        BlockDataSource $$1 = (BlockDataSource)$$0;
        if (!this.posPattern.equals($$1.posPattern)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.posPattern.hashCode();
    }
}

