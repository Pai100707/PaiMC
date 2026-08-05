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
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public record EntityDataSource(String selectorPattern, @Nullable EntitySelector compiledSelector) implements DataSource
{
    public static final MapCodec<EntityDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("entity").forGetter(EntityDataSource::selectorPattern)).apply((Applicative)$$0, EntityDataSource::new));

    public EntityDataSource(String $$0) {
        this($$0, EntityDataSource.compileSelector($$0));
    }

    private static @Nullable EntitySelector compileSelector(String $$0) {
        try {
            EntitySelectorParser $$1 = new EntitySelectorParser(new StringReader($$0), true);
            return $$1.parse();
        }
        catch (CommandSyntaxException $$2) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack $$0) throws CommandSyntaxException {
        if (this.compiledSelector != null) {
            List<? extends Entity> $$1 = this.compiledSelector.findEntities($$0);
            return $$1.stream().map(NbtPredicate::getEntityTagToCompare);
        }
        return Stream.empty();
    }

    public MapCodec<EntityDataSource> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "entity=" + this.selectorPattern;
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
        if (!($$0 instanceof EntityDataSource)) return false;
        EntityDataSource $$1 = (EntityDataSource)$$0;
        if (!this.selectorPattern.equals($$1.selectorPattern)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.selectorPattern.hashCode();
    }
}

