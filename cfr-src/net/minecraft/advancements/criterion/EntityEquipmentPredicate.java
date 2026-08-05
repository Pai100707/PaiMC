/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jspecify.annotations.Nullable;

public record EntityEquipmentPredicate(Optional<ItemPredicate> head, Optional<ItemPredicate> chest, Optional<ItemPredicate> legs, Optional<ItemPredicate> feet, Optional<ItemPredicate> body, Optional<ItemPredicate> mainhand, Optional<ItemPredicate> offhand) {
    public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head), (App)ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest), (App)ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs), (App)ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet), (App)ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body), (App)ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand), (App)ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)).apply((Applicative)$$0, EntityEquipmentPredicate::new));

    public static EntityEquipmentPredicate captainPredicate(HolderGetter<Item> $$0, HolderGetter<BannerPattern> $$1) {
        return Builder.equipment().head(ItemPredicate.Builder.item().of($$0, Items.WHITE_BANNER).withComponents(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.someOf(Raid.getOminousBannerInstance($$1).getComponents(), DataComponents.BANNER_PATTERNS, DataComponents.ITEM_NAME)).build())).build();
    }

    /*
     * WARNING - void declaration
     */
    public boolean matches(@Nullable Entity $$0) {
        void $$2;
        if (!($$0 instanceof LivingEntity)) {
            return false;
        }
        LivingEntity $$1 = (LivingEntity)$$0;
        if (this.head.isPresent() && !this.head.get().test($$2.getItemBySlot(EquipmentSlot.HEAD))) {
            return false;
        }
        if (this.chest.isPresent() && !this.chest.get().test($$2.getItemBySlot(EquipmentSlot.CHEST))) {
            return false;
        }
        if (this.legs.isPresent() && !this.legs.get().test($$2.getItemBySlot(EquipmentSlot.LEGS))) {
            return false;
        }
        if (this.feet.isPresent() && !this.feet.get().test($$2.getItemBySlot(EquipmentSlot.FEET))) {
            return false;
        }
        if (this.body.isPresent() && !this.body.get().test($$2.getItemBySlot(EquipmentSlot.BODY))) {
            return false;
        }
        if (this.mainhand.isPresent() && !this.mainhand.get().test($$2.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return false;
        }
        return !this.offhand.isPresent() || this.offhand.get().test($$2.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    public static class Builder {
        private Optional<ItemPredicate> head = Optional.empty();
        private Optional<ItemPredicate> chest = Optional.empty();
        private Optional<ItemPredicate> legs = Optional.empty();
        private Optional<ItemPredicate> feet = Optional.empty();
        private Optional<ItemPredicate> body = Optional.empty();
        private Optional<ItemPredicate> mainhand = Optional.empty();
        private Optional<ItemPredicate> offhand = Optional.empty();

        public static Builder equipment() {
            return new Builder();
        }

        public Builder head(ItemPredicate.Builder $$0) {
            this.head = Optional.of($$0.build());
            return this;
        }

        public Builder chest(ItemPredicate.Builder $$0) {
            this.chest = Optional.of($$0.build());
            return this;
        }

        public Builder legs(ItemPredicate.Builder $$0) {
            this.legs = Optional.of($$0.build());
            return this;
        }

        public Builder feet(ItemPredicate.Builder $$0) {
            this.feet = Optional.of($$0.build());
            return this;
        }

        public Builder body(ItemPredicate.Builder $$0) {
            this.body = Optional.of($$0.build());
            return this;
        }

        public Builder mainhand(ItemPredicate.Builder $$0) {
            this.mainhand = Optional.of($$0.build());
            return this;
        }

        public Builder offhand(ItemPredicate.Builder $$0) {
            this.offhand = Optional.of($$0.build());
            return this;
        }

        public EntityEquipmentPredicate build() {
            return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
        }
    }
}

