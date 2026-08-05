/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public record Advancement(Optional<Identifier> parent, Optional<DisplayInfo> display, AdvancementRewards rewards, Map<String, Criterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent, Optional<Component> name) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = Codec.unboundedMap((Codec)Codec.STRING, Criterion.CODEC).validate((T $$0) -> $$0.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success((Object)$$0));
    public static final Codec<Advancement> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)Identifier.CODEC.optionalFieldOf("parent").forGetter(Advancement::parent), (App)DisplayInfo.CODEC.optionalFieldOf("display").forGetter(Advancement::display), (App)AdvancementRewards.CODEC.optionalFieldOf("rewards", (Object)AdvancementRewards.EMPTY).forGetter(Advancement::rewards), (App)CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria), (App)AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter($$0 -> Optional.of($$0.requirements())), (App)Codec.BOOL.optionalFieldOf("sends_telemetry_event", (Object)false).forGetter(Advancement::sendsTelemetryEvent)).apply((Applicative)$$02, ($$0, $$1, $$2, $$3, $$4, $$5) -> {
        AdvancementRequirements $$6 = $$4.orElseGet(() -> AdvancementRequirements.allOf($$3.keySet()));
        return new Advancement((Optional<Identifier>)$$0, (Optional<DisplayInfo>)$$1, (AdvancementRewards)$$2, (Map<String, Criterion<?>>)$$3, $$6, (boolean)$$5);
    })).validate(Advancement::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, Advancement> STREAM_CODEC = StreamCodec.ofMember(Advancement::write, Advancement::read);

    public Advancement(Optional<Identifier> $$0, Optional<DisplayInfo> $$1, AdvancementRewards $$2, Map<String, Criterion<?>> $$3, AdvancementRequirements $$4, boolean $$5) {
        this($$0, $$1, $$2, Map.copyOf($$3), $$4, $$5, $$1.map(Advancement::decorateName));
    }

    private static DataResult<Advancement> validate(Advancement $$0) {
        return $$0.requirements().validate($$0.criteria().keySet()).map($$1 -> $$0);
    }

    private static Component decorateName(DisplayInfo $$0) {
        Component $$12 = $$0.getTitle();
        ChatFormatting $$2 = $$0.getType().getChatColor();
        MutableComponent $$3 = ComponentUtils.mergeStyles($$12.copy(), Style.EMPTY.withColor($$2)).append("\n").append($$0.getDescription());
        MutableComponent $$4 = $$12.copy().withStyle($$1 -> $$1.withHoverEvent(new HoverEvent.ShowText($$3)));
        return ComponentUtils.wrapInSquareBrackets($$4).withStyle($$2);
    }

    public static Component name(AdvancementHolder $$0) {
        return $$0.value().name().orElseGet(() -> Component.literal($$0.id().toString()));
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeOptional(this.parent, FriendlyByteBuf::writeIdentifier);
        DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).encode($$0, this.display);
        this.requirements.write($$0);
        $$0.writeBoolean(this.sendsTelemetryEvent);
    }

    private static Advancement read(RegistryFriendlyByteBuf $$0) {
        return new Advancement($$0.readOptional(FriendlyByteBuf::readIdentifier), (Optional)DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).decode($$0), AdvancementRewards.EMPTY, Map.of(), new AdvancementRequirements($$0), $$0.readBoolean());
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ProblemReporter $$0, HolderGetter.Provider $$1) {
        this.criteria.forEach(($$2, $$3) -> {
            CriterionValidator $$4 = new CriterionValidator($$0.forChild(new ProblemReporter.RootFieldPathElement((String)$$2)), $$1);
            $$3.triggerInstance().validate($$4);
        });
    }

    public static class Builder {
        private Optional<Identifier> parent = Optional.empty();
        private Optional<DisplayInfo> display = Optional.empty();
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private final ImmutableMap.Builder<String, Criterion<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private boolean sendsTelemetryEvent;

        public static Builder advancement() {
            return new Builder().sendsTelemetryEvent();
        }

        public static Builder recipeAdvancement() {
            return new Builder();
        }

        public Builder parent(AdvancementHolder $$0) {
            this.parent = Optional.of($$0.id());
            return this;
        }

        @Deprecated(forRemoval=true)
        public Builder parent(Identifier $$0) {
            this.parent = Optional.of($$0);
            return this;
        }

        public Builder display(ItemStack $$0, Component $$1, Component $$2, @Nullable Identifier $$3, AdvancementType $$4, boolean $$5, boolean $$6, boolean $$7) {
            return this.display(new DisplayInfo($$0, $$1, $$2, Optional.ofNullable($$3).map(ClientAsset.ResourceTexture::new), $$4, $$5, $$6, $$7));
        }

        public Builder display(ItemLike $$0, Component $$1, Component $$2, @Nullable Identifier $$3, AdvancementType $$4, boolean $$5, boolean $$6, boolean $$7) {
            return this.display(new DisplayInfo(new ItemStack($$0.asItem()), $$1, $$2, Optional.ofNullable($$3).map(ClientAsset.ResourceTexture::new), $$4, $$5, $$6, $$7));
        }

        public Builder display(DisplayInfo $$0) {
            this.display = Optional.of($$0);
            return this;
        }

        public Builder rewards(AdvancementRewards.Builder $$0) {
            return this.rewards($$0.build());
        }

        public Builder rewards(AdvancementRewards $$0) {
            this.rewards = $$0;
            return this;
        }

        public Builder addCriterion(String $$0, Criterion<?> $$1) {
            this.criteria.put((Object)$$0, $$1);
            return this;
        }

        public Builder requirements(AdvancementRequirements.Strategy $$0) {
            this.requirementsStrategy = $$0;
            return this;
        }

        public Builder requirements(AdvancementRequirements $$0) {
            this.requirements = Optional.of($$0);
            return this;
        }

        public Builder sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementHolder build(Identifier $$0) {
            ImmutableMap $$1 = this.criteria.buildOrThrow();
            AdvancementRequirements $$2 = this.requirements.orElseGet(() -> this.lambda$build$0((Map)$$1));
            return new AdvancementHolder($$0, new Advancement(this.parent, this.display, this.rewards, (Map<String, Criterion<?>>)$$1, $$2, this.sendsTelemetryEvent));
        }

        public AdvancementHolder save(Consumer<AdvancementHolder> $$0, String $$1) {
            AdvancementHolder $$2 = this.build(Identifier.parse($$1));
            $$0.accept($$2);
            return $$2;
        }

        private /* synthetic */ AdvancementRequirements lambda$build$0(Map $$0) {
            return this.requirementsStrategy.create($$0.keySet());
        }
    }
}

