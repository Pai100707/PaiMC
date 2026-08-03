package net.minecraft.world.waypoints;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface WaypointStyleAssets {
   ResourceKey<? extends Registry<net.minecraft.world.waypoints.WaypointStyleAsset>> ROOT_ID = ResourceKey.createRegistryKey(
      Identifier.withDefaultNamespace("waypoint_style_asset")
   );
   ResourceKey<net.minecraft.world.waypoints.WaypointStyleAsset> DEFAULT = createId("default");
   ResourceKey<net.minecraft.world.waypoints.WaypointStyleAsset> BOWTIE = createId("bowtie");

   static ResourceKey<net.minecraft.world.waypoints.WaypointStyleAsset> createId(String $$0) {
      return ResourceKey.create(ROOT_ID, Identifier.withDefaultNamespace($$0));
   }
}
