package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;

public class SingletonArgumentInfo<A extends ArgumentType<?>> implements ArgumentTypeInfo<A, SingletonArgumentInfo<A>.Template> {
   private final SingletonArgumentInfo<A>.Template template;

   private SingletonArgumentInfo(Function<net.minecraft.commands.CommandBuildContext, A> $$0) {
      this.template = new SingletonArgumentInfo.Template($$0);
   }

   public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextFree(Supplier<T> $$0) {
      return new SingletonArgumentInfo<>($$1 -> $$0.get());
   }

   public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextAware(Function<net.minecraft.commands.CommandBuildContext, T> $$0) {
      return new SingletonArgumentInfo<>($$0);
   }

   public void serializeToNetwork(SingletonArgumentInfo<A>.Template $$0, FriendlyByteBuf $$1) {
   }

   public void serializeToJson(SingletonArgumentInfo<A>.Template $$0, JsonObject $$1) {
   }

   public SingletonArgumentInfo<A>.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
      return this.template;
   }

   public SingletonArgumentInfo<A>.Template unpack(A $$0) {
      return this.template;
   }

   public final class Template implements ArgumentTypeInfo.Template<A> {
      private final Function<net.minecraft.commands.CommandBuildContext, A> constructor;

      public Template(final Function<net.minecraft.commands.CommandBuildContext, A> $$1) {
         this.constructor = $$1;
      }

      @Override
      public A instantiate(net.minecraft.commands.CommandBuildContext $$0) {
         return this.constructor.apply($$0);
      }

      @Override
      public ArgumentTypeInfo<A, ?> type() {
         return SingletonArgumentInfo.this;
      }
   }
}
