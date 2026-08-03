package net.minecraft.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.VisibleForDebug;

public class ExpirableValue<T> {
   private final T value;
   private long timeToLive;

   public ExpirableValue(T $$0, long $$1) {
      this.value = $$0;
      this.timeToLive = $$1;
   }

   public void tick() {
      if (this.canExpire()) {
         this.timeToLive--;
      }
   }

   public static <T> ExpirableValue<T> of(T $$0) {
      return new ExpirableValue<>($$0, Long.MAX_VALUE);
   }

   public static <T> ExpirableValue<T> of(T $$0, long $$1) {
      return new ExpirableValue<>($$0, $$1);
   }

   public long getTimeToLive() {
      return this.timeToLive;
   }

   public T getValue() {
      return this.value;
   }

   public boolean hasExpired() {
      return this.timeToLive <= 0L;
   }

   @Override
   public String toString() {
      return this.value + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
   }

   @VisibleForDebug
   public boolean canExpire() {
      return this.timeToLive != Long.MAX_VALUE;
   }

   public static <T> Codec<ExpirableValue<T>> codec(Codec<T> $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group(
               $$0.fieldOf("value").forGetter($$0xx -> $$0xx.value),
               Codec.LONG.lenientOptionalFieldOf("ttl").forGetter($$0xx -> $$0xx.canExpire() ? Optional.of($$0xx.timeToLive) : Optional.empty())
            )
            .apply($$1, ($$0xx, $$1x) -> new ExpirableValue<>($$0xx, $$1x.orElse(Long.MAX_VALUE)))
      );
   }
}
