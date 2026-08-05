/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  org.apache.commons.lang3.exception.ExceptionUtils
 */
package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.ExhaustedAttemptsException;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener
implements GameTestListener {
    private int attempts = 0;
    private int successes = 0;

    @Override
    public void testStructureLoaded(GameTestInfo $$0) {
        ++this.attempts;
    }

    private void handleRetry(GameTestInfo $$0, GameTestRunner $$1, boolean $$2) {
        RetryOptions $$3 = $$0.retryOptions();
        Object $$4 = String.format(Locale.ROOT, "[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);
        if (!$$3.unlimitedTries()) {
            $$4 = (String)$$4 + String.format(Locale.ROOT, ", Left: %4d", $$3.numberOfTries() - this.attempts);
        }
        $$4 = (String)$$4 + "]";
        String $$5 = String.valueOf($$0.id()) + " " + ($$2 ? "passed" : "failed") + "! " + $$0.getRunTime() + "ms";
        String $$6 = String.format(Locale.ROOT, "%-53s%s", $$4, $$5);
        if ($$2) {
            ReportGameListener.reportPassed($$0, $$6);
        } else {
            ReportGameListener.say($$0.getLevel(), ChatFormatting.RED, $$6);
        }
        if ($$3.hasTriesLeft(this.attempts, this.successes)) {
            $$1.rerunTest($$0);
        }
    }

    @Override
    public void testPassed(GameTestInfo $$0, GameTestRunner $$1) {
        ++this.successes;
        if ($$0.retryOptions().hasRetries()) {
            this.handleRetry($$0, $$1, true);
            return;
        }
        if (!$$0.isFlaky()) {
            ReportGameListener.reportPassed($$0, String.valueOf($$0.id()) + " passed! (" + $$0.getRunTime() + "ms / " + $$0.getTick() + "gameticks)");
            return;
        }
        if (this.successes >= $$0.requiredSuccesses()) {
            ReportGameListener.reportPassed($$0, String.valueOf($$0) + " passed " + this.successes + " times of " + this.attempts + " attempts.");
        } else {
            ReportGameListener.say($$0.getLevel(), ChatFormatting.GREEN, "Flaky test " + String.valueOf($$0) + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
            $$1.rerunTest($$0);
        }
    }

    @Override
    public void testFailed(GameTestInfo $$0, GameTestRunner $$1) {
        if (!$$0.isFlaky()) {
            ReportGameListener.reportFailure($$0, $$0.getError());
            if ($$0.retryOptions().hasRetries()) {
                this.handleRetry($$0, $$1, false);
            }
            return;
        }
        GameTestInstance $$2 = $$0.getTest();
        String $$3 = "Flaky test " + String.valueOf($$0) + " failed, attempt: " + this.attempts + "/" + $$2.maxAttempts();
        if ($$2.requiredSuccesses() > 1) {
            $$3 = $$3 + ", successes: " + this.successes + " (" + $$2.requiredSuccesses() + " required)";
        }
        ReportGameListener.say($$0.getLevel(), ChatFormatting.YELLOW, $$3);
        if ($$0.maxAttempts() - this.attempts + this.successes >= $$0.requiredSuccesses()) {
            $$1.rerunTest($$0);
        } else {
            ReportGameListener.reportFailure($$0, new ExhaustedAttemptsException(this.attempts, this.successes, $$0));
        }
    }

    @Override
    public void testAddedForRerun(GameTestInfo $$0, GameTestInfo $$1, GameTestRunner $$2) {
        $$1.addListener(this);
    }

    public static void reportPassed(GameTestInfo $$02, String $$1) {
        ReportGameListener.getTestInstanceBlockEntity($$02).ifPresent($$0 -> $$0.setSuccess());
        ReportGameListener.visualizePassedTest($$02, $$1);
    }

    private static void visualizePassedTest(GameTestInfo $$0, String $$1) {
        ReportGameListener.say($$0.getLevel(), ChatFormatting.GREEN, $$1);
        GlobalTestReporter.onTestSuccess($$0);
    }

    protected static void reportFailure(GameTestInfo $$0, Throwable $$12) {
        MutableComponent $$4;
        if ($$12 instanceof GameTestAssertException) {
            GameTestAssertException $$2 = (GameTestAssertException)$$12;
            Component $$3 = $$2.getDescription();
        } else {
            $$4 = Component.literal(Util.describeError($$12));
        }
        ReportGameListener.getTestInstanceBlockEntity($$0).ifPresent($$1 -> $$1.setErrorMessage($$4));
        ReportGameListener.visualizeFailedTest($$0, $$12);
    }

    protected static void visualizeFailedTest(GameTestInfo $$0, Throwable $$1) {
        String $$2 = $$1.getMessage() + (String)($$1.getCause() == null ? "" : " cause: " + Util.describeError($$1.getCause()));
        String $$3 = ($$0.isRequired() ? "" : "(optional) ") + String.valueOf($$0.id()) + " failed! " + $$2;
        ReportGameListener.say($$0.getLevel(), $$0.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, $$3);
        Throwable $$4 = (Throwable)MoreObjects.firstNonNull((Object)ExceptionUtils.getRootCause((Throwable)$$1), (Object)$$1);
        if ($$4 instanceof GameTestAssertPosException) {
            GameTestAssertPosException $$5 = (GameTestAssertPosException)$$4;
            $$0.getTestInstanceBlockEntity().markError($$5.getAbsolutePos(), $$5.getMessageToShowAtBlock());
        }
        GlobalTestReporter.onTestFailed($$0);
    }

    private static Optional<TestInstanceBlockEntity> getTestInstanceBlockEntity(GameTestInfo $$0) {
        ServerLevel $$12 = $$0.getLevel();
        Optional<BlockPos> $$2 = Optional.ofNullable($$0.getTestBlockPos());
        Optional<TestInstanceBlockEntity> $$3 = $$2.flatMap($$1 -> $$12.getBlockEntity((BlockPos)$$1, BlockEntityType.TEST_INSTANCE_BLOCK));
        return $$3;
    }

    protected static void say(ServerLevel $$02, ChatFormatting $$1, String $$22) {
        $$02.getPlayers($$0 -> true).forEach($$2 -> $$2.sendSystemMessage(Component.literal($$22).withStyle($$1)));
    }
}

