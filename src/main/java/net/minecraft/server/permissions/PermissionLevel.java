package net.minecraft.server.permissions;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum PermissionLevel implements StringRepresentable {
   ALL("all", 0),
   MODERATORS("moderators", 1),
   GAMEMASTERS("gamemasters", 2),
   ADMINS("admins", 3),
   OWNERS("owners", 4);

   public static final Codec<PermissionLevel> CODEC = StringRepresentable.fromEnum(PermissionLevel::values);
   private static final IntFunction<PermissionLevel> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.CLAMP);
   public static final Codec<PermissionLevel> INT_CODEC = Codec.INT.xmap(BY_ID::apply, $$0 -> $$0.id);
   private final String name;
   private final int id;

   private PermissionLevel(final String $$0, final int $$1) {
      this.name = $$0;
      this.id = $$1;
   }

   public boolean isEqualOrHigherThan(PermissionLevel $$0) {
      return this.id >= $$0.id;
   }

   public static PermissionLevel byId(int $$0) {
      return BY_ID.apply($$0);
   }

   public int id() {
      return this.id;
   }

   public String getSerializedName() {
      return this.name;
   }
}
