/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class AdvancementProgress
implements Comparable<AdvancementProgress> {
    private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(OBTAINED_TIME_FORMAT).xmap(Instant::from, $$0 -> $$0.atZone(ZoneId.systemDefault()));
    private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC = Codec.unboundedMap((Codec)Codec.STRING, OBTAINED_TIME_CODEC).xmap($$0 -> Util.mapValues($$0, CriterionProgress::new), $$02 -> $$02.entrySet().stream().filter($$0 -> ((CriterionProgress)$$0.getValue()).isDone()).collect(Collectors.toMap(Map.Entry::getKey, $$0 -> Objects.requireNonNull(((CriterionProgress)$$0.getValue()).getObtained()))));
    public static final Codec<AdvancementProgress> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter($$0 -> $$0.criteria), (App)Codec.BOOL.fieldOf("done").orElse((Object)true).forGetter(AdvancementProgress::isDone)).apply((Applicative)$$02, ($$0, $$1) -> new AdvancementProgress(new HashMap<String, CriterionProgress>((Map<String, CriterionProgress>)$$0))));
    private final Map<String, CriterionProgress> criteria;
    private AdvancementRequirements requirements = AdvancementRequirements.EMPTY;

    private AdvancementProgress(Map<String, CriterionProgress> $$0) {
        this.criteria = $$0;
    }

    public AdvancementProgress() {
        this.criteria = Maps.newHashMap();
    }

    public void update(AdvancementRequirements $$0) {
        Set<String> $$12 = $$0.names();
        this.criteria.entrySet().removeIf($$1 -> !$$12.contains($$1.getKey()));
        for (String $$2 : $$12) {
            this.criteria.putIfAbsent($$2, new CriterionProgress());
        }
        this.requirements = $$0;
    }

    public boolean isDone() {
        return this.requirements.test(this::isCriterionDone);
    }

    public boolean hasProgress() {
        for (CriterionProgress $$0 : this.criteria.values()) {
            if (!$$0.isDone()) continue;
            return true;
        }
        return false;
    }

    public boolean grantProgress(String $$0) {
        CriterionProgress $$1 = this.criteria.get($$0);
        if ($$1 != null && !$$1.isDone()) {
            $$1.grant();
            return true;
        }
        return false;
    }

    public boolean revokeProgress(String $$0) {
        CriterionProgress $$1 = this.criteria.get($$0);
        if ($$1 != null && $$1.isDone()) {
            $$1.revoke();
            return true;
        }
        return false;
    }

    public String toString() {
        return "AdvancementProgress{criteria=" + String.valueOf(this.criteria) + ", requirements=" + String.valueOf(this.requirements) + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf $$02) {
        $$02.writeMap(this.criteria, FriendlyByteBuf::writeUtf, ($$0, $$1) -> $$1.serializeToNetwork((FriendlyByteBuf)((Object)$$0)));
    }

    public static AdvancementProgress fromNetwork(FriendlyByteBuf $$0) {
        Map<String, CriterionProgress> $$1 = $$0.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        return new AdvancementProgress($$1);
    }

    public @Nullable CriterionProgress getCriterion(String $$0) {
        return this.criteria.get($$0);
    }

    private boolean isCriterionDone(String $$0) {
        CriterionProgress $$1 = this.getCriterion($$0);
        return $$1 != null && $$1.isDone();
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0f;
        }
        float $$0 = this.requirements.size();
        float $$1 = this.countCompletedRequirements();
        return $$1 / $$0;
    }

    public @Nullable Component getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        }
        int $$0 = this.requirements.size();
        if ($$0 <= 1) {
            return null;
        }
        int $$1 = this.countCompletedRequirements();
        return Component.translatable("advancements.progress", $$1, $$0);
    }

    private int countCompletedRequirements() {
        return this.requirements.count(this::isCriterionDone);
    }

    public Iterable<String> getRemainingCriteria() {
        ArrayList $$0 = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> $$1 : this.criteria.entrySet()) {
            if ($$1.getValue().isDone()) continue;
            $$0.add($$1.getKey());
        }
        return $$0;
    }

    public Iterable<String> getCompletedCriteria() {
        ArrayList $$0 = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> $$1 : this.criteria.entrySet()) {
            if (!$$1.getValue().isDone()) continue;
            $$0.add($$1.getKey());
        }
        return $$0;
    }

    public @Nullable Instant getFirstProgressDate() {
        return this.criteria.values().stream().map(CriterionProgress::getObtained).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public int compareTo(AdvancementProgress $$0) {
        Instant $$1 = this.getFirstProgressDate();
        Instant $$2 = $$0.getFirstProgressDate();
        if ($$1 == null && $$2 != null) {
            return 1;
        }
        if ($$1 != null && $$2 == null) {
            return -1;
        }
        if ($$1 == null && $$2 == null) {
            return 0;
        }
        return $$1.compareTo($$2);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((AdvancementProgress)object);
    }
}

