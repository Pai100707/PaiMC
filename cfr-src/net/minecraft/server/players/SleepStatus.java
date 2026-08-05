/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.players;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class SleepStatus {
    private int activePlayers;
    private int sleepingPlayers;

    public boolean areEnoughSleeping(int $$0) {
        return this.sleepingPlayers >= this.sleepersNeeded($$0);
    }

    public boolean areEnoughDeepSleeping(int $$0, List<ServerPlayer> $$1) {
        int $$2 = (int)$$1.stream().filter(Player::isSleepingLongEnough).count();
        return $$2 >= this.sleepersNeeded($$0);
    }

    public int sleepersNeeded(int $$0) {
        return Math.max(1, Mth.ceil((float)(this.activePlayers * $$0) / 100.0f));
    }

    public void removeAllSleepers() {
        this.sleepingPlayers = 0;
    }

    public int amountSleeping() {
        return this.sleepingPlayers;
    }

    public boolean update(List<ServerPlayer> $$0) {
        int $$1 = this.activePlayers;
        int $$2 = this.sleepingPlayers;
        this.activePlayers = 0;
        this.sleepingPlayers = 0;
        for (ServerPlayer $$3 : $$0) {
            if ($$3.isSpectator()) continue;
            ++this.activePlayers;
            if (!$$3.isSleeping()) continue;
            ++this.sleepingPlayers;
        }
        return !($$2 <= 0 && this.sleepingPlayers <= 0 || $$1 == this.activePlayers && $$2 == this.sleepingPlayers);
    }
}

