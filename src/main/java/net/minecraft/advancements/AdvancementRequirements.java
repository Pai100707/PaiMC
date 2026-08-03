package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
   public static final Codec<net.minecraft.advancements.AdvancementRequirements> CODEC = Codec.STRING
      .listOf()
      .listOf()
      .xmap(net.minecraft.advancements.AdvancementRequirements::new, net.minecraft.advancements.AdvancementRequirements::requirements);
   public static final net.minecraft.advancements.AdvancementRequirements EMPTY = new net.minecraft.advancements.AdvancementRequirements(List.of());

   public AdvancementRequirements(FriendlyByteBuf $$0) {
      this($$0.readList($$0x -> $$0x.readList(FriendlyByteBuf::readUtf)));
   }

   public void write(FriendlyByteBuf $$0) {
      $$0.writeCollection(this.requirements, ($$0x, $$1) -> $$0x.writeCollection($$1, FriendlyByteBuf::writeUtf));
   }

   public static net.minecraft.advancements.AdvancementRequirements allOf(Collection<String> $$0) {
      return new net.minecraft.advancements.AdvancementRequirements($$0.stream().map(List::of).toList());
   }

   public static net.minecraft.advancements.AdvancementRequirements anyOf(Collection<String> $$0) {
      return new net.minecraft.advancements.AdvancementRequirements(List.of(List.copyOf($$0)));
   }

   public int size() {
      return this.requirements.size();
   }

   public boolean test(Predicate<String> $$0) {
      if (this.requirements.isEmpty()) {
         return false;
      } else {
         for (List<String> $$1 : this.requirements) {
            if (!anyMatch($$1, $$0)) {
               return false;
            }
         }

         return true;
      }
   }

   public int count(Predicate<String> $$0) {
      int $$1 = 0;

      for (List<String> $$2 : this.requirements) {
         if (anyMatch($$2, $$0)) {
            $$1++;
         }
      }

      return $$1;
   }

   private static boolean anyMatch(List<String> $$0, Predicate<String> $$1) {
      for (String $$2 : $$0) {
         if ($$1.test($$2)) {
            return true;
         }
      }

      return false;
   }

   public DataResult<net.minecraft.advancements.AdvancementRequirements> validate(Set<String> $$0) {
      Set<String> $$1 = new ObjectOpenHashSet();

      for (List<String> $$2 : this.requirements) {
         if ($$2.isEmpty() && $$0.isEmpty()) {
            return DataResult.error(() -> "Requirement entry cannot be empty");
         }

         $$1.addAll($$2);
      }

      if (!$$0.equals($$1)) {
         Set<String> $$3 = Sets.difference($$0, $$1);
         Set<String> $$4 = Sets.difference($$1, $$0);
         return DataResult.error(() -> "Advancement completion requirements did not exactly match specified criteria. Missing: " + $$3 + ". Unknown: " + $$4);
      } else {
         return DataResult.success(this);
      }
   }

   public boolean isEmpty() {
      return this.requirements.isEmpty();
   }

   @Override
   public String toString() {
      return this.requirements.toString();
   }

   public Set<String> names() {
      Set<String> $$0 = new ObjectOpenHashSet();

      for (List<String> $$1 : this.requirements) {
         $$0.addAll($$1);
      }

      return $$0;
   }

   public interface Strategy {
      net.minecraft.advancements.AdvancementRequirements.Strategy AND = net.minecraft.advancements.AdvancementRequirements::allOf;
      net.minecraft.advancements.AdvancementRequirements.Strategy OR = net.minecraft.advancements.AdvancementRequirements::anyOf;

      net.minecraft.advancements.AdvancementRequirements create(Collection<String> var1);
   }
}
