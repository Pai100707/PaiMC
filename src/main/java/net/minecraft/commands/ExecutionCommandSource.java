package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.server.permissions.PermissionSetSupplier;
import org.jspecify.annotations.Nullable;

public interface ExecutionCommandSource<T extends net.minecraft.commands.ExecutionCommandSource<T>> extends PermissionSetSupplier {
   T withCallback(net.minecraft.commands.CommandResultCallback var1);

   net.minecraft.commands.CommandResultCallback callback();

   default T clearCallbacks() {
      return this.withCallback(net.minecraft.commands.CommandResultCallback.EMPTY);
   }

   CommandDispatcher<T> dispatcher();

   void handleError(CommandExceptionType var1, Message var2, boolean var3, @Nullable TraceCallbacks var4);

   boolean isSilent();

   default void handleError(CommandSyntaxException $$0, boolean $$1, @Nullable TraceCallbacks $$2) {
      this.handleError($$0.getType(), $$0.getRawMessage(), $$1, $$2);
   }

   static <T extends net.minecraft.commands.ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
      return ($$0, $$1, $$2) -> ((net.minecraft.commands.ExecutionCommandSource)$$0.getSource()).callback().onResult($$1, $$2);
   }
}
