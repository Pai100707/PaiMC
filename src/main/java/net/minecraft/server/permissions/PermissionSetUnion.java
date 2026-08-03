package net.minecraft.server.permissions;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

public class PermissionSetUnion implements PermissionSet {
   private final ReferenceSet<PermissionSet> permissions = new ReferenceArraySet();

   PermissionSetUnion(PermissionSet $$0, PermissionSet $$1) {
      this.permissions.add($$0);
      this.permissions.add($$1);
      this.ensureNoUnionsWithinUnions();
   }

   private PermissionSetUnion(ReferenceSet<PermissionSet> $$0, PermissionSet $$1) {
      this.permissions.addAll($$0);
      this.permissions.add($$1);
      this.ensureNoUnionsWithinUnions();
   }

   private PermissionSetUnion(ReferenceSet<PermissionSet> $$0, ReferenceSet<PermissionSet> $$1) {
      this.permissions.addAll($$0);
      this.permissions.addAll($$1);
      this.ensureNoUnionsWithinUnions();
   }

   @Override
   public boolean hasPermission(Permission $$0) {
      ObjectIterator var2 = this.permissions.iterator();

      while (var2.hasNext()) {
         PermissionSet $$1 = (PermissionSet)var2.next();
         if ($$1.hasPermission($$0)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public PermissionSet union(PermissionSet $$0) {
      return $$0 instanceof PermissionSetUnion $$1 ? new PermissionSetUnion(this.permissions, $$1.permissions) : new PermissionSetUnion(this.permissions, $$0);
   }

   @VisibleForTesting
   public ReferenceSet<PermissionSet> getPermissions() {
      return new ReferenceArraySet(this.permissions);
   }

   private void ensureNoUnionsWithinUnions() {
      ObjectIterator var1 = this.permissions.iterator();

      while (var1.hasNext()) {
         PermissionSet $$0 = (PermissionSet)var1.next();
         if ($$0 instanceof PermissionSetUnion) {
            throw new IllegalArgumentException("Cannot have PermissionSetUnion within another PermissionSetUnion");
         }
      }
   }
}
