package net.minecraft.world.entity.ai.attributes;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class Attribute {
   public static final Codec<Holder<Attribute>> CODEC = BuiltInRegistries.ATTRIBUTE.holderByNameCodec();
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Attribute>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE);
   private final double defaultValue;
   private boolean syncable;
   private final String descriptionId;
   private Attribute.Sentiment sentiment = Attribute.Sentiment.POSITIVE;

   protected Attribute(String $$0, double $$1) {
      this.defaultValue = $$1;
      this.descriptionId = $$0;
   }

   public double getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isClientSyncable() {
      return this.syncable;
   }

   public Attribute setSyncable(boolean $$0) {
      this.syncable = $$0;
      return this;
   }

   public Attribute setSentiment(Attribute.Sentiment $$0) {
      this.sentiment = $$0;
      return this;
   }

   public double sanitizeValue(double $$0) {
      return $$0;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   public ChatFormatting getStyle(boolean $$0) {
      return this.sentiment.getStyle($$0);
   }

   public static enum Sentiment {
      POSITIVE,
      NEUTRAL,
      NEGATIVE;

      public ChatFormatting getStyle(boolean $$0) {
         return switch (this) {
            case POSITIVE -> $$0 ? ChatFormatting.BLUE : ChatFormatting.RED;
            case NEUTRAL -> ChatFormatting.GRAY;
            case NEGATIVE -> $$0 ? ChatFormatting.RED : ChatFormatting.BLUE;
         };
      }
   }
}
