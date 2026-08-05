/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 */
package net.minecraft.server.packs;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackSource;

public record PackLocationInfo(String id, Component title, PackSource source, Optional<KnownPack> knownPackInfo) {
    public Component createChatLink(boolean $$0, Component $$1) {
        return ComponentUtils.wrapInSquareBrackets(this.source.decorate(Component.literal(this.id))).withStyle($$2 -> $$2.withColor($$0 ? ChatFormatting.GREEN : ChatFormatting.RED).withInsertion(StringArgumentType.escapeIfRequired((String)this.id)).withHoverEvent(new HoverEvent.ShowText(Component.empty().append(this.title).append("\n").append($$1))));
    }
}

