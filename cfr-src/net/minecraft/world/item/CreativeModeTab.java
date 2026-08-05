/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class CreativeModeTab {
    static final Identifier DEFAULT_BACKGROUND = CreativeModeTab.createTextureLocation("items");
    private final Component displayName;
    Identifier backgroundTexture = DEFAULT_BACKGROUND;
    boolean canScroll = true;
    boolean showTitle = true;
    boolean alignedRight = false;
    private final Row row;
    private final int column;
    private final Type type;
    private @Nullable ItemStack iconItemStack;
    private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndComponentsSet();
    private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndComponentsSet();
    private final Supplier<ItemStack> iconGenerator;
    private final DisplayItemsGenerator displayItemsGenerator;

    CreativeModeTab(Row $$0, int $$1, Type $$2, Component $$3, Supplier<ItemStack> $$4, DisplayItemsGenerator $$5) {
        this.row = $$0;
        this.column = $$1;
        this.displayName = $$3;
        this.iconGenerator = $$4;
        this.displayItemsGenerator = $$5;
        this.type = $$2;
    }

    public static Identifier createTextureLocation(String $$0) {
        return Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + $$0 + ".png");
    }

    public static Builder builder(Row $$0, int $$1) {
        return new Builder($$0, $$1);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = this.iconGenerator.get();
        }
        return this.iconItemStack;
    }

    public Identifier getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public Row row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != Type.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public Type getType() {
        return this.type;
    }

    public void buildContents(ItemDisplayParameters $$0) {
        ItemDisplayBuilder $$1 = new ItemDisplayBuilder(this, $$0.enabledFeatures);
        ResourceKey<CreativeModeTab> $$2 = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + String.valueOf(this)));
        this.displayItemsGenerator.accept($$0, $$1);
        this.displayItems = $$1.tabContents;
        this.displayItemsSearchTab = $$1.searchTabContents;
    }

    public Collection<ItemStack> getDisplayItems() {
        return this.displayItems;
    }

    public Collection<ItemStack> getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack $$0) {
        return this.displayItemsSearchTab.contains($$0);
    }

    public static enum Row {
        TOP,
        BOTTOM;

    }

    @FunctionalInterface
    public static interface DisplayItemsGenerator {
        public void accept(ItemDisplayParameters var1, Output var2);
    }

    public static enum Type {
        CATEGORY,
        INVENTORY,
        HOTBAR,
        SEARCH;

    }

    public static class Builder {
        private static final DisplayItemsGenerator EMPTY_GENERATOR = ($$0, $$1) -> {};
        private final Row row;
        private final int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private Type type = Type.CATEGORY;
        private Identifier backgroundTexture = DEFAULT_BACKGROUND;

        public Builder(Row $$0, int $$1) {
            this.row = $$0;
            this.column = $$1;
        }

        public Builder title(Component $$0) {
            this.displayName = $$0;
            return this;
        }

        public Builder icon(Supplier<ItemStack> $$0) {
            this.iconGenerator = $$0;
            return this;
        }

        public Builder displayItems(DisplayItemsGenerator $$0) {
            this.displayItemsGenerator = $$0;
            return this;
        }

        public Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected Builder type(Type $$0) {
            this.type = $$0;
            return this;
        }

        public Builder backgroundTexture(Identifier $$0) {
            this.backgroundTexture = $$0;
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == Type.HOTBAR || this.type == Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            }
            CreativeModeTab $$0 = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
            $$0.alignedRight = this.alignedRight;
            $$0.showTitle = this.showTitle;
            $$0.canScroll = this.canScroll;
            $$0.backgroundTexture = this.backgroundTexture;
            return $$0;
        }
    }

    static class ItemDisplayBuilder
    implements Output {
        public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab $$0, FeatureFlagSet $$1) {
            this.tab = $$0;
            this.featureFlagSet = $$1;
        }

        @Override
        public void accept(ItemStack $$0, TabVisibility $$1) {
            boolean $$2;
            if ($$0.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            }
            boolean bl = $$2 = this.tabContents.contains($$0) && $$1 != TabVisibility.SEARCH_TAB_ONLY;
            if ($$2) {
                throw new IllegalStateException("Accidentally adding the same item stack twice " + $$0.getDisplayName().getString() + " to a Creative Mode Tab: " + this.tab.getDisplayName().getString());
            }
            if ($$0.getItem().isEnabled(this.featureFlagSet)) {
                switch ($$1.ordinal()) {
                    case 0: {
                        this.tabContents.add($$0);
                        this.searchTabContents.add($$0);
                        break;
                    }
                    case 1: {
                        this.tabContents.add($$0);
                        break;
                    }
                    case 2: {
                        this.searchTabContents.add($$0);
                    }
                }
            }
        }
    }

    public static final class ItemDisplayParameters
    extends Record {
        final FeatureFlagSet enabledFeatures;
        private final boolean hasPermissions;
        private final HolderLookup.Provider holders;

        public ItemDisplayParameters(FeatureFlagSet $$0, boolean $$1, HolderLookup.Provider $$2) {
            this.enabledFeatures = $$0;
            this.hasPermissions = $$1;
            this.holders = $$2;
        }

        public boolean needsUpdate(FeatureFlagSet $$0, boolean $$1, HolderLookup.Provider $$2) {
            return !this.enabledFeatures.equals($$0) || this.hasPermissions != $$1 || this.holders != $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ItemDisplayParameters.class, "enabledFeatures;hasPermissions;holders", "enabledFeatures", "hasPermissions", "holders"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ItemDisplayParameters.class, "enabledFeatures;hasPermissions;holders", "enabledFeatures", "hasPermissions", "holders"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ItemDisplayParameters.class, "enabledFeatures;hasPermissions;holders", "enabledFeatures", "hasPermissions", "holders"}, this, $$0);
        }

        public FeatureFlagSet enabledFeatures() {
            return this.enabledFeatures;
        }

        public boolean hasPermissions() {
            return this.hasPermissions;
        }

        public HolderLookup.Provider holders() {
            return this.holders;
        }
    }

    public static interface Output {
        public void accept(ItemStack var1, TabVisibility var2);

        default public void accept(ItemStack $$0) {
            this.accept($$0, TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default public void accept(ItemLike $$0, TabVisibility $$1) {
            this.accept(new ItemStack($$0), $$1);
        }

        default public void accept(ItemLike $$0) {
            this.accept(new ItemStack($$0), TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default public void acceptAll(Collection<ItemStack> $$0, TabVisibility $$12) {
            $$0.forEach($$1 -> this.accept((ItemStack)$$1, $$12));
        }

        default public void acceptAll(Collection<ItemStack> $$0) {
            this.acceptAll($$0, TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    protected static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;

    }
}

