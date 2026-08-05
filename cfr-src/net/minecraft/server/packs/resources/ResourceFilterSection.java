/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.packs.resources;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.IdentifierPattern;

public class ResourceFilterSection {
    private static final Codec<ResourceFilterSection> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)Codec.list(IdentifierPattern.CODEC).fieldOf("block").forGetter($$0 -> $$0.blockList)).apply((Applicative)$$02, ResourceFilterSection::new));
    public static final MetadataSectionType<ResourceFilterSection> TYPE = new MetadataSectionType<ResourceFilterSection>("filter", CODEC);
    private final List<IdentifierPattern> blockList;

    public ResourceFilterSection(List<IdentifierPattern> $$0) {
        this.blockList = List.copyOf($$0);
    }

    public boolean isNamespaceFiltered(String $$0) {
        return this.blockList.stream().anyMatch($$1 -> $$1.namespacePredicate().test($$0));
    }

    public boolean isPathFiltered(String $$0) {
        return this.blockList.stream().anyMatch($$1 -> $$1.pathPredicate().test($$0));
    }
}

