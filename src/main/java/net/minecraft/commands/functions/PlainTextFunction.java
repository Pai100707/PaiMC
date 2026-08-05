package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public record PlainTextFunction<T>(Identifier id, List<UnboundEntryAction<T>> entries) implements CommandFunction<T>, InstantiatedFunction<T> {
   @Override
   public InstantiatedFunction<T> instantiate(CompoundTag $$0, CommandDispatcher<T> $$1) throws net.minecraft.commands.FunctionInstantiationException {
      return this;
   }
}
