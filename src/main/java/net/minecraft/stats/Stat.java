package net.minecraft.stats;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jspecify.annotations.Nullable;

public class Stat<T> extends ObjectiveCriteria {
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.stats.Stat<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.STAT_TYPE)
      .dispatch(net.minecraft.stats.Stat::getType, net.minecraft.stats.StatType::streamCodec);
   private final net.minecraft.stats.StatFormatter formatter;
   private final T value;
   private final net.minecraft.stats.StatType<T> type;

   protected Stat(net.minecraft.stats.StatType<T> $$0, T $$1, net.minecraft.stats.StatFormatter $$2) {
      super(buildName($$0, $$1));
      this.type = $$0;
      this.formatter = $$2;
      this.value = $$1;
   }

   public static <T> String buildName(net.minecraft.stats.StatType<T> $$0, T $$1) {
      return locationToKey(BuiltInRegistries.STAT_TYPE.getKey($$0)) + ":" + locationToKey($$0.getRegistry().getKey($$1));
   }

   private static String locationToKey(@Nullable Identifier $$0) {
      return $$0.toString().replace(':', '.');
   }

   public net.minecraft.stats.StatType<T> getType() {
      return this.type;
   }

   public T getValue() {
      return this.value;
   }

   public String format(int $$0) {
      return this.formatter.format($$0);
   }

   public boolean equals(Object $$0) {
      return this == $$0 || $$0 instanceof net.minecraft.stats.Stat && Objects.equals(this.getName(), ((net.minecraft.stats.Stat)$$0).getName());
   }

   public int hashCode() {
      return this.getName().hashCode();
   }

   public String toString() {
      return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + "}";
   }
}
