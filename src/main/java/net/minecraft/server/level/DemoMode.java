package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class DemoMode extends ServerPlayerGameMode {
   public static final int DEMO_DAYS = 5;
   public static final int TOTAL_PLAY_TICKS = 120500;
   private boolean displayedIntro;
   private boolean demoHasEnded;
   private int demoEndedReminder;
   private int gameModeTicks;

   public DemoMode(ServerPlayer $$0) {
      super($$0);
   }

   @Override
   public void tick() {
      super.tick();
      this.gameModeTicks++;
      long $$0 = this.level.getGameTime();
      long $$1 = $$0 / 24000L + 1L;
      if (!this.displayedIntro && this.gameModeTicks > 20) {
         this.displayedIntro = true;
         this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 0.0F));
      }

      this.demoHasEnded = $$0 > 120500L;
      if (this.demoHasEnded) {
         this.demoEndedReminder++;
      }

      if ($$0 % 24000L == 500L) {
         if ($$1 <= 6L) {
            if ($$1 == 6L) {
               this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 104.0F));
            } else {
               this.player.sendSystemMessage(Component.translatable("demo.day." + $$1));
            }
         }
      } else if ($$1 == 1L) {
         if ($$0 == 100L) {
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 101.0F));
         } else if ($$0 == 175L) {
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 102.0F));
         } else if ($$0 == 250L) {
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 103.0F));
         }
      } else if ($$1 == 5L && $$0 % 24000L == 22000L) {
         this.player.sendSystemMessage(Component.translatable("demo.day.warning"));
      }
   }

   private void outputDemoReminder() {
      if (this.demoEndedReminder > 100) {
         this.player.sendSystemMessage(Component.translatable("demo.reminder"));
         this.demoEndedReminder = 0;
      }
   }

   @Override
   public void handleBlockBreakAction(BlockPos $$0, Action $$1, Direction $$2, int $$3, int $$4) {
      if (this.demoHasEnded) {
         this.outputDemoReminder();
      } else {
         super.handleBlockBreakAction($$0, $$1, $$2, $$3, $$4);
      }
   }

   @Override
   public InteractionResult useItem(ServerPlayer $$0, Level $$1, ItemStack $$2, InteractionHand $$3) {
      if (this.demoHasEnded) {
         this.outputDemoReminder();
         return InteractionResult.PASS;
      } else {
         return super.useItem($$0, $$1, $$2, $$3);
      }
   }

   @Override
   public InteractionResult useItemOn(ServerPlayer $$0, Level $$1, ItemStack $$2, InteractionHand $$3, BlockHitResult $$4) {
      if (this.demoHasEnded) {
         this.outputDemoReminder();
         return InteractionResult.PASS;
      } else {
         return super.useItemOn($$0, $$1, $$2, $$3, $$4);
      }
   }
}
