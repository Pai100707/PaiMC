package net.minecraft.server.permissions;

public interface LevelBasedPermissionSet extends PermissionSet {
   @Deprecated
   LevelBasedPermissionSet ALL = create(PermissionLevel.ALL);
   LevelBasedPermissionSet MODERATOR = create(PermissionLevel.MODERATORS);
   LevelBasedPermissionSet GAMEMASTER = create(PermissionLevel.GAMEMASTERS);
   LevelBasedPermissionSet ADMIN = create(PermissionLevel.ADMINS);
   LevelBasedPermissionSet OWNER = create(PermissionLevel.OWNERS);

   PermissionLevel level();

   @Override
   default boolean hasPermission(Permission $$0) {
      if ($$0 instanceof Permission.HasCommandLevel $$1) {
         return this.level().isEqualOrHigherThan($$1.level());
      } else {
         return $$0.equals(Permissions.COMMANDS_ENTITY_SELECTORS) ? this.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS) : false;
      }
   }

   @Override
   default PermissionSet union(PermissionSet $$0) {
      if ($$0 instanceof LevelBasedPermissionSet $$1) {
         return this.level().isEqualOrHigherThan($$1.level()) ? $$1 : this;
      } else {
         return PermissionSet.super.union($$0);
      }
   }

   static LevelBasedPermissionSet forLevel(PermissionLevel $$0) {
      return switch ($$0) {
         case ALL -> ALL;
         case MODERATORS -> MODERATOR;
         case GAMEMASTERS -> GAMEMASTER;
         case ADMINS -> ADMIN;
         case OWNERS -> OWNER;
      };
   }

   private static LevelBasedPermissionSet create(final PermissionLevel $$0) {
      return new LevelBasedPermissionSet() {
         @Override
         public PermissionLevel level() {
            return $$0;
         }

         @Override
         public String toString() {
            return "permission level: " + $$0.name();
         }
      };
   }
}
