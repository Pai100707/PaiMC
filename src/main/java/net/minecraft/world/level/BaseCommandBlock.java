package net.minecraft.world.level;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class BaseCommandBlock {
   private static final Component DEFAULT_NAME = Component.literal("@");
   private static final int NO_LAST_EXECUTION = -1;
   private long lastExecution = -1L;
   private boolean updateLastExecution = true;
   private int successCount;
   private boolean trackOutput = true;
   
   Component lastOutput;
   private String command = "";
   
   private Component customName;

   public int getSuccessCount() {
      return this.successCount;
   }

   public void setSuccessCount(int $$0) {
      this.successCount = $$0;
   }

   public Component getLastOutput() {
      return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
   }

   public void save(ValueOutput $$0) {
      $$0.putString("Command", this.command);
      $$0.putInt("SuccessCount", this.successCount);
      $$0.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
      $$0.putBoolean("TrackOutput", this.trackOutput);
      if (this.trackOutput) {
         $$0.storeNullable("LastOutput", ComponentSerialization.CODEC, this.lastOutput);
      }

      $$0.putBoolean("UpdateLastExecution", this.updateLastExecution);
      if (this.updateLastExecution && this.lastExecution != -1L) {
         $$0.putLong("LastExecution", this.lastExecution);
      }
   }

   public void load(ValueInput $$0) {
      this.command = $$0.getStringOr("Command", "");
      this.successCount = $$0.getIntOr("SuccessCount", 0);
      this.setCustomName(BlockEntity.parseCustomNameSafe($$0, "CustomName"));
      this.trackOutput = $$0.getBooleanOr("TrackOutput", true);
      if (this.trackOutput) {
         this.lastOutput = BlockEntity.parseCustomNameSafe($$0, "LastOutput");
      } else {
         this.lastOutput = null;
      }

      this.updateLastExecution = $$0.getBooleanOr("UpdateLastExecution", true);
      if (this.updateLastExecution) {
         this.lastExecution = $$0.getLongOr("LastExecution", -1L);
      } else {
         this.lastExecution = -1L;
      }
   }

   public void setCommand(String $$0) {
      this.command = $$0;
      this.successCount = 0;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean performCommand(ServerLevel $$0) {
      if ($$0.getGameTime() == this.lastExecution) {
         return false;
      } else if ("Searge".equalsIgnoreCase(this.command)) {
         this.lastOutput = Component.literal("#itzlipofutzli");
         this.successCount = 1;
         return true;
      } else {
         this.successCount = 0;
         if ($$0.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
            try {
               this.lastOutput = null;

               try (net.minecraft.world.level.BaseCommandBlock.CloseableCommandBlockSource $$1 = this.createSource($$0)) {
                  CommandSource $$2 = Objects.requireNonNullElse($$1, CommandSource.NULL);
                  CommandSourceStack $$3 = this.createCommandSourceStack($$0, $$2).withCallback(($$0x, $$1x) -> {
                     if ($$0x) {
                        this.successCount++;
                     }
                  });
                  $$0.getServer().getCommands().performPrefixedCommand($$3, this.command);
               }
            } catch (Throwable var7) {
               CrashReport $$5 = CrashReport.forThrowable(var7, "Executing command block");
               CrashReportCategory $$6 = $$5.addCategory("Command to be executed");
               $$6.setDetail("Command", this::getCommand);
               $$6.setDetail("Name", () -> this.getName().getString());
               throw new ReportedException($$5);
            }
         }

         if (this.updateLastExecution) {
            this.lastExecution = $$0.getGameTime();
         } else {
            this.lastExecution = -1L;
         }

         return true;
      }
   }

   private net.minecraft.world.level.BaseCommandBlock.CloseableCommandBlockSource createSource(ServerLevel $$0) {
      return this.trackOutput ? new net.minecraft.world.level.BaseCommandBlock.CloseableCommandBlockSource($$0) : null;
   }

   public Component getName() {
      return this.customName != null ? this.customName : DEFAULT_NAME;
   }

   
   public Component getCustomName() {
      return this.customName;
   }

   public void setCustomName(Component $$0) {
      this.customName = $$0;
   }

   public abstract void onUpdated(ServerLevel var1);

   public void setLastOutput(Component $$0) {
      this.lastOutput = $$0;
   }

   public void setTrackOutput(boolean $$0) {
      this.trackOutput = $$0;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public abstract CommandSourceStack createCommandSourceStack(ServerLevel var1, CommandSource var2);

   public abstract boolean isValid();

   protected class CloseableCommandBlockSource implements CommandSource, AutoCloseable {
      private final ServerLevel level;
      private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
      private boolean closed;

      protected CloseableCommandBlockSource(final ServerLevel $$1) {
         this.level = $$1;
      }

      public boolean acceptsSuccess() {
         return !this.closed && this.level.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK);
      }

      public boolean acceptsFailure() {
         return !this.closed;
      }

      public boolean shouldInformAdmins() {
         return !this.closed && this.level.getGameRules().get(GameRules.COMMAND_BLOCK_OUTPUT);
      }

      public void sendSystemMessage(Component $$0) {
         if (!this.closed) {
            BaseCommandBlock.this.lastOutput = Component.literal("[" + TIME_FORMAT.format(ZonedDateTime.now()) + "] ").append($$0);
            BaseCommandBlock.this.onUpdated(this.level);
         }
      }

      @Override
      public void close() throws Exception {
         this.closed = true;
      }
   }
}
