package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record SwingAnimation(net.minecraft.world.item.SwingAnimationType type, int duration) {
   public static final SwingAnimation DEFAULT = new SwingAnimation(net.minecraft.world.item.SwingAnimationType.WHACK, 6);
   public static final Codec<SwingAnimation> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.world.item.SwingAnimationType.CODEC.optionalFieldOf("type", DEFAULT.type).forGetter(SwingAnimation::type),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", DEFAULT.duration).forGetter(SwingAnimation::duration)
         )
         .apply($$0, SwingAnimation::new)
   );
   public static final StreamCodec<ByteBuf, SwingAnimation> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.world.item.SwingAnimationType.STREAM_CODEC, SwingAnimation::type, ByteBufCodecs.VAR_INT, SwingAnimation::duration, SwingAnimation::new
   );
}
