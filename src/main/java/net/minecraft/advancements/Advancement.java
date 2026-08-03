package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.RootFieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public record Advancement(
   Optional<Identifier> parent,
   Optional<net.minecraft.advancements.DisplayInfo> display,
   net.minecraft.advancements.AdvancementRewards rewards,
   Map<String, net.minecraft.advancements.Criterion<?>> criteria,
   net.minecraft.advancements.AdvancementRequirements requirements,
   boolean sendsTelemetryEvent,
   Optional<Component> name
) {
   private static final Codec<Map<String, net.minecraft.advancements.Criterion<?>>> CRITERIA_CODEC = Codec.unboundedMap(
         Codec.STRING, net.minecraft.advancements.Criterion.CODEC
      )
      .validate($$0 -> $$0.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success($$0));
   public static final Codec<net.minecraft.advancements.Advancement> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Identifier.CODEC.optionalFieldOf("parent").forGetter(net.minecraft.advancements.Advancement::parent),
               net.minecraft.advancements.DisplayInfo.CODEC.optionalFieldOf("display").forGetter(net.minecraft.advancements.Advancement::display),
               net.minecraft.advancements.AdvancementRewards.CODEC
                  .optionalFieldOf("rewards", net.minecraft.advancements.AdvancementRewards.EMPTY)
                  .forGetter(net.minecraft.advancements.Advancement::rewards),
               CRITERIA_CODEC.fieldOf("criteria").forGetter(net.minecraft.advancements.Advancement::criteria),
               net.minecraft.advancements.AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter($$0x -> Optional.of($$0x.requirements())),
               Codec.BOOL.optionalFieldOf("sends_telemetry_event", false).forGetter(net.minecraft.advancements.Advancement::sendsTelemetryEvent)
            )
            .apply($$0, ($$0x, $$1, $$2, $$3, $$4, $$5) -> {
               net.minecraft.advancements.AdvancementRequirements $$6 = $$4.orElseGet(
                  () -> net.minecraft.advancements.AdvancementRequirements.allOf($$3.keySet())
               );
               return new net.minecraft.advancements.Advancement($$0x, $$1, $$2, $$3, $$6, $$5);
            })
      )
      .validate(net.minecraft.advancements.Advancement::validate);
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.advancements.Advancement> STREAM_CODEC = StreamCodec.ofMember(
      net.minecraft.advancements.Advancement::write, net.minecraft.advancements.Advancement::read
   );

   public Advancement(
      Optional<Identifier> $$0,
      Optional<net.minecraft.advancements.DisplayInfo> $$1,
      net.minecraft.advancements.AdvancementRewards $$2,
      Map<String, net.minecraft.advancements.Criterion<?>> $$3,
      net.minecraft.advancements.AdvancementRequirements $$4,
      boolean $$5
   ) {
      this($$0, $$1, $$2, Map.copyOf($$3), $$4, $$5, $$1.map(net.minecraft.advancements.Advancement::decorateName));
   }

   private static DataResult<net.minecraft.advancements.Advancement> validate(net.minecraft.advancements.Advancement $$0) {
      return $$0.requirements().validate($$0.criteria().keySet()).map($$1 -> $$0);
   }

   private static Component decorateName(net.minecraft.advancements.DisplayInfo $$0) {
      Component $$1 = $$0.getTitle();
      ChatFormatting $$2 = $$0.getType().getChatColor();
      Component $$3 = ComponentUtils.mergeStyles($$1.copy(), Style.EMPTY.withColor($$2)).append("\n").append($$0.getDescription());
      Component $$4 = $$1.copy().withStyle($$1x -> $$1x.withHoverEvent(new ShowText($$3)));
      return ComponentUtils.wrapInSquareBrackets($$4).withStyle($$2);
   }

   public static Component name(net.minecraft.advancements.AdvancementHolder $$0) {
      return $$0.value().name().orElseGet(() -> Component.literal($$0.id().toString()));
   }

   private void write(RegistryFriendlyByteBuf $$0) {
      $$0.writeOptional(this.parent, FriendlyByteBuf::writeIdentifier);
      net.minecraft.advancements.DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).encode($$0, this.display);
      this.requirements.write($$0);
      $$0.writeBoolean(this.sendsTelemetryEvent);
   }

   private static net.minecraft.advancements.Advancement read(RegistryFriendlyByteBuf $$0) {
      return new net.minecraft.advancements.Advancement(
         $$0.readOptional(FriendlyByteBuf::readIdentifier),
         (Optional<net.minecraft.advancements.DisplayInfo>)net.minecraft.advancements.DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).decode($$0),
         net.minecraft.advancements.AdvancementRewards.EMPTY,
         Map.of(),
         new net.minecraft.advancements.AdvancementRequirements($$0),
         $$0.readBoolean()
      );
   }

   public boolean isRoot() {
      return this.parent.isEmpty();
   }

   public void validate(ProblemReporter $$0, Provider $$1) {
      this.criteria.forEach(($$2, $$3) -> {
         CriterionValidator $$4 = new CriterionValidator($$0.forChild(new RootFieldPathElement($$2)), $$1);
         $$3.triggerInstance().validate($$4);
      });
   }

   public static class Builder {
      private Optional<Identifier> parent = Optional.empty();
      private Optional<net.minecraft.advancements.DisplayInfo> display = Optional.empty();
      private net.minecraft.advancements.AdvancementRewards rewards = net.minecraft.advancements.AdvancementRewards.EMPTY;
      private final com.google.common.collect.ImmutableMap.Builder<String, net.minecraft.advancements.Criterion<?>> criteria = ImmutableMap.builder();
      private Optional<net.minecraft.advancements.AdvancementRequirements> requirements = Optional.empty();
      private net.minecraft.advancements.AdvancementRequirements.Strategy requirementsStrategy = net.minecraft.advancements.AdvancementRequirements.Strategy.AND;
      private boolean sendsTelemetryEvent;

      public static net.minecraft.advancements.Advancement.Builder advancement() {
         return new net.minecraft.advancements.Advancement.Builder().sendsTelemetryEvent();
      }

      public static net.minecraft.advancements.Advancement.Builder recipeAdvancement() {
         return new net.minecraft.advancements.Advancement.Builder();
      }

      public net.minecraft.advancements.Advancement.Builder parent(net.minecraft.advancements.AdvancementHolder $$0) {
         this.parent = Optional.of($$0.id());
         return this;
      }

      @Deprecated(
         forRemoval = true
      )
      public net.minecraft.advancements.Advancement.Builder parent(Identifier $$0) {
         this.parent = Optional.of($$0);
         return this;
      }

      public net.minecraft.advancements.Advancement.Builder display(
         ItemStack $$0,
         Component $$1,
         Component $$2,
         @Nullable Identifier $$3,
         net.minecraft.advancements.AdvancementType $$4,
         boolean $$5,
         boolean $$6,
         boolean $$7
      ) {
         return this.display(new net.minecraft.advancements.DisplayInfo($$0, $$1, $$2, Optional.ofNullable($$3).map(ResourceTexture::new), $$4, $$5, $$6, $$7));
      }

      public net.minecraft.advancements.Advancement.Builder display(
         ItemLike $$0,
         Component $$1,
         Component $$2,
         @Nullable Identifier $$3,
         net.minecraft.advancements.AdvancementType $$4,
         boolean $$5,
         boolean $$6,
         boolean $$7
      ) {
         return this.display(
            new net.minecraft.advancements.DisplayInfo(
               new ItemStack($$0.asItem()), $$1, $$2, Optional.ofNullable($$3).map(ResourceTexture::new), $$4, $$5, $$6, $$7
            )
         );
      }

      public net.minecraft.advancements.Advancement.Builder display(net.minecraft.advancements.DisplayInfo $$0) {
         this.display = Optional.of($$0);
         return this;
      }

      public net.minecraft.advancements.Advancement.Builder rewards(net.minecraft.advancements.AdvancementRewards.Builder $$0) {
         return this.rewards($$0.build());
      }

      public net.minecraft.advancements.Advancement.Builder rewards(net.minecraft.advancements.AdvancementRewards $$0) {
         this.rewards = $$0;
         return this;
      }

      public net.minecraft.advancements.Advancement.Builder addCriterion(String $$0, net.minecraft.advancements.Criterion<?> $$1) {
         this.criteria.put($$0, $$1);
         return this;
      }

      public net.minecraft.advancements.Advancement.Builder requirements(net.minecraft.advancements.AdvancementRequirements.Strategy $$0) {
         this.requirementsStrategy = $$0;
         return this;
      }

      public net.minecraft.advancements.Advancement.Builder requirements(net.minecraft.advancements.AdvancementRequirements $$0) {
         this.requirements = Optional.of($$0);
         return this;
      }

      public net.minecraft.advancements.Advancement.Builder sendsTelemetryEvent() {
         this.sendsTelemetryEvent = true;
         return this;
      }

      public net.minecraft.advancements.AdvancementHolder build(Identifier $$0) {
         Map<String, net.minecraft.advancements.Criterion<?>> $$1 = this.criteria.buildOrThrow();
         net.minecraft.advancements.AdvancementRequirements $$2 = this.requirements.orElseGet(() -> this.requirementsStrategy.create($$1.keySet()));
         return new net.minecraft.advancements.AdvancementHolder(
            $$0, new net.minecraft.advancements.Advancement(this.parent, this.display, this.rewards, $$1, $$2, this.sendsTelemetryEvent)
         );
      }

      public net.minecraft.advancements.AdvancementHolder save(Consumer<net.minecraft.advancements.AdvancementHolder> $$0, String $$1) {
         net.minecraft.advancements.AdvancementHolder $$2 = this.build(Identifier.parse($$1));
         $$0.accept($$2);
         return $$2;
      }
   }
}
