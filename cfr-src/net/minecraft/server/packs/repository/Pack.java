/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final ResourcesSupplier resources;
    private final Metadata metadata;
    private final PackSelectionConfig selectionConfig;

    public static @Nullable Pack readMetaAndCreate(PackLocationInfo $$0, ResourcesSupplier $$1, PackType $$2, PackSelectionConfig $$3) {
        PackFormat $$4 = SharedConstants.getCurrentVersion().packVersion($$2);
        Metadata $$5 = Pack.readPackMetadata($$0, $$1, $$4, $$2);
        return $$5 != null ? new Pack($$0, $$1, $$5, $$3) : null;
    }

    public Pack(PackLocationInfo $$0, ResourcesSupplier $$1, Metadata $$2, PackSelectionConfig $$3) {
        this.location = $$0;
        this.resources = $$1;
        this.metadata = $$2;
        this.selectionConfig = $$3;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static @Nullable Metadata readPackMetadata(PackLocationInfo $$0, ResourcesSupplier $$1, PackFormat $$2, PackType $$3) {
        try (PackResources $$4 = $$1.openPrimary($$0);){
            PackMetadataSection $$5 = $$4.getMetadataSection(PackMetadataSection.forPackType($$3));
            if ($$5 == null) {
                $$5 = $$4.getMetadataSection(PackMetadataSection.FALLBACK_TYPE);
            }
            if ($$5 == null) {
                LOGGER.warn("Missing metadata in pack {}", (Object)$$0.id());
                Metadata metadata = null;
                return metadata;
            }
            FeatureFlagsMetadataSection $$6 = $$4.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
            FeatureFlagSet $$7 = $$6 != null ? $$6.flags() : FeatureFlagSet.of();
            PackCompatibility $$8 = PackCompatibility.forVersion($$5.supportedFormats(), $$2);
            OverlayMetadataSection $$9 = $$4.getMetadataSection(OverlayMetadataSection.forPackType($$3));
            List<String> $$10 = $$9 != null ? $$9.overlaysForVersion($$2) : List.of();
            Metadata metadata = new Metadata($$5.description(), $$8, $$7, $$10);
            return metadata;
        }
        catch (Exception $$11) {
            LOGGER.warn("Failed to read pack {} metadata", (Object)$$0.id(), (Object)$$11);
            return null;
        }
    }

    public PackLocationInfo location() {
        return this.location;
    }

    public Component getTitle() {
        return this.location.title();
    }

    public Component getDescription() {
        return this.metadata.description();
    }

    public Component getChatLink(boolean $$0) {
        return this.location.createChatLink($$0, this.metadata.description);
    }

    public PackCompatibility getCompatibility() {
        return this.metadata.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.metadata.requestedFeatures();
    }

    public PackResources open() {
        return this.resources.openFull(this.location, this.metadata);
    }

    public String getId() {
        return this.location.id();
    }

    public PackSelectionConfig selectionConfig() {
        return this.selectionConfig;
    }

    public boolean isRequired() {
        return this.selectionConfig.required();
    }

    public boolean isFixedPosition() {
        return this.selectionConfig.fixedPosition();
    }

    public Position getDefaultPosition() {
        return this.selectionConfig.defaultPosition();
    }

    public PackSource getPackSource() {
        return this.location.source();
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof Pack)) {
            return false;
        }
        Pack $$1 = (Pack)$$0;
        return this.location.equals($$1.location);
    }

    public int hashCode() {
        return this.location.hashCode();
    }

    public static interface ResourcesSupplier {
        public PackResources openPrimary(PackLocationInfo var1);

        public PackResources openFull(PackLocationInfo var1, Metadata var2);
    }

    public static final class Metadata
    extends Record {
        final Component description;
        private final PackCompatibility compatibility;
        private final FeatureFlagSet requestedFeatures;
        private final List<String> overlays;

        public Metadata(Component $$0, PackCompatibility $$1, FeatureFlagSet $$2, List<String> $$3) {
            this.description = $$0;
            this.compatibility = $$1;
            this.requestedFeatures = $$2;
            this.overlays = $$3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this, $$0);
        }

        public Component description() {
            return this.description;
        }

        public PackCompatibility compatibility() {
            return this.compatibility;
        }

        public FeatureFlagSet requestedFeatures() {
            return this.requestedFeatures;
        }

        public List<String> overlays() {
            return this.overlays;
        }
    }

    public static enum Position {
        TOP,
        BOTTOM;


        public <T> int insert(List<T> $$0, T $$1, Function<T, PackSelectionConfig> $$2, boolean $$3) {
            PackSelectionConfig $$8;
            int $$7;
            Position $$4;
            Position position = $$4 = $$3 ? this.opposite() : this;
            if ($$4 == BOTTOM) {
                PackSelectionConfig $$6;
                int $$5;
                for ($$5 = 0; $$5 < $$0.size() && ($$6 = $$2.apply($$0.get($$5))).fixedPosition() && $$6.defaultPosition() == this; ++$$5) {
                }
                $$0.add($$5, $$1);
                return $$5;
            }
            for ($$7 = $$0.size() - 1; $$7 >= 0 && ($$8 = $$2.apply($$0.get($$7))).fixedPosition() && $$8.defaultPosition() == this; --$$7) {
            }
            $$0.add($$7 + 1, $$1);
            return $$7 + 1;
        }

        public Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}

