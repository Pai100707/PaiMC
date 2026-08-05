/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import org.jspecify.annotations.Nullable;

public class AdvancementNode {
    private final AdvancementHolder holder;
    private final @Nullable AdvancementNode parent;
    private final Set<AdvancementNode> children = new ReferenceOpenHashSet();

    @VisibleForTesting
    public AdvancementNode(AdvancementHolder $$0, @Nullable AdvancementNode $$1) {
        this.holder = $$0;
        this.parent = $$1;
    }

    public Advancement advancement() {
        return this.holder.value();
    }

    public AdvancementHolder holder() {
        return this.holder;
    }

    public @Nullable AdvancementNode parent() {
        return this.parent;
    }

    public AdvancementNode root() {
        return AdvancementNode.getRoot(this);
    }

    public static AdvancementNode getRoot(AdvancementNode $$0) {
        AdvancementNode $$1 = $$0;
        AdvancementNode $$2;
        while (($$2 = $$1.parent()) != null) {
            $$1 = $$2;
        }
        return $$1;
    }

    public Iterable<AdvancementNode> children() {
        return this.children;
    }

    @VisibleForTesting
    public void addChild(AdvancementNode $$0) {
        this.children.add($$0);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof AdvancementNode)) return false;
        AdvancementNode $$1 = (AdvancementNode)$$0;
        if (!this.holder.equals($$1.holder)) return false;
        return true;
    }

    public int hashCode() {
        return this.holder.hashCode();
    }

    public String toString() {
        return this.holder.id().toString();
    }
}

