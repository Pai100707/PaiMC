package net.minecraft.server;

public final class RunningOnDifferentThreadException extends RuntimeException {
   public static final net.minecraft.server.RunningOnDifferentThreadException RUNNING_ON_DIFFERENT_THREAD = new net.minecraft.server.RunningOnDifferentThreadException();

   private RunningOnDifferentThreadException() {
      this.setStackTrace(new StackTraceElement[0]);
   }

   @Override
   public synchronized Throwable fillInStackTrace() {
      this.setStackTrace(new StackTraceElement[0]);
      return this;
   }
}
