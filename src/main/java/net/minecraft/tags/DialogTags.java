package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;

public class DialogTags {
   public static final net.minecraft.tags.TagKey<Dialog> PAUSE_SCREEN_ADDITIONS = create("pause_screen_additions");
   public static final net.minecraft.tags.TagKey<Dialog> QUICK_ACTIONS = create("quick_actions");

   private DialogTags() {
   }

   private static net.minecraft.tags.TagKey<Dialog> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.DIALOG, Identifier.withDefaultNamespace($$0));
   }
}
