/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public class CompositePackResources
implements PackResources {
    private final PackResources primaryPackResources;
    private final List<PackResources> packResourcesStack;

    public CompositePackResources(PackResources $$0, List<PackResources> $$1) {
        this.primaryPackResources = $$0;
        ArrayList<PackResources> $$2 = new ArrayList<PackResources>($$1.size() + 1);
        $$2.addAll(Lists.reverse($$1));
        $$2.add($$0);
        this.packResourcesStack = List.copyOf($$2);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... $$0) {
        return this.primaryPackResources.getRootResource($$0);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType $$0, Identifier $$1) {
        for (PackResources $$2 : this.packResourcesStack) {
            IoSupplier<InputStream> $$3 = $$2.getResource($$0, $$1);
            if ($$3 == null) continue;
            return $$3;
        }
        return null;
    }

    @Override
    public void listResources(PackType $$0, String $$1, String $$2, PackResources.ResourceOutput $$3) {
        HashMap<Identifier, IoSupplier<InputStream>> $$4 = new HashMap<Identifier, IoSupplier<InputStream>>();
        for (PackResources $$5 : this.packResourcesStack) {
            $$5.listResources($$0, $$1, $$2, $$4::putIfAbsent);
        }
        $$4.forEach($$3);
    }

    @Override
    public Set<String> getNamespaces(PackType $$0) {
        HashSet<String> $$1 = new HashSet<String>();
        for (PackResources $$2 : this.packResourcesStack) {
            $$1.addAll($$2.getNamespaces($$0));
        }
        return $$1;
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> $$0) throws IOException {
        return this.primaryPackResources.getMetadataSection($$0);
    }

    @Override
    public PackLocationInfo location() {
        return this.primaryPackResources.location();
    }

    @Override
    public void close() {
        this.packResourcesStack.forEach(PackResources::close);
    }
}

