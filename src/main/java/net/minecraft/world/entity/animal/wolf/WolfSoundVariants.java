package net.minecraft.world.entity.animal.wolf;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

public class WolfSoundVariants {
   public static final ResourceKey<WolfSoundVariant> CLASSIC = createKey(WolfSoundVariants.SoundSet.CLASSIC);
   public static final ResourceKey<WolfSoundVariant> PUGLIN = createKey(WolfSoundVariants.SoundSet.PUGLIN);
   public static final ResourceKey<WolfSoundVariant> SAD = createKey(WolfSoundVariants.SoundSet.SAD);
   public static final ResourceKey<WolfSoundVariant> ANGRY = createKey(WolfSoundVariants.SoundSet.ANGRY);
   public static final ResourceKey<WolfSoundVariant> GRUMPY = createKey(WolfSoundVariants.SoundSet.GRUMPY);
   public static final ResourceKey<WolfSoundVariant> BIG = createKey(WolfSoundVariants.SoundSet.BIG);
   public static final ResourceKey<WolfSoundVariant> CUTE = createKey(WolfSoundVariants.SoundSet.CUTE);

   private static ResourceKey<WolfSoundVariant> createKey(WolfSoundVariants.SoundSet $$0) {
      return ResourceKey.create(Registries.WOLF_SOUND_VARIANT, Identifier.withDefaultNamespace($$0.getIdentifier()));
   }

   public static void bootstrap(BootstrapContext<WolfSoundVariant> $$0) {
      register($$0, CLASSIC, WolfSoundVariants.SoundSet.CLASSIC);
      register($$0, PUGLIN, WolfSoundVariants.SoundSet.PUGLIN);
      register($$0, SAD, WolfSoundVariants.SoundSet.SAD);
      register($$0, ANGRY, WolfSoundVariants.SoundSet.ANGRY);
      register($$0, GRUMPY, WolfSoundVariants.SoundSet.GRUMPY);
      register($$0, BIG, WolfSoundVariants.SoundSet.BIG);
      register($$0, CUTE, WolfSoundVariants.SoundSet.CUTE);
   }

   private static void register(BootstrapContext<WolfSoundVariant> $$0, ResourceKey<WolfSoundVariant> $$1, WolfSoundVariants.SoundSet $$2) {
      $$0.register($$1, (WolfSoundVariant)SoundEvents.WOLF_SOUNDS.get($$2));
   }

   public static Holder<WolfSoundVariant> pickRandomSoundVariant(RegistryAccess $$0, RandomSource $$1) {
      return (Holder<WolfSoundVariant>)$$0.lookupOrThrow(Registries.WOLF_SOUND_VARIANT).getRandom($$1).orElseThrow();
   }

   public static enum SoundSet {
      CLASSIC("classic", ""),
      PUGLIN("puglin", "_puglin"),
      SAD("sad", "_sad"),
      ANGRY("angry", "_angry"),
      GRUMPY("grumpy", "_grumpy"),
      BIG("big", "_big"),
      CUTE("cute", "_cute");

      private final String identifier;
      private final String soundEventSuffix;

      private SoundSet(final String $$0, final String $$1) {
         this.identifier = $$0;
         this.soundEventSuffix = $$1;
      }

      public String getIdentifier() {
         return this.identifier;
      }

      public String getSoundEventSuffix() {
         return this.soundEventSuffix;
      }
   }
}
