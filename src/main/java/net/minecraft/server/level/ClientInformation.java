package net.minecraft.server.level;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;

public record ClientInformation(
   String language,
   int viewDistance,
   ChatVisiblity chatVisibility,
   boolean chatColors,
   int modelCustomisation,
   HumanoidArm mainHand,
   boolean textFilteringEnabled,
   boolean allowsListing,
   ParticleStatus particleStatus
) {
   public static final int MAX_LANGUAGE_LENGTH = 16;

   public ClientInformation(FriendlyByteBuf $$0) {
      this(
         $$0.readUtf(16),
         $$0.readByte(),
         (ChatVisiblity)$$0.readEnum(ChatVisiblity.class),
         $$0.readBoolean(),
         $$0.readUnsignedByte(),
         (HumanoidArm)$$0.readEnum(HumanoidArm.class),
         $$0.readBoolean(),
         $$0.readBoolean(),
         (ParticleStatus)$$0.readEnum(ParticleStatus.class)
      );
   }

   public void write(FriendlyByteBuf $$0) {
      $$0.writeUtf(this.language);
      $$0.writeByte(this.viewDistance);
      $$0.writeEnum(this.chatVisibility);
      $$0.writeBoolean(this.chatColors);
      $$0.writeByte(this.modelCustomisation);
      $$0.writeEnum(this.mainHand);
      $$0.writeBoolean(this.textFilteringEnabled);
      $$0.writeBoolean(this.allowsListing);
      $$0.writeEnum(this.particleStatus);
   }

   public static ClientInformation createDefault() {
      return new ClientInformation("en_us", 2, ChatVisiblity.FULL, true, 0, Player.DEFAULT_MAIN_HAND, false, false, ParticleStatus.ALL);
   }
}
