/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

public final class MutableComponent
implements Component {
    private final ComponentContents contents;
    private final List<Component> siblings;
    private Style style;
    private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
    private @Nullable Language decomposedWith;

    MutableComponent(ComponentContents $$0, List<Component> $$1, Style $$2) {
        this.contents = $$0;
        this.siblings = $$1;
        this.style = $$2;
    }

    public static MutableComponent create(ComponentContents $$0) {
        return new MutableComponent($$0, Lists.newArrayList(), Style.EMPTY);
    }

    @Override
    public ComponentContents getContents() {
        return this.contents;
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    public MutableComponent setStyle(Style $$0) {
        this.style = $$0;
        return this;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    public MutableComponent append(String $$0) {
        if ($$0.isEmpty()) {
            return this;
        }
        return this.append(Component.literal($$0));
    }

    public MutableComponent append(Component $$0) {
        this.siblings.add($$0);
        return this;
    }

    public MutableComponent withStyle(UnaryOperator<Style> $$0) {
        this.setStyle((Style)$$0.apply(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(Style $$0) {
        this.setStyle($$0.applyTo(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting ... $$0) {
        this.setStyle(this.getStyle().applyFormats($$0));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting $$0) {
        this.setStyle(this.getStyle().applyFormat($$0));
        return this;
    }

    public MutableComponent withColor(int $$0) {
        this.setStyle(this.getStyle().withColor($$0));
        return this;
    }

    public MutableComponent withoutShadow() {
        this.setStyle(this.getStyle().withoutShadow());
        return this;
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        Language $$0 = Language.getInstance();
        if (this.decomposedWith != $$0) {
            this.visualOrderText = $$0.getVisualOrder(this);
            this.decomposedWith = $$0;
        }
        return this.visualOrderText;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof MutableComponent)) return false;
        MutableComponent $$1 = (MutableComponent)$$0;
        if (!this.contents.equals($$1.contents)) return false;
        if (!this.style.equals($$1.style)) return false;
        if (!this.siblings.equals($$1.siblings)) return false;
        return true;
    }

    public int hashCode() {
        int $$0 = 1;
        $$0 = 31 * $$0 + this.contents.hashCode();
        $$0 = 31 * $$0 + this.style.hashCode();
        $$0 = 31 * $$0 + this.siblings.hashCode();
        return $$0;
    }

    public String toString() {
        boolean $$2;
        StringBuilder $$0 = new StringBuilder(this.contents.toString());
        boolean $$1 = !this.style.isEmpty();
        boolean bl = $$2 = !this.siblings.isEmpty();
        if ($$1 || $$2) {
            $$0.append('[');
            if ($$1) {
                $$0.append("style=");
                $$0.append(this.style);
            }
            if ($$1 && $$2) {
                $$0.append(", ");
            }
            if ($$2) {
                $$0.append("siblings=");
                $$0.append(this.siblings);
            }
            $$0.append(']');
        }
        return $$0.toString();
    }
}

