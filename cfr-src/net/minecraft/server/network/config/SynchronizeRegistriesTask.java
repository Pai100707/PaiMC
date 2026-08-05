/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network.config;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.tags.TagNetworkSerialization;

public class SynchronizeRegistriesTask
implements ConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("synchronize_registries");
    private final List<KnownPack> requestedPacks;
    private final LayeredRegistryAccess<RegistryLayer> registries;

    public SynchronizeRegistriesTask(List<KnownPack> $$0, LayeredRegistryAccess<RegistryLayer> $$1) {
        this.requestedPacks = $$0;
        this.registries = $$1;
    }

    @Override
    public void start(Consumer<Packet<?>> $$0) {
        $$0.accept(new ClientboundSelectKnownPacks(this.requestedPacks));
    }

    private void sendRegistries(Consumer<Packet<?>> $$0, Set<KnownPack> $$12) {
        RegistryOps<Tag> $$22 = this.registries.compositeAccess().createSerializationContext(NbtOps.INSTANCE);
        RegistrySynchronization.packRegistries($$22, this.registries.getAccessFrom(RegistryLayer.WORLDGEN), $$12, ($$1, $$2) -> $$0.accept(new ClientboundRegistryDataPacket((ResourceKey<? extends Registry<?>>)$$1, (List<RegistrySynchronization.PackedRegistryEntry>)$$2)));
        $$0.accept(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
    }

    public void handleResponse(List<KnownPack> $$0, Consumer<Packet<?>> $$1) {
        if ($$0.equals(this.requestedPacks)) {
            this.sendRegistries($$1, Set.copyOf(this.requestedPacks));
        } else {
            this.sendRegistries($$1, Set.of());
        }
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}

