package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.CommandStorage;

public class StorageDataAccessor implements DataAccessor {
   static final SuggestionProvider<CommandSourceStack> SUGGEST_STORAGE = ($$0, $$1) -> SharedSuggestionProvider.suggestResource(getGlobalTags($$0).keys(), $$1);
   public static final Function<String, DataCommands.DataProvider> PROVIDER = $$0 -> new DataCommands.DataProvider() {
      @Override
      public DataAccessor access(CommandContext<CommandSourceStack> $$0x) {
         return new StorageDataAccessor(StorageDataAccessor.getGlobalTags($$0), IdentifierArgument.getId($$0, $$0));
      }

      @Override
      public ArgumentBuilder<CommandSourceStack, ?> wrap(
         ArgumentBuilder<CommandSourceStack, ?> $$0x, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> $$1
      ) {
         return $$0.then(
            Commands.literal("storage").then($$1.apply(Commands.argument($$0, IdentifierArgument.id()).suggests(StorageDataAccessor.SUGGEST_STORAGE)))
         );
      }
   };
   private final CommandStorage storage;
   private final Identifier id;

   static CommandStorage getGlobalTags(CommandContext<CommandSourceStack> $$0) {
      return ((CommandSourceStack)$$0.getSource()).getServer().getCommandStorage();
   }

   StorageDataAccessor(CommandStorage $$0, Identifier $$1) {
      this.storage = $$0;
      this.id = $$1;
   }

   @Override
   public void setData(CompoundTag $$0) {
      this.storage.set(this.id, $$0);
   }

   @Override
   public CompoundTag getData() {
      return this.storage.get(this.id);
   }

   @Override
   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.storage.modified", new Object[]{Component.translationArg(this.id)});
   }

   @Override
   public Component getPrintSuccess(Tag $$0) {
      return Component.translatable("commands.data.storage.query", new Object[]{Component.translationArg(this.id), NbtUtils.toPrettyComponent($$0)});
   }

   @Override
   public Component getPrintSuccess(NbtPath $$0, double $$1, int $$2) {
      return Component.translatable(
         "commands.data.storage.get", new Object[]{$$0.asString(), Component.translationArg(this.id), String.format(Locale.ROOT, "%.2f", $$1), $$2}
      );
   }
}
