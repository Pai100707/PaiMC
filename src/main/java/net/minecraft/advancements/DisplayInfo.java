package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
   public static final Codec<net.minecraft.advancements.DisplayInfo> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ItemStack.STRICT_CODEC.fieldOf("icon").forGetter(net.minecraft.advancements.DisplayInfo::getIcon),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(net.minecraft.advancements.DisplayInfo::getTitle),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(net.minecraft.advancements.DisplayInfo::getDescription),
            ResourceTexture.CODEC.optionalFieldOf("background").forGetter(net.minecraft.advancements.DisplayInfo::getBackground),
            net.minecraft.advancements.AdvancementType.CODEC
               .optionalFieldOf("frame", net.minecraft.advancements.AdvancementType.TASK)
               .forGetter(net.minecraft.advancements.DisplayInfo::getType),
            Codec.BOOL.optionalFieldOf("show_toast", true).forGetter(net.minecraft.advancements.DisplayInfo::shouldShowToast),
            Codec.BOOL.optionalFieldOf("announce_to_chat", true).forGetter(net.minecraft.advancements.DisplayInfo::shouldAnnounceChat),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(net.minecraft.advancements.DisplayInfo::isHidden)
         )
         .apply($$0, net.minecraft.advancements.DisplayInfo::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.advancements.DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(
      net.minecraft.advancements.DisplayInfo::serializeToNetwork, net.minecraft.advancements.DisplayInfo::fromNetwork
   );
   private final Component title;
   private final Component description;
   private final ItemStack icon;
   private final Optional<ResourceTexture> background;
   private final net.minecraft.advancements.AdvancementType type;
   private final boolean showToast;
   private final boolean announceChat;
   private final boolean hidden;
   private float x;
   private float y;

   public DisplayInfo(
      ItemStack $$0,
      Component $$1,
      Component $$2,
      Optional<ResourceTexture> $$3,
      net.minecraft.advancements.AdvancementType $$4,
      boolean $$5,
      boolean $$6,
      boolean $$7
   ) {
      this.title = $$1;
      this.description = $$2;
      this.icon = $$0;
      this.background = $$3;
      this.type = $$4;
      this.showToast = $$5;
      this.announceChat = $$6;
      this.hidden = $$7;
   }

   public void setLocation(float $$0, float $$1) {
      this.x = $$0;
      this.y = $$1;
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getDescription() {
      return this.description;
   }

   public ItemStack getIcon() {
      return this.icon;
   }

   public Optional<ResourceTexture> getBackground() {
      return this.background;
   }

   public net.minecraft.advancements.AdvancementType getType() {
      return this.type;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public boolean shouldShowToast() {
      return this.showToast;
   }

   public boolean shouldAnnounceChat() {
      return this.announceChat;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   private void serializeToNetwork(RegistryFriendlyByteBuf $$0) {
      ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.title);
      ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.description);
      ItemStack.STREAM_CODEC.encode($$0, this.icon);
      $$0.writeEnum(this.type);
      int $$1 = 0;
      if (this.background.isPresent()) {
         $$1 |= 1;
      }

      if (this.showToast) {
         $$1 |= 2;
      }

      if (this.hidden) {
         $$1 |= 4;
      }

      $$0.writeInt($$1);
      this.background.map(ClientAsset::id).ifPresent($$0::writeIdentifier);
      $$0.writeFloat(this.x);
      $$0.writeFloat(this.y);
   }

   private static net.minecraft.advancements.DisplayInfo fromNetwork(RegistryFriendlyByteBuf $$0) {
      Component $$1 = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
      Component $$2 = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
      ItemStack $$3 = (ItemStack)ItemStack.STREAM_CODEC.decode($$0);
      net.minecraft.advancements.AdvancementType $$4 = (net.minecraft.advancements.AdvancementType)$$0.readEnum(
         net.minecraft.advancements.AdvancementType.class
      );
      int $$5 = $$0.readInt();
      Optional<ResourceTexture> $$6 = ($$5 & 1) != 0 ? Optional.of(new ResourceTexture($$0.readIdentifier())) : Optional.empty();
      boolean $$7 = ($$5 & 2) != 0;
      boolean $$8 = ($$5 & 4) != 0;
      net.minecraft.advancements.DisplayInfo $$9 = new net.minecraft.advancements.DisplayInfo($$3, $$1, $$2, $$6, $$4, $$7, false, $$8);
      $$9.setLocation($$0.readFloat(), $$0.readFloat());
      return $$9;
   }
}
