package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class DustColorTransitionOptions extends ScalableParticleOptionsBase {
   public static final int SCULK_PARTICLE_COLOR = 3790560;
   public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(3790560, 16711680, 1.0F);
   public static final MapCodec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("from_color").forGetter($$0x -> $$0x.fromColor),
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("to_color").forGetter($$0x -> $$0x.toColor),
            SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)
         )
         .apply($$0, DustColorTransitionOptions::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, DustColorTransitionOptions> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      $$0 -> $$0.fromColor,
      ByteBufCodecs.INT,
      $$0 -> $$0.toColor,
      ByteBufCodecs.FLOAT,
      ScalableParticleOptionsBase::getScale,
      DustColorTransitionOptions::new
   );
   private final int fromColor;
   private final int toColor;

   public DustColorTransitionOptions(int $$0, int $$1, float $$2) {
      super($$2);
      this.fromColor = $$0;
      this.toColor = $$1;
   }

   public Vector3f getFromColor() {
      return ARGB.vector3fFromRGB24(this.fromColor);
   }

   public Vector3f getToColor() {
      return ARGB.vector3fFromRGB24(this.toColor);
   }

   @Override
   public ParticleType<DustColorTransitionOptions> getType() {
      return ParticleTypes.DUST_COLOR_TRANSITION;
   }
}
