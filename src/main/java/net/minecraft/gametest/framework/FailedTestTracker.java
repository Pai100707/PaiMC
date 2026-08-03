package net.minecraft.gametest.framework;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder.Reference;

public class FailedTestTracker {
   private static final Set<Reference<GameTestInstance>> LAST_FAILED_TESTS = Sets.newHashSet();

   public static Stream<Reference<GameTestInstance>> getLastFailedTests() {
      return LAST_FAILED_TESTS.stream();
   }

   public static void rememberFailedTest(Reference<GameTestInstance> $$0) {
      LAST_FAILED_TESTS.add($$0);
   }

   public static void forgetFailedTests() {
      LAST_FAILED_TESTS.clear();
   }
}
