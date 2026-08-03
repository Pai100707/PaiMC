package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
   private int attempts = 0;
   private int successes = 0;

   public ReportGameListener() {
   }

   @Override
   public void testStructureLoaded(GameTestInfo $$0) {
      this.attempts++;
   }

   private void handleRetry(GameTestInfo $$0, GameTestRunner $$1, boolean $$2) {
      RetryOptions $$3 = $$0.retryOptions();
      String $$4 = String.format(Locale.ROOT, "[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);
      if (!$$3.unlimitedTries()) {
         $$4 = $$4 + String.format(Locale.ROOT, ", Left: %4d", $$3.numberOfTries() - this.attempts);
      }

      $$4 = $$4 + "]";
      String $$5 = $$0.id() + " " + ($$2 ? "passed" : "failed") + "! " + $$0.getRunTime() + "ms";
      String $$6 = String.format(Locale.ROOT, "%-53s%s", $$4, $$5);
      if ($$2) {
         reportPassed($$0, $$6);
      } else {
         say($$0.getLevel(), ChatFormatting.RED, $$6);
      }

      if ($$3.hasTriesLeft(this.attempts, this.successes)) {
         $$1.rerunTest($$0);
      }
   }

   @Override
   public void testPassed(GameTestInfo $$0, GameTestRunner $$1) {
      this.successes++;
      if ($$0.retryOptions().hasRetries()) {
         this.handleRetry($$0, $$1, true);
      } else if (!$$0.isFlaky()) {
         reportPassed($$0, $$0.id() + " passed! (" + $$0.getRunTime() + "ms / " + $$0.getTick() + "gameticks)");
      } else {
         if (this.successes >= $$0.requiredSuccesses()) {
            reportPassed($$0, $$0 + " passed " + this.successes + " times of " + this.attempts + " attempts.");
         } else {
            say($$0.getLevel(), ChatFormatting.GREEN, "Flaky test " + $$0 + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
            $$1.rerunTest($$0);
         }
      }
   }

   @Override
   public void testFailed(GameTestInfo $$0, GameTestRunner $$1) {
      if (!$$0.isFlaky()) {
         reportFailure($$0, $$0.getError());
         if ($$0.retryOptions().hasRetries()) {
            this.handleRetry($$0, $$1, false);
         }
      } else {
         GameTestInstance $$2 = $$0.getTest();
         String $$3 = "Flaky test " + $$0 + " failed, attempt: " + this.attempts + "/" + $$2.maxAttempts();
         if ($$2.requiredSuccesses() > 1) {
            $$3 = $$3 + ", successes: " + this.successes + " (" + $$2.requiredSuccesses() + " required)";
         }

         say($$0.getLevel(), ChatFormatting.YELLOW, $$3);
         if ($$0.maxAttempts() - this.attempts + this.successes >= $$0.requiredSuccesses()) {
            $$1.rerunTest($$0);
         } else {
            reportFailure($$0, new ExhaustedAttemptsException(this.attempts, this.successes, $$0));
         }
      }
   }

   @Override
   public void testAddedForRerun(GameTestInfo $$0, GameTestInfo $$1, GameTestRunner $$2) {
      $$1.addListener(this);
   }

   public static void reportPassed(GameTestInfo $$0, String $$1) {
      getTestInstanceBlockEntity($$0).ifPresent($$0x -> $$0x.setSuccess());
      visualizePassedTest($$0, $$1);
   }

   private static void visualizePassedTest(GameTestInfo $$0, String $$1) {
      say($$0.getLevel(), ChatFormatting.GREEN, $$1);
      GlobalTestReporter.onTestSuccess($$0);
   }

   protected static void reportFailure(GameTestInfo $$0, Throwable $$1) {
      Component $$3;
      if ($$1 instanceof GameTestAssertException $$2) {
         $$3 = $$2.getDescription();
      } else {
         $$3 = Component.literal(Util.describeError($$1));
      }

      getTestInstanceBlockEntity($$0).ifPresent($$1x -> $$1x.setErrorMessage($$3));
      visualizeFailedTest($$0, $$1);
   }

   protected static void visualizeFailedTest(GameTestInfo $$0, Throwable $$1) {
      String $$2 = $$1.getMessage() + ($$1.getCause() == null ? "" : " cause: " + Util.describeError($$1.getCause()));
      String $$3 = ($$0.isRequired() ? "" : "(optional) ") + $$0.id() + " failed! " + $$2;
      say($$0.getLevel(), $$0.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, $$3);
      Throwable $$4 = (Throwable)MoreObjects.firstNonNull(ExceptionUtils.getRootCause($$1), $$1);
      if ($$4 instanceof GameTestAssertPosException $$5) {
         $$0.getTestInstanceBlockEntity().markError($$5.getAbsolutePos(), $$5.getMessageToShowAtBlock());
      }

      GlobalTestReporter.onTestFailed($$0);
   }

   private static Optional<TestInstanceBlockEntity> getTestInstanceBlockEntity(GameTestInfo $$0) {
      ServerLevel $$1 = $$0.getLevel();
      Optional<BlockPos> $$2 = Optional.ofNullable($$0.getTestBlockPos());
      return $$2.flatMap($$1x -> $$1.getBlockEntity($$1x, BlockEntityType.TEST_INSTANCE_BLOCK));
   }

   protected static void say(ServerLevel $$0, ChatFormatting $$1, String $$2) {
      $$0.getPlayers($$0x -> true).forEach($$2x -> $$2x.sendSystemMessage(Component.literal($$2).withStyle($$1)));
   }
}
