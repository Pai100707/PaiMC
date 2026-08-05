/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;

public class PacketReport
implements DataProvider {
    private final PackOutput output;

    public PacketReport(PackOutput $$0) {
        this.output = $$0;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        Path $$1 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("packets.json");
        return DataProvider.saveStable($$0, this.serializePackets(), $$1);
    }

    private JsonElement serializePackets() {
        JsonObject $$0 = new JsonObject();
        Stream.of(HandshakeProtocols.SERVERBOUND_TEMPLATE, StatusProtocols.CLIENTBOUND_TEMPLATE, StatusProtocols.SERVERBOUND_TEMPLATE, LoginProtocols.CLIENTBOUND_TEMPLATE, LoginProtocols.SERVERBOUND_TEMPLATE, ConfigurationProtocols.CLIENTBOUND_TEMPLATE, ConfigurationProtocols.SERVERBOUND_TEMPLATE, GameProtocols.CLIENTBOUND_TEMPLATE, GameProtocols.SERVERBOUND_TEMPLATE).map(ProtocolInfo.DetailsProvider::details).collect(Collectors.groupingBy(ProtocolInfo.Details::id)).forEach(($$1, $$2) -> {
            JsonObject $$3 = new JsonObject();
            $$0.add($$1.id(), (JsonElement)$$3);
            $$2.forEach($$12 -> {
                JsonObject $$22 = new JsonObject();
                $$3.add($$12.flow().id(), (JsonElement)$$22);
                $$12.listPackets(($$1, $$2) -> {
                    JsonObject $$3 = new JsonObject();
                    $$3.addProperty("protocol_id", (Number)$$2);
                    $$22.add($$1.id().toString(), (JsonElement)$$3);
                });
            });
        });
        return $$0;
    }

    @Override
    public String getName() {
        return "Packet Report";
    }
}

