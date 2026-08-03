package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugGameEventListenerInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry implements GameEventListenerRegistry {
   private final List<GameEventListener> listeners = Lists.newArrayList();
   private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
   private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
   private boolean processing;
   private final ServerLevel level;
   private final int sectionY;
   private final EuclideanGameEventListenerRegistry.OnEmptyAction onEmptyAction;

   public EuclideanGameEventListenerRegistry(ServerLevel $$0, int $$1, EuclideanGameEventListenerRegistry.OnEmptyAction $$2) {
      this.level = $$0;
      this.sectionY = $$1;
      this.onEmptyAction = $$2;
   }

   @Override
   public boolean isEmpty() {
      return this.listeners.isEmpty();
   }

   @Override
   public void register(GameEventListener $$0) {
      if (this.processing) {
         this.listenersToAdd.add($$0);
      } else {
         this.listeners.add($$0);
      }

      sendDebugInfo(this.level, $$0);
   }

   private static void sendDebugInfo(ServerLevel $$0, GameEventListener $$1) {
      if ($$0.debugSynchronizers().hasAnySubscriberFor(DebugSubscriptions.GAME_EVENT_LISTENERS)) {
         DebugGameEventListenerInfo $$2 = new DebugGameEventListenerInfo($$1.getListenerRadius());
         PositionSource $$3 = $$1.getListenerSource();
         if ($$3 instanceof BlockPositionSource $$4) {
            $$0.debugSynchronizers().sendBlockValue($$4.pos(), DebugSubscriptions.GAME_EVENT_LISTENERS, $$2);
         } else if ($$3 instanceof EntityPositionSource $$5) {
            Entity $$6 = $$0.getEntity($$5.getUuid());
            if ($$6 != null) {
               $$0.debugSynchronizers().sendEntityValue($$6, DebugSubscriptions.GAME_EVENT_LISTENERS, $$2);
            }
         }
      }
   }

   @Override
   public void unregister(GameEventListener $$0) {
      if (this.processing) {
         this.listenersToRemove.add($$0);
      } else {
         this.listeners.remove($$0);
      }

      if (this.listeners.isEmpty()) {
         this.onEmptyAction.apply(this.sectionY);
      }
   }

   @Override
   public boolean visitInRangeListeners(Holder<GameEvent> $$0, Vec3 $$1, GameEvent.Context $$2, GameEventListenerRegistry.ListenerVisitor $$3) {
      this.processing = true;
      boolean $$4 = false;

      try {
         Iterator<GameEventListener> $$5 = this.listeners.iterator();

         while ($$5.hasNext()) {
            GameEventListener $$6 = $$5.next();
            if (this.listenersToRemove.remove($$6)) {
               $$5.remove();
            } else {
               Optional<Vec3> $$7 = getPostableListenerPosition(this.level, $$1, $$6);
               if ($$7.isPresent()) {
                  $$3.visit($$6, $$7.get());
                  $$4 = true;
               }
            }
         }
      } finally {
         this.processing = false;
      }

      if (!this.listenersToAdd.isEmpty()) {
         this.listeners.addAll(this.listenersToAdd);
         this.listenersToAdd.clear();
      }

      if (!this.listenersToRemove.isEmpty()) {
         this.listeners.removeAll(this.listenersToRemove);
         this.listenersToRemove.clear();
      }

      return $$4;
   }

   private static Optional<Vec3> getPostableListenerPosition(ServerLevel $$0, Vec3 $$1, GameEventListener $$2) {
      Optional<Vec3> $$3 = $$2.getListenerSource().getPosition($$0);
      if ($$3.isEmpty()) {
         return Optional.empty();
      } else {
         double $$4 = BlockPos.containing((Position)$$3.get()).distSqr(BlockPos.containing($$1));
         int $$5 = $$2.getListenerRadius() * $$2.getListenerRadius();
         return $$4 > $$5 ? Optional.empty() : $$3;
      }
   }

   @FunctionalInterface
   public interface OnEmptyAction {
      void apply(int var1);
   }
}
