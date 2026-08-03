package net.minecraft.util.random;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.slf4j.Logger;

public record Weighted<T>(T value, int weight) {
   private static final Logger LOGGER = LogUtils.getLogger();

   public Weighted(T value, int weight) {
      if (weight < 0) {
         throw (IllegalArgumentException)net.minecraft.util.Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
      } else {
         if (weight == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.warn("Found 0 weight, make sure this is intentional!");
         }

         this.value = value;
         this.weight = weight;
      }
   }

   public static <E> Codec<Weighted<E>> codec(Codec<E> $$0) {
      return codec($$0.fieldOf("data"));
   }

   public static <E> Codec<Weighted<E>> codec(MapCodec<E> $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group($$0.forGetter(Weighted::value), net.minecraft.util.ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(Weighted::weight))
            .apply($$1, Weighted::new)
      );
   }

   public static <B extends ByteBuf, T> StreamCodec<B, Weighted<T>> streamCodec(StreamCodec<B, T> $$0) {
      return StreamCodec.composite($$0, Weighted::value, ByteBufCodecs.VAR_INT, Weighted::weight, Weighted::new);
   }

   public <U> Weighted<U> map(Function<T, U> $$0) {
      return new Weighted<>($$0.apply(this.value()), this.weight);
   }
}
