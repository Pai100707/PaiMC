package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public record FireworkExplosion(FireworkExplosion.Shape shape, IntList colors, IntList fadeColors, boolean hasTrail, boolean hasTwinkle)
   implements TooltipProvider {
   public static final FireworkExplosion DEFAULT = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(), IntList.of(), false, false);
   public static final Codec<IntList> COLOR_LIST_CODEC = Codec.INT.listOf().xmap(IntArrayList::new, ArrayList::new);
   public static final Codec<FireworkExplosion> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            FireworkExplosion.Shape.CODEC.fieldOf("shape").forGetter(FireworkExplosion::shape),
            COLOR_LIST_CODEC.optionalFieldOf("colors", IntList.of()).forGetter(FireworkExplosion::colors),
            COLOR_LIST_CODEC.optionalFieldOf("fade_colors", IntList.of()).forGetter(FireworkExplosion::fadeColors),
            Codec.BOOL.optionalFieldOf("has_trail", false).forGetter(FireworkExplosion::hasTrail),
            Codec.BOOL.optionalFieldOf("has_twinkle", false).forGetter(FireworkExplosion::hasTwinkle)
         )
         .apply($$0, FireworkExplosion::new)
   );
   private static final StreamCodec<ByteBuf, IntList> COLOR_LIST_STREAM_CODEC = ByteBufCodecs.INT
      .apply(ByteBufCodecs.list())
      .map(IntArrayList::new, ArrayList::new);
   public static final StreamCodec<ByteBuf, FireworkExplosion> STREAM_CODEC = StreamCodec.composite(
      FireworkExplosion.Shape.STREAM_CODEC,
      FireworkExplosion::shape,
      COLOR_LIST_STREAM_CODEC,
      FireworkExplosion::colors,
      COLOR_LIST_STREAM_CODEC,
      FireworkExplosion::fadeColors,
      ByteBufCodecs.BOOL,
      FireworkExplosion::hasTrail,
      ByteBufCodecs.BOOL,
      FireworkExplosion::hasTwinkle,
      FireworkExplosion::new
   );
   private static final Component CUSTOM_COLOR_NAME = Component.translatable("item.minecraft.firework_star.custom_color");

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      $$1.accept(this.shape.getName().withStyle(ChatFormatting.GRAY));
      this.addAdditionalTooltip($$1);
   }

   public void addAdditionalTooltip(Consumer<Component> $$0) {
      if (!this.colors.isEmpty()) {
         $$0.accept(appendColors(Component.empty().withStyle(ChatFormatting.GRAY), this.colors));
      }

      if (!this.fadeColors.isEmpty()) {
         $$0.accept(
            appendColors(
               Component.translatable("item.minecraft.firework_star.fade_to").append(CommonComponents.SPACE).withStyle(ChatFormatting.GRAY), this.fadeColors
            )
         );
      }

      if (this.hasTrail) {
         $$0.accept(Component.translatable("item.minecraft.firework_star.trail").withStyle(ChatFormatting.GRAY));
      }

      if (this.hasTwinkle) {
         $$0.accept(Component.translatable("item.minecraft.firework_star.flicker").withStyle(ChatFormatting.GRAY));
      }
   }

   private static Component appendColors(MutableComponent $$0, IntList $$1) {
      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         if ($$2 > 0) {
            $$0.append(", ");
         }

         $$0.append(getColorName($$1.getInt($$2)));
      }

      return $$0;
   }

   private static Component getColorName(int $$0) {
      net.minecraft.world.item.DyeColor $$1 = net.minecraft.world.item.DyeColor.byFireworkColor($$0);
      return (Component)($$1 == null ? CUSTOM_COLOR_NAME : Component.translatable("item.minecraft.firework_star." + $$1.getName()));
   }

   public FireworkExplosion withFadeColors(IntList $$0) {
      return new FireworkExplosion(this.shape, this.colors, new IntArrayList($$0), this.hasTrail, this.hasTwinkle);
   }

   public static enum Shape implements StringRepresentable {
      SMALL_BALL(0, "small_ball"),
      LARGE_BALL(1, "large_ball"),
      STAR(2, "star"),
      CREEPER(3, "creeper"),
      BURST(4, "burst");

      private static final IntFunction<FireworkExplosion.Shape> BY_ID = ByIdMap.continuous(FireworkExplosion.Shape::getId, values(), OutOfBoundsStrategy.ZERO);
      public static final StreamCodec<ByteBuf, FireworkExplosion.Shape> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FireworkExplosion.Shape::getId);
      public static final Codec<FireworkExplosion.Shape> CODEC = StringRepresentable.fromValues(FireworkExplosion.Shape::values);
      private final int id;
      private final String name;

      private Shape(final int $$0, final String $$1) {
         this.id = $$0;
         this.name = $$1;
      }

      public MutableComponent getName() {
         return Component.translatable("item.minecraft.firework_star.shape." + this.name);
      }

      public int getId() {
         return this.id;
      }

      public static FireworkExplosion.Shape byId(int $$0) {
         return BY_ID.apply($$0);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
