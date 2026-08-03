package net.minecraft.gametest.framework;

class ExhaustedAttemptsException extends Throwable {
   public ExhaustedAttemptsException(int $$0, int $$1, GameTestInfo $$2) {
      super(
         "Not enough successes: "
            + $$1
            + " out of "
            + $$0
            + " attempts. Required successes: "
            + $$2.requiredSuccesses()
            + ". max attempts: "
            + $$2.maxAttempts()
            + ".",
         $$2.getError()
      );
   }
}
