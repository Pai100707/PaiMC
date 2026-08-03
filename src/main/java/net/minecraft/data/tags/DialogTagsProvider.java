package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.tags.DialogTags;

public class DialogTagsProvider extends KeyTagProvider<Dialog> {
   public DialogTagsProvider(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      super($$0, Registries.DIALOG, $$1);
   }

   @Override
   protected void addTags(Provider $$0) {
      this.tag(DialogTags.PAUSE_SCREEN_ADDITIONS);
      this.tag(DialogTags.QUICK_ACTIONS);
   }
}
