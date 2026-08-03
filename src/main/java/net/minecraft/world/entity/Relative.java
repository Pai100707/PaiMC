package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum Relative {
   X(0),
   Y(1),
   Z(2),
   Y_ROT(3),
   X_ROT(4),
   DELTA_X(5),
   DELTA_Y(6),
   DELTA_Z(7),
   ROTATE_DELTA(8);

   public static final Set<net.minecraft.world.entity.Relative> ALL = Set.of(values());
   public static final Set<net.minecraft.world.entity.Relative> ROTATION = Set.of(X_ROT, Y_ROT);
   public static final Set<net.minecraft.world.entity.Relative> DELTA = Set.of(DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA);
   public static final StreamCodec<ByteBuf, Set<net.minecraft.world.entity.Relative>> SET_STREAM_CODEC = ByteBufCodecs.INT
      .map(net.minecraft.world.entity.Relative::unpack, net.minecraft.world.entity.Relative::pack);
   private final int bit;

   @SafeVarargs
   public static Set<net.minecraft.world.entity.Relative> union(Set<net.minecraft.world.entity.Relative>... $$0) {
      HashSet<net.minecraft.world.entity.Relative> $$1 = new HashSet<>();

      for (Set<net.minecraft.world.entity.Relative> $$2 : $$0) {
         $$1.addAll($$2);
      }

      return $$1;
   }

   public static Set<net.minecraft.world.entity.Relative> rotation(boolean $$0, boolean $$1) {
      Set<net.minecraft.world.entity.Relative> $$2 = EnumSet.noneOf(net.minecraft.world.entity.Relative.class);
      if ($$0) {
         $$2.add(Y_ROT);
      }

      if ($$1) {
         $$2.add(X_ROT);
      }

      return $$2;
   }

   public static Set<net.minecraft.world.entity.Relative> position(boolean $$0, boolean $$1, boolean $$2) {
      Set<net.minecraft.world.entity.Relative> $$3 = EnumSet.noneOf(net.minecraft.world.entity.Relative.class);
      if ($$0) {
         $$3.add(X);
      }

      if ($$1) {
         $$3.add(Y);
      }

      if ($$2) {
         $$3.add(Z);
      }

      return $$3;
   }

   public static Set<net.minecraft.world.entity.Relative> direction(boolean $$0, boolean $$1, boolean $$2) {
      Set<net.minecraft.world.entity.Relative> $$3 = EnumSet.noneOf(net.minecraft.world.entity.Relative.class);
      if ($$0) {
         $$3.add(DELTA_X);
      }

      if ($$1) {
         $$3.add(DELTA_Y);
      }

      if ($$2) {
         $$3.add(DELTA_Z);
      }

      return $$3;
   }

   private Relative(final int $$0) {
      this.bit = $$0;
   }

   private int getMask() {
      return 1 << this.bit;
   }

   private boolean isSet(int $$0) {
      return ($$0 & this.getMask()) == this.getMask();
   }

   public static Set<net.minecraft.world.entity.Relative> unpack(int $$0) {
      Set<net.minecraft.world.entity.Relative> $$1 = EnumSet.noneOf(net.minecraft.world.entity.Relative.class);

      for (net.minecraft.world.entity.Relative $$2 : values()) {
         if ($$2.isSet($$0)) {
            $$1.add($$2);
         }
      }

      return $$1;
   }

   public static int pack(Set<net.minecraft.world.entity.Relative> $$0) {
      int $$1 = 0;

      for (net.minecraft.world.entity.Relative $$2 : $$0) {
         $$1 |= $$2.getMask();
      }

      return $$1;
   }
}
