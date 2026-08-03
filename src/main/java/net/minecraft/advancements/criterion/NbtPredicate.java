package net.minecraft.advancements.criterion;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record NbtPredicate(CompoundTag tag) {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<NbtPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(NbtPredicate::new, NbtPredicate::tag);
   public static final StreamCodec<ByteBuf, NbtPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(NbtPredicate::new, NbtPredicate::tag);
   public static final String SELECTED_ITEM_TAG = "SelectedItem";

   public boolean matches(DataComponentGetter $$0) {
      CustomData $$1 = (CustomData)$$0.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
      return $$1.matchedBy(this.tag);
   }

   public boolean matches(Entity $$0) {
      return this.matches(getEntityTagToCompare($$0));
   }

   public boolean matches(@Nullable Tag $$0) {
      return $$0 != null && NbtUtils.compareNbt(this.tag, $$0, true);
   }

   public static CompoundTag getEntityTagToCompare(Entity $$0) {
      ScopedCollector $$1 = new ScopedCollector($$0.problemPath(), LOGGER);

      CompoundTag var7;
      try {
         TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0.registryAccess());
         $$0.saveWithoutId($$2);
         if ($$0 instanceof Player $$3) {
            ItemStack $$4 = $$3.getInventory().getSelectedItem();
            if (!$$4.isEmpty()) {
               $$2.store("SelectedItem", ItemStack.CODEC, $$4);
            }
         }

         var7 = $$2.buildResult();
      } catch (Throwable var6) {
         try {
            $$1.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      $$1.close();
      return var7;
   }
}
