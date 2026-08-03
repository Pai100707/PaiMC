package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public class AppendStatic implements RuleBlockEntityModifier {
   public static final MapCodec<AppendStatic> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(CompoundTag.CODEC.fieldOf("data").forGetter($$0x -> $$0x.tag)).apply($$0, AppendStatic::new)
   );
   private final CompoundTag tag;

   public AppendStatic(CompoundTag $$0) {
      this.tag = $$0;
   }

   @Override
   public CompoundTag apply(RandomSource $$0, @Nullable CompoundTag $$1) {
      return $$1 == null ? this.tag.copy() : $$1.merge(this.tag);
   }

   @Override
   public RuleBlockEntityModifierType<?> getType() {
      return RuleBlockEntityModifierType.APPEND_STATIC;
   }
}
