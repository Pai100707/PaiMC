package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

public enum AdvancementType implements StringRepresentable {
   TASK("task", ChatFormatting.GREEN),
   CHALLENGE("challenge", ChatFormatting.DARK_PURPLE),
   GOAL("goal", ChatFormatting.GREEN);

   public static final Codec<net.minecraft.advancements.AdvancementType> CODEC = StringRepresentable.fromEnum(
      net.minecraft.advancements.AdvancementType::values
   );
   private final String name;
   private final ChatFormatting chatColor;
   private final Component displayName;

   private AdvancementType(final String $$0, final ChatFormatting $$1) {
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

   public String getSerializedName() {
      return this.name;
   }

   public MutableComponent createAnnouncement(net.minecraft.advancements.AdvancementHolder $$0, ServerPlayer $$1) {
      return Component.translatable("chat.type.advancement." + this.name, new Object[]{$$1.getDisplayName(), net.minecraft.advancements.Advancement.name($$0)});
   }
}
