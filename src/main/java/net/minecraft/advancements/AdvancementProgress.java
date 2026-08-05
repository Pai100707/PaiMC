package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;

public class AdvancementProgress implements Comparable<net.minecraft.advancements.AdvancementProgress> {
   private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
   private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(OBTAINED_TIME_FORMAT)
      .xmap(Instant::from, $$0 -> $$0.atZone(ZoneId.systemDefault()));
   private static final Codec<Map<String, net.minecraft.advancements.CriterionProgress>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, OBTAINED_TIME_CODEC)
      .xmap(
         $$0 -> Util.mapValues($$0, net.minecraft.advancements.CriterionProgress::new),
         $$0 -> $$0.entrySet()
            .stream()
            .filter($$0x -> ((net.minecraft.advancements.CriterionProgress)$$0x.getValue()).isDone())
            .collect(
               Collectors.toMap(Entry::getKey, $$0x -> Objects.requireNonNull(((net.minecraft.advancements.CriterionProgress)$$0x.getValue()).getObtained()))
            )
      );
   public static final Codec<net.minecraft.advancements.AdvancementProgress> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter($$0x -> $$0x.criteria),
            Codec.BOOL.fieldOf("done").orElse(true).forGetter(net.minecraft.advancements.AdvancementProgress::isDone)
         )
         .apply($$0, ($$0x, $$1) -> new net.minecraft.advancements.AdvancementProgress(new HashMap<>($$0x)))
   );
   private final Map<String, net.minecraft.advancements.CriterionProgress> criteria;
   private net.minecraft.advancements.AdvancementRequirements requirements = net.minecraft.advancements.AdvancementRequirements.EMPTY;

   private AdvancementProgress(Map<String, net.minecraft.advancements.CriterionProgress> $$0) {
      this.criteria = $$0;
   }

   public AdvancementProgress() {
      this.criteria = Maps.newHashMap();
   }

   public void update(net.minecraft.advancements.AdvancementRequirements $$0) {
      Set<String> $$1 = $$0.names();
      this.criteria.entrySet().removeIf($$1x -> !$$1.contains($$1x.getKey()));

      for (String $$2 : $$1) {
         this.criteria.putIfAbsent($$2, new net.minecraft.advancements.CriterionProgress());
      }

      this.requirements = $$0;
   }

   public boolean isDone() {
      return this.requirements.test(this::isCriterionDone);
   }

   public boolean hasProgress() {
      for (net.minecraft.advancements.CriterionProgress $$0 : this.criteria.values()) {
         if ($$0.isDone()) {
            return true;
         }
      }

      return false;
   }

   public boolean grantProgress(String $$0) {
      net.minecraft.advancements.CriterionProgress $$1 = this.criteria.get($$0);
      if ($$1 != null && !$$1.isDone()) {
         $$1.grant();
         return true;
      } else {
         return false;
      }
   }

   public boolean revokeProgress(String $$0) {
      net.minecraft.advancements.CriterionProgress $$1 = this.criteria.get($$0);
      if ($$1 != null && $$1.isDone()) {
         $$1.revoke();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + this.requirements + "}";
   }

   public void serializeToNetwork(FriendlyByteBuf $$0) {
      $$0.writeMap(this.criteria, FriendlyByteBuf::writeUtf, ($$0x, $$1) -> $$1.serializeToNetwork($$0x));
   }

   public static net.minecraft.advancements.AdvancementProgress fromNetwork(FriendlyByteBuf $$0) {
      Map<String, net.minecraft.advancements.CriterionProgress> $$1 = $$0.readMap(
         FriendlyByteBuf::readUtf, net.minecraft.advancements.CriterionProgress::fromNetwork
      );
      return new net.minecraft.advancements.AdvancementProgress($$1);
   }

   
   public net.minecraft.advancements.CriterionProgress getCriterion(String $$0) {
      return this.criteria.get($$0);
   }

   private boolean isCriterionDone(String $$0) {
      net.minecraft.advancements.CriterionProgress $$1 = this.getCriterion($$0);
      return $$1 != null && $$1.isDone();
   }

   public float getPercent() {
      if (this.criteria.isEmpty()) {
         return 0.0F;
      } else {
         float $$0 = this.requirements.size();
         float $$1 = this.countCompletedRequirements();
         return $$1 / $$0;
      }
   }

   
   public Component getProgressText() {
      if (this.criteria.isEmpty()) {
         return null;
      } else {
         int $$0 = this.requirements.size();
         if ($$0 <= 1) {
            return null;
         } else {
            int $$1 = this.countCompletedRequirements();
            return Component.translatable("advancements.progress", new Object[]{$$1, $$0});
         }
      }
   }

   private int countCompletedRequirements() {
      return this.requirements.count(this::isCriterionDone);
   }

   public Iterable<String> getRemainingCriteria() {
      List<String> $$0 = Lists.newArrayList();

      for (Entry<String, net.minecraft.advancements.CriterionProgress> $$1 : this.criteria.entrySet()) {
         if (!$$1.getValue().isDone()) {
            $$0.add($$1.getKey());
         }
      }

      return $$0;
   }

   public Iterable<String> getCompletedCriteria() {
      List<String> $$0 = Lists.newArrayList();

      for (Entry<String, net.minecraft.advancements.CriterionProgress> $$1 : this.criteria.entrySet()) {
         if ($$1.getValue().isDone()) {
            $$0.add($$1.getKey());
         }
      }

      return $$0;
   }

   
   public Instant getFirstProgressDate() {
      return this.criteria
         .values()
         .stream()
         .map(net.minecraft.advancements.CriterionProgress::getObtained)
         .filter(Objects::nonNull)
         .min(Comparator.naturalOrder())
         .orElse(null);
   }

   public int compareTo(net.minecraft.advancements.AdvancementProgress $$0) {
      Instant $$1 = this.getFirstProgressDate();
      Instant $$2 = $$0.getFirstProgressDate();
      if ($$1 == null && $$2 != null) {
         return 1;
      } else if ($$1 != null && $$2 == null) {
         return -1;
      } else {
         return $$1 == null && $$2 == null ? 0 : $$1.compareTo($$2);
      }
   }
}
