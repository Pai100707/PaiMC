package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class HorseBodyArmorItemFix extends NamedEntityWriteReadFix {
   private final String previousBodyArmorTag;
   private final boolean clearArmorItems;

   public HorseBodyArmorItemFix(Schema $$0, String $$1, String $$2, boolean $$3) {
      super($$0, true, "Horse armor fix for " + $$1, References.ENTITY, $$1);
      this.previousBodyArmorTag = $$2;
      this.clearArmorItems = $$3;
   }

   @Override
   protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
      Optional<? extends Dynamic<?>> $$1 = $$0.get(this.previousBodyArmorTag).result();
      if ($$1.isPresent()) {
         Dynamic<?> $$2 = (Dynamic<?>)$$1.get();
         Dynamic<T> $$3 = $$0.remove(this.previousBodyArmorTag);
         if (this.clearArmorItems) {
            $$3 = $$3.update(
               "ArmorItems", $$0x -> $$0x.createList(Streams.mapWithIndex($$0x.asStream(), ($$0xx, $$1x) -> $$1x == 2L ? $$0xx.emptyMap() : $$0xx))
            );
            $$3 = $$3.update(
               "ArmorDropChances",
               $$0x -> $$0x.createList(Streams.mapWithIndex($$0x.asStream(), ($$0xx, $$1x) -> $$1x == 2L ? $$0xx.createFloat(0.085F) : $$0xx))
            );
         }

         $$3 = $$3.set("body_armor_item", $$2);
         return $$3.set("body_armor_drop_chance", $$0.createFloat(2.0F));
      } else {
         return $$0;
      }
   }
}
