package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class GameTestTicker {
   public static final GameTestTicker SINGLETON = new GameTestTicker();
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Collection<GameTestInfo> testInfos = Lists.newCopyOnWriteArrayList();
   
   private GameTestRunner runner;
   private GameTestTicker.State state = GameTestTicker.State.IDLE;

   private GameTestTicker() {
   }

   public void add(GameTestInfo $$0) {
      this.testInfos.add($$0);
   }

   public void clear() {
      if (this.state != GameTestTicker.State.IDLE) {
         this.state = GameTestTicker.State.HALTING;
      } else {
         this.testInfos.clear();
         if (this.runner != null) {
            this.runner.stop();
            this.runner = null;
         }
      }
   }

   public void setRunner(GameTestRunner $$0) {
      if (this.runner != null) {
         Util.logAndPauseIfInIde("The runner was already set in GameTestTicker");
      }

      this.runner = $$0;
   }

   public void tick() {
      if (this.runner != null) {
         this.state = GameTestTicker.State.RUNNING;
         this.testInfos.forEach($$0x -> $$0x.tick(this.runner));
         this.testInfos.removeIf(GameTestInfo::isDone);
         GameTestTicker.State $$0 = this.state;
         this.state = GameTestTicker.State.IDLE;
         if ($$0 == GameTestTicker.State.HALTING) {
            this.clear();
         }
      }
   }

   static enum State {
      IDLE,
      RUNNING,
      HALTING;
   }
}
