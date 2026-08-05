package net.minecraft.gametest.framework;


class GameTestEvent {
   
   public final Long expectedDelay;
   public final Runnable assertion;

   private GameTestEvent(Long $$0, Runnable $$1) {
      this.expectedDelay = $$0;
      this.assertion = $$1;
   }

   static GameTestEvent create(Runnable $$0) {
      return new GameTestEvent(null, $$0);
   }

   static GameTestEvent create(long $$0, Runnable $$1) {
      return new GameTestEvent($$0, $$1);
   }
}
