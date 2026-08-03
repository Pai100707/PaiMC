package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class StatType<T> implements Iterable<net.minecraft.stats.Stat<T>> {
   private final Registry<T> registry;
   private final Map<T, net.minecraft.stats.Stat<T>> map = new IdentityHashMap<>();
   private final Component displayName;
   private final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.stats.Stat<T>> streamCodec;

   public StatType(Registry<T> $$0, Component $$1) {
      this.registry = $$0;
      this.displayName = $$1;
      this.streamCodec = ByteBufCodecs.registry($$0.key()).map(this::get, net.minecraft.stats.Stat::getValue);
   }

   public StreamCodec<RegistryFriendlyByteBuf, net.minecraft.stats.Stat<T>> streamCodec() {
      return this.streamCodec;
   }

   public boolean contains(T $$0) {
      return this.map.containsKey($$0);
   }

   public net.minecraft.stats.Stat<T> get(T $$0, net.minecraft.stats.StatFormatter $$1) {
      return this.map.computeIfAbsent($$0, $$1x -> new net.minecraft.stats.Stat<>(this, (T)$$1x, $$1));
   }

   public Registry<T> getRegistry() {
      return this.registry;
   }

   @Override
   public Iterator<net.minecraft.stats.Stat<T>> iterator() {
      return this.map.values().iterator();
   }

   public net.minecraft.stats.Stat<T> get(T $$0) {
      return this.get($$0, net.minecraft.stats.StatFormatter.DEFAULT);
   }

   public Component getDisplayName() {
      return this.displayName;
   }
}
