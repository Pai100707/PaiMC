/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Collection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

public class CommonComponents {
    public static final Component EMPTY = Component.empty();
    public static final Component OPTION_ON = Component.translatable("options.on");
    public static final Component OPTION_OFF = Component.translatable("options.off");
    public static final Component GUI_DONE = Component.translatable("gui.done");
    public static final Component GUI_CANCEL = Component.translatable("gui.cancel");
    public static final Component GUI_YES = Component.translatable("gui.yes");
    public static final Component GUI_NO = Component.translatable("gui.no");
    public static final Component GUI_OK = Component.translatable("gui.ok");
    public static final Component GUI_PROCEED = Component.translatable("gui.proceed");
    public static final Component GUI_CONTINUE = Component.translatable("gui.continue");
    public static final Component GUI_BACK = Component.translatable("gui.back");
    public static final Component GUI_TO_TITLE = Component.translatable("gui.toTitle");
    public static final Component GUI_ACKNOWLEDGE = Component.translatable("gui.acknowledge");
    public static final Component GUI_OPEN_IN_BROWSER = Component.translatable("chat.link.open");
    public static final Component GUI_COPY_TO_CLIPBOARD = Component.translatable("chat.copy");
    public static final Component GUI_COPY_LINK_TO_CLIPBOARD = Component.translatable("gui.copy_link_to_clipboard");
    public static final Component GUI_DISCONNECT = Component.translatable("menu.disconnect");
    public static final Component GUI_RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
    public static final Component TRANSFER_CONNECT_FAILED = Component.translatable("connect.failed.transfer");
    public static final Component CONNECT_FAILED = Component.translatable("connect.failed");
    public static final Component NEW_LINE = Component.literal("\n");
    public static final Component NARRATION_SEPARATOR = Component.literal(". ");
    public static final Component ELLIPSIS = Component.literal("...");
    public static final Component SPACE = CommonComponents.space();

    public static MutableComponent space() {
        return Component.literal(" ");
    }

    public static MutableComponent days(long $$0) {
        return Component.translatable("gui.days", $$0);
    }

    public static MutableComponent hours(long $$0) {
        return Component.translatable("gui.hours", $$0);
    }

    public static MutableComponent minutes(long $$0) {
        return Component.translatable("gui.minutes", $$0);
    }

    public static Component optionStatus(boolean $$0) {
        return $$0 ? OPTION_ON : OPTION_OFF;
    }

    public static Component disconnectButtonLabel(boolean $$0) {
        return $$0 ? GUI_RETURN_TO_MENU : GUI_DISCONNECT;
    }

    public static MutableComponent optionStatus(Component $$0, boolean $$1) {
        return Component.translatable($$1 ? "options.on.composed" : "options.off.composed", $$0);
    }

    public static MutableComponent optionNameValue(Component $$0, Component $$1) {
        return Component.translatable("options.generic_value", $$0, $$1);
    }

    public static MutableComponent joinForNarration(Component ... $$0) {
        MutableComponent $$1 = Component.empty();
        for (int $$2 = 0; $$2 < $$0.length; ++$$2) {
            $$1.append($$0[$$2]);
            if ($$2 == $$0.length - 1) continue;
            $$1.append(NARRATION_SEPARATOR);
        }
        return $$1;
    }

    public static Component joinLines(Component ... $$0) {
        return CommonComponents.joinLines(Arrays.asList($$0));
    }

    public static Component joinLines(Collection<? extends Component> $$0) {
        return ComponentUtils.formatList($$0, NEW_LINE);
    }
}

