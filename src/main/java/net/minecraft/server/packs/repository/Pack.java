package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
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
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackLocationInfo location;
   private final Pack.ResourcesSupplier resources;
   private final Pack.Metadata metadata;
   private final PackSelectionConfig selectionConfig;

   
   public static Pack readMetaAndCreate(PackLocationInfo $$0, Pack.ResourcesSupplier $$1, PackType $$2, PackSelectionConfig $$3) {
      PackFormat $$4 = SharedConstants.getCurrentVersion().packVersion($$2);
      Pack.Metadata $$5 = readPackMetadata($$0, $$1, $$4, $$2);
      return $$5 != null ? new Pack($$0, $$1, $$5, $$3) : null;
   }

   public Pack(PackLocationInfo $$0, Pack.ResourcesSupplier $$1, Pack.Metadata $$2, PackSelectionConfig $$3) {
      this.location = $$0;
      this.resources = $$1;
      this.metadata = $$2;
      this.selectionConfig = $$3;
   }

   
   public static Pack.Metadata readPackMetadata(PackLocationInfo $$0, Pack.ResourcesSupplier $$1, PackFormat $$2, PackType $$3) {
      try {
         Pack.Metadata var11;
         try (PackResources $$4 = $$1.openPrimary($$0)) {
            PackMetadataSection $$5 = $$4.getMetadataSection(PackMetadataSection.forPackType($$3));
            if ($$5 == null) {
               $$5 = $$4.getMetadataSection(PackMetadataSection.FALLBACK_TYPE);
            }

            if ($$5 == null) {
               LOGGER.warn("Missing metadata in pack {}", $$0.id());
               return null;
            }

            FeatureFlagsMetadataSection $$6 = $$4.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
            FeatureFlagSet $$7 = $$6 != null ? $$6.flags() : FeatureFlagSet.of();
            PackCompatibility $$8 = PackCompatibility.forVersion($$5.supportedFormats(), $$2);
            OverlayMetadataSection $$9 = $$4.getMetadataSection(OverlayMetadataSection.forPackType($$3));
            List<String> $$10 = $$9 != null ? $$9.overlaysForVersion($$2) : List.of();
            var11 = new Pack.Metadata($$5.description(), $$8, $$7, $$10);
         }

         return var11;
      } catch (Exception var14) {
         LOGGER.warn("Failed to read pack {} metadata", $$0.id(), var14);
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

   public Pack.Position getDefaultPosition() {
      return this.selectionConfig.defaultPosition();
   }

   public PackSource getPackSource() {
      return this.location.source();
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof Pack $$1) ? false : this.location.equals($$1.location);
      }
   }

   @Override
   public int hashCode() {
      return this.location.hashCode();
   }

   public record Metadata(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {
   }

   public static enum Position {
      TOP,
      BOTTOM;

      public <T> int insert(List<T> $$0, T $$1, Function<T, PackSelectionConfig> $$2, boolean $$3) {
         Pack.Position $$4 = $$3 ? this.opposite() : this;
         if ($$4 == BOTTOM) {
            int $$5;
            for ($$5 = 0; $$5 < $$0.size(); $$5++) {
               PackSelectionConfig $$6 = $$2.apply($$0.get($$5));
               if (!$$6.fixedPosition() || $$6.defaultPosition() != this) {
                  break;
               }
            }

            $$0.add($$5, $$1);
            return $$5;
         } else {
            int $$7;
            for ($$7 = $$0.size() - 1; $$7 >= 0; $$7--) {
               PackSelectionConfig $$8 = $$2.apply($$0.get($$7));
               if (!$$8.fixedPosition() || $$8.defaultPosition() != this) {
                  break;
               }
            }

            $$0.add($$7 + 1, $$1);
            return $$7 + 1;
         }
      }

      public Pack.Position opposite() {
         return this == TOP ? BOTTOM : TOP;
      }
   }

   public interface ResourcesSupplier {
      PackResources openPrimary(PackLocationInfo var1);

      PackResources openFull(PackLocationInfo var1, Pack.Metadata var2);
   }
}
