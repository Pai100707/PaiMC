package net.minecraft.commands;

import net.minecraft.network.chat.Component;

public interface CommandSource {
   net.minecraft.commands.CommandSource NULL = new net.minecraft.commands.CommandSource() {
      @Override
      public void sendSystemMessage(Component $$0) {
      }

      @Override
      public boolean acceptsSuccess() {
         return false;
      }

      @Override
      public boolean acceptsFailure() {
         return false;
      }

      @Override
      public boolean shouldInformAdmins() {
         return false;
      }
   };

   void sendSystemMessage(Component var1);

   boolean acceptsSuccess();

   boolean acceptsFailure();

   boolean shouldInformAdmins();

   default boolean alwaysAccepts() {
      return false;
   }
}
