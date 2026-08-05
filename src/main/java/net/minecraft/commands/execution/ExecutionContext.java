package net.minecraft.commands.execution;

import com.google.common.collect.Queues;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ExecutionContext<T> implements AutoCloseable {
   private static final int MAX_QUEUE_DEPTH = 10000000;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int commandLimit;
   private final int forkLimit;
   private final ProfilerFiller profiler;
   
   private TraceCallbacks tracer;
   private int commandQuota;
   private boolean queueOverflow;
   private final Deque<CommandQueueEntry<T>> commandQueue = Queues.newArrayDeque();
   private final List<CommandQueueEntry<T>> newTopCommands = new ObjectArrayList();
   private int currentFrameDepth;

   public ExecutionContext(int $$0, int $$1, ProfilerFiller $$2) {
      this.commandLimit = $$0;
      this.forkLimit = $$1;
      this.profiler = $$2;
      this.commandQuota = $$0;
   }

   private static <T extends net.minecraft.commands.ExecutionCommandSource<T>> Frame createTopFrame(
      ExecutionContext<T> $$0, net.minecraft.commands.CommandResultCallback $$1
   ) {
      if ($$0.currentFrameDepth == 0) {
         return new Frame(0, $$1, $$0.commandQueue::clear);
      } else {
         int $$2 = $$0.currentFrameDepth + 1;
         return new Frame($$2, $$1, $$0.frameControlForDepth($$2));
      }
   }

   public static <T extends net.minecraft.commands.ExecutionCommandSource<T>> void queueInitialFunctionCall(
      ExecutionContext<T> $$0, InstantiatedFunction<T> $$1, T $$2, net.minecraft.commands.CommandResultCallback $$3
   ) {
      $$0.queueNext(new CommandQueueEntry<>(createTopFrame($$0, $$3), new CallFunction<>($$1, $$2.callback(), false).bind($$2)));
   }

   public static <T extends net.minecraft.commands.ExecutionCommandSource<T>> void queueInitialCommandExecution(
      ExecutionContext<T> $$0, String $$1, ContextChain<T> $$2, T $$3, net.minecraft.commands.CommandResultCallback $$4
   ) {
      $$0.queueNext(new CommandQueueEntry<>(createTopFrame($$0, $$4), new BuildContexts.TopLevel<>($$1, $$2, $$3)));
   }

   private void handleQueueOverflow() {
      this.queueOverflow = true;
      this.newTopCommands.clear();
      this.commandQueue.clear();
   }

   public void queueNext(CommandQueueEntry<T> $$0) {
      if (this.newTopCommands.size() + this.commandQueue.size() > 10000000) {
         this.handleQueueOverflow();
      }

      if (!this.queueOverflow) {
         this.newTopCommands.add($$0);
      }
   }

   public void discardAtDepthOrHigher(int $$0) {
      while (!this.commandQueue.isEmpty() && this.commandQueue.peek().frame().depth() >= $$0) {
         this.commandQueue.removeFirst();
      }
   }

   public Frame.FrameControl frameControlForDepth(int $$0) {
      return () -> this.discardAtDepthOrHigher($$0);
   }

   public void runCommandQueue() {
      this.pushNewCommands();

      while (true) {
         if (this.commandQuota <= 0) {
            LOGGER.info("Command execution stopped due to limit (executed {} commands)", this.commandLimit);
            break;
         }

         CommandQueueEntry<T> $$0 = this.commandQueue.pollFirst();
         if ($$0 == null) {
            return;
         }

         this.currentFrameDepth = $$0.frame().depth();
         $$0.execute(this);
         if (this.queueOverflow) {
            LOGGER.error("Command execution stopped due to command queue overflow (max {})", 10000000);
            break;
         }

         this.pushNewCommands();
      }

      this.currentFrameDepth = 0;
   }

   private void pushNewCommands() {
      for (int $$0 = this.newTopCommands.size() - 1; $$0 >= 0; $$0--) {
         this.commandQueue.addFirst(this.newTopCommands.get($$0));
      }

      this.newTopCommands.clear();
   }

   public void tracer(TraceCallbacks $$0) {
      this.tracer = $$0;
   }

   
   public TraceCallbacks tracer() {
      return this.tracer;
   }

   public ProfilerFiller profiler() {
      return this.profiler;
   }

   public int forkLimit() {
      return this.forkLimit;
   }

   public void incrementCost() {
      this.commandQuota--;
   }

   @Override
   public void close() {
      if (this.tracer != null) {
         this.tracer.close();
      }
   }
}
