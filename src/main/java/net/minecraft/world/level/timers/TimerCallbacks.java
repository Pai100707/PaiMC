package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;

public class TimerCallbacks<C> {
   public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = new TimerCallbacks<MinecraftServer>()
      .register(Identifier.withDefaultNamespace("function"), FunctionCallback.CODEC)
      .register(Identifier.withDefaultNamespace("function_tag"), FunctionTagCallback.CODEC);
   private final LateBoundIdMapper<Identifier, MapCodec<? extends TimerCallback<C>>> idMapper = new LateBoundIdMapper();
   private final Codec<TimerCallback<C>> codec = this.idMapper.codec(Identifier.CODEC).dispatch("Type", TimerCallback::codec, Function.identity());

   public TimerCallbacks<C> register(Identifier $$0, MapCodec<? extends TimerCallback<C>> $$1) {
      this.idMapper.put($$0, $$1);
      return this;
   }

   public Codec<TimerCallback<C>> codec() {
      return this.codec;
   }
}
