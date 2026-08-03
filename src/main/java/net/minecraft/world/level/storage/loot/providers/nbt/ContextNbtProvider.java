package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextArg;
import org.jspecify.annotations.Nullable;

public class ContextNbtProvider implements NbtProvider {
   private static final Codec<LootContextArg<Tag>> GETTER_CODEC = LootContextArg.createArgCodec(
      $$0 -> $$0.anyBlockEntity(ContextNbtProvider.BlockEntitySource::new).anyEntity(ContextNbtProvider.EntitySource::new)
   );
   public static final MapCodec<ContextNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(GETTER_CODEC.fieldOf("target").forGetter($$0x -> $$0x.source)).apply($$0, ContextNbtProvider::new)
   );
   public static final Codec<ContextNbtProvider> INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, $$0 -> $$0.source);
   private final LootContextArg<Tag> source;

   private ContextNbtProvider(LootContextArg<Tag> $$0) {
      this.source = $$0;
   }

   @Override
   public LootNbtProviderType getType() {
      return NbtProviders.CONTEXT;
   }

   @Nullable
   @Override
   public Tag get(LootContext $$0) {
      return this.source.get($$0);
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(this.source.contextParam());
   }

   public static NbtProvider forContextEntity(LootContext.EntityTarget $$0) {
      return new ContextNbtProvider(new ContextNbtProvider.EntitySource($$0.contextParam()));
   }

   record BlockEntitySource(ContextKey<? extends BlockEntity> contextParam) implements LootContextArg.Getter<BlockEntity, Tag> {
      public Tag get(BlockEntity $$0) {
         return $$0.saveWithFullMetadata($$0.getLevel().registryAccess());
      }
   }

   record EntitySource(ContextKey<? extends Entity> contextParam) implements LootContextArg.Getter<Entity, Tag> {
      public Tag get(Entity $$0) {
         return NbtPredicate.getEntityTagToCompare($$0);
      }
   }
}
