package net.minecraft.server.jsonrpc.websocket;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;

public class WebSocketToJsonCodec extends MessageToMessageDecoder<TextWebSocketFrame> {
   protected void decode(ChannelHandlerContext $$0, TextWebSocketFrame $$1, List<Object> $$2) {
      JsonElement $$3 = JsonParser.parseString($$1.text());
      $$2.add($$3);
   }
}
