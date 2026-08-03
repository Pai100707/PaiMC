package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public class Passthrough implements RuleBlockEntityModifier {
   public static final Passthrough INSTANCE = new Passthrough();
   public static final MapCodec<Passthrough> CODEC = MapCodec.unit(INSTANCE);

   @Nullable
   @Override
   public CompoundTag apply(RandomSource $$0, @Nullable CompoundTag $$1) {
      return $$1;
   }

   @Override
   public RuleBlockEntityModifierType<?> getType() {
      return RuleBlockEntityModifierType.PASSTHROUGH;
   }
}
