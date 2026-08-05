/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.bossevents;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class CustomBossEvent
extends ServerBossEvent {
    private static final int DEFAULT_MAX = 100;
    private final Identifier id;
    private final Set<UUID> players = Sets.newHashSet();
    private int value;
    private int max = 100;

    public CustomBossEvent(Identifier $$0, Component $$1) {
        super($$1, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
        this.id = $$0;
        this.setProgress(0.0f);
    }

    public Identifier getTextId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayer $$0) {
        super.addPlayer($$0);
        this.players.add($$0.getUUID());
    }

    public void addOfflinePlayer(UUID $$0) {
        this.players.add($$0);
    }

    @Override
    public void removePlayer(ServerPlayer $$0) {
        super.removePlayer($$0);
        this.players.remove($$0.getUUID());
    }

    @Override
    public void removeAllPlayers() {
        super.removeAllPlayers();
        this.players.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMax() {
        return this.max;
    }

    public void setValue(int $$0) {
        this.value = $$0;
        this.setProgress(Mth.clamp((float)$$0 / (float)this.max, 0.0f, 1.0f));
    }

    public void setMax(int $$0) {
        this.max = $$0;
        this.setProgress(Mth.clamp((float)this.value / (float)$$0, 0.0f, 1.0f));
    }

    public final Component getDisplayName() {
        return ComponentUtils.wrapInSquareBrackets(this.getName()).withStyle($$0 -> $$0.withColor(this.getColor().getFormatting()).withHoverEvent(new HoverEvent.ShowText(Component.literal(this.getTextId().toString()))).withInsertion(this.getTextId().toString()));
    }

    public boolean setPlayers(Collection<ServerPlayer> $$0) {
        HashSet $$1 = Sets.newHashSet();
        HashSet $$2 = Sets.newHashSet();
        for (UUID $$3 : this.players) {
            boolean $$4 = false;
            for (ServerPlayer $$5 : $$0) {
                if (!$$5.getUUID().equals($$3)) continue;
                $$4 = true;
                break;
            }
            if ($$4) continue;
            $$1.add($$3);
        }
        for (ServerPlayer $$6 : $$0) {
            boolean $$7 = false;
            for (UUID $$8 : this.players) {
                if (!$$6.getUUID().equals($$8)) continue;
                $$7 = true;
                break;
            }
            if ($$7) continue;
            $$2.add($$6);
        }
        for (UUID $$9 : $$1) {
            for (ServerPlayer $$10 : this.getPlayers()) {
                if (!$$10.getUUID().equals($$9)) continue;
                this.removePlayer($$10);
                break;
            }
            this.players.remove($$9);
        }
        for (ServerPlayer $$11 : $$2) {
            this.addPlayer($$11);
        }
        return !$$1.isEmpty() || !$$2.isEmpty();
    }

    public static CustomBossEvent load(Identifier $$0, Packed $$1) {
        CustomBossEvent $$2 = new CustomBossEvent($$0, $$1.name);
        $$2.setVisible($$1.visible);
        $$2.setValue($$1.value);
        $$2.setMax($$1.max);
        $$2.setColor($$1.color);
        $$2.setOverlay($$1.overlay);
        $$2.setDarkenScreen($$1.darkenScreen);
        $$2.setPlayBossMusic($$1.playBossMusic);
        $$2.setCreateWorldFog($$1.createWorldFog);
        $$1.players.forEach($$2::addOfflinePlayer);
        return $$2;
    }

    public Packed pack() {
        return new Packed(this.getName(), this.isVisible(), this.getValue(), this.getMax(), this.getColor(), this.getOverlay(), this.shouldDarkenScreen(), this.shouldPlayBossMusic(), this.shouldCreateWorldFog(), Set.copyOf(this.players));
    }

    public void onPlayerConnect(ServerPlayer $$0) {
        if (this.players.contains($$0.getUUID())) {
            this.addPlayer($$0);
        }
    }

    public void onPlayerDisconnect(ServerPlayer $$0) {
        super.removePlayer($$0);
    }

    public static final class Packed
    extends Record {
        final Component name;
        final boolean visible;
        final int value;
        final int max;
        final BossEvent.BossBarColor color;
        final BossEvent.BossBarOverlay overlay;
        final boolean darkenScreen;
        final boolean playBossMusic;
        final boolean createWorldFog;
        final Set<UUID> players;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)ComponentSerialization.CODEC.fieldOf("Name").forGetter(Packed::name), (App)Codec.BOOL.optionalFieldOf("Visible", (Object)false).forGetter(Packed::visible), (App)Codec.INT.optionalFieldOf("Value", (Object)0).forGetter(Packed::value), (App)Codec.INT.optionalFieldOf("Max", (Object)100).forGetter(Packed::max), (App)BossEvent.BossBarColor.CODEC.optionalFieldOf("Color", (Object)BossEvent.BossBarColor.WHITE).forGetter(Packed::color), (App)BossEvent.BossBarOverlay.CODEC.optionalFieldOf("Overlay", (Object)BossEvent.BossBarOverlay.PROGRESS).forGetter(Packed::overlay), (App)Codec.BOOL.optionalFieldOf("DarkenScreen", (Object)false).forGetter(Packed::darkenScreen), (App)Codec.BOOL.optionalFieldOf("PlayBossMusic", (Object)false).forGetter(Packed::playBossMusic), (App)Codec.BOOL.optionalFieldOf("CreateWorldFog", (Object)false).forGetter(Packed::createWorldFog), (App)UUIDUtil.CODEC_SET.optionalFieldOf("Players", Set.of()).forGetter(Packed::players)).apply((Applicative)$$0, Packed::new));

        public Packed(Component $$0, boolean $$1, int $$2, int $$3, BossEvent.BossBarColor $$4, BossEvent.BossBarOverlay $$5, boolean $$6, boolean $$7, boolean $$8, Set<UUID> $$9) {
            this.name = $$0;
            this.visible = $$1;
            this.value = $$2;
            this.max = $$3;
            this.color = $$4;
            this.overlay = $$5;
            this.darkenScreen = $$6;
            this.playBossMusic = $$7;
            this.createWorldFog = $$8;
            this.players = $$9;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this, $$0);
        }

        public Component name() {
            return this.name;
        }

        public boolean visible() {
            return this.visible;
        }

        public int value() {
            return this.value;
        }

        public int max() {
            return this.max;
        }

        public BossEvent.BossBarColor color() {
            return this.color;
        }

        public BossEvent.BossBarOverlay overlay() {
            return this.overlay;
        }

        public boolean darkenScreen() {
            return this.darkenScreen;
        }

        public boolean playBossMusic() {
            return this.playBossMusic;
        }

        public boolean createWorldFog() {
            return this.createWorldFog;
        }

        public Set<UUID> players() {
            return this.players;
        }
    }
}

