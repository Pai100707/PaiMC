package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;

public record JukeboxPlayablePredicate(Optional<net.minecraft.core.HolderSet<JukeboxSong>> song) implements SingleComponentItemPredicate<JukeboxPlayable> {
   public static final Codec<JukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.core.RegistryCodecs.homogeneousList(Registries.JUKEBOX_SONG).optionalFieldOf("song").forGetter(JukeboxPlayablePredicate::song)
         )
         .apply($$0, JukeboxPlayablePredicate::new)
   );

   public DataComponentType<JukeboxPlayable> componentType() {
      return DataComponents.JUKEBOX_PLAYABLE;
   }

   public boolean matches(JukeboxPlayable $$0) {
      if (!this.song.isPresent()) {
         return true;
      } else {
         boolean $$1 = false;

         for (net.minecraft.core.Holder<JukeboxSong> $$2 : this.song.get()) {
            Optional<ResourceKey<JukeboxSong>> $$3 = $$2.unwrapKey();
            if (!$$3.isEmpty() && $$3.equals($$0.song().key())) {
               $$1 = true;
               break;
            }
         }

         return $$1;
      }
   }

   public static JukeboxPlayablePredicate any() {
      return new JukeboxPlayablePredicate(Optional.empty());
   }
}
