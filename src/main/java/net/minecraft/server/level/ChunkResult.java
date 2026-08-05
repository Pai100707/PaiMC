package net.minecraft.server.level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ChunkResult<T> {
   static <T> ChunkResult<T> of(T $$0) {
      return new ChunkResult.Success<>($$0);
   }

   static <T> ChunkResult<T> error(String $$0) {
      return error(() -> $$0);
   }

   static <T> ChunkResult<T> error(Supplier<String> $$0) {
      return new ChunkResult.Fail<>($$0);
   }

   boolean isSuccess();

   
   T orElse(T var1);

   
   static <R> R orElse(ChunkResult<? extends R> $$0, R $$1) {
      R $$2 = (R)$$0.orElse(null);
      return $$2 != null ? $$2 : $$1;
   }

   
   String getError();

   ChunkResult<T> ifSuccess(Consumer<T> var1);

   <R> ChunkResult<R> map(Function<T, R> var1);

   <E extends Throwable> T orElseThrow(Supplier<E> var1) throws E;

   public record Fail<T>(Supplier<String> error) implements ChunkResult<T> {
      @Override
      public boolean isSuccess() {
         return false;
      }

      
      @Override
      public T orElse(T $$0) {
         return $$0;
      }

      @Override
      public String getError() {
         return this.error.get();
      }

      @Override
      public ChunkResult<T> ifSuccess(Consumer<T> $$0) {
         return this;
      }

      @Override
      public <R> ChunkResult<R> map(Function<T, R> $$0) {
         return new ChunkResult.Fail(this.error);
      }

      @Override
      public <E extends Throwable> T orElseThrow(Supplier<E> $$0) throws E {
         throw $$0.get();
      }
   }

   public record Success<T>(T value) implements ChunkResult<T> {
      @Override
      public boolean isSuccess() {
         return true;
      }

      @Override
      public T orElse(T $$0) {
         return this.value;
      }

      
      @Override
      public String getError() {
         return null;
      }

      @Override
      public ChunkResult<T> ifSuccess(Consumer<T> $$0) {
         $$0.accept(this.value);
         return this;
      }

      @Override
      public <R> ChunkResult<R> map(Function<T, R> $$0) {
         return new ChunkResult.Success<>($$0.apply(this.value));
      }

      @Override
      public <E extends Throwable> T orElseThrow(Supplier<E> $$0) throws E {
         return this.value;
      }
   }
}
