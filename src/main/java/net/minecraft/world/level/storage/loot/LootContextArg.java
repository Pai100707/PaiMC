package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface LootContextArg<R> {
   Codec<LootContextArg<Object>> ENTITY_OR_BLOCK = createArgCodec(
      $$0 -> $$0.anyOf(LootContext.EntityTarget.values()).anyOf(LootContext.BlockEntityTarget.values())
   );

   
   R get(LootContext var1);

   ContextKey<?> contextParam();

   static <U> LootContextArg<U> cast(LootContextArg<? extends U> $$0) {
      return (LootContextArg<U>)$$0;
   }

   static <R> Codec<LootContextArg<R>> createArgCodec(UnaryOperator<LootContextArg.ArgCodecBuilder<R>> $$0) {
      return $$0.apply(new LootContextArg.ArgCodecBuilder<>()).build();
   }

   public static final class ArgCodecBuilder<R> {
      private final LateBoundIdMapper<String, LootContextArg<R>> sources = new LateBoundIdMapper();

      ArgCodecBuilder() {
      }

      public <T> LootContextArg.ArgCodecBuilder<R> anyOf(T[] $$0, Function<T, String> $$1, Function<T, ? extends LootContextArg<R>> $$2) {
         for (T $$3 : $$0) {
            this.sources.put($$1.apply($$3), $$2.apply($$3));
         }

         return this;
      }

      public <T extends StringRepresentable> LootContextArg.ArgCodecBuilder<R> anyOf(T[] $$0, Function<T, ? extends LootContextArg<R>> $$1) {
         return this.anyOf($$0, StringRepresentable::getSerializedName, $$1);
      }

      public <T extends StringRepresentable & LootContextArg<? extends R>> LootContextArg.ArgCodecBuilder<R> anyOf(T[] $$0) {
         return this.anyOf($$0, $$0x -> LootContextArg.cast((LootContextArg<? extends R>)$$0x));
      }

      public LootContextArg.ArgCodecBuilder<R> anyEntity(Function<? super ContextKey<? extends Entity>, ? extends LootContextArg<R>> $$0) {
         return this.anyOf(LootContext.EntityTarget.values(), $$1 -> $$0.apply($$1.contextParam()));
      }

      public LootContextArg.ArgCodecBuilder<R> anyBlockEntity(Function<? super ContextKey<? extends BlockEntity>, ? extends LootContextArg<R>> $$0) {
         return this.anyOf(LootContext.BlockEntityTarget.values(), $$1 -> $$0.apply($$1.contextParam()));
      }

      public LootContextArg.ArgCodecBuilder<R> anyItemStack(Function<? super ContextKey<? extends ItemStack>, ? extends LootContextArg<R>> $$0) {
         return this.anyOf(LootContext.ItemStackTarget.values(), $$1 -> $$0.apply($$1.contextParam()));
      }

      Codec<LootContextArg<R>> build() {
         return this.sources.codec(Codec.STRING);
      }
   }

   public interface Getter<T, R> extends LootContextArg<R> {
      
      R get(T var1);

      @Override
      ContextKey<? extends T> contextParam();

      
      @Override
      default R get(LootContext $$0) {
         T $$1 = $$0.getOptionalParameter((ContextKey<T>)this.contextParam());
         return $$1 != null ? this.get($$1) : null;
      }
   }

   public interface SimpleGetter<T> extends LootContextArg<T> {
      @Override
      ContextKey<? extends T> contextParam();

      
      @Override
      default T get(LootContext $$0) {
         return $$0.getOptionalParameter((ContextKey<T>)this.contextParam());
      }
   }
}
