package net.minecraft.server.level;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum ParticleStatus {
   ALL(0, "options.particles.all"),
   DECREASED(1, "options.particles.decreased"),
   MINIMAL(2, "options.particles.minimal");

   private static final IntFunction<ParticleStatus> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.WRAP);
   public static final Codec<ParticleStatus> LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, $$0 -> $$0.id);
   private final int id;
   private final Component caption;

   private ParticleStatus(final int $$0, final String $$1) {
      this.id = $$0;
      this.caption = Component.translatable($$1);
   }

   public Component caption() {
      return this.caption;
   }
}
