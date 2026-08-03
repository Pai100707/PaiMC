package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;

public interface SimpleUnboundProtocol<T extends net.minecraft.network.PacketListener, B extends ByteBuf>
   extends net.minecraft.network.ProtocolInfo.DetailsProvider {
   net.minecraft.network.ProtocolInfo<T> bind(Function<ByteBuf, B> var1);
}
