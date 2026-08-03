package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.network.ProtocolInfo.Details;
import net.minecraft.network.ProtocolInfo.DetailsProvider;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;

public class PacketReport implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput output;

   public PacketReport(net.minecraft.data.PackOutput $$0) {
      this.output = $$0;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      Path $$1 = this.output.getOutputFolder(net.minecraft.data.PackOutput.Target.REPORTS).resolve("packets.json");
      return net.minecraft.data.DataProvider.saveStable($$0, this.serializePackets(), $$1);
   }

   private JsonElement serializePackets() {
      JsonObject $$0 = new JsonObject();
      Stream.of(
            HandshakeProtocols.SERVERBOUND_TEMPLATE,
            StatusProtocols.CLIENTBOUND_TEMPLATE,
            StatusProtocols.SERVERBOUND_TEMPLATE,
            LoginProtocols.CLIENTBOUND_TEMPLATE,
            LoginProtocols.SERVERBOUND_TEMPLATE,
            ConfigurationProtocols.CLIENTBOUND_TEMPLATE,
            ConfigurationProtocols.SERVERBOUND_TEMPLATE,
            GameProtocols.CLIENTBOUND_TEMPLATE,
            GameProtocols.SERVERBOUND_TEMPLATE
         )
         .map(DetailsProvider::details)
         .collect(Collectors.groupingBy(Details::id))
         .forEach(($$1, $$2) -> {
            JsonObject $$3 = new JsonObject();
            $$0.add($$1.id(), $$3);
            $$2.forEach($$1x -> {
               JsonObject $$2x = new JsonObject();
               $$3.add($$1x.flow().id(), $$2x);
               $$1x.listPackets(($$1xx, $$2xx) -> {
                  JsonObject $$3x = new JsonObject();
                  $$3x.addProperty("protocol_id", $$2xx);
                  $$2x.add($$1xx.id().toString(), $$3x);
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
