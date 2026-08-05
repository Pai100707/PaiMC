/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.server.dialog;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.server.dialog.ConfirmationDialog;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogListDialog;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.ServerLinksDialog;

public class DialogTypes {
    public static MapCodec<? extends Dialog> bootstrap(Registry<MapCodec<? extends Dialog>> $$0) {
        Registry.register($$0, "notice", NoticeDialog.MAP_CODEC);
        Registry.register($$0, "server_links", ServerLinksDialog.MAP_CODEC);
        Registry.register($$0, "dialog_list", DialogListDialog.MAP_CODEC);
        Registry.register($$0, "multi_action", MultiActionDialog.MAP_CODEC);
        return Registry.register($$0, "confirmation", ConfirmationDialog.MAP_CODEC);
    }
}

