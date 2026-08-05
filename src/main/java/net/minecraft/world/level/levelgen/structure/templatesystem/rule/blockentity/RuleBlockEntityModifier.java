package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

public interface RuleBlockEntityModifier {
   Codec<RuleBlockEntityModifier> CODEC = BuiltInRegistries.RULE_BLOCK_ENTITY_MODIFIER
      .byNameCodec()
      .dispatch(RuleBlockEntityModifier::getType, RuleBlockEntityModifierType::codec);

   
   CompoundTag apply(RandomSource var1, CompoundTag var2);

   RuleBlockEntityModifierType<?> getType();
}
