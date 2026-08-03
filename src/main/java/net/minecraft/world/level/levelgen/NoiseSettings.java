package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.dimension.DimensionType;

public record NoiseSettings(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
   public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
               Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
               Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
               Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical)
            )
            .apply($$0, NoiseSettings::new)
      )
      .comapFlatMap(NoiseSettings::guardY, Function.identity());
   protected static final NoiseSettings OVERWORLD_NOISE_SETTINGS = create(-64, 384, 1, 2);
   protected static final NoiseSettings NETHER_NOISE_SETTINGS = create(0, 128, 1, 2);
   protected static final NoiseSettings END_NOISE_SETTINGS = create(0, 128, 2, 1);
   protected static final NoiseSettings CAVES_NOISE_SETTINGS = create(-64, 192, 1, 2);
   protected static final NoiseSettings FLOATING_ISLANDS_NOISE_SETTINGS = create(0, 256, 2, 1);

   private static DataResult<NoiseSettings> guardY(NoiseSettings $$0) {
      if ($$0.minY() + $$0.height() > DimensionType.MAX_Y + 1) {
         return DataResult.error(() -> "min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
      } else if ($$0.height() % 16 != 0) {
         return DataResult.error(() -> "height has to be a multiple of 16");
      } else {
         return $$0.minY() % 16 != 0 ? DataResult.error(() -> "min_y has to be a multiple of 16") : DataResult.success($$0);
      }
   }

   public static NoiseSettings create(int $$0, int $$1, int $$2, int $$3) {
      NoiseSettings $$4 = new NoiseSettings($$0, $$1, $$2, $$3);
      guardY($$4).error().ifPresent($$0x -> {
         throw new IllegalStateException($$0x.message());
      });
      return $$4;
   }

   public int getCellHeight() {
      return QuartPos.toBlock(this.noiseSizeVertical());
   }

   public int getCellWidth() {
      return QuartPos.toBlock(this.noiseSizeHorizontal());
   }

   public NoiseSettings clampToHeightAccessor(net.minecraft.world.level.LevelHeightAccessor $$0) {
      int $$1 = Math.max(this.minY, $$0.getMinY());
      int $$2 = Math.min(this.minY + this.height, $$0.getMaxY() + 1) - $$1;
      return new NoiseSettings($$1, $$2, this.noiseSizeHorizontal, this.noiseSizeVertical);
   }
}
