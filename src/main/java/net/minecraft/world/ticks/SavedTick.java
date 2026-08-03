package net.minecraft.world.ticks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public record SavedTick<T>(T type, BlockPos pos, int delay, net.minecraft.world.ticks.TickPriority priority) {
   public static final Strategy<net.minecraft.world.ticks.SavedTick<?>> UNIQUE_TICK_HASH = new Strategy<net.minecraft.world.ticks.SavedTick<?>>() {
      public int hashCode(net.minecraft.world.ticks.SavedTick<?> $$0) {
         return 31 * $$0.pos().hashCode() + $$0.type().hashCode();
      }

      public boolean equals(@Nullable net.minecraft.world.ticks.SavedTick<?> $$0, @Nullable net.minecraft.world.ticks.SavedTick<?> $$1) {
         if ($$0 == $$1) {
            return true;
         } else {
            return $$0 != null && $$1 != null ? $$0.type() == $$1.type() && $$0.pos().equals($$1.pos()) : false;
         }
      }
   };

   public static <T> Codec<net.minecraft.world.ticks.SavedTick<T>> codec(Codec<T> $$0) {
      MapCodec<BlockPos> $$1 = RecordCodecBuilder.mapCodec(
         $$0x -> $$0x.group(
               Codec.INT.fieldOf("x").forGetter(Vec3i::getX), Codec.INT.fieldOf("y").forGetter(Vec3i::getY), Codec.INT.fieldOf("z").forGetter(Vec3i::getZ)
            )
            .apply($$0x, BlockPos::new)
      );
      return RecordCodecBuilder.create(
         $$2 -> $$2.group(
               $$0.fieldOf("i").forGetter(net.minecraft.world.ticks.SavedTick::type),
               $$1.forGetter(net.minecraft.world.ticks.SavedTick::pos),
               Codec.INT.fieldOf("t").forGetter(net.minecraft.world.ticks.SavedTick::delay),
               net.minecraft.world.ticks.TickPriority.CODEC.fieldOf("p").forGetter(net.minecraft.world.ticks.SavedTick::priority)
            )
            .apply($$2, net.minecraft.world.ticks.SavedTick::new)
      );
   }

   public static <T> List<net.minecraft.world.ticks.SavedTick<T>> filterTickListForChunk(List<net.minecraft.world.ticks.SavedTick<T>> $$0, ChunkPos $$1) {
      long $$2 = $$1.toLong();
      return $$0.stream().filter($$1x -> ChunkPos.asLong($$1x.pos()) == $$2).toList();
   }

   public net.minecraft.world.ticks.ScheduledTick<T> unpack(long $$0, long $$1) {
      return new net.minecraft.world.ticks.ScheduledTick<>(this.type, this.pos, $$0 + this.delay, this.priority, $$1);
   }

   public static <T> net.minecraft.world.ticks.SavedTick<T> probe(T $$0, BlockPos $$1) {
      return new net.minecraft.world.ticks.SavedTick<>($$0, $$1, 0, net.minecraft.world.ticks.TickPriority.NORMAL);
   }
}
