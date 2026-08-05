/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.packs.metadata.pack;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;

public record PackMetadataSection(Component description, InclusiveRange<PackFormat> supportedFormats) {
    private static final Codec<PackMetadataSection> FALLBACK_CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)ComponentSerialization.CODEC.fieldOf("description").forGetter(PackMetadataSection::description)).apply((Applicative)$$02, $$0 -> new PackMetadataSection((Component)$$0, new InclusiveRange<PackFormat>(PackFormat.of(Integer.MAX_VALUE)))));
    public static final MetadataSectionType<PackMetadataSection> CLIENT_TYPE = new MetadataSectionType<PackMetadataSection>("pack", PackMetadataSection.codecForPackType(PackType.CLIENT_RESOURCES));
    public static final MetadataSectionType<PackMetadataSection> SERVER_TYPE = new MetadataSectionType<PackMetadataSection>("pack", PackMetadataSection.codecForPackType(PackType.SERVER_DATA));
    public static final MetadataSectionType<PackMetadataSection> FALLBACK_TYPE = new MetadataSectionType<PackMetadataSection>("pack", FALLBACK_CODEC);

    private static Codec<PackMetadataSection> codecForPackType(PackType $$0) {
        return RecordCodecBuilder.create($$1 -> $$1.group((App)ComponentSerialization.CODEC.fieldOf("description").forGetter(PackMetadataSection::description), (App)PackFormat.packCodec($$0).forGetter(PackMetadataSection::supportedFormats)).apply((Applicative)$$1, PackMetadataSection::new));
    }

    public static MetadataSectionType<PackMetadataSection> forPackType(PackType $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case PackType.CLIENT_RESOURCES -> CLIENT_TYPE;
            case PackType.SERVER_DATA -> SERVER_TYPE;
        };
    }
}

