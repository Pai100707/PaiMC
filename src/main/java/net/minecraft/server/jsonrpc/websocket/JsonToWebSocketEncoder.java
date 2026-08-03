package net.minecraft.server.jsonrpc.websocket;

import com.google.gson.JsonElement;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;

public class JsonToWebSocketEncoder extends MessageToMessageEncoder<JsonElement> {
   protected void encode(ChannelHandlerContext $$0, JsonElement $$1, List<Object> $$2) {
      $$2.add(new TextWebSocketFrame($$1.toString()));
   }
}
