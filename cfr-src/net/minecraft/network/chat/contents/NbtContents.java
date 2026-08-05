/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.network.chat.contents.data.DataSources;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NbtContents
implements ComponentContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<NbtContents> MAP_CODEC = RecordCodecBuilder.mapCodec($$0 -> $$0.group((App)Codec.STRING.fieldOf("nbt").forGetter(NbtContents::getNbtPath), (App)Codec.BOOL.lenientOptionalFieldOf("interpret", (Object)false).forGetter(NbtContents::isInterpreting), (App)ComponentSerialization.CODEC.lenientOptionalFieldOf("separator").forGetter(NbtContents::getSeparator), (App)DataSources.CODEC.forGetter(NbtContents::getDataSource)).apply((Applicative)$$0, NbtContents::new));
    private final boolean interpreting;
    private final Optional<Component> separator;
    private final String nbtPathPattern;
    private final DataSource dataSource;
    protected final @Nullable NbtPathArgument.NbtPath compiledNbtPath;

    public NbtContents(String $$0, boolean $$1, Optional<Component> $$2, DataSource $$3) {
        this($$0, NbtContents.compileNbtPath($$0), $$1, $$2, $$3);
    }

    private NbtContents(String $$0, @Nullable NbtPathArgument.NbtPath $$1, boolean $$2, Optional<Component> $$3, DataSource $$4) {
        this.nbtPathPattern = $$0;
        this.compiledNbtPath = $$1;
        this.interpreting = $$2;
        this.separator = $$3;
        this.dataSource = $$4;
    }

    private static @Nullable NbtPathArgument.NbtPath compileNbtPath(String $$0) {
        try {
            return new NbtPathArgument().parse(new StringReader($$0));
        }
        catch (CommandSyntaxException $$1) {
            return null;
        }
    }

    public String getNbtPath() {
        return this.nbtPathPattern;
    }

    public boolean isInterpreting() {
        return this.interpreting;
    }

    public Optional<Component> getSeparator() {
        return this.separator;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof NbtContents)) return false;
        NbtContents $$1 = (NbtContents)$$0;
        if (!this.dataSource.equals($$1.dataSource)) return false;
        if (!this.separator.equals($$1.separator)) return false;
        if (this.interpreting != $$1.interpreting) return false;
        if (!this.nbtPathPattern.equals($$1.nbtPathPattern)) return false;
        return true;
    }

    public int hashCode() {
        int $$0 = this.interpreting ? 1 : 0;
        $$0 = 31 * $$0 + this.separator.hashCode();
        $$0 = 31 * $$0 + this.nbtPathPattern.hashCode();
        $$0 = 31 * $$0 + this.dataSource.hashCode();
        return $$0;
    }

    public String toString() {
        return "nbt{" + String.valueOf(this.dataSource) + ", interpreting=" + this.interpreting + ", separator=" + String.valueOf(this.separator) + "}";
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack $$02, @Nullable Entity $$13, int $$22) throws CommandSyntaxException {
        if ($$02 == null || this.compiledNbtPath == null) {
            return Component.empty();
        }
        Stream<String> $$3 = this.dataSource.getData($$02).flatMap($$0 -> {
            try {
                return this.compiledNbtPath.get((Tag)$$0).stream();
            }
            catch (CommandSyntaxException $$1) {
                return Stream.empty();
            }
        });
        if (this.interpreting) {
            RegistryOps<Tag> $$42 = $$02.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            Component $$5 = (Component)DataFixUtils.orElse(ComponentUtils.updateForEntity($$02, this.separator, $$13, $$22), (Object)ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR);
            return $$3.flatMap($$4 -> {
                try {
                    Component $$5 = (Component)ComponentSerialization.CODEC.parse((DynamicOps)$$42, $$4).getOrThrow();
                    return Stream.of(ComponentUtils.updateForEntity($$02, $$5, $$13, $$22));
                }
                catch (Exception $$6) {
                    LOGGER.warn("Failed to parse component: {}", $$4, (Object)$$6);
                    return Stream.of(new MutableComponent[0]);
                }
            }).reduce(($$1, $$2) -> $$1.append($$5).append((Component)$$2)).orElseGet(Component::empty);
        }
        Stream<String> $$6 = $$3.map(NbtContents::asString);
        return ComponentUtils.updateForEntity($$02, this.separator, $$13, $$22).map($$12 -> $$6.map(Component::literal).reduce(($$1, $$2) -> $$1.append((Component)$$12).append((Component)$$2)).orElseGet(Component::empty)).orElseGet(() -> Component.literal($$6.collect(Collectors.joining(", "))));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static String asString(Tag $$0) {
        if (!($$0 instanceof StringTag)) return $$0.toString();
        StringTag stringTag = (StringTag)$$0;
        try {
            String string = stringTag.value();
            return string;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public MapCodec<NbtContents> codec() {
        return MAP_CODEC;
    }
}

