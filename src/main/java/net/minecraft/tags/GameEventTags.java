package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
   public static final net.minecraft.tags.TagKey<GameEvent> VIBRATIONS = create("vibrations");
   public static final net.minecraft.tags.TagKey<GameEvent> WARDEN_CAN_LISTEN = create("warden_can_listen");
   public static final net.minecraft.tags.TagKey<GameEvent> SHRIEKER_CAN_LISTEN = create("shrieker_can_listen");
   public static final net.minecraft.tags.TagKey<GameEvent> IGNORE_VIBRATIONS_SNEAKING = create("ignore_vibrations_sneaking");
   public static final net.minecraft.tags.TagKey<GameEvent> ALLAY_CAN_LISTEN = create("allay_can_listen");

   private static net.minecraft.tags.TagKey<GameEvent> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.GAME_EVENT, Identifier.withDefaultNamespace($$0));
   }
}
