package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;

public record StorageValue(Identifier storage, NbtPath path) implements NumberProvider {
   public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Identifier.CODEC.fieldOf("storage").forGetter(StorageValue::storage), NbtPath.CODEC.fieldOf("path").forGetter(StorageValue::path))
         .apply($$0, StorageValue::new)
   );

   @Override
   public LootNumberProviderType getType() {
      return NumberProviders.STORAGE;
   }

   private Number getNumericTag(LootContext $$0, Number $$1) {
      CompoundTag $$2 = $$0.getLevel().getServer().getCommandStorage().get(this.storage);

      try {
         List<Tag> $$3 = this.path.get($$2);
         if ($$3.size() == 1 && $$3.getFirst() instanceof NumericTag $$4) {
            return $$4.box();
         }
      } catch (CommandSyntaxException var7) {
      }

      return $$1;
   }

   @Override
   public float getFloat(LootContext $$0) {
      return this.getNumericTag($$0, 0.0F).floatValue();
   }

   @Override
   public int getInt(LootContext $$0) {
      return this.getNumericTag($$0, 0).intValue();
   }
}
