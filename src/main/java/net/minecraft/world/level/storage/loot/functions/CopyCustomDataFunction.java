package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.apache.commons.lang3.mutable.MutableObject;

public class CopyCustomDataFunction extends LootItemConditionalFunction {
   public static final MapCodec<CopyCustomDataFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               NbtProviders.CODEC.fieldOf("source").forGetter($$0x -> $$0x.source),
               CopyCustomDataFunction.CopyOperation.CODEC.listOf().fieldOf("ops").forGetter($$0x -> $$0x.operations)
            )
         )
         .apply($$0, CopyCustomDataFunction::new)
   );
   private final NbtProvider source;
   private final List<CopyCustomDataFunction.CopyOperation> operations;

   CopyCustomDataFunction(List<LootItemCondition> $$0, NbtProvider $$1, List<CopyCustomDataFunction.CopyOperation> $$2) {
      super($$0);
      this.source = $$1;
      this.operations = List.copyOf($$2);
   }

   @Override
   public LootItemFunctionType<CopyCustomDataFunction> getType() {
      return LootItemFunctions.COPY_CUSTOM_DATA;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.source.getReferencedContextParams();
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      Tag $$2 = this.source.get($$1);
      if ($$2 == null) {
         return $$0;
      } else {
         MutableObject<CompoundTag> $$3 = new MutableObject();
         Supplier<Tag> $$4 = () -> {
            if ($$3.get() == null) {
               $$3.setValue(((CustomData)$$0.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag());
            }

            return (Tag)$$3.get();
         };
         this.operations.forEach($$2x -> $$2x.apply($$4, $$2));
         CompoundTag $$5 = (CompoundTag)$$3.get();
         if ($$5 != null) {
            CustomData.set(DataComponents.CUSTOM_DATA, $$0, $$5);
         }

         return $$0;
      }
   }

   @Deprecated
   public static CopyCustomDataFunction.Builder copyData(NbtProvider $$0) {
      return new CopyCustomDataFunction.Builder($$0);
   }

   public static CopyCustomDataFunction.Builder copyData(LootContext.EntityTarget $$0) {
      return new CopyCustomDataFunction.Builder(ContextNbtProvider.forContextEntity($$0));
   }

   public static class Builder extends LootItemConditionalFunction.Builder<CopyCustomDataFunction.Builder> {
      private final NbtProvider source;
      private final List<CopyCustomDataFunction.CopyOperation> ops = Lists.newArrayList();

      Builder(NbtProvider $$0) {
         this.source = $$0;
      }

      public CopyCustomDataFunction.Builder copy(String $$0, String $$1, CopyCustomDataFunction.MergeStrategy $$2) {
         try {
            this.ops.add(new CopyCustomDataFunction.CopyOperation(NbtPath.of($$0), NbtPath.of($$1), $$2));
            return this;
         } catch (CommandSyntaxException var5) {
            throw new IllegalArgumentException(var5);
         }
      }

      public CopyCustomDataFunction.Builder copy(String $$0, String $$1) {
         return this.copy($$0, $$1, CopyCustomDataFunction.MergeStrategy.REPLACE);
      }

      protected CopyCustomDataFunction.Builder getThis() {
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new CopyCustomDataFunction(this.getConditions(), this.source, this.ops);
      }
   }

   record CopyOperation(NbtPath sourcePath, NbtPath targetPath, CopyCustomDataFunction.MergeStrategy op) {
      public static final Codec<CopyCustomDataFunction.CopyOperation> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               NbtPath.CODEC.fieldOf("source").forGetter(CopyCustomDataFunction.CopyOperation::sourcePath),
               NbtPath.CODEC.fieldOf("target").forGetter(CopyCustomDataFunction.CopyOperation::targetPath),
               CopyCustomDataFunction.MergeStrategy.CODEC.fieldOf("op").forGetter(CopyCustomDataFunction.CopyOperation::op)
            )
            .apply($$0, CopyCustomDataFunction.CopyOperation::new)
      );

      public void apply(Supplier<Tag> $$0, Tag $$1) {
         try {
            List<Tag> $$2 = this.sourcePath.get($$1);
            if (!$$2.isEmpty()) {
               this.op.merge($$0.get(), this.targetPath, $$2);
            }
         } catch (CommandSyntaxException var4) {
         }
      }
   }

   public static enum MergeStrategy implements StringRepresentable {
      REPLACE("replace") {
         @Override
         public void merge(Tag $$0, NbtPath $$1, List<Tag> $$2) throws CommandSyntaxException {
            $$1.set($$0, (Tag)Iterables.getLast($$2));
         }
      },
      APPEND("append") {
         @Override
         public void merge(Tag $$0, NbtPath $$1, List<Tag> $$2) throws CommandSyntaxException {
            List<Tag> $$3 = $$1.getOrCreate($$0, ListTag::new);
            $$3.forEach($$1x -> {
               if ($$1x instanceof ListTag) {
                  $$2.forEach($$1xx -> ((ListTag)$$1x).add($$1xx.copy()));
               }
            });
         }
      },
      MERGE("merge") {
         @Override
         public void merge(Tag $$0, NbtPath $$1, List<Tag> $$2) throws CommandSyntaxException {
            List<Tag> $$3 = $$1.getOrCreate($$0, CompoundTag::new);
            $$3.forEach($$1x -> {
               if ($$1x instanceof CompoundTag) {
                  $$2.forEach($$1xx -> {
                     if ($$1xx instanceof CompoundTag) {
                        ((CompoundTag)$$1x).merge((CompoundTag)$$1xx);
                     }
                  });
               }
            });
         }
      };

      public static final Codec<CopyCustomDataFunction.MergeStrategy> CODEC = StringRepresentable.fromEnum(CopyCustomDataFunction.MergeStrategy::values);
      private final String name;

      public abstract void merge(Tag var1, NbtPath var2, List<Tag> var3) throws CommandSyntaxException;

      MergeStrategy(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
