package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;

public class DefaultedMappedRegistry<T> extends net.minecraft.core.MappedRegistry<T> implements net.minecraft.core.DefaultedRegistry<T> {
   private final Identifier defaultKey;
   private net.minecraft.core.Holder.Reference<T> defaultValue;

   public DefaultedMappedRegistry(String $$0, ResourceKey<? extends net.minecraft.core.Registry<T>> $$1, Lifecycle $$2, boolean $$3) {
      super($$1, $$2, $$3);
      this.defaultKey = Identifier.parse($$0);
   }

   @Override
   public net.minecraft.core.Holder.Reference<T> register(ResourceKey<T> $$0, T $$1, net.minecraft.core.RegistrationInfo $$2) {
      net.minecraft.core.Holder.Reference<T> $$3 = super.register($$0, $$1, $$2);
      if (this.defaultKey.equals($$0.identifier())) {
         this.defaultValue = $$3;
      }

      return $$3;
   }

   @Override
   public int getId(T $$0) {
      int $$1 = super.getId($$0);
      return $$1 == -1 ? super.getId(this.defaultValue.value()) : $$1;
   }

   @Override
   public Identifier getKey(T $$0) {
      Identifier $$1 = super.getKey($$0);
      return $$1 == null ? this.defaultKey : $$1;
   }

   @Override
   public T getValue(Identifier $$0) {
      T $$1 = super.getValue($$0);
      return $$1 == null ? this.defaultValue.value() : $$1;
   }

   @Override
   public Optional<T> getOptional(Identifier $$0) {
      return Optional.ofNullable(super.getValue($$0));
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> getAny() {
      return Optional.ofNullable(this.defaultValue);
   }

   @Override
   public T byId(int $$0) {
      T $$1 = super.byId($$0);
      return $$1 == null ? this.defaultValue.value() : $$1;
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> getRandom(RandomSource $$0) {
      return super.getRandom($$0).or(() -> Optional.of(this.defaultValue));
   }

   @Override
   public Identifier getDefaultKey() {
      return this.defaultKey;
   }
}
