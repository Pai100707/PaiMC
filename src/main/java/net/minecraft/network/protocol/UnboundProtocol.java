package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;

public interface UnboundProtocol<T extends net.minecraft.network.PacketListener, B extends ByteBuf, C>
   extends net.minecraft.network.ProtocolInfo.DetailsProvider {
   net.minecraft.network.ProtocolInfo<T> bind(Function<ByteBuf, B> var1, C var2);
}
