/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.advancements;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class AdvancementTree {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Identifier, AdvancementNode> nodes = new Object2ObjectOpenHashMap();
    private final Set<AdvancementNode> roots = new ObjectLinkedOpenHashSet();
    private final Set<AdvancementNode> tasks = new ObjectLinkedOpenHashSet();
    private @Nullable Listener listener;

    private void remove(AdvancementNode $$0) {
        for (AdvancementNode $$1 : $$0.children()) {
            this.remove($$1);
        }
        LOGGER.info("Forgot about advancement {}", (Object)$$0.holder());
        this.nodes.remove($$0.holder().id());
        if ($$0.parent() == null) {
            this.roots.remove($$0);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementRoot($$0);
            }
        } else {
            this.tasks.remove($$0);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementTask($$0);
            }
        }
    }

    public void remove(Set<Identifier> $$0) {
        for (Identifier $$1 : $$0) {
            AdvancementNode $$2 = this.nodes.get($$1);
            if ($$2 == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)$$1);
                continue;
            }
            this.remove($$2);
        }
    }

    public void addAll(Collection<AdvancementHolder> $$0) {
        ArrayList<AdvancementHolder> $$1 = new ArrayList<AdvancementHolder>($$0);
        while (!$$1.isEmpty()) {
            if ($$1.removeIf(this::tryInsert)) continue;
            LOGGER.error("Couldn't load advancements: {}", $$1);
            break;
        }
        LOGGER.info("Loaded {} advancements", (Object)this.nodes.size());
    }

    private boolean tryInsert(AdvancementHolder $$0) {
        Optional<Identifier> $$1 = $$0.value().parent();
        AdvancementNode $$2 = $$1.map(this.nodes::get).orElse(null);
        if ($$2 == null && $$1.isPresent()) {
            return false;
        }
        AdvancementNode $$3 = new AdvancementNode($$0, $$2);
        if ($$2 != null) {
            $$2.addChild($$3);
        }
        this.nodes.put($$0.id(), $$3);
        if ($$2 == null) {
            this.roots.add($$3);
            if (this.listener != null) {
                this.listener.onAddAdvancementRoot($$3);
            }
        } else {
            this.tasks.add($$3);
            if (this.listener != null) {
                this.listener.onAddAdvancementTask($$3);
            }
        }
        return true;
    }

    public void clear() {
        this.nodes.clear();
        this.roots.clear();
        this.tasks.clear();
        if (this.listener != null) {
            this.listener.onAdvancementsCleared();
        }
    }

    public Iterable<AdvancementNode> roots() {
        return this.roots;
    }

    public Collection<AdvancementNode> nodes() {
        return this.nodes.values();
    }

    public @Nullable AdvancementNode get(Identifier $$0) {
        return this.nodes.get($$0);
    }

    public @Nullable AdvancementNode get(AdvancementHolder $$0) {
        return this.nodes.get($$0.id());
    }

    public void setListener(@Nullable Listener $$0) {
        this.listener = $$0;
        if ($$0 != null) {
            for (AdvancementNode $$1 : this.roots) {
                $$0.onAddAdvancementRoot($$1);
            }
            for (AdvancementNode $$2 : this.tasks) {
                $$0.onAddAdvancementTask($$2);
            }
        }
    }

    public static interface Listener {
        public void onAddAdvancementRoot(AdvancementNode var1);

        public void onRemoveAdvancementRoot(AdvancementNode var1);

        public void onAddAdvancementTask(AdvancementNode var1);

        public void onRemoveAdvancementTask(AdvancementNode var1);

        public void onAdvancementsCleared();
    }
}

