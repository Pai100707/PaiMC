package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record GlobalPos(ResourceKey<Level> dimension, net.minecraft.core.BlockPos pos) {
   public static final MapCodec<net.minecraft.core.GlobalPos> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(net.minecraft.core.GlobalPos::dimension),
            net.minecraft.core.BlockPos.CODEC.fieldOf("pos").forGetter(net.minecraft.core.GlobalPos::pos)
         )
         .apply($$0, net.minecraft.core.GlobalPos::of)
   );
   public static final Codec<net.minecraft.core.GlobalPos> CODEC = MAP_CODEC.codec();
   public static final StreamCodec<ByteBuf, net.minecraft.core.GlobalPos> STREAM_CODEC = StreamCodec.composite(
      ResourceKey.streamCodec(Registries.DIMENSION),
      net.minecraft.core.GlobalPos::dimension,
      net.minecraft.core.BlockPos.STREAM_CODEC,
      net.minecraft.core.GlobalPos::pos,
      net.minecraft.core.GlobalPos::of
   );

   public static net.minecraft.core.GlobalPos of(ResourceKey<Level> $$0, net.minecraft.core.BlockPos $$1) {
      return new net.minecraft.core.GlobalPos($$0, $$1);
   }

   @Override
   public String toString() {
      return this.dimension + " " + this.pos;
   }

   public boolean isCloseEnough(ResourceKey<Level> $$0, net.minecraft.core.BlockPos $$1, int $$2) {
      return this.dimension.equals($$0) && this.pos.distChessboard($$1) <= $$2;
   }
}
