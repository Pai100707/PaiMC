package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
   public static final Codec<net.minecraft.commands.CacheableFunction> CODEC = Identifier.CODEC
      .xmap(net.minecraft.commands.CacheableFunction::new, net.minecraft.commands.CacheableFunction::getId);
   private final Identifier id;
   private boolean resolved;
   private Optional<CommandFunction<net.minecraft.commands.CommandSourceStack>> function = Optional.empty();

   public CacheableFunction(Identifier $$0) {
      this.id = $$0;
   }

   public Optional<CommandFunction<net.minecraft.commands.CommandSourceStack>> get(ServerFunctionManager $$0) {
      if (!this.resolved) {
         this.function = $$0.get(this.id);
         this.resolved = true;
      }

      return this.function;
   }

   public Identifier getId() {
      return this.id;
   }

   @Override
   public boolean equals(Object $$0) {
      return $$0 == this ? true : $$0 instanceof net.minecraft.commands.CacheableFunction $$1 && this.getId().equals($$1.getId());
   }
}
