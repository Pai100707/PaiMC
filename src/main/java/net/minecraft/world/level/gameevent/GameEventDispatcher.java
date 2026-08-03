package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugGameEventInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
   private final ServerLevel level;

   public GameEventDispatcher(ServerLevel $$0) {
      this.level = $$0;
   }

   public void post(Holder<GameEvent> $$0, Vec3 $$1, GameEvent.Context $$2) {
      int $$3 = ((GameEvent)$$0.value()).notificationRadius();
      BlockPos $$4 = BlockPos.containing($$1);
      int $$5 = SectionPos.blockToSectionCoord($$4.getX() - $$3);
      int $$6 = SectionPos.blockToSectionCoord($$4.getY() - $$3);
      int $$7 = SectionPos.blockToSectionCoord($$4.getZ() - $$3);
      int $$8 = SectionPos.blockToSectionCoord($$4.getX() + $$3);
      int $$9 = SectionPos.blockToSectionCoord($$4.getY() + $$3);
      int $$10 = SectionPos.blockToSectionCoord($$4.getZ() + $$3);
      List<GameEvent.ListenerInfo> $$11 = new ArrayList<>();
      GameEventListenerRegistry.ListenerVisitor $$12 = ($$4x, $$5x) -> {
         if ($$4x.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
            $$11.add(new GameEvent.ListenerInfo($$0, $$1, $$2, $$4x, $$5x));
         } else {
            $$4x.handleGameEvent(this.level, $$0, $$2, $$1);
         }
      };
      boolean $$13 = false;

      for (int $$14 = $$5; $$14 <= $$8; $$14++) {
         for (int $$15 = $$7; $$15 <= $$10; $$15++) {
            ChunkAccess $$16 = this.level.getChunkSource().getChunkNow($$14, $$15);
            if ($$16 != null) {
               for (int $$17 = $$6; $$17 <= $$9; $$17++) {
                  $$13 |= $$16.getListenerRegistry($$17).visitInRangeListeners($$0, $$1, $$2, $$12);
               }
            }
         }
      }

      if (!$$11.isEmpty()) {
         this.handleGameEventMessagesInQueue($$11);
      }

      if ($$13) {
         this.level.debugSynchronizers().broadcastEventToTracking(BlockPos.containing($$1), DebugSubscriptions.GAME_EVENTS, new DebugGameEventInfo($$0, $$1));
      }
   }

   private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> $$0) {
      Collections.sort($$0);

      for (GameEvent.ListenerInfo $$1 : $$0) {
         GameEventListener $$2 = $$1.recipient();
         $$2.handleGameEvent(this.level, $$1.gameEvent(), $$1.context(), $$1.source());
      }
   }
}
