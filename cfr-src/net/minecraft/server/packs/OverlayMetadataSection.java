/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayEntry> overlays) {
    private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
    public static final MetadataSectionType<OverlayMetadataSection> CLIENT_TYPE = new MetadataSectionType<OverlayMetadataSection>("overlays", OverlayMetadataSection.codecForPackType(PackType.CLIENT_RESOURCES));
    public static final MetadataSectionType<OverlayMetadataSection> SERVER_TYPE = new MetadataSectionType<OverlayMetadataSection>("overlays", OverlayMetadataSection.codecForPackType(PackType.SERVER_DATA));

    private static DataResult<String> validateOverlayDir(String $$0) {
        if (!DIR_VALIDATOR.matcher($$0).matches()) {
            return DataResult.error(() -> $$0 + " is not accepted directory name");
        }
        return DataResult.success((Object)$$0);
    }

    @VisibleForTesting
    public static Codec<OverlayMetadataSection> codecForPackType(PackType $$0) {
        return RecordCodecBuilder.create($$1 -> $$1.group((App)OverlayEntry.listCodecForPackType($$0).fieldOf("entries").forGetter(OverlayMetadataSection::overlays)).apply((Applicative)$$1, OverlayMetadataSection::new));
    }

    public static MetadataSectionType<OverlayMetadataSection> forPackType(PackType $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case PackType.CLIENT_RESOURCES -> CLIENT_TYPE;
            case PackType.SERVER_DATA -> SERVER_TYPE;
        };
    }

    public List<String> overlaysForVersion(PackFormat $$0) {
        return this.overlays.stream().filter($$1 -> $$1.isApplicable($$0)).map(OverlayEntry::overlay).toList();
    }

    public record OverlayEntry(InclusiveRange<PackFormat> format, String overlay) {
        static Codec<List<OverlayEntry>> listCodecForPackType(PackType $$0) {
            int $$1 = PackFormat.lastPreMinorVersion($$0);
            return IntermediateEntry.CODEC.listOf().flatXmap($$12 -> PackFormat.validateHolderList($$12, $$1, ($$0, $$1) -> new OverlayEntry((InclusiveRange<PackFormat>)$$1, $$0.overlay())), $$12 -> DataResult.success($$12.stream().map($$1 -> new IntermediateEntry(PackFormat.IntermediaryFormat.fromRange($$1.format(), $$1), $$1.overlay())).toList()));
        }

        public boolean isApplicable(PackFormat $$0) {
            return this.format.isValueInRange($$0);
        }

        record IntermediateEntry(PackFormat.IntermediaryFormat format, String overlay) implements PackFormat.IntermediaryFormatHolder
        {
            static final Codec<IntermediateEntry> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)PackFormat.IntermediaryFormat.OVERLAY_CODEC.forGetter(IntermediateEntry::format), (App)Codec.STRING.validate(OverlayMetadataSection::validateOverlayDir).fieldOf("directory").forGetter(IntermediateEntry::overlay)).apply((Applicative)$$0, IntermediateEntry::new));

            @Override
            public String toString() {
                return this.overlay;
            }
        }
    }
}

