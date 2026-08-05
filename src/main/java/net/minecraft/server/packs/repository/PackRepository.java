package net.minecraft.server.packs.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.packs.PackResources;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackRepository {
   private final Set<RepositorySource> sources;
   private Map<String, Pack> available = ImmutableMap.of();
   private List<Pack> selected = ImmutableList.of();

   public PackRepository(RepositorySource... $$0) {
      this.sources = ImmutableSet.copyOf($$0);
   }

   public static String displayPackList(Collection<Pack> $$0) {
      return $$0.stream().map($$0x -> $$0x.getId() + ($$0x.getCompatibility().isCompatible() ? "" : " (incompatible)")).collect(Collectors.joining(", "));
   }

   public void reload() {
      List<String> $$0 = this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
      this.available = this.discoverAvailable();
      this.selected = this.rebuildSelected($$0);
   }

   private Map<String, Pack> discoverAvailable() {
      Map<String, Pack> $$0 = Maps.newTreeMap();

      for (RepositorySource $$1 : this.sources) {
         $$1.loadPacks($$1x -> $$0.put($$1x.getId(), $$1x));
      }

      return ImmutableMap.copyOf($$0);
   }

   public boolean isAbleToClearAnyPack() {
      List<Pack> $$0 = this.rebuildSelected(List.of());
      return !this.selected.equals($$0);
   }

   public void setSelected(Collection<String> $$0) {
      this.selected = this.rebuildSelected($$0);
   }

   public boolean addPack(String $$0) {
      Pack $$1 = this.available.get($$0);
      if ($$1 != null && !this.selected.contains($$1)) {
         List<Pack> $$2 = Lists.newArrayList(this.selected);
         $$2.add($$1);
         this.selected = $$2;
         return true;
      } else {
         return false;
      }
   }

   public boolean removePack(String $$0) {
      Pack $$1 = this.available.get($$0);
      if ($$1 != null && this.selected.contains($$1)) {
         List<Pack> $$2 = Lists.newArrayList(this.selected);
         $$2.remove($$1);
         this.selected = $$2;
         return true;
      } else {
         return false;
      }
   }

   private List<Pack> rebuildSelected(Collection<String> $$0) {
      List<Pack> $$1 = this.getAvailablePacks($$0).collect(Util.toMutableList());

      for (Pack $$2 : this.available.values()) {
         if ($$2.isRequired() && !$$1.contains($$2)) {
            $$2.getDefaultPosition().insert($$1, $$2, Pack::selectionConfig, false);
         }
      }

      return ImmutableList.copyOf($$1);
   }

   private Stream<Pack> getAvailablePacks(Collection<String> $$0) {
      return $$0.stream().map(this.available::get).filter(Objects::nonNull);
   }

   public Collection<String> getAvailableIds() {
      return this.available.keySet();
   }

   public Collection<Pack> getAvailablePacks() {
      return this.available.values();
   }

   public Collection<String> getSelectedIds() {
      return this.selected.stream().map(Pack::getId).collect(ImmutableSet.toImmutableSet());
   }

   public FeatureFlagSet getRequestedFeatureFlags() {
      return this.getSelectedPacks().stream().map(Pack::getRequestedFeatures).reduce(FeatureFlagSet::join).orElse(FeatureFlagSet.of());
   }

   public Collection<Pack> getSelectedPacks() {
      return this.selected;
   }

   
   public Pack getPack(String $$0) {
      return this.available.get($$0);
   }

   public boolean isAvailable(String $$0) {
      return this.available.containsKey($$0);
   }

   public List<PackResources> openAllSelected() {
      return this.selected.stream().map(Pack::open).collect(ImmutableList.toImmutableList());
   }
}
