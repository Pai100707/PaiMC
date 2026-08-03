package net.minecraft.server.dialog;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class DialogTypes {
   public static MapCodec<? extends Dialog> bootstrap(Registry<MapCodec<? extends Dialog>> $$0) {
      Registry.register($$0, "notice", NoticeDialog.MAP_CODEC);
      Registry.register($$0, "server_links", ServerLinksDialog.MAP_CODEC);
      Registry.register($$0, "dialog_list", DialogListDialog.MAP_CODEC);
      Registry.register($$0, "multi_action", MultiActionDialog.MAP_CODEC);
      return (MapCodec<? extends Dialog>)Registry.register($$0, "confirmation", ConfirmationDialog.MAP_CODEC);
   }
}
