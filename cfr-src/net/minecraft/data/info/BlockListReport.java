/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockListReport
implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public BlockListReport(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        this.output = $$0;
        this.registries = $$1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        Path $$1 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("blocks.json");
        return this.registries.thenCompose($$22 -> {
            JsonObject $$3 = new JsonObject();
            RegistryOps $$4 = $$22.createSerializationContext(JsonOps.INSTANCE);
            $$22.lookupOrThrow(Registries.BLOCK).listElements().forEach($$2 -> {
                JsonObject $$3 = new JsonObject();
                StateDefinition<Block, BlockState> $$4 = ((Block)$$2.value()).getStateDefinition();
                if (!$$4.getProperties().isEmpty()) {
                    JsonObject $$5 = new JsonObject();
                    for (Property property : $$4.getProperties()) {
                        JsonArray $$7 = new JsonArray();
                        for (Comparable $$8 : property.getPossibleValues()) {
                            $$7.add(Util.getPropertyName(property, $$8));
                        }
                        $$5.add(property.getName(), (JsonElement)$$7);
                    }
                    $$3.add("properties", (JsonElement)$$5);
                }
                JsonArray $$9 = new JsonArray();
                for (BlockState blockState : $$4.getPossibleStates()) {
                    JsonObject $$11 = new JsonObject();
                    JsonObject $$122 = new JsonObject();
                    for (Property<?> $$13 : $$4.getProperties()) {
                        $$122.addProperty($$13.getName(), Util.getPropertyName($$13, blockState.getValue($$13)));
                    }
                    if (!$$122.isEmpty()) {
                        $$11.add("properties", (JsonElement)$$122);
                    }
                    $$11.addProperty("id", (Number)Block.getId(blockState));
                    if (blockState == ((Block)$$2.value()).defaultBlockState()) {
                        $$11.addProperty("default", Boolean.valueOf(true));
                    }
                    $$9.add((JsonElement)$$11);
                }
                $$3.add("states", (JsonElement)$$9);
                String $$14 = $$2.getRegisteredName();
                JsonElement jsonElement = (JsonElement)BlockTypes.CODEC.codec().encodeStart((DynamicOps)$$4, (Object)((Block)$$2.value())).getOrThrow($$1 -> new AssertionError((Object)("Failed to serialize block " + $$14 + " (is type registered in BlockTypes?): " + $$1)));
                $$3.add("definition", jsonElement);
                $$3.add($$14, (JsonElement)$$3);
            });
            return DataProvider.saveStable($$0, (JsonElement)$$3, $$1);
        });
    }

    @Override
    public final String getName() {
        return "Block List";
    }
}

