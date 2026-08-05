/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.animal.feline;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record CatVariant(ClientAsset.ResourceTexture assetInfo, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<CatVariant> DIRECT_CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ClientAsset.ResourceTexture.DEFAULT_FIELD_CODEC.forGetter(CatVariant::assetInfo), (App)SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(CatVariant::spawnConditions)).apply((Applicative)$$0, CatVariant::new));
    public static final Codec<CatVariant> NETWORK_CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ClientAsset.ResourceTexture.DEFAULT_FIELD_CODEC.forGetter(CatVariant::assetInfo)).apply((Applicative)$$0, CatVariant::new));
    public static final Codec<Holder<CatVariant>> CODEC = RegistryFixedCodec.create(Registries.CAT_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<CatVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.CAT_VARIANT);

    private CatVariant(ClientAsset.ResourceTexture $$0) {
        this($$0, SpawnPrioritySelectors.EMPTY);
    }

    @Override
    public List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> selectors() {
        return this.spawnConditions.selectors();
    }
}

