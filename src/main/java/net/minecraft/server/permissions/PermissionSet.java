package net.minecraft.server.permissions;

public interface PermissionSet {
   PermissionSet NO_PERMISSIONS = $$0 -> false;
   PermissionSet ALL_PERMISSIONS = $$0 -> true;

   boolean hasPermission(Permission var1);

   default PermissionSet union(PermissionSet $$0) {
      return (PermissionSet)($$0 instanceof PermissionSetUnion ? $$0.union(this) : new PermissionSetUnion(this, $$0));
   }
}
