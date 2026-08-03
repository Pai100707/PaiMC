package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class AdventureModePredicate {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<net.minecraft.world.item.AdventureModePredicate> CODEC = ExtraCodecs.compactListCodec(
         BlockPredicate.CODEC, ExtraCodecs.nonEmptyList(BlockPredicate.CODEC.listOf())
      )
      .xmap(net.minecraft.world.item.AdventureModePredicate::new, $$0 -> $$0.predicates);
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(
      BlockPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()), $$0 -> $$0.predicates, net.minecraft.world.item.AdventureModePredicate::new
   );
   public static final Component CAN_BREAK_HEADER = Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY);
   public static final Component CAN_PLACE_HEADER = Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY);
   private static final Component UNKNOWN_USE = Component.translatable("item.canUse.unknown").withStyle(ChatFormatting.GRAY);
   private final List<BlockPredicate> predicates;
   @Nullable
   private List<Component> cachedTooltip;
   @Nullable
   private BlockInWorld lastCheckedBlock;
   private boolean lastResult;
   private boolean checksBlockEntity;

   public AdventureModePredicate(List<BlockPredicate> $$0) {
      this.predicates = $$0;
   }

   private static boolean areSameBlocks(BlockInWorld $$0, @Nullable BlockInWorld $$1, boolean $$2) {
      if ($$1 == null || $$0.getState() != $$1.getState()) {
         return false;
      } else if (!$$2) {
         return true;
      } else if ($$0.getEntity() == null && $$1.getEntity() == null) {
         return true;
      } else if ($$0.getEntity() != null && $$1.getEntity() != null) {
         ScopedCollector $$3 = new ScopedCollector(LOGGER);

         boolean var7;
         try {
            RegistryAccess $$4 = $$0.getLevel().registryAccess();
            CompoundTag $$5 = saveBlockEntity($$0.getEntity(), $$4, $$3);
            CompoundTag $$6 = saveBlockEntity($$1.getEntity(), $$4, $$3);
            var7 = Objects.equals($$5, $$6);
         } catch (Throwable var9) {
            try {
               $$3.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         $$3.close();
         return var7;
      } else {
         return false;
      }
   }

   private static CompoundTag saveBlockEntity(BlockEntity $$0, RegistryAccess $$1, ProblemReporter $$2) {
      TagValueOutput $$3 = TagValueOutput.createWithContext($$2.forChild($$0.problemPath()), $$1);
      $$0.saveWithId($$3);
      return $$3.buildResult();
   }

   public boolean test(BlockInWorld $$0) {
      if (areSameBlocks($$0, this.lastCheckedBlock, this.checksBlockEntity)) {
         return this.lastResult;
      } else {
         this.lastCheckedBlock = $$0;
         this.checksBlockEntity = false;

         for (BlockPredicate $$1 : this.predicates) {
            if ($$1.matches($$0)) {
               this.checksBlockEntity = this.checksBlockEntity | $$1.requiresNbt();
               this.lastResult = true;
               return true;
            }
         }

         this.lastResult = false;
         return false;
      }
   }

   private List<Component> tooltip() {
      if (this.cachedTooltip == null) {
         this.cachedTooltip = computeTooltip(this.predicates);
      }

      return this.cachedTooltip;
   }

   public void addToTooltip(Consumer<Component> $$0) {
      this.tooltip().forEach($$0);
   }

   private static List<Component> computeTooltip(List<BlockPredicate> $$0) {
      for (BlockPredicate $$1 : $$0) {
         if ($$1.blocks().isEmpty()) {
            return List.of(UNKNOWN_USE);
         }
      }

      return $$0.stream()
         .flatMap($$0x -> ((HolderSet)$$0x.blocks().orElseThrow()).stream())
         .distinct()
         .map($$0x -> ((Block)$$0x.value()).getName().withStyle(ChatFormatting.DARK_GRAY))
         .toList();
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return $$0 instanceof net.minecraft.world.item.AdventureModePredicate $$1 ? this.predicates.equals($$1.predicates) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.predicates.hashCode();
   }

   @Override
   public String toString() {
      return "AdventureModePredicate{predicates=" + this.predicates + "}";
   }
}
