/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

public enum AdvancementType implements StringRepresentable
{
    TASK("task", ChatFormatting.GREEN),
    CHALLENGE("challenge", ChatFormatting.DARK_PURPLE),
    GOAL("goal", ChatFormatting.GREEN);

    public static final Codec<AdvancementType> CODEC;
    private final String name;
    private final ChatFormatting chatColor;
    private final Component displayName;

    private AdvancementType(String $$0, ChatFormatting $$1) {
        this.name = $$0;
        this.chatColor = $$1;
        this.displayName = Component.translatable("advancements.toast." + $$0);
    }

    public ChatFormatting getChatColor() {
        return this.chatColor;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public MutableComponent createAnnouncement(AdvancementHolder $$0, ServerPlayer $$1) {
        return Component.translatable("chat.type.advancement." + this.name, $$1.getDisplayName(), Advancement.name($$0));
    }

    static {
        CODEC = StringRepresentable.fromEnum(AdvancementType::values);
    }
}

