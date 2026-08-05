/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class TranslatableContents
implements ComponentContents {
    public static final Object[] NO_ARGS = new Object[0];
    private static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.JAVA.validate(TranslatableContents::filterAllowedArguments);
    private static final Codec<Object> ARG_CODEC = Codec.either(PRIMITIVE_ARG_CODEC, ComponentSerialization.CODEC).xmap($$02 -> $$02.map($$0 -> $$0, $$0 -> Objects.requireNonNullElse($$0.tryCollapseToString(), $$0)), $$0 -> {
        Either either;
        if ($$0 instanceof Component) {
            Component $$1 = (Component)$$0;
            either = Either.right((Object)$$1);
        } else {
            either = Either.left((Object)$$0);
        }
        return either;
    });
    public static final MapCodec<TranslatableContents> MAP_CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)Codec.STRING.fieldOf("translate").forGetter($$0 -> $$0.key), (App)Codec.STRING.lenientOptionalFieldOf("fallback").forGetter($$0 -> Optional.ofNullable($$0.fallback)), (App)ARG_CODEC.listOf().optionalFieldOf("with").forGetter($$0 -> TranslatableContents.adjustArgs($$0.args))).apply((Applicative)$$02, TranslatableContents::create));
    private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
    private static final FormattedText TEXT_NULL = FormattedText.of("null");
    private final String key;
    private final @Nullable String fallback;
    private final Object[] args;
    private @Nullable Language decomposedWith;
    private List<FormattedText> decomposedParts = ImmutableList.of();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private static DataResult<Object> filterAllowedArguments(@Nullable Object $$0) {
        if (!TranslatableContents.isAllowedPrimitiveArgument($$0)) {
            return DataResult.error(() -> "This value needs to be parsed as component");
        }
        return DataResult.success((Object)$$0);
    }

    public static boolean isAllowedPrimitiveArgument(@Nullable Object $$0) {
        return $$0 instanceof Number || $$0 instanceof Boolean || $$0 instanceof String;
    }

    private static Optional<List<Object>> adjustArgs(Object[] $$0) {
        return $$0.length == 0 ? Optional.empty() : Optional.of(Arrays.asList($$0));
    }

    private static Object[] adjustArgs(Optional<List<Object>> $$02) {
        return $$02.map($$0 -> $$0.isEmpty() ? NO_ARGS : $$0.toArray()).orElse(NO_ARGS);
    }

    private static TranslatableContents create(String $$0, Optional<String> $$1, Optional<List<Object>> $$2) {
        return new TranslatableContents($$0, $$1.orElse(null), TranslatableContents.adjustArgs($$2));
    }

    public TranslatableContents(String $$0, @Nullable String $$1, Object[] $$2) {
        this.key = $$0;
        this.fallback = $$1;
        this.args = $$2;
    }

    public MapCodec<TranslatableContents> codec() {
        return MAP_CODEC;
    }

    private void decompose() {
        Language $$0 = Language.getInstance();
        if ($$0 == this.decomposedWith) {
            return;
        }
        this.decomposedWith = $$0;
        String $$1 = this.fallback != null ? $$0.getOrDefault(this.key, this.fallback) : $$0.getOrDefault(this.key);
        try {
            ImmutableList.Builder $$2 = ImmutableList.builder();
            this.decomposeTemplate($$1, arg_0 -> ((ImmutableList.Builder)$$2).add(arg_0));
            this.decomposedParts = $$2.build();
        }
        catch (TranslatableFormatException $$3) {
            this.decomposedParts = ImmutableList.of((Object)FormattedText.of($$1));
        }
    }

    private void decomposeTemplate(String $$0, Consumer<FormattedText> $$1) {
        Matcher $$2 = FORMAT_PATTERN.matcher($$0);
        try {
            int $$3 = 0;
            int $$4 = 0;
            while ($$2.find($$4)) {
                int $$5 = $$2.start();
                int $$6 = $$2.end();
                if ($$5 > $$4) {
                    String $$7 = $$0.substring($$4, $$5);
                    if ($$7.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    $$1.accept(FormattedText.of($$7));
                }
                String $$8 = $$2.group(2);
                String $$9 = $$0.substring($$5, $$6);
                if ("%".equals($$8) && "%%".equals($$9)) {
                    $$1.accept(TEXT_PERCENT);
                } else if ("s".equals($$8)) {
                    String $$10 = $$2.group(1);
                    int $$11 = $$10 != null ? Integer.parseInt($$10) - 1 : $$3++;
                    $$1.accept(this.getArgument($$11));
                } else {
                    throw new TranslatableFormatException(this, "Unsupported format: '" + $$9 + "'");
                }
                $$4 = $$6;
            }
            if ($$4 < $$0.length()) {
                String $$12 = $$0.substring($$4);
                if ($$12.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }
                $$1.accept(FormattedText.of($$12));
            }
        }
        catch (IllegalArgumentException $$13) {
            throw new TranslatableFormatException(this, (Throwable)$$13);
        }
    }

    private FormattedText getArgument(int $$0) {
        if ($$0 < 0 || $$0 >= this.args.length) {
            throw new TranslatableFormatException(this, $$0);
        }
        Object $$1 = this.args[$$0];
        if ($$1 instanceof Component) {
            Component $$2 = (Component)$$1;
            return $$2;
        }
        return $$1 == null ? TEXT_NULL : FormattedText.of($$1.toString());
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
        this.decompose();
        for (FormattedText $$2 : this.decomposedParts) {
            Optional<T> $$3 = $$2.visit($$0, $$1);
            if (!$$3.isPresent()) continue;
            return $$3;
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
        this.decompose();
        for (FormattedText $$1 : this.decomposedParts) {
            Optional<T> $$2 = $$1.visit($$0);
            if (!$$2.isPresent()) continue;
            return $$2;
        }
        return Optional.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack $$0, @Nullable Entity $$1, int $$2) throws CommandSyntaxException {
        Object[] $$3 = new Object[this.args.length];
        for (int $$4 = 0; $$4 < $$3.length; ++$$4) {
            Object $$5 = this.args[$$4];
            if ($$5 instanceof Component) {
                Component $$6 = (Component)$$5;
                $$3[$$4] = ComponentUtils.updateForEntity($$0, $$6, $$1, $$2);
                continue;
            }
            $$3[$$4] = $$5;
        }
        return MutableComponent.create(new TranslatableContents(this.key, this.fallback, $$3));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof TranslatableContents)) return false;
        TranslatableContents $$1 = (TranslatableContents)$$0;
        if (!Objects.equals(this.key, $$1.key)) return false;
        if (!Objects.equals(this.fallback, $$1.fallback)) return false;
        if (!Arrays.equals(this.args, $$1.args)) return false;
        return true;
    }

    public int hashCode() {
        int $$0 = Objects.hashCode(this.key);
        $$0 = 31 * $$0 + Objects.hashCode(this.fallback);
        $$0 = 31 * $$0 + Arrays.hashCode(this.args);
        return $$0;
    }

    public String toString() {
        return "translation{key='" + this.key + "'" + (String)(this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
    }

    public String getKey() {
        return this.key;
    }

    public @Nullable String getFallback() {
        return this.fallback;
    }

    public Object[] getArgs() {
        return this.args;
    }
}

