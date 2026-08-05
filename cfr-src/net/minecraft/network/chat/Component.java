/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.util.Either;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public ComponentContents getContents();

    @Override
    default public String getString() {
        return FormattedText.super.getString();
    }

    default public String getString(int $$0) {
        StringBuilder $$1 = new StringBuilder();
        this.visit($$2 -> {
            int $$3 = $$0 - $$1.length();
            if ($$3 <= 0) {
                return STOP_ITERATION;
            }
            $$1.append($$2.length() <= $$3 ? $$2 : $$2.substring(0, $$3));
            return Optional.empty();
        });
        return $$1.toString();
    }

    public List<Component> getSiblings();

    default public @Nullable String tryCollapseToString() {
        ComponentContents componentContents = this.getContents();
        if (componentContents instanceof PlainTextContents) {
            PlainTextContents $$0 = (PlainTextContents)componentContents;
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return $$0.text();
            }
        }
        return null;
    }

    default public MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default public MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<Component>(this.getSiblings()), this.getStyle());
    }

    public FormattedCharSequence getVisualOrderText();

    @Override
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> $$0, Style $$1) {
        Style $$2 = this.getStyle().applyTo($$1);
        Optional<T> $$3 = this.getContents().visit($$0, $$2);
        if ($$3.isPresent()) {
            return $$3;
        }
        for (Component $$4 : this.getSiblings()) {
            Optional<T> $$5 = $$4.visit($$0, $$2);
            if (!$$5.isPresent()) continue;
            return $$5;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> $$0) {
        Optional<T> $$1 = this.getContents().visit($$0);
        if ($$1.isPresent()) {
            return $$1;
        }
        for (Component $$2 : this.getSiblings()) {
            Optional<T> $$3 = $$2.visit($$0);
            if (!$$3.isPresent()) continue;
            return $$3;
        }
        return Optional.empty();
    }

    default public List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default public List<Component> toFlatList(Style $$0) {
        ArrayList $$12 = Lists.newArrayList();
        this.visit(($$1, $$2) -> {
            if (!$$2.isEmpty()) {
                $$12.add(Component.literal($$2).withStyle($$1));
            }
            return Optional.empty();
        }, $$0);
        return $$12;
    }

    default public boolean contains(Component $$0) {
        List<Component> $$2;
        if (this.equals($$0)) {
            return true;
        }
        List<Component> $$1 = this.toFlatList();
        return Collections.indexOfSubList($$1, $$2 = $$0.toFlatList(this.getStyle())) != -1;
    }

    public static Component nullToEmpty(@Nullable String $$0) {
        return $$0 != null ? Component.literal($$0) : CommonComponents.EMPTY;
    }

    public static MutableComponent literal(String $$0) {
        return MutableComponent.create(PlainTextContents.create($$0));
    }

    public static MutableComponent translatable(String $$0) {
        return MutableComponent.create(new TranslatableContents($$0, null, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatable(String $$0, Object ... $$1) {
        return MutableComponent.create(new TranslatableContents($$0, null, $$1));
    }

    public static MutableComponent translatableEscape(String $$0, Object ... $$1) {
        for (int $$2 = 0; $$2 < $$1.length; ++$$2) {
            Object $$3 = $$1[$$2];
            if (TranslatableContents.isAllowedPrimitiveArgument($$3) || $$3 instanceof Component) continue;
            $$1[$$2] = String.valueOf($$3);
        }
        return Component.translatable($$0, $$1);
    }

    public static MutableComponent translatableWithFallback(String $$0, @Nullable String $$1) {
        return MutableComponent.create(new TranslatableContents($$0, $$1, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatableWithFallback(String $$0, @Nullable String $$1, Object ... $$2) {
        return MutableComponent.create(new TranslatableContents($$0, $$1, $$2));
    }

    public static MutableComponent empty() {
        return MutableComponent.create(PlainTextContents.EMPTY);
    }

    public static MutableComponent keybind(String $$0) {
        return MutableComponent.create(new KeybindContents($$0));
    }

    public static MutableComponent nbt(String $$0, boolean $$1, Optional<Component> $$2, DataSource $$3) {
        return MutableComponent.create(new NbtContents($$0, $$1, $$2, $$3));
    }

    public static MutableComponent score(SelectorPattern $$0, String $$1) {
        return MutableComponent.create(new ScoreContents((Either<SelectorPattern, String>)Either.left((Object)$$0), $$1));
    }

    public static MutableComponent score(String $$0, String $$1) {
        return MutableComponent.create(new ScoreContents((Either<SelectorPattern, String>)Either.right((Object)$$0), $$1));
    }

    public static MutableComponent selector(SelectorPattern $$0, Optional<Component> $$1) {
        return MutableComponent.create(new SelectorContents($$0, $$1));
    }

    public static MutableComponent object(ObjectInfo $$0) {
        return MutableComponent.create(new ObjectContents($$0));
    }

    public static Component translationArg(Date $$0) {
        return Component.literal($$0.toString());
    }

    public static Component translationArg(Message $$0) {
        Component component;
        if ($$0 instanceof Component) {
            Component $$1 = (Component)$$0;
            component = $$1;
        } else {
            component = Component.literal($$0.getString());
        }
        return component;
    }

    public static Component translationArg(UUID $$0) {
        return Component.literal($$0.toString());
    }

    public static Component translationArg(Identifier $$0) {
        return Component.literal($$0.toString());
    }

    public static Component translationArg(ChunkPos $$0) {
        return Component.literal($$0.toString());
    }

    public static Component translationArg(URI $$0) {
        return Component.literal($$0.toString());
    }
}

