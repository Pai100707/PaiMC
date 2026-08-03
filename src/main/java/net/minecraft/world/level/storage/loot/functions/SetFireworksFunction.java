package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFireworksFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetFireworksFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ListOperation.StandAlone.codec(FireworkExplosion.CODEC, 256).optionalFieldOf("explosions").forGetter($$0x -> $$0x.explosions),
               ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration").forGetter($$0x -> $$0x.flightDuration)
            )
         )
         .apply($$0, SetFireworksFunction::new)
   );
   public static final Fireworks DEFAULT_VALUE = new Fireworks(0, List.of());
   private final Optional<ListOperation.StandAlone<FireworkExplosion>> explosions;
   private final Optional<Integer> flightDuration;

   protected SetFireworksFunction(List<LootItemCondition> $$0, Optional<ListOperation.StandAlone<FireworkExplosion>> $$1, Optional<Integer> $$2) {
      super($$0);
      this.explosions = $$1;
      this.flightDuration = $$2;
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.update(DataComponents.FIREWORKS, DEFAULT_VALUE, this::apply);
      return $$0;
   }

   private Fireworks apply(Fireworks $$0) {
      return new Fireworks(
         this.flightDuration.orElseGet($$0::flightDuration), this.explosions.<List>map($$1 -> $$1.apply($$0.explosions())).orElse($$0.explosions())
      );
   }

   @Override
   public LootItemFunctionType<SetFireworksFunction> getType() {
      return LootItemFunctions.SET_FIREWORKS;
   }
}
