/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  org.slf4j.Logger
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.slf4j.Logger;

public class BiomeParametersDumpReport
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path topPath;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private static final MapCodec<ResourceKey<Biome>> ENTRY_CODEC = ResourceKey.codec(Registries.BIOME).fieldOf("biome");
    private static final Codec<Climate.ParameterList<ResourceKey<Biome>>> CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes").codec();

    public BiomeParametersDumpReport(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        this.topPath = $$0.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
        this.registries = $$1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        return this.registries.thenCompose($$1 -> {
            RegistryOps $$2 = $$1.createSerializationContext(JsonOps.INSTANCE);
            ArrayList $$32 = new ArrayList();
            MultiNoiseBiomeSourceParameterList.knownPresets().forEach(($$3, $$4) -> $$32.add(BiomeParametersDumpReport.dumpValue(this.createPath($$3.id()), $$0, $$2, CODEC, $$4)));
            return CompletableFuture.allOf((CompletableFuture[])$$32.toArray(CompletableFuture[]::new));
        });
    }

    private static <E> CompletableFuture<?> dumpValue(Path $$0, CachedOutput $$12, DynamicOps<JsonElement> $$2, Encoder<E> $$3, E $$4) {
        Optional $$5 = $$3.encodeStart($$2, $$4).resultOrPartial($$1 -> LOGGER.error("Couldn't serialize element {}: {}", (Object)$$0, $$1));
        if ($$5.isPresent()) {
            return DataProvider.saveStable($$12, (JsonElement)$$5.get(), $$0);
        }
        return CompletableFuture.completedFuture(null);
    }

    private Path createPath(Identifier $$0) {
        return this.topPath.resolve($$0.getNamespace()).resolve($$0.getPath() + ".json");
    }

    @Override
    public final String getName() {
        return "Biome Parameters";
    }
}

