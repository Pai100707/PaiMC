package net.minecraft.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class BannerPatterns {
   public static final ResourceKey<BannerPattern> BASE = create("base");
   public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_LEFT = create("square_bottom_left");
   public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_RIGHT = create("square_bottom_right");
   public static final ResourceKey<BannerPattern> SQUARE_TOP_LEFT = create("square_top_left");
   public static final ResourceKey<BannerPattern> SQUARE_TOP_RIGHT = create("square_top_right");
   public static final ResourceKey<BannerPattern> STRIPE_BOTTOM = create("stripe_bottom");
   public static final ResourceKey<BannerPattern> STRIPE_TOP = create("stripe_top");
   public static final ResourceKey<BannerPattern> STRIPE_LEFT = create("stripe_left");
   public static final ResourceKey<BannerPattern> STRIPE_RIGHT = create("stripe_right");
   public static final ResourceKey<BannerPattern> STRIPE_CENTER = create("stripe_center");
   public static final ResourceKey<BannerPattern> STRIPE_MIDDLE = create("stripe_middle");
   public static final ResourceKey<BannerPattern> STRIPE_DOWNRIGHT = create("stripe_downright");
   public static final ResourceKey<BannerPattern> STRIPE_DOWNLEFT = create("stripe_downleft");
   public static final ResourceKey<BannerPattern> STRIPE_SMALL = create("small_stripes");
   public static final ResourceKey<BannerPattern> CROSS = create("cross");
   public static final ResourceKey<BannerPattern> STRAIGHT_CROSS = create("straight_cross");
   public static final ResourceKey<BannerPattern> TRIANGLE_BOTTOM = create("triangle_bottom");
   public static final ResourceKey<BannerPattern> TRIANGLE_TOP = create("triangle_top");
   public static final ResourceKey<BannerPattern> TRIANGLES_BOTTOM = create("triangles_bottom");
   public static final ResourceKey<BannerPattern> TRIANGLES_TOP = create("triangles_top");
   public static final ResourceKey<BannerPattern> DIAGONAL_LEFT = create("diagonal_left");
   public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT = create("diagonal_up_right");
   public static final ResourceKey<BannerPattern> DIAGONAL_LEFT_MIRROR = create("diagonal_up_left");
   public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT_MIRROR = create("diagonal_right");
   public static final ResourceKey<BannerPattern> CIRCLE_MIDDLE = create("circle");
   public static final ResourceKey<BannerPattern> RHOMBUS_MIDDLE = create("rhombus");
   public static final ResourceKey<BannerPattern> HALF_VERTICAL = create("half_vertical");
   public static final ResourceKey<BannerPattern> HALF_HORIZONTAL = create("half_horizontal");
   public static final ResourceKey<BannerPattern> HALF_VERTICAL_MIRROR = create("half_vertical_right");
   public static final ResourceKey<BannerPattern> HALF_HORIZONTAL_MIRROR = create("half_horizontal_bottom");
   public static final ResourceKey<BannerPattern> BORDER = create("border");
   public static final ResourceKey<BannerPattern> CURLY_BORDER = create("curly_border");
   public static final ResourceKey<BannerPattern> GRADIENT = create("gradient");
   public static final ResourceKey<BannerPattern> GRADIENT_UP = create("gradient_up");
   public static final ResourceKey<BannerPattern> BRICKS = create("bricks");
   public static final ResourceKey<BannerPattern> GLOBE = create("globe");
   public static final ResourceKey<BannerPattern> CREEPER = create("creeper");
   public static final ResourceKey<BannerPattern> SKULL = create("skull");
   public static final ResourceKey<BannerPattern> FLOWER = create("flower");
   public static final ResourceKey<BannerPattern> MOJANG = create("mojang");
   public static final ResourceKey<BannerPattern> PIGLIN = create("piglin");
   public static final ResourceKey<BannerPattern> FLOW = create("flow");
   public static final ResourceKey<BannerPattern> GUSTER = create("guster");

   private static ResourceKey<BannerPattern> create(String $$0) {
      return ResourceKey.create(Registries.BANNER_PATTERN, Identifier.withDefaultNamespace($$0));
   }

   public static void bootstrap(BootstrapContext<BannerPattern> $$0) {
      register($$0, BASE);
      register($$0, SQUARE_BOTTOM_LEFT);
      register($$0, SQUARE_BOTTOM_RIGHT);
      register($$0, SQUARE_TOP_LEFT);
      register($$0, SQUARE_TOP_RIGHT);
      register($$0, STRIPE_BOTTOM);
      register($$0, STRIPE_TOP);
      register($$0, STRIPE_LEFT);
      register($$0, STRIPE_RIGHT);
      register($$0, STRIPE_CENTER);
      register($$0, STRIPE_MIDDLE);
      register($$0, STRIPE_DOWNRIGHT);
      register($$0, STRIPE_DOWNLEFT);
      register($$0, STRIPE_SMALL);
      register($$0, CROSS);
      register($$0, STRAIGHT_CROSS);
      register($$0, TRIANGLE_BOTTOM);
      register($$0, TRIANGLE_TOP);
      register($$0, TRIANGLES_BOTTOM);
      register($$0, TRIANGLES_TOP);
      register($$0, DIAGONAL_LEFT);
      register($$0, DIAGONAL_RIGHT);
      register($$0, DIAGONAL_LEFT_MIRROR);
      register($$0, DIAGONAL_RIGHT_MIRROR);
      register($$0, CIRCLE_MIDDLE);
      register($$0, RHOMBUS_MIDDLE);
      register($$0, HALF_VERTICAL);
      register($$0, HALF_HORIZONTAL);
      register($$0, HALF_VERTICAL_MIRROR);
      register($$0, HALF_HORIZONTAL_MIRROR);
      register($$0, BORDER);
      register($$0, GRADIENT);
      register($$0, GRADIENT_UP);
      register($$0, BRICKS);
      register($$0, CURLY_BORDER);
      register($$0, GLOBE);
      register($$0, CREEPER);
      register($$0, SKULL);
      register($$0, FLOWER);
      register($$0, MOJANG);
      register($$0, PIGLIN);
      register($$0, FLOW);
      register($$0, GUSTER);
   }

   public static void register(BootstrapContext<BannerPattern> $$0, ResourceKey<BannerPattern> $$1) {
      $$0.register($$1, new BannerPattern($$1.identifier(), "block.minecraft.banner." + $$1.identifier().toShortLanguageKey()));
   }
}
