package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.DataResult.Success;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.util.ProblemReporter.Problem;

public class TagValueOutput implements ValueOutput {
   private final ProblemReporter problemReporter;
   private final DynamicOps<Tag> ops;
   private final CompoundTag output;

   TagValueOutput(ProblemReporter $$0, DynamicOps<Tag> $$1, CompoundTag $$2) {
      this.problemReporter = $$0;
      this.ops = $$1;
      this.output = $$2;
   }

   public static TagValueOutput createWithContext(ProblemReporter $$0, Provider $$1) {
      return new TagValueOutput($$0, $$1.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
   }

   public static TagValueOutput createWithoutContext(ProblemReporter $$0) {
      return new TagValueOutput($$0, NbtOps.INSTANCE, new CompoundTag());
   }

   @Override
   public <T> void store(String $$0, Codec<T> $$1, T $$2) {
      switch ($$1.encodeStart(this.ops, $$2)) {
         case Success<Tag> $$3:
            this.output.put($$0, (Tag)$$3.value());
            break;
         case Error<Tag> $$4:
            this.problemReporter.report(new TagValueOutput.EncodeToFieldFailedProblem($$0, $$2, $$4));
            $$4.partialValue().ifPresent($$1x -> this.output.put($$0, $$1x));
            break;
         default:
            throw new MatchException(null, null);
      }
   }

   @Override
   public <T> void storeNullable(String $$0, Codec<T> $$1, T $$2) {
      if ($$2 != null) {
         this.store($$0, $$1, $$2);
      }
   }

   @Override
   public <T> void store(MapCodec<T> $$0, T $$1) {
      switch ($$0.encoder().encodeStart(this.ops, $$1)) {
         case Success<Tag> $$2:
            this.output.merge((CompoundTag)$$2.value());
            break;
         case Error<Tag> $$3:
            this.problemReporter.report(new TagValueOutput.EncodeToMapFailedProblem($$1, $$3));
            $$3.partialValue().ifPresent($$0x -> this.output.merge((CompoundTag)$$0x));
            break;
         default:
            throw new MatchException(null, null);
      }
   }

   @Override
   public void putBoolean(String $$0, boolean $$1) {
      this.output.putBoolean($$0, $$1);
   }

   @Override
   public void putByte(String $$0, byte $$1) {
      this.output.putByte($$0, $$1);
   }

   @Override
   public void putShort(String $$0, short $$1) {
      this.output.putShort($$0, $$1);
   }

   @Override
   public void putInt(String $$0, int $$1) {
      this.output.putInt($$0, $$1);
   }

   @Override
   public void putLong(String $$0, long $$1) {
      this.output.putLong($$0, $$1);
   }

   @Override
   public void putFloat(String $$0, float $$1) {
      this.output.putFloat($$0, $$1);
   }

   @Override
   public void putDouble(String $$0, double $$1) {
      this.output.putDouble($$0, $$1);
   }

   @Override
   public void putString(String $$0, String $$1) {
      this.output.putString($$0, $$1);
   }

   @Override
   public void putIntArray(String $$0, int[] $$1) {
      this.output.putIntArray($$0, $$1);
   }

   private ProblemReporter reporterForChild(String $$0) {
      return this.problemReporter.forChild(new FieldPathElement($$0));
   }

   @Override
   public ValueOutput child(String $$0) {
      CompoundTag $$1 = new CompoundTag();
      this.output.put($$0, $$1);
      return new TagValueOutput(this.reporterForChild($$0), this.ops, $$1);
   }

   @Override
   public ValueOutput.ValueOutputList childrenList(String $$0) {
      ListTag $$1 = new ListTag();
      this.output.put($$0, $$1);
      return new TagValueOutput.ListWrapper($$0, this.problemReporter, this.ops, $$1);
   }

   @Override
   public <T> ValueOutput.TypedOutputList<T> list(String $$0, Codec<T> $$1) {
      ListTag $$2 = new ListTag();
      this.output.put($$0, $$2);
      return new TagValueOutput.TypedListWrapper<>(this.problemReporter, $$0, this.ops, $$1, $$2);
   }

   @Override
   public void discard(String $$0) {
      this.output.remove($$0);
   }

   @Override
   public boolean isEmpty() {
      return this.output.isEmpty();
   }

   public CompoundTag buildResult() {
      return this.output;
   }

   public record EncodeToFieldFailedProblem(String name, Object value, Error<?> error) implements Problem {
      public String description() {
         return "Failed to encode value '" + this.value + "' to field '" + this.name + "': " + this.error.message();
      }
   }

   public record EncodeToListFailedProblem(String name, Object value, Error<?> error) implements Problem {
      public String description() {
         return "Failed to append value '" + this.value + "' to list '" + this.name + "': " + this.error.message();
      }
   }

   public record EncodeToMapFailedProblem(Object value, Error<?> error) implements Problem {
      public String description() {
         return "Failed to merge value '" + this.value + "' to an object: " + this.error.message();
      }
   }

   static class ListWrapper implements ValueOutput.ValueOutputList {
      private final String fieldName;
      private final ProblemReporter problemReporter;
      private final DynamicOps<Tag> ops;
      private final ListTag output;

      ListWrapper(String $$0, ProblemReporter $$1, DynamicOps<Tag> $$2, ListTag $$3) {
         this.fieldName = $$0;
         this.problemReporter = $$1;
         this.ops = $$2;
         this.output = $$3;
      }

      @Override
      public ValueOutput addChild() {
         int $$0 = this.output.size();
         CompoundTag $$1 = new CompoundTag();
         this.output.add($$1);
         return new TagValueOutput(this.problemReporter.forChild(new IndexedFieldPathElement(this.fieldName, $$0)), this.ops, $$1);
      }

      @Override
      public void discardLast() {
         this.output.removeLast();
      }

      @Override
      public boolean isEmpty() {
         return this.output.isEmpty();
      }
   }

   static class TypedListWrapper<T> implements ValueOutput.TypedOutputList<T> {
      private final ProblemReporter problemReporter;
      private final String name;
      private final DynamicOps<Tag> ops;
      private final Codec<T> codec;
      private final ListTag output;

      TypedListWrapper(ProblemReporter $$0, String $$1, DynamicOps<Tag> $$2, Codec<T> $$3, ListTag $$4) {
         this.problemReporter = $$0;
         this.name = $$1;
         this.ops = $$2;
         this.codec = $$3;
         this.output = $$4;
      }

      @Override
      public void add(T $$0) {
         switch (this.codec.encodeStart(this.ops, $$0)) {
            case Success<Tag> $$1:
               this.output.add((Tag)$$1.value());
               break;
            case Error<Tag> $$2:
               this.problemReporter.report(new TagValueOutput.EncodeToListFailedProblem(this.name, $$0, $$2));
               $$2.partialValue().ifPresent(this.output::add);
               break;
            default:
               throw new MatchException(null, null);
         }
      }

      @Override
      public boolean isEmpty() {
         return this.output.isEmpty();
      }
   }
}
